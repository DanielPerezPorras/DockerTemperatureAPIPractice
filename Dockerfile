FROM java:8
WORKDIR /
EXPOSE 4567 6379
ADD out/artifacts/temp_api_jar/java_spark.jar java_spark.jar
ENV REDIS_HOST redis
CMD java -jar java_spark.jar
