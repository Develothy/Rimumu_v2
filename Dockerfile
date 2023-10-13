FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 JAR 파일 복사
COPY . /app
#COPY ./build/libs/rimumu-0.0.1-SNAPSHOT.jar /app/app.jar

# 애플리케이션 실행
CMD ["java", "-jar", "./build/libs/rimumu-0.0.1-SNAPSHOT.jar"]