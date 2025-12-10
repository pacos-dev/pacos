package org.pacos.base.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * This class is responsible for saving uploaded file from the UI
 */
public class StoreFileFromUpload {

    private StoreFileFromUpload() {

    }

    public static void save(File destinationFile, InputStream fileData) {
        try (
                InputStream inputStream = fileData;
                FileOutputStream outStream = new FileOutputStream(destinationFile)
        ) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new FileUploadException("Error while saving uploaded file to location: " + destinationFile, e);
        }
    }

}
