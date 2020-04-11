//	verify nginx config file and reload nginx


setJobProperties()
def config = readYaml: "nginx_pipeline.yaml"

node () {
	stage("prepare") {
		scmCheckout(config)
	}
	if (shouldProceed(config)) {
		stage("check config") {
			checkNginxConfig(config)
		}
		stage("reload config") {
			reloadNginxConfig(config)
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

// checkNginxConfig verifies nginx config file.
def checkNginxConfig(config) {
	sh "docker run --volume ${env.WORKSPACE}:${env.WORKSPACE} ${config.image} nginx -t -c ${config.nginx_config}"
}

// reloadNginxConfig reloads nginx.
def reloadNginxConfig(config) {
	def reloadFailed = false
	try {
		def container = locateContainer(config.image)
		// if nginx container was not up, just up
		if (container == "") {
			sh "docker run --volume ${env.WORKSPACE}:${env.WORKSPACE} \
				--publish 80:80 --detach ${config.image} nginx -g 'daemon off;' -c ${config.nginx_config}"
		}
		sh "docker exec ${container} nginx -c ${config.nginx_config} -s reload"
	}
	catch(Exception e) {
		reloadFailed = true
		throw e
	}
	finally {
		sendMail(config, reloadFailed)
	}
}

// sendMail sends mail according to the value of onFailed.
def sendMail(config, onFailed) {
	def msg = config.mail.failed
	if (!onFail) {
		msg = config.mail.success
	}
	mail(msg)
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

// locateContainer finds container by image name.
def locateContainer(image) {
	def out = sh(returnStdout: true, script: """docker ps | awk '{print \$1" "\$2}'""").split("\n")
	for(line in out) {
		v = line.trim().split(" ")[0]
		k = line.trim().split(" ")[1]
		if (image == k) {
			return v
		}
	}
	return ""
}

// shouldProceed proceeds the pipeline when `config.nginx_config` was changed in this commit.
def shouldProceed(config) {
	def file = config.nginx_config.trim().split("/")[1..-1].join("/")
	ws("${env.WORKSPACE}/${repoNameFromUrl(config.git.url)}") {
		def out = sh(returnStdout: true, script: "git diff HEAD HEAD^ --name-only ${file}")
		if (out.trim() == "") {
			return false
		}
	}
	return true
}