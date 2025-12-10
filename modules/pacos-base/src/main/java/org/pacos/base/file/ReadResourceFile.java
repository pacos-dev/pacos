package org.pacos.base.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to load file from resource directory
 */
public class ReadResourceFile {

    private static final Logger LOG = LoggerFactory.getLogger(ReadResourceFile.class);

    private ReadResourceFile() {
    }

    /**
     * Reads file from resource path and return content as list of string
     */
    public static List<String> readFile(String path) {
        ClassLoader classLoader = ReadResourceFile.class.getClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream(path);
        List<String> content = new ArrayList<>();
        try {
            if(inputStream==null){
                throw new FileNotFoundException("Can't find resource file: "+path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.add(line);
                }
            }
        } catch (IOException e) {
            LOG.error("Error while reading resource file from '{}'", path, e);
        }
        return content;
    }
}
