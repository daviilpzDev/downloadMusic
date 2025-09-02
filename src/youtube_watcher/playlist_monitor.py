"""
Monitor de playlist de YouTube - Obtiene información de videos
"""

import json
import logging
import subprocess
from typing import Dict, List

logger = logging.getLogger(__name__)


class PlaylistMonitor:
    """
    Clase para monitorear y obtener información de playlists de YouTube
    """
    
    def __init__(self, playlist_url: str, cookies_path: str | None = None):
        """
        Inicializar monitor de playlist.
        
        Args:
            playlist_url: URL de la playlist de YouTube
        """
        self.playlist_url = playlist_url
        self.cookies_path = cookies_path
    
    def get_playlist_videos(self) -> List[Dict]:
        """
        Obtener lista de videos de la playlist.

        Returns:
            Lista de diccionarios con información de cada video
        """
        try:
            # Obtener metadatos por-video de la playlist. yt-dlp imprime 1 JSON por video.
            cmd = ["yt-dlp", "--print-json", "--no-simulate", "--skip-download"]
            if self.cookies_path:
                cmd += ["--cookies", self.cookies_path]
            cmd += [self.playlist_url]
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)

            videos: List[Dict] = []

            for line in result.stdout.strip().splitlines():
                if not line.strip():
                    continue
                try:
                    item = json.loads(line)
                except json.JSONDecodeError:
                    continue

                # Si se obtuvo un objeto playlist con entries (poco común con --print-json), expandir
                if isinstance(item, dict) and 'entries' in item and isinstance(item['entries'], list):
                    for entry in item['entries']:
                        if entry:
                            videos.append(entry)
                else:
                    videos.append(item)

            logger.info(f"Obtenidos {len(videos)} videos de la playlist")
            return videos
        
        except subprocess.CalledProcessError as e:
            stderr = e.stderr if isinstance(e.stderr, str) else (e.stderr.decode(errors='ignore') if e.stderr else '')
            logger.error(f"Error ejecutando yt-dlp: {e}\nstderr: {stderr}")
            return []
        except Exception as e:
            logger.error(f"Error obteniendo videos de playlist: {e}")
            return []
    
    def get_playlist_info(self) -> Dict:
        """
        Obtener información general de la playlist.

        Returns:
            Diccionario con información de la playlist
        """
        try:
            # Usar un único JSON con metadatos de la playlist
            cmd = ["yt-dlp", "-J"]  # equivalente a --dump-single-json
            if self.cookies_path:
                cmd += ["--cookies", self.cookies_path]
            cmd += [self.playlist_url]
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)

            data = json.loads(result.stdout)
            entries = data.get('entries', [])

            return {
                'title': data.get('title', 'Unknown Playlist'),
                'uploader': data.get('uploader', 'Unknown'),
                'video_count': len(entries) if isinstance(entries, list) else 0,
                'description': data.get('description', ''),
                'upload_date': data.get('upload_date', '')
            }
            
        except subprocess.CalledProcessError as e:
            stderr = e.stderr if isinstance(e.stderr, str) else (e.stderr.decode(errors='ignore') if e.stderr else '')
            logger.error(f"Error ejecutando yt-dlp para info de playlist: {e}\nstderr: {stderr}")
            return {}
        except Exception as e:
            logger.error(f"Error obteniendo información de playlist: {e}")
            return {}
