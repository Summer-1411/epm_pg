FROM openjdk:8-jre-alpine
USER root
ENV TZ="Asia/Ho_Chi_Minh"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
MAINTAINER epm
RUN mkdir /app
RUN mkdir /app/config
COPY target/PMTTDT-0.0.1-SNAPSHOT.jar /app/
COPY config/* /app/config/
WORKDIR /app
CMD ["java", "-Xms256m", "-Xmx8g", "-Duser.timezone=GMT+7", "-jar", "PMTTDT-0.0.1-SNAPSHOT.jar"]
EXPOSE 9005