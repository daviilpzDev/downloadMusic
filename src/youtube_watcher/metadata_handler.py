"""
Manejador de metadatos - Añade tags y portada a archivos FLAC
"""

import logging
import requests
from pathlib import Path
from typing import Optional
from PIL import Image
from io import BytesIO
from mutagen.flac import FLAC, Picture

logger = logging.getLogger(__name__)


class MetadataHandler:
    """
    Clase para manejar metadatos y portadas de archivos FLAC
    """
    
    def __init__(self):
        """Inicializar manejador de metadatos"""
        pass
    
    def add_metadata_and_cover(self, flac_path: Path, title: str, artist: str, 
                              album: str, year: Optional[str], thumbnail_url: Optional[str]):
        """
        Añadir metadatos y portada al archivo FLAC.
        
        Args:
            flac_path: Path al archivo FLAC
            title: Título de la canción
            artist: Artista
            album: Álbum
            year: Año de lanzamiento
            thumbnail_url: URL de la portada
        """
        try:
            audio = FLAC(flac_path)
            
            # Metadatos básicos
            audio['title'] = title
            audio['artist'] = artist
            audio['album'] = album
            if year:
                audio['date'] = year
                audio['originalyear'] = year
            
            # Portada
            if thumbnail_url:
                # Evitar duplicados de portadas
                try:
                    audio.clear_pictures()
                except Exception:
                    pass
                self._add_cover(audio, thumbnail_url, title)
            
            # Guardar cambios
            audio.save()
            logger.info(f"Metadatos guardados para '{title}'")
            
        except Exception as e:
            logger.error(f"Error añadiendo metadatos: {e}")
    
    def _add_cover(self, audio: FLAC, thumbnail_url: str, title: str):
        """
        Añadir portada al archivo FLAC.
        
        Args:
            audio: Objeto FLAC
            thumbnail_url: URL de la portada
            title: Título de la canción
        """
        try:
            logger.info(f"Descargando portada para '{title}'...")
            
            # Descargar imagen
            response = requests.get(thumbnail_url, timeout=10)
            response.raise_for_status()
            
            # Procesar imagen
            img_data = self._process_image(response.content)
            if not img_data:
                return
            
            # Crear objeto Picture
            picture = Picture()
            picture.data = img_data
            picture.type = 3  # Front cover
            picture.mime = "image/jpeg"
            
            # Añadir portada
            audio.add_picture(picture)
            logger.info(f"Portada añadida para '{title}'")
            
        except requests.exceptions.RequestException as e:
            logger.warning(f"Error al descargar la portada para '{title}': {e}")
        except Exception as e:
            logger.warning(f"Error al procesar la portada para '{title}': {e}")
    
    def _process_image(self, image_content: bytes) -> Optional[bytes]:
        """
        Procesar y redimensionar imagen.
        
        Args:
            image_content: Contenido de la imagen
            
        Returns:
            Bytes de la imagen procesada o None si falla
        """
        try:
            # Abrir imagen
            img = Image.open(BytesIO(image_content))
            
            # Redimensionar manteniendo proporción
            img.thumbnail((1000, 1000), Image.LANCZOS)
            
            # Convertir a bytes JPEG
            img_bytes = BytesIO()
            img.save(img_bytes, format="JPEG", quality=95)
            
            return img_bytes.getvalue()
            
        except Exception as e:
            logger.warning(f"Error procesando imagen: {e}")
            return None
