// verify nginx config file and reload it

setJobProperties()
def config = readYaml: "config.yaml"

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
		sh "mkdir -p /etc/nginx && cp -f ${config.nginx_config} /etc/nginx/nginx.current.default"
		def container = locateContainer(config.image)
		// if nginx container was not up, just up
		if (container == "") {
			ws("${env.WORKSPACE}/${repoNameFromUrl(config.git.url)}/compose/jenkins-nginx") {
				sh "docker-compose up -d"
			}
		} else {
			sh "docker exec ${container} nginx -s reload"
		}
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
