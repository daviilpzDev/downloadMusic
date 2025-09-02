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
            # Usar salida única de la playlist en modo "flat"; tolerar errores
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
            stdout = (result.stdout or "").strip()
            if stdout:
                # Intentar parsear un único JSON (playlist o video)
                try:
                    data = json.loads(stdout)
                    if isinstance(data, dict):
                        entries = data.get("entries")
                        if isinstance(entries, list) and entries:
                            for entry in entries:
                                if entry and isinstance(entry, dict):
                                    videos.append(entry)
                        else:
                            # Fallback: salida de un solo video
                            if data.get("id"):
                                videos.append(data)
                    # Si no es dict, caeremos al parseo por líneas
                except json.JSONDecodeError:
                    # Fallback: varias líneas JSON
                    for line in stdout.splitlines():
                        if not line.strip():
                            continue
                        try:
                            item = json.loads(line)
                            if item:
                                videos.append(item)
                        except json.JSONDecodeError:
                            continue

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
