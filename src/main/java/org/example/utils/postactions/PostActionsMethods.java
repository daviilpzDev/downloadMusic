package org.example.utils.postactions;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.example.utils.Constants;
import org.example.utils.Helper;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;


public class PostActionsMethods {

    public static final Logger logger = Constants.logger;

    public static void deleteNotNeededFiles(String smbUrl) {
        try {
            SmbFile smbFolder = new SmbFile(smbUrl);
            deleteAction(smbFolder);
            logger.info("Proceso de limpieza de SMB completado.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void deleteAction(SmbFile smbFolder) throws SmbException {
        if (!smbFolder.exists() || !smbFolder.isDirectory()) {
            logger.error("La carpeta de SMB no existe o no es un directorio.");
            return;
        }

        SmbFile[] files = smbFolder.listFiles();

        if (files == null || files.length == 0) {
            logger.error("No se encontraron archivos en la carpeta SMB.");
            return;
        }

        String[] allowedExtensions = {".mp3", ".opus", ".aac", ".m4a"};

        for (SmbFile file : files) {
            if (!file.isFile()) continue;

            String fileName = file.getName().toLowerCase();

            boolean isAllowed = false;
            for (String ext : allowedExtensions) {
                if (fileName.endsWith(ext)) {
                    isAllowed = true;
                    break;
                }
            }

            if (!isAllowed) {
                logger.info("Eliminando archivo no permitido: " + fileName);
                file.delete();
            }
        }
    }

    public static void moveFilesToSMB() {
        try {
            File folder = new File(Constants.downloadFilepath);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".mp3"));

            if (files == null || files.length == 0) {
                logger.error("No se encontraron archivos MP3.");
                return;
            }

            for (File localFile : files) {
                String smbUrl = Helper.loadSMB() + localFile.getName();

                try {
                    SmbFile smbFile = new SmbFile(smbUrl);
                    try (FileInputStream fis = new FileInputStream(localFile);
                         OutputStream os = new SmbFileOutputStream(smbFile)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) > 0) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }

                    System.out.println("Archivo movido a SMB: " + smbUrl);
                    localFile.delete();  // Eliminar el archivo local después de moverlo
                } catch (Exception e) {
                    logger.error("Error al mover el archivo: " + localFile.getName(), e);
                }
            }

        } catch (Exception e) {
            logger.error("Error en el proceso de mover archivos a SMB", e);
        }
    }


}
