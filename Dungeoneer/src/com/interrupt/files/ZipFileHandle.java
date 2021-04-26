package com.interrupt.files;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public Files.FileType type() {
        return Files.FileType.Internal;
    }

    @Override
    public File file() {
        throw new GdxRuntimeException("Cannot get a FileHandle for a ZipFileHandle object");
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
    public ByteBuffer map() {
        throw new GdxRuntimeException("Error ZipFileHandle objects do not support memory mapping");
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode) {
        throw new GdxRuntimeException("Error ZipFileHandle objects do not support memory mapping");
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
    public FileHandle[] list() {
        Path basePath = Paths.get(entry.getName());

        Array<String> children = new Array<>();
        for (String entry : entries) {
            Path entryPath = Paths.get(entry);

            if (entryPath.startsWith(basePath) && !entryPath.equals(basePath)) {
                Path relativePath = basePath.relativize(entryPath);
                Path firstPart = relativePath.subpath(0, 1);
                String child = basePath.resolve(firstPart).toString();

                if (child.length() < entry.length()) {
                    int i = child.length();
                    if (entry.charAt(i) == '/') child += "/";
                }

                if (!children.contains(child, false)) {
                    children.add(child);
                }
            }
        }

        FileHandle[] handles = new FileHandle[children.size];
        for (int i = 0; i < children.size; i++) {
            handles[i] = new ZipFileHandle(archive, file, children.get(i));
        }

        return handles;
    }

    @Override
    public FileHandle[] list(FileFilter filter) {
        throw new GdxRuntimeException("Unsupported method");
    }

    @Override
    public FileHandle[] list(FilenameFilter filter) {
        throw new GdxRuntimeException("Unsupported method");
    }

    @Override
    public FileHandle[] list(String suffix) {
        throw new GdxRuntimeException("Unsupported method");
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    @Override
    public FileHandle child(String name) {
        return super.child(name);
    }

    @Override
    public FileHandle sibling(String name) {
        return super.sibling(name);
    }

    @Override
    public FileHandle parent() {
        Path parent = Paths.get(entry.getName()).getParent();
        return new ZipFileHandle(archive, file, parent.toString() + "/");
    }

    @Override
    public void mkdirs() {
        throw new GdxRuntimeException("Unsupported method");
    }

    @Override
    public boolean exists() {
        return entries.contains(entry.getName(), false);
    }

    @Override
    public boolean delete() {
        throw new GdxRuntimeException("Unsupported method");
    }

    @Override
    public boolean deleteDirectory() {
        throw new GdxRuntimeException("Unsupported method");
    }

    @Override
    public void emptyDirectory() {
        throw new GdxRuntimeException("Unsupported method");
    }

    @Override
    public void emptyDirectory(boolean preserveTree) {
        throw new GdxRuntimeException("Unsupported method");
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
    public String toString() {
        return (file.getPath() + "/" + entry.getName()).replace('\\', '/');
    }

    /** Returns a ZipFileHandle for an asset inside a zip file. */
    static public FileHandle get(String name) {
        FileHandle handle = Gdx.files.internal(name);

        if (!name.contains(".zip")) return handle;

        int i = name.indexOf(".zip") + ".zip".length();
        String zipFilePath = name.substring(0, i);
        String zipEntryName = name.substring(i);
        if (zipEntryName.startsWith("/"))
            zipEntryName = zipEntryName.substring(1);

        try {
            FileHandle f = Gdx.files.internal(zipFilePath);
            ZipFile z = new ZipFile(f.file());
            handle = new ZipFileHandle(z, f.file(), zipEntryName);
        }
        catch (Exception ignored) {
            Gdx.app.debug("Delver", "Unable to open zip: " + zipFilePath);
        }

        return handle;
    }
}
