"""
YouTube Playlist Watcher - Clase principal para monitoreo continuo
"""

import logging
import time
import json
from pathlib import Path
from typing import Dict, Optional

from .downloader import YouTubeDownloader
from .playlist_monitor import PlaylistMonitor

logger = logging.getLogger(__name__)


class YouTubeWatcher:
    """
    Watcher principal que monitorea una playlist de YouTube y descarga
    automáticamente nuevas canciones.
    """
    
    def __init__(self, playlist_url: str, download_path: str, interval_ms: int = 60000, *, cookies_path: str | None = None):
        """
        Inicializar el watcher.
        
        Args:
            playlist_url: URL de la playlist de YouTube
            download_path: Directorio donde guardar archivos FLAC
            interval_ms: Intervalo de verificación en milisegundos
        """
        self.playlist_url = playlist_url
        self.download_path = Path(download_path)
        self.interval_ms = interval_ms
        self.downloaded_videos = set()
        self._state_file = self.download_path / ".downloaded.json"
        
        # Crear directorio de descargas si no existe
        self.download_path.mkdir(parents=True, exist_ok=True)
        
        # Inicializar componentes
        self.monitor = PlaylistMonitor(playlist_url, cookies_path=cookies_path)
        self.downloader = YouTubeDownloader(download_path, cookies_path=cookies_path)
        
        # Cargar estado previo de descargas
        self._load_state()

        logger.info(f"Watcher iniciado para playlist: {playlist_url}")
        logger.info(f"Directorio de descargas: {self.download_path}")
        logger.info(f"Intervalo de observación: {interval_ms}ms")
    
    def start(self):
        """Iniciar el watcher en bucle continuo"""
        logger.info("Iniciando monitoreo de playlist...")

        errors = 0
        base_sleep = max(1.0, self.interval_ms / 1000.0)
        max_backoff = 300.0  # 5 minutos

        try:
            while True:
                try:
                    self._check_playlist()
                    errors = 0
                    time.sleep(base_sleep)
                except KeyboardInterrupt:
                    raise
                except Exception as e:
                    errors += 1
                    backoff = min(max_backoff, base_sleep * (2 ** min(errors, 8)))
                    logger.error(f"Error en el watcher: {e}. Reintentando en {backoff:.1f}s")
                    time.sleep(backoff)
        except KeyboardInterrupt:
            logger.info("Watcher detenido por el usuario")
    
    def _check_playlist(self):
        """Verificar playlist para nuevas canciones"""
        try:
            logger.info("Verificando playlist para nuevas canciones...")
            
            # Obtener videos de la playlist
            videos = self.monitor.get_playlist_videos()
            
            for video_data in videos:
                self._process_video(video_data)
                
        except Exception as e:
            logger.error(f"Error verificando playlist: {e}")
    
    def _process_video(self, video_data: Dict):
        """Procesar un video individual"""
        video_id = video_data.get('id')
        title = video_data.get('title', 'Unknown Title')
        
        if not video_id:
            logger.warning(f"No se encontró ID para: {title}")
            return
        
        # Verificar si ya se descargó
        if video_id in self.downloaded_videos:
            return
        
        logger.info(f"Nueva canción detectada: {title}")
        
        try:
            self.downloader.download_and_convert(video_data)
            self.downloaded_videos.add(video_id)
            self._save_state()
            logger.info(f"✅ Descarga completada: {title}")
        except Exception as e:
            logger.error(f"Error descargando {title}: {e}")
    
    def download_latest_song(self):
        """Descargar únicamente la última canción de la playlist"""
        try:
            logger.info("Obteniendo la última canción de la playlist...")
            
            # Obtener videos de la playlist
            videos = self.monitor.get_playlist_videos()
            
            if not videos:
                logger.warning("No se encontraron canciones en la playlist")
                return None
            
            # Seleccionar la última canción por fecha si está disponible; si no, usar la primera
            latest_video = videos[0]
            try:
                candidates = [v for v in videos if isinstance(v, dict) and v.get('upload_date')]
                if candidates:
                    latest_video = max(candidates, key=lambda v: str(v.get('upload_date')))
            except Exception:
                pass
            video_id = latest_video.get('id')
            title = latest_video.get('title', 'Unknown Title')
            
            if not video_id:
                logger.warning(f"No se encontró ID para: {title}")
                return None
            
            logger.info(f"Descargando última canción: {title}")
            
            try:
                self.downloader.download_and_convert(latest_video)
                if video_id:
                    self.downloaded_videos.add(video_id)
                    self._save_state()
                logger.info(f"✅ Descarga completada: {title}")
                return latest_video
            except Exception as e:
                logger.error(f"Error descargando {title}: {e}")
                return None
                
        except Exception as e:
            logger.error(f"Error obteniendo la última canción: {e}")
            return None
    
    def get_stats(self) -> Dict:
        """Obtener estadísticas del watcher"""
        return {
            'playlist_url': self.playlist_url,
            'download_path': str(self.download_path),
            'interval_ms': self.interval_ms,
            'downloaded_count': len(self.downloaded_videos),
            'downloaded_videos': list(self.downloaded_videos)
        }

    # Estado persistente de descargas
    def _load_state(self) -> None:
        try:
            if self._state_file.exists():
                data = json.loads(self._state_file.read_text(encoding="utf-8"))
                if isinstance(data, dict) and isinstance(data.get("video_ids"), list):
                    self.downloaded_videos = set(str(v) for v in data["video_ids"])
        except Exception as e:
            logger.warning(f"No se pudo cargar estado previo: {e}")

    def _save_state(self) -> None:
        try:
            payload = {"video_ids": sorted(self.downloaded_videos)}
            self._state_file.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
        except Exception as e:
            logger.warning(f"No se pudo guardar estado: {e}")
