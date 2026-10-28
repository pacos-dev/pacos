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
  `docker run -ti -p 8090:8086 pacos"`
- Run podman:
  `podman run ca4d2e65f62b -p 8086:8086"`
- You can launch app on localhost:8090

###  

- Install docker-buildix that allows build image for amd64
  `brew install docker-buildx
  mkdir -p ~/.docker/cli-plugins
  ln -sfn "$(brew --prefix)/opt/docker-buildx/bin/docker-buildx" \
  ~/.docker/cli-plugins/docker-buildx`
- Build image for amd64
  `docker buildx build \
  --platform linux/amd64 \
  --pull \
  -t docker.io/pacosdev/webos:latest \
  --push . `
### Push to remote hub

- `docker login` or `podman login docker.io`
- `docker tag pacos pacosdev/webos:<tag>`
- `docker push pacosdev/webos:<tag>`
  