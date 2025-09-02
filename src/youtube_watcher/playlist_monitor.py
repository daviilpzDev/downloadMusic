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
            # Usar salida única de la playlist en modo "flat" para minimizar errores
            cmd = [
                "yt-dlp",
                "-J",
                "--flat-playlist",
                "--ignore-errors",
                "--cache-dir",
                "/tmp/yt-dlp-cache",
            ]
            if self.cookies_path:
                cmd += ["--cookies", self.cookies_path]
            cmd += [self.playlist_url]

            result = subprocess.run(cmd, capture_output=True, text=True, check=False)

            videos: List[Dict] = []
            if result.stdout:
                try:
                    data = json.loads(result.stdout)
                    entries = data.get("entries", []) if isinstance(data, dict) else []
                    for entry in entries:
                        if entry and isinstance(entry, dict):
                            videos.append(entry)
                except json.JSONDecodeError:
                    pass

            logger.info(f"Obtenidos {len(videos)} videos de la playlist")
            return videos

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
            cmd = [
                "yt-dlp",
                "-J",  # equivalente a --dump-single-json
                "--ignore-errors",
                "--cache-dir",
                "/tmp/yt-dlp-cache",
            ]
            if self.cookies_path:
                cmd += ["--cookies", self.cookies_path]
            cmd += [self.playlist_url]
            result = subprocess.run(cmd, capture_output=True, text=True, check=False)

            if not result.stdout:
                return {}

            data = json.loads(result.stdout)
            entries = data.get("entries", [])

            return {
                "title": data.get("title", "Unknown Playlist"),
                "uploader": data.get("uploader", "Unknown"),
                "video_count": len(entries) if isinstance(entries, list) else 0,
                "description": data.get("description", ""),
                "upload_date": data.get("upload_date", ""),
            }

        except Exception as e:
            logger.error(f"Error obteniendo información de playlist: {e}")
            return {}
