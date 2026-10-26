FROM jenkins/jenkins:lts-jdk21

USER root

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        maven \
        libatk1.0-0 \
        libatk-bridge2.0-0 \
        libatspi2.0-0 \
        libx11-6 \
        libxcomposite1 \
        libxdamage1 \
        libxext6 \
        libxfixes3 \
        libxrandr2 \
        libgbm1 \
        libxcb1 \
        libxkbcommon0 \
        libxcursor1 \
        libgtk-3-0 \
        libpangocairo-1.0-0 \
        libcairo-gobject2 \
        libgdk-pixbuf-2.0-0 \
    && rm -rf /var/lib/apt/lists/*

USER jenkins