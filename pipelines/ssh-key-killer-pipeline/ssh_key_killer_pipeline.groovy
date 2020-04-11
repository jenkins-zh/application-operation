//	refresh ssh-key-killer config


setJobProperties()
def config = readYaml: "ssh_key_killer_pipeline.yaml"

node () {
	stage("prepare") {
		scmCheckout(config)
	}
	if (shouldProceed(config)) {
		stage("check config") {
			checkConfig(config)
		}
		stage("refresh config") {
			refreshConfig(config)
		}
	}
}

def setJobProperties() {
	properties([buildDiscarder(logRotator(daysToKeepStr: '7', numToKeepStr: '100')),
		disableConcurrentBuilds(),
		disableResume(),
		pipelineTriggers([githubPush()])]
	)
}

// verifies the config file.
def checkConfig(config) {
	readYaml: config.config_file
}

def refreshConfig(config) {
	def path = "/etc/ssh-key-killer"
	ws(env.WORKSPACE) {
		sh "mkdir -p ${path} && cp ${config.config_file} ${path}/config.yaml"
		sh "python -m ssh-key-killer"
	}
}

// repoNameFromUrl extracts the repo name from a git url (git or http).
def repoNameFromUrl(url) {
	def repoName = url.trim().split("/")[-1].split("\\.")[0]
}

// scmCheckout downloads git repo to `WORKSPACE/${repo name}`.
def scmCheckout() {
	ws(env.WORKSPACE) {
		checkout([$class: 'GitSCM', branches: [[name: "*/${config.git.branch}"]], 
			extensions: [[$class: 'CheckoutOption', timeout: 5], 
			[$class: 'CloneOption', honorRefspec: true, depth: 2, noTags: true],
				[$class: 'CleanBeforeCheckout'],
				[$class: 'RelativeTargetDirectory', relativeTargetDir: repoNameFromUrl(config.git.url)]], 
			userRemoteConfigs: [[credentialsId: config.git.credentials_id, url: config.git.url]]]
		)
	}
}

// shouldProceed proceeds the pipeline when `config.config_file` was changed in this commit.
def shouldProceed(config) {
	def file = config.config_file.trim().split("/")[1..-1].join("/")
	ws("${env.WORKSPACE}/${repoNameFromUrl(config.git.url)}") {
		def out = sh(returnStdout: true, script: "git diff HEAD HEAD^ --name-only ${file}")
		if (out.trim() == "") {
			return false
		}
	}
	return true
}