package com.interrupt.dungeoneer.dto;

import com.badlogic.gdx.graphics.Color;

public class LookAtInfo {
    private String prompt;
    private String additionalInfo;
    private Color color;

    public String getPrompt() {
        return prompt != null ? prompt : "";
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getAdditionalInfo() {
        return additionalInfo != null ? additionalInfo : "";
    }

    public void setAdditionalInfo(String attributes) {
        this.additionalInfo = attributes;
    }

    public Color getColor() {
        return color != null ? color : Color.WHITE;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void reset() {
        setPrompt(null);
        setAdditionalInfo(null);
        setColor(null);
    }
}
