
---

# Download Music - Cucumber Test Project

Este proyecto automatiza la descarga de música desde YouTube utilizando Cucumber para las pruebas de comportamiento y yt-dlp para las descargas.

## 🏗️ Arquitectura del Proyecto

```
src/
├── main/java/org/example/
│   ├── services/
│   │   └── DownloadService.java      # Lógica de descarga de música
│   ├── utils/
│   │   ├── Actions.java              # Utilidades para archivos YAML
│   │   ├── Constants.java            # Constantes centralizadas
│   │   └── Globals.java              # Variables globales
│   └── Methods.java                  # Puntos de entrada para Cucumber
├── test/java/org/example/
│   ├── hooks/
│   │   └── Hooks.java               # Hooks de Cucumber
│   ├── runner/
│   │   └── RunCucumberTest.java     # Runner de pruebas
│   ├── services/
│   │   └── DownloadServiceTest.java  # Tests unitarios
│   └── steps/
│       └── DownloadSteps.java       # Definiciones de pasos
└── test/resources/
    ├── features/
    │   └── downloads.feature         # Escenarios de Cucumber
    ├── data/
    │   └── songs.yml                 # Lista de canciones
    ├── cucumber.properties           # Configuración de Cucumber
    └── logback-test.xml             # Configuración de logging
```

## 🚀 Funcionalidades

- **Búsqueda automática**: Busca URLs de YouTube para canciones especificadas
- **Descarga de audio**: Descarga y convierte videos a formato MP3
- **Gestión de duplicados**: Detecta y maneja URLs y archivos duplicados automáticamente
- **Logging detallado**: Registro completo de todas las operaciones
- **Manejo de errores**: Gestión robusta de excepciones
- **Logs en tiempo real**: Visualización inmediata de logs en contenedores

## 🛠️ Tecnologías Utilizadas

- **Java 11**: Lenguaje de programación
- **Maven**: Gestión de dependencias y build
- **Cucumber 7.11.0**: Framework de BDD
- **JUnit 5**: Framework de testing
- **yt-dlp**: Herramienta de descarga de YouTube
- **Logback**: Sistema de logging
- **SnakeYAML**: Procesamiento de archivos YAML
- **Docker**: Contenedorización

## 📋 Prerrequisitos

- Java 11 o superior
- Maven 3.6+
- yt-dlp instalado
- ffmpeg instalado
- Docker (para ejecución en contenedor)

## 🔧 Instalación

### Instalación Local

1. **Clonar el repositorio**:
   ```bash
   git clone <repository-url>
   cd downloadMusic
   ```

2. **Instalar dependencias**:
   ```bash
   mvn clean install
   ```

3. **Configurar variables de entorno** (opcional):
   ```bash
   export DOWNLOAD_PATH="/path/to/downloads/"
   export YML_PATH="/path/to/data/"
   ```

### Instalación con Docker

1. **Construir la imagen**:
   ```bash
   docker build -t downloadmusic:latest .
   ```

2. **Ejecutar el contenedor**:
   ```bash
   docker run -v /path/to/downloads:/target downloadmusic:latest
   ```

## 🐳 Ejecutar en Contenedor con Logs en Tiempo Real

### 🚀 Opción 1: Script Automático (Recomendado)

```bash
# Ejecutar el script que construye y ejecuta automáticamente
./run-container-realtime.sh
```

### 🚀 Opción 2: Pasos Manuales

#### 1. Construir la imagen Docker:
```bash
docker build -t downloadmusic:latest .
```

#### 2. Ejecutar el contenedor con logs en tiempo real:
```bash
docker run --rm -it \
  --name music-downloader \
  -e DOWNLOAD_PATH=/target/ \
  -e YML_PATH=/app/data \
  -e PYTHONUNBUFFERED=1 \
  -e JAVA_OPTS="-Dlogback.configurationFile=/app/src/test/resources/logback-test.xml" \
  -v /mnt/storage/media/music/:/target/ \
  -v /home/lpzserv/downloadMusic/src/test/resources/data/songs.yml:/app/data/songs.yml:ro \
  downloadmusic:latest
```

### 🚀 Opción 3: Usar docker-compose

```bash
# Ejecutar con docker-compose
docker-compose up --build
```

### 🚀 Opción 4: Comando Simple para Pruebas

```bash
# Construir la imagen
docker build -t downloadmusic:latest .

# Ejecutar (esto usará las rutas por defecto)
docker run --rm -it downloadmusic:latest
```

## ⚙️ Configuración de Rutas

### Verificar rutas en tu sistema:
```bash
# Verificar si existe el directorio de música
ls -la /mnt/storage/media/music/

# Verificar si existe el archivo de canciones
ls -la /home/lpzserv/downloadMusic/src/test/resources/data/songs.yml
```

### Ajustar rutas según tu sistema:
```bash
docker run --rm -it \
  --name music-downloader \
  -e DOWNLOAD_PATH=/target/ \
  -e YML_PATH=/app/data \
  -e PYTHONUNBUFFERED=1 \
  -e JAVA_OPTS="-Dlogback.configurationFile=/app/src/test/resources/logback-test.xml" \
  -v /tu/ruta/local/music/:/target/ \
  -v /tu/ruta/local/songs.yml:/app/data/songs.yml:ro \
  downloadmusic:latest
```

## 🧪 Ejecutar Pruebas

### Ejecutar todas las pruebas
```bash
mvn test
```

### Ejecutar pruebas específicas
```bash
mvn test -Dtest=RunCucumberTest
```

## 📁 Configuración

### Archivo de canciones (songs.yml)
```yaml
songs:
  - "Nombre de la canción - Artista"
  - "Otra canción - Otro artista"
```

### Variables de entorno
- `DOWNLOAD_PATH`: Ruta donde se guardarán las descargas
- `YML_PATH`: Ruta donde se encuentra el archivo songs.yml
- `PYTHONUNBUFFERED`: Para logs en tiempo real de Python
- `JAVA_OPTS`: Configuración de logging de Java

## 🔍 Troubleshooting

### Problemas Comunes

1. **Error de compilación**:
   ```bash
   mvn clean compile
   ```

2. **Problemas con yt-dlp**:
   ```bash
   pip3 install --upgrade yt-dlp
   ```

3. **Problemas de permisos**:
   ```bash
   chmod +x /usr/local/bin/yt-dlp
   ```

4. **Problemas de memoria en Docker**:
   ```bash
   docker run --memory=1g downloadmusic:latest
   ```

5. **Logs no aparecen en tiempo real**:
   ```bash
   # Verificar que el script es ejecutable
   chmod +x run-container-realtime.sh
   
   # Ejecutar con docker run directamente
   ./run-docker-realtime.sh
   ```

6. **Rutas de volumen no encontradas**:
   ```bash
   # Crear directorios si no existen
   mkdir -p /mnt/storage/media/music/
   mkdir -p /home/lpzserv/downloadMusic/src/test/resources/data/
   ```

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.
