// verify docker-compose files located in `compose/<app-name>/` and start/restart apps in need.

setJobProperties()
def config = readYaml: "config.yaml"

node () {
	stage("prepare") {
		scmCheckout(config)
	}
	if (shouldProceed(config)) {
		stage("verify config") {
			checkConfig(config)
		}
		stage("update apps") {
			updateApps(config)
		}
	}
}

// this job should be non-concurrent.
def setJobProperties() {
	properties([buildDiscarder(logRotator(daysToKeepStr: '7', numToKeepStr: '100')),
		disableConcurrentBuilds(),
		disableResume(),
		pipelineTriggers([githubPush()])]
	)
}

// checkConfig verifies config file.
def checkConfig(config) {
	walk(config, {it -> sh "docker-compose -f ${it} config -q"})
}

// updateApps updates apps in need.
def updateApps(config) {
	def reloadFailed = false
	try {
		walk(config, {it -> sh "docker-compose -f ${it} up -d"})
	}
	catch(Exception e) {
		reloadFailed = true
		throw e
	}
	finally {
		sendMail(config, reloadFailed)
	}
}

// walk walks `compose tree` and executes closure for every `docker-compose.yaml`.
def walk(config, closure) {
	ws("${env.WORKSPACE}/${repoNameFromUrl(config.git.url)}/compose") {
		def files = findFiles(glob: '**/*/docker-compose.yaml')
		for(file in files) {
			if (!file.directory) {
				closure(file.path)
			}
		}
	}
}

// repoNameFromUrl extracts the repo name from a git url (git or http).
def repoNameFromUrl(url) {
	def repoName = url.trim().split("/")[-1].split("\\.")[0]
}

// scmCheckout downloads git repo to `WORKSPACE/${repoName}`, the branch is default to master.
def scmCheckout(config) {
	def branch = config.git.branch?:"master"
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

// shouldProceed proceeds the pipeline when files in the folder which are interested was changed in this commit.
def shouldProceed(config) {
	def folders = config.git.folders?:[""]
	ws("${env.WORKSPACE}/${repoNameFromUrl(config.git.url)}") {
		def out = sh(returnStdout: true, script: "git diff HEAD HEAD^ --name-only ${folders.join(' ')}")
		if (out.trim() == "") {
			return false
		}
	}
	return true
}

// sendMail sends mail according to the value of onFailed.
def sendMail(config, onFailed) {
	def msg = config.mail.failed
	if (!onFail) {
		msg = config.mail.success
	}
	mail(msg)
}