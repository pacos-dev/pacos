## Create dir structure
mkdir -p /srv/ci/jenkins
mkdir -p /srv/ci/nexus
mkdir -p /srv/ci/sonar/data
mkdir -p /srv/ci/sonar/extensions
chown -R root:root /srv/ci
chmod -R 755 /srv/ci

sudo chown -R 1000:1000 /srv/ci/sonar/data
sudo chown -R 1000:1000 /srv/ci/sonar/extensions
sudo chmod -R 775 /srv/ci/sonar/data
sudo chmod -R 775 /srv/ci/sonar/extensions

## add permissions
chown -R 1000:1000 /srv/ci/jenkins
chown -R 200:200 /srv/ci/nexus
chown -R 1000:1000 /srv/ci/sonar


apt install podman-compose
podman system service -t 0 &
podman-compose up -d

##access
http://HOST:8080/jenkins/

## Podman
`podman-compose up -d docusaurus` start single image from compose


sudo systemctl reload nginx


sudo certbot --nginx -d jenkins.pacos.dev
sudo ln -s /etc/nginx/sites-available/jenkins.pacos.dev /etc/nginx/sites-enabled/