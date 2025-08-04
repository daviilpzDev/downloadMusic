#!/bin/bash

echo "🚀 Ejecutando contenedor con logs en tiempo real..."

# Construir la imagen si no existe
if [[ "$(docker images -q downloadmusic:latest 2> /dev/null)" == "" ]]; then
    echo "📦 Construyendo imagen Docker..."
    docker build -t downloadmusic:latest .
fi

# Ejecutar el contenedor con logs en tiempo real
echo "🔍 Ejecutando tests con logs en tiempo real..."
docker-compose up --build --abort-on-container-exit

echo "✅ Tests completados!" 