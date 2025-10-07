package com.welfareconnect.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class FileStorage {
    private static final String UPLOAD_DIR = System.getProperty("wc.upload.dir", "uploads");

    private FileStorage() {}

    public static String store(File source) throws IOException {
        ensureDir();
        String ext = "";
        String name = source.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) ext = name.substring(dot);
        String newName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = Path.of(UPLOAD_DIR, newName);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toAbsolutePath().toString();
    }

    private static void ensureDir() throws IOException {
        Path dir = Path.of(UPLOAD_DIR);
        if (!Files.exists(dir)) Files.createDirectories(dir);
    }
}
