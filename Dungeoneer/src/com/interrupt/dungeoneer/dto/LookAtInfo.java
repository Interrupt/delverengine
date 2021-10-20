package com.interrupt.dungeoneer.dto;

import com.badlogic.gdx.graphics.Color;

public class LookAtInfo {
    private String prompt;
    private String attributes;
    private Color color;

    public String getPrompt() {
        return prompt != null ? prompt : "";
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getAttributes() {
        return attributes != null ? attributes : "";
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public Color getTitleColor() {
        return color != null ? color : Color.WHITE;
    }

    public void setTitleColor(Color color) {
        this.color = color;
    }

    public void reset() {
        setPrompt(null);
        setAttributes(null);
        setTitleColor(null);
    }
}
