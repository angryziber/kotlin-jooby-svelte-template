pipeline {
  agent any

  environment {
    APP = "app"
    BUILD = "${JOB_NAME.replace('/', '-')}-${BUILD_NUMBER}"
    RUN_TESTS = "docker run -v `pwd`/build/test-results:/app/build/test-results"
  }

  stages {
    stage('Build UI') {
      steps {
        sh "docker build --target ui-build -t ${BUILD}_ui ."
      }
    }
    stage('Test UI') {
      steps {
        sh "${RUN_TESTS} ${BUILD}_ui npm test"
      }
    }
    stage('Build Server') {
      steps {
        sh "docker build --target server-build -t ${BUILD}_server ."
      }
    }
    stage('Test Server') {
      steps {
        sh "${RUN_TESTS} ${BUILD}_server ./gradlew --no-daemon --info test"
      }
    }
    stage('E2E Tests') {
      steps {
        sh "${RUN_TESTS} ${BUILD}_server ./gradlew --no-daemon --info -Pheadless e2eTest"
      }
    }
    stage('Build final') {
      when {
        branch 'master'
      }
      steps {
        sh "docker build --target final -t ${APP}_${APP} ."
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        sh "docker-compose -f docker-compose.yml -f docker-compose.codeborne.yml -p ${APP} up -d --remove-orphans"
        sh "sleep 5 && docker logs ${APP}_${APP}_1 | grep -A1 'listening on:'"
      }
    }
  }
  post {
    always {
      sh "touch build/test-results/**/*.xml"
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
