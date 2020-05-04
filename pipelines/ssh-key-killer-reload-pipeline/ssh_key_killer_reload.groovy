//	refresh ssh-key-killer config


setJobProperties()
def config = readYaml file: "config.yaml"

node (config.node) {
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

// verifies the config file.
def checkConfig(config) {
	sh "python3 -m SshKeyKiller -c ${config.config_file} --verify"
}

def refreshConfig(config) {
	def path = "/etc/ssh-key-killer"
	sh "mkdir -p ${path} && cp -f ${config.config_file}/* ${path}/"
	sh "python -m SshKeyKiller"
}

// this job should be non-concurrent.
def setJobProperties() {
	properties([buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '100')),
		disableConcurrentBuilds(),
		disableResume()]
	)
}

// repoNameFromUrl return the repo name from a git url (git or http).
def repoNameFromUrl(url) {
	return url.trim().split("/")[-1].split("\\.")[0]
}

// scmCheckout downloads git repo to `${env.WORKSPACE}/${repoName}`, the branch is default to master.
def scmCheckout(config) {
	def branch = config.git.branch?:"master"
	sh "rm -rf ${repoNameFromUrl(config.git.url)}"
	sh "git clone ${config.git.url} --depth=2 --no-tags --branch ${branch}"
}

// shouldProceed proceeds the pipeline when files in the folders which are interested was changed in this commit.
def shouldProceed(config) {
	def folders = config.git.folders?:[""]
	ws("${env.WORKSPACE}/${repoNameFromUrl(config.git.url)}") {
		def out = sh(returnStdout: true, script: "git diff HEAD HEAD^ --name-only ${folders.join(' ')}")
		if (out.trim() == "") {
			return false
		}
		return true
	}
}