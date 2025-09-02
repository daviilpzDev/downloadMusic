#!/usr/bin/env python3
"""
Script de prueba para descargar la última canción
"""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent / "src"))

from youtube_watcher.watcher import YouTubeWatcher

def main():
    playlist_url = "https://music.youtube.com/playlist?list=PLFgquLnL59anUbTCum31nHshzm-3pAMP-"
    download_path = "./downloads"
    
    print("🎵 Probando descarga de última canción...")
    print(f"Playlist: {playlist_url}")
    print(f"Directorio: {download_path}")
    print()
    
    try:
        watcher = YouTubeWatcher(playlist_url, download_path)
        result = watcher.download_latest_song()
        
        if result:
            print(f"✅ Descarga completada: {result.get('title', 'Unknown')}")
        else:
            print("❌ Error en la descarga")
            
    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    main()
