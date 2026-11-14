pipeline {
    agent any

    options {
        disableConcurrentBuilds(abortPrevious: true)
        quietPeriod(15)
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    tools {
        maven 'mvn'
        jdk 'jdk21'
    }

    environment {
        GITHUB_STATUS_CONTEXT = 'continuous-integration/jenkins/pacos-build'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def sourceBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    env.SONAR_SOURCE_BRANCH = sourceBranch
                    env.SONAR_ENABLED = (
                        sourceBranch == 'main' ||
                        sourceBranch == 'origin/main' ||
                        sourceBranch == '*/main' ||
                        sourceBranch.endsWith('/sonar') ||
                        sourceBranch.endsWith('-sonar')
                    ) ? 'true' : 'false'
                    echo "SonarQube analysis ${env.SONAR_ENABLED == 'true' ? 'enabled' : 'disabled'} for source branch '${sourceBranch}' (BRANCH_NAME='${env.BRANCH_NAME}', CHANGE_BRANCH='${env.CHANGE_BRANCH}')."
                    env.GITHUB_LAST_STAGE = 'Checkout'
                }
                milestone(ordinal: 1)
            }
        }

        stage('Detect generated SonarQube report commit') {
            steps {
                script {
                    env.GITHUB_LAST_STAGE = 'Detect generated SonarQube report commit'
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
                    env.GITHUB_LAST_STAGE = 'Resolve GitHub status SHA'
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
                            payload="$(printf '{"state":"pending","context":"%s","description":"Jenkins build is running","target_url":"%s"}' "$GITHUB_STATUS_CONTEXT" "$BUILD_URL")"
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


        stage('Build and tests') {
            when {
                expression { env.SKIP_REPORT_COMMIT != 'true' }
            }
            steps {
                script {
                    env.GITHUB_LAST_STAGE = 'Build and tests'
                    sh 'mkdir -p .jenkins && : > .jenkins/build.log'

                    // Run two Maven reactor threads while keeping JUnit execution sequential within each module.
                    def mavenExitCode = sh(
                        script: """#!/usr/bin/env bash
                            set -euo pipefail
                    mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.8.1:get -Dartifact=org.mockito:mockito-core:5.18.0
                    MOCKITO_AGENT="\$HOME/.m2/repository/org/mockito/mockito-core/5.18.0/mockito-core-5.18.0.jar"
                    test -f "\$MOCKITO_AGENT"
                    export JAVA_TOOL_OPTIONS="\${JAVA_TOOL_OPTIONS:-} -javaagent:\$MOCKITO_AGENT"
                    echo "Using Mockito javaagent: \$MOCKITO_AGENT"
                    mvn -T 2 -U clean verify \
                    -DworkingDir=./pacos \
                    -Dlogback.configurationFile=src/test/resources/logback-test.xml \
                    -Pproduction \
                    -Pcoverage-create-reports \
                    -Djunit.jupiter.execution.parallel.enabled=true \
                    -Djunit.jupiter.execution.parallel.mode.default=same_thread \
                    -Djunit.jupiter.execution.parallel.mode.classes.default=same_thread \
                    -Djunit.jupiter.execution.parallel.config.strategy=fixed \
                    -Djunit.jupiter.execution.parallel.config.fixed.parallelism=2 \
                    -Djunit.jupiter.execution.parallel.config.fixed.max-pool-size=2
                            2>&1 | tee -a .jenkins/build.log
                        """,
                        returnStatus: true
                    )
                    env.MAVEN_BUILD_EXIT_CODE = mavenExitCode.toString()

                    junit(
                        testResults: '**/target/surefire-reports/TEST-*.xml',
                        allowEmptyResults: true,
                        skipPublishingChecks: false,
                        checksName: 'Unit Tests'
                    )
                    junit(
                        testResults: '**/target/failsafe-reports/TEST-*.xml',
                        allowEmptyResults: true,
                        skipPublishingChecks: false,
                        checksName: 'Integration Tests'
                    )

                    if (mavenExitCode != 0) {
                        error "Maven build or tests failed with exit code: ${mavenExitCode}"
                    }

                    echo "Maven build completed successfully with exit code ${env.MAVEN_BUILD_EXIT_CODE}."
                }
            }
        }
        // Run SonarQube analysis on main and opted-in branches. Jenkins PR jobs expose the source branch through CHANGE_BRANCH.
        // SonarQube Community Build cannot attach analysis results to GitHub pull requests.
        stage('SonarQube Analysis') {
            when {
                expression {
                    def sourceBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    def branchOptedIn = sourceBranch == 'main' ||
                        sourceBranch.endsWith('/sonar') ||
                        sourceBranch.endsWith('-sonar')
                    def mavenBuildSucceeded = env.MAVEN_BUILD_EXIT_CODE == '0'
                    def shouldRun = env.SKIP_REPORT_COMMIT != 'true' && branchOptedIn && mavenBuildSucceeded
                    echo "Sonar stage condition: skipReportCommit=${env.SKIP_REPORT_COMMIT}, sourceBranch='${sourceBranch}', branchOptedIn=${branchOptedIn}, mavenBuildSucceeded=${mavenBuildSucceeded}, shouldRun=${shouldRun}"
                    return shouldRun
                }
            }
            steps {
                script { env.GITHUB_LAST_STAGE = 'SonarQube Analysis' }
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
                        sh """#!/usr/bin/env bash
                            set -o pipefail
                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar \
                            -Dsonar.projectKey=PacOS \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths=${xmls} \
                            -Dsonar.exclusions=**/e2e-tests/**,**/test/**/*,**/frontend/*,**/node_modules/* \
                            -Dsonar.coverage.exclusions=**/e2e-tests/**,**/test/**/*,**/frontend/*,**/node_modules/* \
                            2>&1 | tee -a .jenkins/build.log
                        """
                    }
                }
            }
        }

        stage('Wait for SonarQube Quality Gate') {
            when {
                expression {
                    def sourceBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    def branchOptedIn = sourceBranch == 'main' ||
                        sourceBranch.endsWith('/sonar') ||
                        sourceBranch.endsWith('-sonar')
                    def shouldRun = env.SKIP_REPORT_COMMIT != 'true' && branchOptedIn
                    echo "Sonar stage condition: skipReportCommit=${env.SKIP_REPORT_COMMIT}, sourceBranch='${sourceBranch}', branchOptedIn=${branchOptedIn}, shouldRun=${shouldRun}"
                    return shouldRun
                }
            }
            steps {
                script { env.GITHUB_LAST_STAGE = 'Wait for SonarQube Quality Gate' }
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
                    def sourceBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    def branchOptedIn = sourceBranch == 'main' ||
                        sourceBranch.endsWith('/sonar') ||
                        sourceBranch.endsWith('-sonar')
                    def shouldRun = env.SKIP_REPORT_COMMIT != 'true' && branchOptedIn
                    echo "Sonar stage condition: skipReportCommit=${env.SKIP_REPORT_COMMIT}, sourceBranch='${sourceBranch}', branchOptedIn=${branchOptedIn}, shouldRun=${shouldRun}"
                    return shouldRun
                }
            }
            steps {
                script { env.GITHUB_LAST_STAGE = 'Trigger SonarQube report export' }
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
                    def sourceBranch = env.CHANGE_BRANCH ?: env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    def branchOptedIn = sourceBranch == 'main' ||
                        sourceBranch.endsWith('/sonar') ||
                        sourceBranch.endsWith('-sonar')
                    def shouldRun = env.SKIP_REPORT_COMMIT != 'true' && branchOptedIn
                    echo "Sonar stage condition: skipReportCommit=${env.SKIP_REPORT_COMMIT}, sourceBranch='${sourceBranch}', branchOptedIn=${branchOptedIn}, shouldRun=${shouldRun}"
                    return shouldRun
                }
            }
            steps {
                script { env.GITHUB_LAST_STAGE = 'Enforce Quality Gate' }
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
                            payload="$(printf '{"state":"%s","context":"%s","description":"%s","target_url":"%s"}' "$GITHUB_FINAL_STATE" "$GITHUB_STATUS_CONTEXT" "$GITHUB_FINAL_DESCRIPTION" "$BUILD_URL")"
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

                        def buildLog = fileExists('.jenkins/build.log') ? readFile('.jenkins/build.log') : ''
                        def errorLines = buildLog.readLines().findAll { line ->
                            line =~ /(?i)(\[ERROR\]|BUILD FAILURE|Tests run:.*(Failures|Errors)|Caused by:)/
                        }
                        def errorSummary = errorLines ? errorLines.takeRight(80).join('\n') : 'No Maven/Sonar error excerpt was captured. Open the Jenkins build log for details.'
                        errorSummary = errorSummary.replaceAll(/(?i)(password|token|secret)(\s*[:=]\s*)[^\s,;]+/, '$1$2[REDACTED]')
                        if (errorSummary.length() > 12000) {
                            errorSummary = errorSummary.takeRight(12000)
                        }

                        def checkConclusion = result == 'SUCCESS' ? 'SUCCESS' : (result == 'ABORTED' ? 'CANCELED' : 'FAILURE')
                        def checkTitle = result == 'SUCCESS' ? 'PacOS build passed' : (result == 'ABORTED' ? 'PacOS build was aborted' : 'PacOS build failed')
                        def checkSummary = 'Result: ' + result + '\nLast stage: ' + (env.GITHUB_LAST_STAGE ?: 'Unknown')
                        def checkText = """### Build diagnostics

${errorSummary}

[Open Jenkins build](${env.BUILD_URL}console)
"""

                        try {
                            publishChecks(
                                name: 'PacOS Build',
                                status: 'COMPLETED',
                                conclusion: checkConclusion,
                                title: checkTitle,
                                summary: checkSummary,
                                text: checkText,
                                detailsURL: env.BUILD_URL
                            )
                            echo 'Published detailed PacOS build check to GitHub.'
                        } catch (Exception exception) {
                            echo "WARNING: Failed to publish detailed GitHub Check: ${exception.message}"
                        }
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
