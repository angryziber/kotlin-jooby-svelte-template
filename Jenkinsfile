pipeline {
  agent any

  environment {
    APP = "app" 
    BUILD_ID = "${JOB_NAME}-${BUILD_NUMBER}"
    // label build containers before running tests, so we can retrieve test results in case of failure
    LABEL_ARGS = "--build-arg BUILD_ID=${BUILD_ID}"
  }

  stages {
    stage('Build UI') {
      steps {
        sh 'docker build $LABEL_ARGS --target ui-build -t ${APP}_ui .'
      }
    }
    stage('Build Server') {
      steps {
        sh 'docker build $LABEL_ARGS --target server-build -t ${APP}_server .'
      }
    }
    stage('E2E Tests') {
      steps {
        sh 'docker build $LABEL_ARGS --target e2e-tests -t ${APP}_e2e .'
      }
    }
    stage('Build final') {
      steps {
        sh 'docker build $LABEL_ARGS --target final -t ${APP}_${APP} .'
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        sh 'docker-compose -f docker-compose.yml -f docker-compose.codeborne.yml -p ${APP} up -d --remove-orphans'
        sh 'sleep 5 && docker logs ${APP}_${APP}_1 | grep -A1 "listening on:"'
      }
    }
  }
  post {
    always {
      sh 'rm -fr build && mkdir -p build/test-results'
      sh 'docker cp `docker ps -aqf label=ui-build=$BUILD_ID | grep . || docker create ${APP}_ui`:/app/build/test-results build/'
      sh 'docker cp `docker ps -aqf label=server-build=$BUILD_ID | grep . || docker create ${APP}_server`:/app/build/test-results build/'
      sh 'docker cp `docker ps -aqf label=e2e-tests=$BUILD_ID | grep . || docker create ${APP}_e2e`:/app/build/test-results build/'
      sh 'touch build/test-results/*/*.xml'
      junit 'build/test-results/**/*.xml'
      script {
        env.GIT_LAST_CHANGE = sh(script: 'git show', returnStdout: true)
        env.EMAIL_BODY = "Project: ${JOB_NAME}\nBuild Number: ${BUILD_NUMBER}\n\nLast change:\n\n${env.GIT_LAST_CHANGE}"
      }
    }
//     success {
//       mail to: "${APP}@email", subject: "Deployed to test: ${JOB_NAME} #${BUILD_NUMBER}", body: EMAIL_BODY
//     }
//     failure {
//       mail to: "${APP}@email", subject: "Build failed: ${JOB_NAME} #${BUILD_NUMBER}", body: EMAIL_BODY
//     }
  }
}
