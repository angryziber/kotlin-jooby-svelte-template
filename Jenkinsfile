pipeline {
  agent any

  environment {
    APP = "app"
    BUILD = "${JOB_NAME.replace('/', '-')}-${BUILD_NUMBER}"
    RUN_TESTS = "docker run -v `pwd`/build/test-results:/app/build/test-results"
    // EMAIL = "developers@domain"
    GIT_LAST_CHANGE = sh(script: 'git show', returnStdout: true)
    EMAIL_BODY = "Project: ${JOB_NAME}\nBuild Number: ${BUILD_NUMBER}\n\nLast change:\n\n${env.GIT_LAST_CHANGE}"
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
        script {
          def startLogs = sh script: "sleep 5 && docker logs ${APP}_${APP}_1 | grep -B50 -A1 'listening on:'", returnStdout: true
          println(startLogs)
          if (EMAIL) mail to: EMAIL, subject: "$APP deployed to test", body: EMAIL_BODY + "\n\nStart logs:\n...\n$startLogs"
        }
      }
    }
  }
  post {
    always {
      sh "touch build/test-results/**/*.xml"
      junit 'build/test-results/**/*.xml'
    }
    failure {
      script {
        if (EMAIL) mail to: EMAIL, subject: "$APP ($BRANCH_NAME) build failed", body: EMAIL_BODY
      }
    }
  }
}
