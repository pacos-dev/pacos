pipeline {
    agent any

    tools {
        maven 'mvn'      // name from Jenkins → Configure Tools
        jdk 'jdk21'      // name JDK from Jenkins → Configure Tools
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checkout git'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'run maven build'
                sh """
                    mvn clean verify package \
                    -DworkingDir=./pacos \
                    -Dlogback.configurationFile=src/test/resources/logback-test.xml \
                    -Dmaven.test.failure.ignore=true \
                    -Pproduction \
                    -Pcoverage-create-reports
                """
            }
        }


        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    script {
                        def xmls = sh(
                            script: """
                                find . -path '*/jacoco/jacoco.xml' | grep -v 'jacoco-aggregate' | paste -sd ',' -
                            """,
                            returnStdout: true
                        ).trim()

                        echo "Found XML reports: ${xmls}"
                        sh """
                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar \
                            -Dsonar.projectKey=PacOS \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths=${xmls} \
                            -Dsonar.exclusions=**/test/**/*,**/frontend/*,**/node_modules/* \
                            -Dsonar.coverage.exclusions=**/test/**/*,**/frontend/*,**/node_modules/*
                          """
                    }
                }
            }
        }
    }

    post {
        failure {
            echo "Build failed on branch ${env.BRANCH_NAME}"
        }
        success {
            echo "Build OK on branch ${env.BRANCH_NAME}"
        }
    }
}
