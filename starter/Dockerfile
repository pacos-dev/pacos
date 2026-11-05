FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

WORKDIR /

COPY target/starter-3.5.1-jar-with-dependencies.jar starter.jar

RUN useradd -m pacosuser \
    && mkdir -p /opt/.pacos \
    && chown -R pacosuser:pacosuser /opt/.pacos \
    && chmod 755 /opt/.pacos

USER pacosuser

EXPOSE 8086

CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar starter.jar"]