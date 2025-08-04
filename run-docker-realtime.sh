#!/bin/bash

echo "🚀 Ejecutando contenedor con logs en tiempo real usando docker run..."

# Construir la imagen si no existe
if [[ "$(docker images -q downloadmusic:latest 2> /dev/null)" == "" ]]; then
    echo "📦 Construyendo imagen Docker..."
    docker build -t downloadmusic:latest .
fi

# Ejecutar el contenedor con logs en tiempo real usando docker run
echo "🔍 Ejecutando tests con logs en tiempo real..."
docker run --rm \
  -it \
  --name music-downloader-realtime \
  -e DOWNLOAD_PATH=/target/ \
  -e YML_PATH=/app/data \
  -e PYTHONUNBUFFERED=1 \
  -e JAVA_OPTS="-Dlogback.configurationFile=/app/src/test/resources/logback-test.xml" \
  -e MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m" \
  -v /mnt/storage/media/music/:/target/ \
  -v /home/lpzserv/downloadMusic/src/test/resources/data/songs.yml:/app/data/songs.yml:ro \
  downloadmusic:latest

echo "✅ Tests completados!" 