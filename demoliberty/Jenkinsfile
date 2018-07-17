node {
  def mvnHome
  stage('Preparation') { // for display purposes
    // Get some code from a GitHub repository
    git credentialsId: 'ec8087f2-8cc0-4d65-99bc-aaaaaaaaaa', url: 'https://github.com/niklaushirt/demoliberty.git'
    // Get the Maven tool.
    // ** NOTE: This 'M3' Maven tool must be configured
    // **       in the global configuration.
    mvnHome = tool 'M3'
  }
  stage('CI-Build') {
    // Run the maven build
    if (isUnix()) {
      sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore package"
    } else {
      bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore package/)
    }
  }
  stage('CI-Docker-Build') {
    sh "cd '$JENKINS_HOME/jobs/$JOB_BASE_NAME/workspace/'"
    sh "ls"
    //docker.withRegistry('http://mycluster.icp:8500', 'admin') {
    //sh "sudo docker build -t test ."
    def customImage = docker.build("liberty_demo:demo")
    //customImage.push('demo')
    //}
  }
  stage('CI-Push-To-UrbanCode') {
    step([$class: 'UCDeployPublisher',
      siteName: 'LOCAL',
      component: [
        $class: 'com.urbancode.jenkins.plugins.ucdeploy.VersionHelper$VersionBlock',
        componentName: 'LIBERTY',
        delivery: [
          $class: 'com.urbancode.jenkins.plugins.ucdeploy.DeliveryHelper$Push',
          pushVersion: '${BUILD_NUMBER}',
          baseDir: '$JENKINS_HOME/jobs/$JOB_BASE_NAME/workspace/',
          fileIncludePatterns: '*.*',
          fileExcludePatterns: '',
          pushProperties: 'jenkins.server=Local\njenkins.reviewed=false',
          pushDescription: 'Pushed from Jenkins'
        ]
      ]
    ])
  }
  stage('CD-Deploy-To-ICP') {
   step([$class: 'UCDeployPublisher',
        siteName: 'LOCAL',
        deploy: [
            $class: 'com.urbancode.jenkins.plugins.ucdeploy.DeployHelper$DeployBlock',
            deployApp: 'DEMO',
            deployEnv: 'TEST',
            deployProc: 'deploy',
            deployVersions: 'LIBERTY:${BUILD_NUMBER}',
            //deployVersions: 'LIBERTY:49',

            deployOnlyChanged: false
        ]
    ])
}
}
