FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

WORKDIR /
ADD target/starter-2.0.0.R1-jar-with-dependencies.jar starter.jar

RUN useradd -m pacosuser

RUN mkdir -p /opt/.pacos
RUN chown -R pacosuser:pacosuser /opt/.pacos
RUN chmod 755 /opt/.pacos

USER pacosuser
EXPOSE 8086
# Start app
CMD ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar starter.jar"]