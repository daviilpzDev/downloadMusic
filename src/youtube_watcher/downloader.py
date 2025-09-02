"""
Downloader de YouTube - Descarga y convierte videos a FLAC
"""

import logging
import subprocess
from pathlib import Path
from typing import Dict, Optional

from .metadata_handler import MetadataHandler

logger = logging.getLogger(__name__)


class YouTubeDownloader:
    """
    Clase para descargar y convertir videos de YouTube a FLAC
    """
    
    def __init__(self, download_path: str, cookies_path: str | None = None):
        """
        Inicializar downloader.
        
        Args:
            download_path: Directorio donde guardar archivos
        """
        self.download_path = Path(download_path)
        self.metadata_handler = MetadataHandler()
        self.cookies_path = cookies_path
        
        # Crear directorio si no existe
        self.download_path.mkdir(parents=True, exist_ok=True)
    
    def download_and_convert(self, video_data: Dict):
        """
        Descargar y convertir un video a FLAC con metadatos.
        
        Args:
            video_data: Diccionario con información del video
        """
        title = video_data.get('title', 'Unknown Title')
        artist = (video_data.get('artist') or 
                 video_data.get('channel') or 
                 video_data.get('uploader', 'Unknown Artist'))
        album = video_data.get('album', f"YouTube - {artist}")
        upload_date = video_data.get('upload_date')
        year = upload_date[:4] if upload_date and len(upload_date) >= 4 else None
        thumbnail_url = video_data.get('thumbnail')
        
        # Sanitizar nombres
        safe_title = self._sanitize_filename(title)
        safe_artist = self._sanitize_filename(artist)
        
        # Crear nombre de archivo
        filename = f"{safe_artist} - {safe_title}.flac"
        filename = self._trim_filename(filename, max_len=200)
        output_path = self.download_path / filename
        
        # Evitar duplicados: si el archivo ya existe, no volver a descargar
        if output_path.exists():
            logger.info(f"Archivo ya existe, omitiendo descarga: {filename}")
            return
        
        # Paso 1: Descargar audio en Opus
        temp_opus = self._download_opus(video_data, title)
        if not temp_opus:
            return
        
        # Paso 2: Convertir a FLAC
        if not self._convert_to_flac(temp_opus, output_path, title):
            return
        
        # Limpiar archivo temporal
        temp_opus.unlink(missing_ok=True)
        
        # Paso 3: Añadir metadatos y portada
        self.metadata_handler.add_metadata_and_cover(
            output_path, title, artist, album, year, thumbnail_url
        )
        
        logger.info(f"FLAC listo: {filename}")
    
    def _download_opus(self, video_data: Dict, title: str) -> Optional[Path]:
        """
        Descargar audio en formato Opus.
        
        Args:
            video_data: Información del video
            title: Título del video
            
        Returns:
            Path al archivo Opus temporal o None si falla
        """
        base_filename = f"temp_{video_data['id']}"
        temp_opus = self.download_path / f"{base_filename}.opus"
        
        download_cmd = [
            "yt-dlp",
            "--extract-audio",
            "--audio-format", "opus",
            "--audio-quality", "0",
            "-o", str(self.download_path / f"{base_filename}.%(ext)s"),
        ]
        if self.cookies_path:
            download_cmd += ["--cookies", self.cookies_path]
        download_cmd += [f"https://www.youtube.com/watch?v={video_data['id']}"]
        
        try:
            logger.info(f"Descargando '{title}' en Opus...")
            subprocess.run(download_cmd, check=True, capture_output=True, text=True)
            
            # Buscar el archivo descargado (puede tener extensión .opus o .webm)
            for ext in ['opus', 'webm']:
                downloaded_file = self.download_path / f"{base_filename}.{ext}"
                if downloaded_file.exists():
                    return downloaded_file
            
            logger.error(f"No se encontró archivo descargado para '{title}'")
            return None
            
        except subprocess.CalledProcessError as e:
            logger.error(f"Error descargando '{title}': {e}")
            if e.stderr:
                logger.error(f"yt-dlp stderr: {e.stderr}")
            if e.stdout:
                logger.error(f"yt-dlp stdout: {e.stdout}")
            return None
    
    def _convert_to_flac(self, opus_path: Path, flac_path: Path, title: str) -> bool:
        """
        Convertir archivo Opus a FLAC.
        
        Args:
            opus_path: Path al archivo Opus
            flac_path: Path de salida para FLAC
            title: Título del video
            
        Returns:
            True si la conversión fue exitosa
        """
        ffmpeg_cmd = [
            "ffmpeg",
            "-i", str(opus_path),
            "-vn",
            "-acodec", "flac",
            "-compression_level", "8",
            "-sample_fmt", "s16",
            "-y",
            str(flac_path)
        ]
        
        try:
            logger.info(f"Convirtiendo a FLAC: {flac_path.name}")
            subprocess.run(ffmpeg_cmd, check=True, capture_output=True, text=True)
            return True
        except subprocess.CalledProcessError as e:
            logger.error(f"Error convirtiendo a FLAC para '{title}': {e}")
            if e.stderr:
                logger.error(f"ffmpeg stderr: {e.stderr}")
            if e.stdout:
                logger.error(f"ffmpeg stdout: {e.stdout}")
            return False
    
    def _sanitize_filename(self, filename: str) -> str:
        """
        Sanitizar nombre de archivo.
        
        Args:
            filename: Nombre original
            
        Returns:
            Nombre sanitizado
        """
        # Reemplazar caracteres problemáticos
        filename = filename.replace('/', '_').replace('\\', '_')
        
        # Mantener solo caracteres seguros
        safe_chars = []
        for char in filename:
            if char.isalnum() or char in (' ', '.', '-', '_'):
                safe_chars.append(char)
        
        filename = ''.join(safe_chars).strip()
        
        # Eliminar punto final
        if filename.endswith('.'):
            filename = filename[:-1]
        # Colapsar espacios múltiples
        filename = ' '.join(filename.split())

        return filename

    def _trim_filename(self, filename: str, max_len: int = 200) -> str:
        """
        Truncar el nombre del archivo manteniendo la extensión si excede max_len.
        """
        try:
            if len(filename) <= max_len:
                return filename
            stem, dot, ext = filename.rpartition('.')
            if not dot:  # no extension
                return filename[:max_len]
            keep = max_len - len(ext) - 1  # account for dot
            stem = stem[:keep]
            return f"{stem}.{ext}"
        except Exception:
            return filename[:max_len]
