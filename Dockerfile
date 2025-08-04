# Imagen base con Maven y JDK 11 para la compilación
FROM maven:3.9.0-eclipse-temurin-11 AS build

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente
COPY src ./src

# Construir el proyecto sin ejecutar pruebas
RUN mvn clean package -DskipTests

# Imagen final para ejecutar las pruebas
FROM maven:3.9.0-eclipse-temurin-11

# Instalar yt-dlp y dependencias necesarias
RUN apt-get update && apt-get install -y \
    curl \
    ffmpeg \
    python3 \
    python3-pip && \
    pip3 install --upgrade yt-dlp && \
    rm -rf /var/lib/apt/lists/*

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el código y la configuración
COPY --from=build /app /app

# Definir variable de entorno para descargas
ENV DOWNLOAD_PATH="/target"

# Configurar variables de entorno para logging en tiempo real
ENV PYTHONUNBUFFERED=1
ENV JAVA_OPTS="-Dlogback.configurationFile=/app/src/test/resources/logback-test.xml"

# Crear script para ejecutar con salida no bufferizada
RUN echo '#!/bin/bash\n\
set -e\n\
echo "Iniciando tests con salida no bufferizada..."\n\
timeout 300 mvn test -Dtest=RunCucumberTest -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.showThreadName=true -Dorg.slf4j.simpleLogger.showLogName=true -Dorg.slf4j.simpleLogger.showShortLogName=true -Dorg.slf4j.simpleLogger.logFile=System.out\n\
' > /app/run-tests.sh && chmod +x /app/run-tests.sh

# Ejecutar con script que fuerza salida no bufferizada
CMD ["/app/run-tests.sh"]
