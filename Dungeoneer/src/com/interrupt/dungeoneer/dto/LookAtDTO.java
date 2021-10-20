package com.interrupt.dungeoneer.dto;

import com.badlogic.gdx.graphics.Color;

public class LookAtDTO {
    private String title;
    private String attributes;
    private Color color;

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
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
        setTitle(null);
        setAttributes(null);
        setTitleColor(null);
    }
}
