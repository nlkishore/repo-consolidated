FROM openjdk:8-jdk
EXPOSE 8888
WORKDIR /app
COPY target/SpringDemo.jar SpringDemo.jar
COPY target/SpringDemo.jar /app/SpringDemo.jar
CMD ["java", "-jar", "SpringDemo.jar"]
#ENTRYPOINT ["java","-jar","SpringDemo.jar"]