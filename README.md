# Download Music - Playlist Watcher (Python)

Este proyecto observa una playlist de YouTube y descarga automáticamente nuevas canciones a FLAC con metadatos y portada, brindando una solución autónoma para mantener tu colección musical actualizada.

## 🏗️ Arquitectura del Proyecto

```
.
├── youtube_watcher.py              # Punto de entrada (CLI local)
├── src/youtube_watcher/            # Paquete principal
│   ├── cli.py                      # CLI y manejo de args/env
│   ├── watcher.py                  # Bucle de monitoreo principal
│   ├── playlist_monitor.py         # Obtiene videos de la playlist (yt-dlp)
│   ├── downloader.py               # Descarga, convierte y nombra FLAC
│   └── metadata_handler.py         # Metadatos y portada (mutagen/Pillow)
├── tests/                          # Tests unitarios (pytest)
├── requirements.txt                # Dependencias Python
├── Dockerfile                      # Imagen Docker con Python + yt-dlp/ffmpeg
└── docker-compose.yml              # Orquestación (volúmenes/env)
```

## 🚀 Funcionalidades

- **Monitoreo continuo**: Observa periódicamente una playlist de YouTube
- **Descarga automática**: Detecta y descarga nuevas canciones automáticamente
- **Calidad FLAC**: Convierte audio a formato FLAC sin pérdida
- **Metadatos completos**: Añade título, artista, álbum, año y portada
- **Nombres inteligentes**: Archivos nombrados como "Artist - Title.flac"
- **Gestión de duplicados**: Evita re-descargas de videos ya procesados
- **Inicio rápido**: Script automatizado para configuración y ejecución

## 🛠️ Tecnologías Utilizadas

- **Python 3.11+**: Lenguaje principal
- **yt-dlp**: Descarga de videos de YouTube
- **ffmpeg**: Conversión de audio a FLAC
- **mutagen**: Manipulación de metadatos FLAC
- **Pillow (PIL)**: Procesamiento de imágenes y portadas
- **requests**: Descarga de portadas
- **Docker**: Contenedorización

## 🔧 Instalación Local

### Opción 1: Inicio Rápido (Recomendado)

1. Clona el repositorio:
   ```bash
   git clone <repository-url>
   cd downloadMusic
   ```

2. Ejecuta el script de inicio rápido:
   ```bash
   ./scripts/quick_start.sh
   ```

3. Sigue las instrucciones del script para configurar y ejecutar

### Opción 2: Instalación Manual

#### Prerrequisitos

1. **Python 3.11+** instalado
2. **yt-dlp** instalado globalmente
3. **ffmpeg** instalado globalmente

#### Instalación

1. Clona el repositorio:
   ```bash
   git clone <repository-url>
   cd downloadMusic
   ```

2. Instala las dependencias del sistema:
   ```bash
   ./scripts/install_dependencies.sh
   ```

3. Configura las variables de entorno:
   ```bash
   export PLAYLIST_URL="https://www.youtube.com/playlist?list=YOUR_PLAYLIST_ID"
   export DOWNLOAD_PATH="./downloads"
   export OBSERVER_INTERVAL_MS="60000"  # Opcional, default 60 segundos
   ```

4. Ejecuta el watcher:
   ```bash
   python youtube_watcher.py
   ```

## 🐳 Instalación con Docker

### Opción 1: Docker Compose (Recomendado)

1. Configura las variables de entorno:
   ```bash
   # Copia el archivo de ejemplo
   cp env.example .env
   
   # Edita .env con tu playlist (y opcionalmente HOST_DOWNLOAD_PATH)
   nano .env
   ```

2. Ejecuta:
   ```bash
   docker-compose up -d
   ```

### Opción 2: Docker Manual

1. Construye la imagen:
   ```bash
   docker build -t youtube-watcher:latest .
   ```

2. Ejecuta el contenedor:
   ```bash
   docker run -d \
     --name youtube-watcher \
     -e PLAYLIST_URL="https://www.youtube.com/playlist?list=YOUR_PLAYLIST_ID" \
     -e DOWNLOAD_PATH="/downloads" \
     -e OBSERVER_INTERVAL_MS="60000" \
     -v /path/to/local/downloads:/downloads \
     youtube-watcher:latest
   ```

## 🛠️ Scripts Útiles

- **`./scripts/quick_start.sh`**: Inicio rápido automatizado
- **`./scripts/install_dependencies.sh`**: Instalación de dependencias del sistema

## ⚙️ Configuración

### Variables de Entorno

- `PLAYLIST_URL` (requerido): URL de la playlist de YouTube a observar
- `DOWNLOAD_PATH` (opcional): Ruta donde guardar archivos FLAC (default: `./downloads`)
- `OBSERVER_INTERVAL_MS` (opcional): Intervalo de verificación en milisegundos (default: `60000`)

### Archivo de Configuración

Para facilitar la configuración, puedes usar un archivo `.env`:

1. Copia el archivo de ejemplo:
   ```bash
   cp env.example .env
   ```

2. Edita `.env` con tu configuración:
   ```bash
   PLAYLIST_URL=https://www.youtube.com/playlist?list=YOUR_PLAYLIST_ID
   DOWNLOAD_PATH=./downloads
   OBSERVER_INTERVAL_MS=60000
   ```

### Formato de Salida

- **Archivos**: FLAC con compresión nivel 8, 16-bit
- **Nombres**: `Artist - Title.flac`
- **Metadatos**: Título, artista, álbum, año, portada embebida
- **Calidad**: Conversión desde Opus calidad 0 (máxima)

## 📁 Estructura de Salida

```
downloads/
├── Artist1 - Song1.flac
├── Artist1 - Song2.flac
├── Artist2 - Song3.flac
└── ...
```

## 🔍 Monitoreo y Logs

El watcher proporciona logs detallados de:
- Inicio y configuración
- Verificación de playlist
- Detección de nuevas canciones
- Progreso de descarga y conversión
- Añadido de metadatos y portada
- Errores y advertencias

## 🚨 Solución de Problemas

### Herramientas No Encontradas

Si `yt-dlp` o `ffmpeg` no están disponibles:
```bash
# macOS
brew install yt-dlp ffmpeg

# Ubuntu/Debian
sudo apt update
sudo apt install yt-dlp ffmpeg

# Windows
# Descargar desde https://github.com/yt-dlp/yt-dlp y https://ffmpeg.org/
```

### Dependencias Python

Si hay problemas con las dependencias:
```bash
pip install --upgrade pip
pip install -r requirements.txt --force-reinstall
```

## 🧪 Tests

Para ejecutar los tests de forma local:

```bash
python -m venv venv
source venv/bin/activate
pip install -e .[dev]
pytest
```

Nota: los tests no descargan contenido real; las llamadas a `yt-dlp` se simulan donde aplica.

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request
