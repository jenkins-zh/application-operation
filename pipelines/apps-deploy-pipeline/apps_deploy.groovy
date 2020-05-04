// deploy apps in need.

setJobProperties()

def config = readConfig()

node (config.node) {
	stage("prepare") {
		scmCheckout(config)
	}
	if (shouldProceed(config) || env.forceDeploy) {
		stage("verify config") {
			checkConfig(config)
		}
		stage("copy config") {
			copyConfig(config)
		}
		stage("update apps") {
			updateApps(config)
		}
	}
}

// checkConfig verifies config files.
def checkConfig(config) {
	ws("${env.WORKSPACE}/${repoName(config)}") {
		// verifies docker-compose config
		sh "docker-compose -f ${config.compose_file} config"
		// verifies nginx config
		sh """docker run -v ${pwd()}:${pwd()} \
			-v "/etc/nginx:/etc/nginx" \
			${nginxImage(config)} sh -c ' \
			echo 127.0.0.1 jenkins-wechat >> /etc/hosts && \
			echo 127.0.0.1 jenkins-mirror-proxy >> /etc/hosts && \
			nginx -t -c ${pwd()}/${config.nginx_config}'"""
	}
}

// copyConfig copies config files to specific host paths
def copyConfig(config) {
	// copy nginx config
	sh "mkdir -p /etc/nginx && cp -f ${repoName(config)}/${config.nginx_config} /etc/nginx/nginx.current.conf"
}

// updateApps updates apps in need.
def updateApps(config) {
	def force = Boolean.valueOf(env.forceDeploy)? "--force-recreate":""
	def reloadFailed = false
	try {
		sh "docker-compose -f ${repoName(config)}/${config.compose_file} up -d ${force}"
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
	def msg = config.email.failed
	if (!onFailed) {
		msg = config.email.success
	}
	try {
		mail(msg)
	}
	catch(Exception e) {
		// just prints a message and sets this build to unstable status.
		println "failed to send mail"
		setUnstable()
	}
}

// nginxImage returns the name of nginx image.
def nginxImage(config) {
	def compose = readYaml file: config.compose_file
	return compose.services."jenkins-nginx".image
}

// setJobProperties sets the pipeline properties.
def setJobProperties() {
	properties([buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '100')),
		parameters([booleanParam(defaultValue: false, description: '', name: 'forceDeploy')]),
		disableConcurrentBuilds(),
		disableResume()]
	)
}

// repoName returns the repo name.
def repoName(config) {
	return config.git.url.trim().split("/")[-1].split("\\.")[0]
}

// scmCheckout downloads the git repo, branch is default to master.
def scmCheckout(config) {
	def branch = config.git.branch?:"master"
	sh "rm -rf ${repoName(config)}"
	sh "git clone ${config.git.url} --depth=2 --no-tags --branch ${branch}"
}

// shouldProceed proceeds the pipeline in need.
def shouldProceed(config) {
	def folders = config.git.folders?:[""]
	ws("${env.WORKSPACE}/${repoName(config)}") {
		def out = sh(returnStdout: true, script: "git diff HEAD HEAD^ --name-only ${folders.join(' ')}")
		if (out.trim() == "") {
			return false
		}
		return true
	}
}

// setUnstable sets the build result to another status.
def setUnstable() {
	currentBuild.result = "UNSTABLE"
}

// readConfig reads pipeline config.
def readConfig() {
	def text = readTrusted 'pipelines/apps-deploy-pipeline/config.yaml'
	def config = readYaml text: text
	return config
}