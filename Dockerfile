FROM eclipse-temurin:21-jdk-jammy AS build

RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update && apt-get install -y --no-install-recommends \
    libasound2 libgl1 libgtk-3-0 libx11-6 libxext6 libxi6 libxrender1 libxtst6 unzip wget \
    && rm -rf /var/lib/apt/lists/* \
    && wget -q https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-x64_bin-sdk.zip -O /tmp/javafx.zip \
    && unzip -q /tmp/javafx.zip -d /opt \
    && ln -s /opt/javafx-sdk-21 /opt/javafx-sdk \
    && rm /tmp/javafx.zip

WORKDIR /app
COPY --from=build /src/target/bmidemo.jar /app/bmidemo.jar

ENV DISPLAY=host.docker.internal:0.0
ENV DB_HOST=db \
    DB_PORT=3306 \
    DB_NAME=bmi_localization \
    DB_USER=root

ENTRYPOINT ["java", "--module-path", "/opt/javafx-sdk/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "/app/bmidemo.jar"]
