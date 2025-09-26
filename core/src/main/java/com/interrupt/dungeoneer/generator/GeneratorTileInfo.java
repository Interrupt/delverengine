package com.interrupt.dungeoneer.generator;

class GeneratorTileInfo {
    public String zipFile = "generator.zip";
    public String filename;

    public GeneratorTileInfo(String zipFile, String filename) {
        this.zipFile = zipFile;
        this.filename = filename;
    }

    public GeneratorTileInfo(String filename) {
        this.filename = filename;
    }
}