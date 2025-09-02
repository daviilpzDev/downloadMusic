#!/usr/bin/env python3
"""
YouTube Playlist Watcher - Punto de entrada principal
"""

import sys
from pathlib import Path

# Añadir src al path para importar módulos
sys.path.insert(0, str(Path(__file__).parent / "src"))

from youtube_watcher.cli import main

if __name__ == "__main__":
    main()
