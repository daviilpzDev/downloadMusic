Changelog

Este proyecto sigue el formato Keep a Changelog y Semantic Versioning (SemVer).

Guía rápida de SemVer (X.Y.Z)
- X (MAJOR): cambios incompatibles (rompes CLI, variables obligatorias, formato de salida, rutas…)
- Y (MINOR): nuevas funcionalidades compatibles (nuevas flags/opciones, mejoras sin romper contratos)
- Z (PATCH): correcciones y mejoras internas (bugs, rendimiento, logs)

Cómo publicar una versión
1) En “Unreleased”, lista los cambios hechos. Cuando cortes versión, muévelos a una nueva sección `X.Y.Z - YYYY-MM-DD`.
2) Crea el tag Git `vX.Y.Z` y haz push. La versión del paquete se deriva del tag vía `setuptools_scm`.
3) El workflow de GitHub Actions crea la Release, adjunta artefactos y (opcional) publica la imagen Docker.

Unreleased
- Added:
- Changed:
- Fixed:

3.0.2 - 2025-09-02
- Added: Documentación de versionado y releases en README y este CHANGELOG.
- Added: Workflow de GitHub Actions para releases (paquete + GitHub Release, Docker opcional).
- Added: Pre-commit, EditorConfig, Makefile y configuraciones de flake8/black para un flujo profesional.
- Changed: Versionado dinámico desde tags (`setuptools_scm`) y validación tag/paquete en el workflow.
- Changed: Docker Compose con healthcheck y mapeo de usuario configurable (`UID`/`GID`).

3.0.1 - 2025-09-02
- Added: `LOG_LEVEL`, soporte de `COOKIES_FILE`, y guía de despliegue en Portainer.
- Changed: Reorganización del proyecto (runner vía `python -m youtube_watcher`, scripts en `scripts/`, retirada de `setup.py`).
- Changed: Docker ejecuta como usuario no-root y `yt-dlp` pinneado por ARG.
- Changed: Parsing de playlist más robusto y selección de “última canción” por `upload_date` cuando esté.
- Fixed: Logs de error detallados de `yt-dlp/ffmpeg`, limpieza de portadas duplicadas, truncado seguro de nombres largos.
- Fixed: Estado persistente de descargas (`.downloaded.json`) y backoff exponencial en el watcher.

3.0.0 - 2025-09-02
- Added: Migración mayor y estructura base del watcher en Python (CLI, monitor, downloader y metadatos) con soporte de Docker/Compose.
