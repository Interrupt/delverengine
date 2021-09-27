package com.interrupt.dungeoneer.dto;

import com.badlogic.gdx.graphics.Color;

public class LookAtDTO {
    private String title;
    private String attributes;
    private Color color;

    public LookAtDTO(String title, String attributes, Color color) {
        this.title = title;
        this.attributes = attributes;
        this.color = color;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAttributes() {
        return this.attributes;
    }

    public Color getTitleColor() {
        return this.color;
    }
}
