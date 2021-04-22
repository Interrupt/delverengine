package com.interrupt.files;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileHandle extends FileHandle {
    private final ZipFile archive;
    private ZipEntry entry;
    private final Array<String> entries;

    public ZipFileHandle(ZipFile archive, File file, String name) {
        super(file, Files.FileType.Internal);
        this.archive = archive;
        entry = archive.getEntry(name);
        if (entry == null) {
            entry = new ZipEntry(name);
        }

        entries = new Array<>();
        Enumeration<? extends ZipEntry> es = archive.entries();
        while (es.hasMoreElements()) {
            entries.add(es.nextElement().getName());
        }
    }

    @Override
    public String path() {
        return file.getPath().replace("\\", "/") + "/" + entry.getName();
    }

    @Override
    public String name() {
        String path = path();
        int index = path.lastIndexOf('/');
        if (index < 0) return path;
        return path.substring(index + 1);
    }

    @Override
    public String extension() {
        String name = name();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) return "";
        return name.substring(dotIndex + 1);
    }

    @Override
    public String nameWithoutExtension() {
        String name = name();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) return name;
        return name.substring(0, dotIndex);
    }

    @Override
    public String pathWithoutExtension() {
        String path = path();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1) return path;
        return path.substring(0, dotIndex);
    }

    @Override
    public boolean exists() {
        return entries.contains(entry.getName(), false);
    }

    @Override
    public long length() {
        return entry.getSize();
    }

    @Override
    public long lastModified() {
        return entry.getTime();
    }

    @Override
    public InputStream read() {
        try {
            return archive.getInputStream(entry);
        }
        catch (Exception ignored) {}

        return null;
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) {
        throw new GdxRuntimeException("Cannot map a ZipFileHandle");
    }

    @Override
    public OutputStream write(boolean append) {
        throw new GdxRuntimeException("Cannot write to a ZipFileHandle");
    }

    @Override
    public void write(InputStream input, boolean append) {
        throw new GdxRuntimeException("Cannot write to a ZipFileHandle");
    }

    @Override
    public Writer writer(boolean append) {
        return writer(append, null);
    }

    @Override
    public Writer writer(boolean append, String charset) {
        throw new GdxRuntimeException("Cannot write to a ZipFileHandle");
    }

    @Override
    public void writeString(String string, boolean append) {
        writeString(string, append, null);
    }

    @Override
    public void writeString(String string, boolean append, String charset) {
        throw new GdxRuntimeException("Cannot write to a ZipFileHandle");
    }

    @Override
    public void writeBytes(byte[] bytes, boolean append) {
        throw new GdxRuntimeException("Cannot write to a ZipFileHandle");
    }

    @Override
    public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
        throw new GdxRuntimeException("Cannot write to a ZipFileHandle");
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }
}
