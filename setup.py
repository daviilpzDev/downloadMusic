"""
Setup script para YouTube Playlist Watcher
"""

from setuptools import setup, find_packages

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

with open("requirements.txt", "r", encoding="utf-8") as fh:
    requirements = [line.strip() for line in fh if line.strip() and not line.startswith("#")]

setup(
    name="youtube-playlist-watcher",
    version="1.0.0",
    author="David Lopez",
    description="YouTube Playlist Watcher - Descarga automática a FLAC",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/yourusername/downloadMusic",
    packages=find_packages(where="src"),
    package_dir={"": "src"},
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: End Users/Desktop",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.11",
        "Programming Language :: Python :: 3.12",
        "Topic :: Multimedia :: Sound/Audio",
        "Topic :: Internet :: WWW/HTTP",
    ],
    python_requires=">=3.11",
    install_requires=requirements,
    entry_points={
        "console_scripts": [
            "youtube-watcher=youtube_watcher.cli:main",
        ],
    },
    extras_require={
        "dev": [
            "pytest>=7.0.0",
            "pytest-cov>=4.0.0",
            "black>=23.0.0",
            "flake8>=6.0.0",
            "mypy>=1.0.0",
        ],
    },
    keywords="youtube, music, download, flac, playlist, watcher",
    project_urls={
        "Bug Reports": "https://github.com/yourusername/downloadMusic/issues",
        "Source": "https://github.com/yourusername/downloadMusic",
        "Documentation": "https://github.com/yourusername/downloadMusic#readme",
    },
)
