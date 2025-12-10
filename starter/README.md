## About
The pacos-starter.jar file is a runnable Java application responsible for initializing the Coupler system. 
Once launched, the latest version of the Coupler will be downloaded and run.

After starting the engine, open the browser page with the host address and configured port. 
Locally it will be "http://localhost:8086" for the default configuration

The first launch will trigger the installation mode, where you can configure the basic application settings.
## Requirements
The pacos requires java in version 17

## Optional parameters JAVA_OPTS

| property               | Defalt value                                                                    | Description                                                                                       |
|------------------------|---------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| -DworkingDir           | win -{userHome}/.pacos<br/>OS - /opt/.pacos<br/>Linux - /usr/local/.pacos | The installation directory where all resources <br/>including library, logs and db will be placed |
| -DserverPort           | 8086                                                                            | The port on which the Coupler will be launched                                                    |
| -Dmodule.list.repo.url | https://repo.pacos.dev/repository/pacos-maven-repo                         | The repository address from which Coupler will be downloaded                                      |
| -Dplugin.list.repo.url | https://repo.pacos.dev/repository/pacos-maven-repo                         | The repository address from which Coupler plugins will be downloaded                              |
| -Dversion              | ---                                                                             | During installation, the selected version will be downloaded                                      |

## Run command on standalone version

`{JAVA_HOME}/java -jat pacos-starter.jar`

## Example configuration with property
`{JAVA_HOME}/java -jar pacos-starter.jar -DworkingDir=/opt/pacos -DserverPort=8080`
## Docker

### Build process
- Create prod version: mvn install -Pproduction -DskipTests
- Edit core/Dockerfile about version
- Build docker image: `docker build -t pacos .`
- Run docker:
  `docker run --mount type=bind,source=C:/pacos,target=/pacos -ti -p 8090:8086 pacos  JAVA_OPTS="-Dproperty=xxx"`
- Run podman:
  `podman run 248033ca5e49 -p 8092:8086 JAVA-OPTS="-Dmodule.list.repo.url=http://127.0.0.1:8081/repository/pacos-maven-repo -Dplugin.list.repo.url=http://127.0.0.1:8081/repository/pacos-maven-repo"`
- You can launch app on localhost:8090

### Push to remote hub

- `docker login` or `podman login docker.io`
- `docker tag pacos pacos/webos:<tag>`
- `docker push pacos/pacos:<tag>`