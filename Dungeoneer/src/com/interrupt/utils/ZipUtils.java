package com.interrupt.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {
    public static void extract(FileHandle zip, FileHandle path) {
        if (!path.exists()) {
            if (!path.file().mkdirs()) {
                return;
            }
        }

        try {
            ZipFile zf = new ZipFile(zip.file());

            Enumeration<? extends ZipEntry> es = zf.entries();
            ZipEntry entry;
            FileHandle file;
            while (es.hasMoreElements()) {
                entry = es.nextElement();
                InputStream is = zf.getInputStream(entry);
                file = path.child(entry.getName());
                OutputStream os = file.write(false);
                StreamUtils.copyStream(is, os);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
