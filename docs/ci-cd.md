# CI/CD: GitHub, Jenkins and SonarQube

PacOS uses a Jenkins Multibranch Pipeline defined in the repository root `Jenkinsfile`.

## Pipeline behavior

- Every discovered branch and pull request runs `mvn clean verify package`.
- Test failures fail the Jenkins build. Tests must not be run with `-Dmaven.test.failure.ignore=true`.
- SonarQube analysis and the Quality Gate are run only on `main`, because this project uses SonarQube Community Build and its supported branch-analysis scope is limited.
- Pull requests are validated by Jenkins build/test results. Community Build does not provide the same full pull-request analysis/decorations as paid SonarQube Server editions.

## Jenkins configuration

Configure the following global tools in **Manage Jenkins → Tools**:

- Maven installation named `mvn`
- JDK installation named `jdk21` (Java 21)

Under **Manage Jenkins → System**, configure a SonarQube installation named exactly `sonarqube`. Store its token in Jenkins credentials / the SonarQube server configuration; do not put tokens in this repository or in the Jenkinsfile.

The Multibranch Pipeline should use the GitHub Branch Source plugin and discover both branches and pull requests. Ensure GitHub webhooks (or periodic branch indexing) trigger scans/builds. The GitHub Branch Source integration can publish build status to commits and pull requests.

## SonarQube webhook for Quality Gate

The `Quality Gate` stage uses Jenkins' `waitForQualityGate` step. For this to complete promptly, configure a webhook in SonarQube:

1. Open **Administration → Configuration → Webhooks** (menu wording can vary by version).
2. Add a webhook pointing to `https://jenkins.pacos.dev/jenkins/sonarqube-webhook/`.
3. Save it and verify delivery after the next main-branch analysis.

The Jenkins SonarQube Scanner plugin must be installed and its SonarQube server configuration must use the name `sonarqube`. The webhook endpoint includes the Jenkins context path `/jenkins/`.

## Require CI before merge

In GitHub repository settings, configure branch protection or a ruleset for `main` and require the Jenkins status check emitted by the Multibranch Pipeline. Use the exact check/status name shown after a successful run; the name depends on installed Jenkins plugins and job configuration.

Do not make SonarQube analysis a required check on pull requests when using Community Build: the pipeline deliberately runs the Sonar analysis and Quality Gate only on `main`. Jenkins build/test validation remains the PR merge gate.
