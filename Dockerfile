FROM openjdk:8-jdk-alpine
WORKDIR /app

COPY target/DataEngineService-0.0.1-SNAPSHOT.jar /app/tripy.jar
COPY kafka.properties /app/kafka.properties
COPY src/dbscan/target/scala-2.10/spark_dbscan_2.10-0.0.4.jar /app/spark_dbscan_2.10-0.0.4.jar

ENV DB_URL jdbc:postgresql://dpg-cglesuu4dad69r7upa9g-a.frankfurt-postgres.render.com:5432/tripy
ENV DB_USER admin
ENV DB_PASS RqrWKnc3gCJkTOnhO4PAk9Rt6BqQjdXV

CMD ["java", "-jar", "/app/tripy.jar"]
