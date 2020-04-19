// verify docker-compose files located in `compose/<app-name>/` and start/restart apps in need.

setJobProperties()
def config = readYaml file: "config.yaml"

node (config.node) {
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

// walk walks `compose` folder and executes closure for every `docker-compose.yaml`.
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

// this job should be non-concurrent.
def setJobProperties() {
	properties([buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '100')),
		disableConcurrentBuilds(),
		disableResume()]
	)
}

// repoNameFromUrl return the repo name from a git url (git or http).
def repoNameFromUrl(url) {
	url.trim().split("/")[-1].split("\\.")[0]
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

// sendMail sends mail according to the value of onFailed.
def sendMail(config, onFailed) {
	def msg = config.email.failed
	if (!onFailed) {
		msg = config.email.success
	}
	try {
		mail(msg)
	}
	catch(Exception e) {
		println "failed to send mail"
	}
}
