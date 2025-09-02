"""
Tests para YouTube Playlist Watcher
"""

import pytest
from pathlib import Path
from unittest.mock import Mock, patch

from youtube_watcher.watcher import YouTubeWatcher
from youtube_watcher.playlist_monitor import PlaylistMonitor
from youtube_watcher.downloader import YouTubeDownloader


class TestYouTubeWatcher:
    """Tests para la clase principal YouTubeWatcher"""

    def test_init(self):
        """Test de inicialización"""
        watcher = YouTubeWatcher("https://example.com", "./test_downloads", 30000)

        assert watcher.playlist_url == "https://example.com"
        assert watcher.download_path == Path("./test_downloads")
        assert watcher.interval_ms == 30000
        assert len(watcher.downloaded_videos) == 0

    def test_get_stats(self):
        """Test de obtención de estadísticas"""
        watcher = YouTubeWatcher("https://example.com", "./test_downloads")
        stats = watcher.get_stats()

        assert stats["playlist_url"] == "https://example.com"
        assert stats["download_path"] == "./test_downloads"
        assert stats["interval_ms"] == 60000
        assert stats["downloaded_count"] == 0


class TestPlaylistMonitor:
    """Tests para PlaylistMonitor"""

    def test_init(self):
        """Test de inicialización"""
        monitor = PlaylistMonitor("https://example.com")
        assert monitor.playlist_url == "https://example.com"

    @patch("subprocess.run")
    def test_get_playlist_videos_success(self, mock_run):
        """Test de obtención exitosa de videos"""
        mock_result = Mock()
        mock_result.stdout = '{"id": "123", "title": "Test Video"}'
        mock_result.returncode = 0
        mock_run.return_value = mock_result

        monitor = PlaylistMonitor("https://example.com")
        videos = monitor.get_playlist_videos()

        assert len(videos) == 1
        assert videos[0]["id"] == "123"
        assert videos[0]["title"] == "Test Video"

    @patch("subprocess.run")
    def test_get_playlist_videos_failure(self, mock_run):
        """Test de fallo en obtención de videos"""
        mock_run.side_effect = Exception("yt-dlp error")

        monitor = PlaylistMonitor("https://example.com")
        videos = monitor.get_playlist_videos()

        assert len(videos) == 0


class TestYouTubeDownloader:
    """Tests para YouTubeDownloader"""

    def test_init(self):
        """Test de inicialización"""
        downloader = YouTubeDownloader("./test_downloads")
        assert downloader.download_path == Path("./test_downloads")

    def test_sanitize_filename(self):
        """Test de sanitización de nombres de archivo"""
        downloader = YouTubeDownloader("./test_downloads")

        # Test casos normales
        assert downloader._sanitize_filename("Test Song") == "Test Song"
        assert downloader._sanitize_filename("Test/Song") == "Test_Song"
        assert downloader._sanitize_filename("Test.Song.") == "Test.Song"

        # Test caracteres especiales
        assert downloader._sanitize_filename("Test@Song#") == "TestSong"
        assert downloader._sanitize_filename("Test Song (Remix)") == "Test Song Remix"


@pytest.fixture
def temp_dir(tmp_path):
    """Fixture para directorio temporal"""
    return tmp_path


@pytest.fixture
def sample_video_data():
    """Fixture para datos de video de ejemplo"""
    return {
        "id": "test123",
        "title": "Test Song",
        "artist": "Test Artist",
        "channel": "Test Channel",
        "uploader": "Test Uploader",
        "upload_date": "20230101",
        "thumbnail": "https://example.com/thumb.jpg",
    }
