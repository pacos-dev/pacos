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

        stage('Resolve GitHub status SHA') {
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
                    echo "GitHub build status will be reported for commit ${env.GITHUB_STATUS_SHA}."

                    withCredentials([usernamePassword(
                        credentialsId: 'github-pacos-token',
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_TOKEN'
                    )]) {
                        sh '''
                            set -u
                            payload="$(printf '{"state":"pending","context":"continuous-integration/jenkins/pacos-build","description":"Jenkins build is running","target_url":"%s"}' "$BUILD_URL")"
                            status_code="$(curl --silent --show-error \
                              --output /dev/null --write-out '%{http_code}' \
                              --request POST \
                              --url "https://api.github.com/repos/pacos-dev/pacos/statuses/$GITHUB_STATUS_SHA" \
                              --header "Accept: application/vnd.github+json" \
                              --header "Authorization: Bearer $GITHUB_TOKEN" \
                              --header "X-GitHub-Api-Version: 2022-11-28" \
                              --data "$payload")"
                            curl_result=$?
                            if [ "$curl_result" -ne 0 ] || [ "$status_code" -lt 200 ] || [ "$status_code" -ge 300 ]; then
                                echo "WARNING: Failed to publish pending GitHub status (HTTP ${status_code:-unknown}). The build will continue, but GitHub may not display the Jenkins check."
                            else
                                echo "GitHub Jenkins status set to pending."
                            fi
                        '''
                    }
                }
            }
        }

        stage('Install Playwright Chromium') {
            when {
                expression {
                    env.SKIP_REPORT_COMMIT != 'true' &&
                    (env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main')
                }
            }
            steps {
                sh '''
                    mvn -pl e2e-tests -am \
                    -Dexec.classpathScope=test \
                    -Dexec.mainClass=com.microsoft.playwright.CLI \
                    -Dexec.args="install chromium" \
                    org.codehaus.mojo:exec-maven-plugin:3.5.0:java
                '''
            }
        }

        stage('Build and tests') {
            when {
                expression { env.SKIP_REPORT_COMMIT != 'true' }
            }
            steps {
                // Keep test classes sequential: VaadinMock uses shared static state and is not concurrency-safe.
                // Parallel class execution causes Mockito stubbing races in existing UI tests.
                sh """
                    mvn -U clean verify \
                    -DworkingDir=./pacos \
                    -Dlogback.configurationFile=src/test/resources/logback-test.xml \
                    -Pproduction \
                    -Pcoverage-create-reports \
                    -Dpacos.e2e.skip=${!(env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main')} \
                    -Dpacos.e2e.browser=${(env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main' || env.GIT_BRANCH == '*/main')} \
                    -Djunit.jupiter.execution.parallel.enabled=true \
                    -Djunit.jupiter.execution.parallel.mode.default=same_thread \
                    -Djunit.jupiter.execution.parallel.mode.classes.default=same_thread \
                    -Djunit.jupiter.execution.parallel.config.strategy=fixed \
                    -Djunit.jupiter.execution.parallel.config.fixed.parallelism=2 \
                    -Djunit.jupiter.execution.parallel.config.fixed.max-pool-size=2
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
                            -Dsonar.exclusions=**/e2e-tests/**,**/test/**/*,**/frontend/*,**/node_modules/* \
                            -Dsonar.coverage.exclusions=**/e2e-tests/**,**/test/**/*,**/frontend/*,**/node_modules/*
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
                          cat "$response_file"
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
                    def finalState = result == 'SUCCESS' ? 'success' : (result == 'ABORTED' ? 'error' : 'failure')
                    def finalDescription = result == 'SUCCESS' ? 'Jenkins build passed' : (result == 'ABORTED' ? 'Jenkins build was aborted' : 'Jenkins build failed')
                    env.GITHUB_FINAL_STATE = finalState
                    env.GITHUB_FINAL_DESCRIPTION = finalDescription
                    withCredentials([usernamePassword(
                        credentialsId: 'github-pacos-token',
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_TOKEN'
                    )]) {
                        sh '''
                            set -u
                            payload="$(printf '{"state":"%s","context":"continuous-integration/jenkins/pacos-build","description":"%s","target_url":"%s"}' "$GITHUB_FINAL_STATE" "$GITHUB_FINAL_DESCRIPTION" "$BUILD_URL")"
                            status_code="$(curl --silent --show-error \
                              --output /dev/null --write-out '%{http_code}' \
                              --request POST \
                              --url "https://api.github.com/repos/pacos-dev/pacos/statuses/$GITHUB_STATUS_SHA" \
                              --header "Accept: application/vnd.github+json" \
                              --header "Authorization: Bearer $GITHUB_TOKEN" \
                              --header "X-GitHub-Api-Version: 2022-11-28" \
                              --data "$payload")"
                            curl_result=$?
                            if [ "$curl_result" -ne 0 ] || [ "$status_code" -lt 200 ] || [ "$status_code" -ge 300 ]; then
                                echo "WARNING: Failed to publish final GitHub status (HTTP ${status_code:-unknown})."
                            else
                                echo "Published GitHub Jenkins status: $GITHUB_FINAL_STATE."
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
