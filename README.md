
---

# PREREQUISITES

1. Put your music list in `/src/test/resources/data/songs.yml`
2. Install Navidrome in your server with docker compose (https://www.navidrome.org/docs/installation/docker/)
3. Put in Navidrome volumes :
    - `ubication/to/save/navidrome/data/:/data`
    - `/mnt/storage/media/music/:/music` (Where music will be downloaded)
---

# Server Deployment Guide

This document explains the steps to upload, build, and run the `downloadMusic` project on a server using Docker.

## 1️⃣ Upload the Files to the Server

To transfer the project files from your local machine to the server, you can use `scp` or `rsync`:

### Using `rsync`:
```bash
rsync -avz ./downloadMusic user@server-ip:/destination/path
```

**Note:** Make sure you have `scp` or `rsync` installed on your local machine. You can also use `git clone` if you have Git installed on the server.

## 2️⃣ Access the Server

Connect to your server via SSH:
```bash
ssh -i example user@server-ip
```
**Note:** Replace `user` and `server-ip` with your actual values.

## 3️⃣ Navigate to the Project Directory

Once connected to the server, navigate to the directory where you uploaded the project:
```bash
cd /destination/path/downloadMusic
```

## 4️⃣ Build and Start the Containers

To build the Docker images and start the containers, run:
```bash
docker build -t downloadmusic:latest .
docker-compose up -d
```

### Explanation of options:
- `--build`: Forces a rebuild of the images with the latest changes.
- `-d`: Runs the containers in the background (detached mode).

To verify that the containers are running, use:
```bash
docker ps
```

## 5️⃣ Review the Logs

If you need to check what is happening inside the containers, you can view the logs by running:
```bash
docker-compose logs -f
```
**Note:** Use `CTRL + C` to exit the logs.

To view logs of a specific container (e.g., `music-downloader`):
```bash
docker logs -f music-downloader
```

## 6️⃣ Run Tests Manually

If you want to run the tests manually inside the container, first access the container:
```bash
docker exec -it music-downloader bash
```

Then, inside the container, run the tests with:
```bash
mvn test
```

## 7️⃣ Manage Containers

Here are some useful commands to manage the containers:

- **Stop all containers:**
  ```bash
  docker-compose down
  ```

- **Restart the containers:**
  ```bash
  docker-compose restart
  ```

- **Remove old images and containers (cleanup):**
  ```bash
  docker system prune -af
  ```

---
