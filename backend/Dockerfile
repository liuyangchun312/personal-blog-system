FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y default-mysql-client && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY target/enterprise-blog-1.0.0.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD sh -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /api/actuator/health HTTP/1.0\r\n\r\n" >&3 && grep -q UP <&3'
ENTRYPOINT ["java","-jar","/app/app.jar"]
