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

        stage('Publish GitHub build status: pending') {
            steps {
                script {
                    env.GITHUB_STATUS_SHA = sh(
                        script: '''
                            if [ -n "${CHANGE_ID:-}" ] && git rev-parse --verify HEAD^2 >/dev/null 2>&1; then
                                git rev-parse HEAD^2
                            else
                                git rev-parse HEAD
                            fi
                        ''',
                        returnStdout: true
                    ).trim()

                    withCredentials([usernamePassword(
                        credentialsId: 'github-pacos-token',
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_TOKEN'
                    )]) {
                        sh '''
                            set -eu
                            status_code="$(curl --silent --show-error \
                              --output /dev/null \
                              --write-out '%{http_code}' \
                              --request POST \
                              --url "https://api.github.com/repos/pacos-dev/pacos/statuses/$GITHUB_STATUS_SHA" \
                              --header "Accept: application/vnd.github+json" \
                              --header "Authorization: Bearer $GITHUB_TOKEN" \
                              --header "X-GitHub-Api-Version: 2022-11-28" \
                              --data "{\"state\":\"pending\",\"context\":\"continuous-integration/jenkins/pacos-build\",\"description\":\"Jenkins build is running\",\"target_url\":\"$BUILD_URL\"}")"
                            if [ "$status_code" -lt 200 ] || [ "$status_code" -ge 300 ]; then
                                echo "WARNING: Could not publish pending GitHub status (HTTP $status_code). Check token Commit statuses: Read and write permission."
                            else
                                echo "Published pending GitHub status for $GITHUB_STATUS_SHA."
                            fi
                        '''
                    }
                }
            }
        }

        stage('Detect generated SonarQube report commit') {
            steps {
                script {
                    def markerFound = sh(
                        script: "git log -1 --format=%B | grep -Fq '[pacos-sonar-report]'",
                        returnStatus: true
                    ) == 0
                    env.SKIP_REPORT_COMMIT = markerFound ? 'true' : 'false'
                    if (markerFound) {
                        echo 'This commit only updates the generated SonarQube report; skipping build, analysis, and report dispatch to prevent a trigger loop.'
                    }
                }
            }
        }

        stage('Build and tests') {
            when {
                expression { env.SKIP_REPORT_COMMIT != 'true' }
            }
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
                    env.SKIP_REPORT_COMMIT != 'true' &&
                    (env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main')
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

        stage('Wait for SonarQube Quality Gate') {
            when {
                expression {
                    env.SKIP_REPORT_COMMIT != 'true' &&
                    (env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main')
                }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        def qualityGate = waitForQualityGate()
                        env.SONAR_QG_STATUS = qualityGate.status
                        echo "SonarQube Quality Gate status: ${env.SONAR_QG_STATUS}"
                    }
                }
            }
        }

        stage('Trigger SonarQube report export') {
            when {
                expression {
                    env.SKIP_REPORT_COMMIT != 'true' &&
                    (env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main')
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'github-pacos-token',
                    usernameVariable: 'GITHUB_USERNAME',
                    passwordVariable: 'GITHUB_TOKEN'
                )]) {
                    sh '''
                        set -eu
                        test -n "$GITHUB_TOKEN"
                        response_file="$(mktemp)"
                        trap 'rm -f "$response_file"' EXIT
                        status="$(curl --silent --show-error \
                          --output "$response_file" \
                          --write-out '%{http_code}' \
                          --request POST \
                          --url https://api.github.com/repos/pacos-dev/pacos/dispatches \
                          --header "Accept: application/vnd.github+json" \
                          --header "Authorization: Bearer $GITHUB_TOKEN" \
                          --header "X-GitHub-Api-Version: 2022-11-28" \
                          --data '{"event_type":"sonarqube-analysis-completed","client_payload":{"project_key":"PacOS"}}')"
                        if [ "$status" -lt 200 ] || [ "$status" -ge 300 ]; then
                          echo "GitHub repository_dispatch failed with HTTP $status."
                          if command -v jq >/dev/null 2>&1; then
                            jq -r '.message // "No error message returned by GitHub."' "$response_file" 2>/dev/null || cat "$response_file"
                          else
                            cat "$response_file"
                          fi
                          exit 1
                        fi
                        echo "GitHub repository_dispatch accepted (HTTP $status)."
                    '''
                }
            }
        }

        stage('Enforce Quality Gate') {
            when {
                expression {
                    env.SKIP_REPORT_COMMIT != 'true' &&
                    (env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main')
                }
            }
            steps {
                script {
                    if (env.SONAR_QG_STATUS != 'OK') {
                        error "SonarQube Quality Gate failed with status: ${env.SONAR_QG_STATUS}"
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                if (env.GITHUB_STATUS_SHA) {
                    def result = currentBuild.currentResult ?: 'SUCCESS'
                    env.GITHUB_FINAL_STATE = result == 'SUCCESS' ? 'success' : (result == 'ABORTED' ? 'error' : 'failure')
                    env.GITHUB_FINAL_DESCRIPTION = result == 'SUCCESS' ? 'Jenkins build passed' : (result == 'ABORTED' ? 'Jenkins build was aborted' : 'Jenkins build failed')
                    withCredentials([usernamePassword(
                        credentialsId: 'github-pacos-token',
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_TOKEN'
                    )]) {
                        sh '''
                            status_code="$(curl --silent --show-error \
                              --output /dev/null \
                              --write-out '%{http_code}' \
                              --request POST \
                              --url "https://api.github.com/repos/pacos-dev/pacos/statuses/$GITHUB_STATUS_SHA" \
                              --header "Accept: application/vnd.github+json" \
                              --header "Authorization: Bearer $GITHUB_TOKEN" \
                              --header "X-GitHub-Api-Version: 2022-11-28" \
                              --data "{\"state\":\"$GITHUB_FINAL_STATE\",\"context\":\"continuous-integration/jenkins/pacos-build\",\"description\":\"$GITHUB_FINAL_DESCRIPTION\",\"target_url\":\"$BUILD_URL\"}")"
                            if [ "$status_code" -lt 200 ] || [ "$status_code" -ge 300 ]; then
                                echo "WARNING: Could not publish final GitHub status (HTTP $status_code). Check token Commit statuses: Read and write permission."
                            else
                                echo "Published final GitHub status '$GITHUB_FINAL_STATE' for $GITHUB_STATUS_SHA."
                            fi
                        '''
                    }
                }
            }
        }
        failure {
            echo "Build failed on branch ${env.BRANCH_NAME}"
        }
        success {
            echo "Build OK on branch ${env.BRANCH_NAME}"
        }
    }
}
