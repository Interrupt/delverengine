package com.interrupt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Logger {
    public static void logExceptionToFile(Exception ex) {
        logExceptionToFile("DelverGameManager", "Fatal error!", ex);
    }

    public static void logExceptionToFile(String tag, String message, Throwable ex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ex.printStackTrace(ps);
        ps.close();

        FileHandle f = Game.getFile("errorlog.txt");
        f.writeString("Fatal error in Game loop!\n\n", true);
        f.writeString(baos.toString(), true);
        f.writeString("\n", true);

        Gdx.app.log(tag, message, ex);
    }
}
