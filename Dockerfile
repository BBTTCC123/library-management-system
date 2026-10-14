# ---------- 1. 前端构建 ----------
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install --no-fund --no-audit
COPY frontend/ ./
# vite.config.js 中 outDir = ../src/main/resources/static
RUN npm run build

# ---------- 2. 后端构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# 先复制 pom.xml 以利用依赖缓存
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# 用容器内新构建的前端产物覆盖仓库中的静态资源
COPY --from=frontend /app/src/main/resources/static ./src/main/resources/static
RUN mvn -B -q clean package -DskipTests

# ---------- 3. 运行 ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 非 root 用户运行
RUN useradd -r -u 1001 appuser
COPY --from=build /build/target/library-management-system-*.jar app.jar
USER appuser

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
