package com.interrupt.dungeoneer.game;

public class Message {
    private float timer = 0f;
    private String message = "";

    public void tick(float delta) {
        if (timer > 0f) {
            timer -= delta;
        }

        if (timer <= 0f && !message.isEmpty()) {
            message = "";
        }
    }

    public void add(String message, float seconds) {
        timer = seconds * 60f;
        this.message = message;
    }

    public void clear() {
        message = "";
        timer = 0f;
    }

    public boolean hasActiveMessage() {
        return !message.isEmpty() && timer > 0f;
    }

    public String getMessage() {
        return message;
    }
}
