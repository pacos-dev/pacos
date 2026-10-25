pipeline {
    agent any

    tools {
        maven 'mvn'
        jdk 'jdk21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and tests') {
            steps {
                sh """
                    mvn -U clean verify package \
                    -DworkingDir=./pacos \
                    -Dlogback.configurationFile=src/test/resources/logback-test.xml \
                    -Pproduction \
                    -Pcoverage-create-reports
                """
            }
        }

        // SonarQube Community Build supports main-branch analysis only.
        // PR validation is performed by the build and test stage above.
        stage('SonarQube Analysis') {
            when {
                expression {
                    env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main'
                }
            }
            steps {
                withSonarQubeEnv('sonarqube') {
                    script {
                        def xmls = sh(
                            script: """
                                find . -path '*/jacoco/jacoco.xml' ! -path '*/jacoco-aggregate/*' -print | paste -sd ',' -
                            """,
                            returnStdout: true
                        ).trim()

                        if (!xmls) {
                            error 'No JaCoCo XML reports found. Ensure the coverage-create-reports profile is enabled and tests generate coverage data.'
                        }

                        echo "Found JaCoCo XML reports: ${xmls}"
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

        stage('Quality Gate') {
            when {
                expression {
                    env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main'
                }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Trigger SonarQube report export') {
            when {
                expression {
                    env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main'
                }
            }
            steps {
                withCredentials([string(credentialsId: 'github-pacos-token', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        set -eu
                        curl --fail --silent --show-error \
                          --request POST \
                          --url https://api.github.com/repos/pacos-dev/pacos/dispatches \
                          --header "Accept: application/vnd.github+json" \
                          --header "Authorization: Bearer $GITHUB_TOKEN" \
                          --header "X-GitHub-Api-Version: 2022-11-28" \
                          --data '{"event_type":"sonarqube-analysis-completed","client_payload":{"project_key":"PacOS"}}'
                    '''
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
