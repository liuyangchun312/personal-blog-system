FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y default-mysql-client curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY target/enterprise-blog-1.0.0.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -fsS http://127.0.0.1:8080/api/actuator/health/readiness >/dev/null || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
