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

# Configurar Maven para evitar problemas de memoria
ENV MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"

# Ejecutar pruebas de Cucumber con Maven con timeout
CMD ["timeout", "300", "mvn", "test", "-Dtest=RunCucumberTest"]
