FROM openjdk:21-jdk

# Setup environment
ENV JAVA_TOOL_OPTIONS -Dfile.encoding=UTF8
ENV DOCKER true

# Setup workspace
RUN mkdir -p /run
RUN mkdir -p /run/lib

ADD ./lib /run/lib
ADD ./run.sh /run
ADD ./server.jar /run
ADD ./log4j.xml /run
WORKDIR /
MAINTAINER ultreon

# Set permissions for jar
RUN chmod +x ./run/server.jar

# Set volumes
VOLUME /run

# Expose port
EXPOSE 38800

# Set working directory
WORKDIR /run

# Set stop signal
STOPSIGNAL SIGINT

# Set entrypoint
ENTRYPOINT ["bash", "run.sh"]
