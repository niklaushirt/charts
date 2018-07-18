
node {
  def mvnHome
  stage('Preparation') { // for display purposes
    withCredentials([usernamePassword(credentialsId: 'GIT', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
      // Get some code from a GitHub repository
      //    git credentialsId: 'ec8087f2-8cc0-4d65-99bc-5efdd0085d6f', url: 'https://github.com/niklaushirt/libertydemo.git'
      git credentialsId: '$PASSWORD', url: 'https://github.com/niklaushirt/libertydemo.git'
    }

    // Get the Maven tool.
    // ** NOTE: This 'M3' Maven tool must be configured
    // **       in the global configuration.
    mvnHome = tool 'M3'
    //dockerHome = tool 'docker'
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
    withCredentials([usernamePassword(credentialsId: 'ICP', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
      sh "cd '$WORKSPACE'"
      sh "ls"
      //docker.withRegistry('http://mycluster.icp:8500', 'admin') {
      //sh "${dockerHome} ps"
      //sh "${dockerHome}/bin/docker ps"
      sh "docker build -t liberty_demo:1.${BUILD_NUMBER} ."
      sh "docker login mycluster.icp:8500 -u $USERNAME -p $PASSWORD"
      sh "docker tag liberty_demo:1.${BUILD_NUMBER} mycluster.icp:8500/default/liberty_demo:1.${BUILD_NUMBER}"
      sh "docker push mycluster.icp:8500/default/liberty_demo:1.${BUILD_NUMBER}"
      //def customImage = docker.build("liberty_demo:demo")
      //customImage.push('demo')
      //}
    }
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
          baseDir: '$WORKSPACE',
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
