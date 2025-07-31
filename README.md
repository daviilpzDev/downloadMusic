
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
- **Renombrado automático**: Renombra archivos según el nombre de la canción
- **Logging detallado**: Registro completo de todas las operaciones
- **Manejo de errores**: Gestión robusta de excepciones

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

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.
