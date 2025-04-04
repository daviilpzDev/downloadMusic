Para ejecutar todo en tu servidor correctamente, sigue estos pasos:

1️⃣ Subir los archivos al servidor
Si estás en tu PC local, usa scp o rsync para copiar los archivos al servidor.
rsync -avz ./downloadMusic usuario@ip-del-servidor:/ruta/destino

📌 Asegúrate de tener scp o rsync instalados en tu máquina local.

2️⃣ Acceder al servidor
Conéctate a tu servidor por SSH:
ssh usuario@ip-del-servidor

📌 Reemplaza usuario e ip-del-servidor por los valores correctos.

3️⃣ Ir al directorio del proyecto
cd /ruta/destino/downloadMusic

4️⃣ Construir y levantar los contenedores
Ejecuta lo siguiente:
docker-compose up --build -d

📌 Explicación:

--build: Asegura que Docker construya la imagen con los últimos cambios.

-d: Ejecuta los contenedores en segundo plano (modo "detached").

Puedes verificar que los contenedores están corriendo con:
docker ps

5️⃣ Revisar los logs
Si necesitas ver lo que está pasando:
docker-compose logs -f
📌 Usa CTRL + C para salir de los logs.

Para ver logs de un solo servicio (ejemplo, el downloader):
docker logs -f music-downloader

6️⃣ Probar la ejecución manualmente
Si necesitas ejecutar las pruebas sin usar CMD en el Dockerfile, entra al contenedor:
docker exec -it music-downloader bash

Dentro del contenedor, ejecuta manualmente:
mvn test

7️⃣ Gestionar los contenedores
Detener todos los contenedores:
docker-compose down

Reiniciar contenedores:
docker-compose restart

Eliminar imágenes y contenedores viejos (limpieza):
docker system prune -af