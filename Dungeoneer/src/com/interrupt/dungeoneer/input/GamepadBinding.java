package com.interrupt.dungeoneer.input;

public class GamepadBinding {
    public enum GamepadInputType { BUTTON, SLIDER, POV, AXIS }
    public int number;
    public int modifier = 0;
    public GamepadInputType type = GamepadInputType.BUTTON;

    public GamepadBinding() { }

    public GamepadBinding(int number, GamepadInputType type) {
        this.number = number;
        this.type = type;
    }

    public GamepadBinding(int number, GamepadInputType type, Integer modifier) {
        this.number = number;
        this.type = type;
        this.modifier = modifier;
    }

    public String toString() {
        if(type == GamepadInputType.AXIS) {
            return type.toString() + " " + Integer.toString(number) + " " + (modifier > 0 ? "(+)" : "(-)");
        }
        else if(type == GamepadInputType.POV) {
            return type.toString() + " " + Integer.toString(number) + " (" + modifier + ")";
        }
        return type.toString() + " " + Integer.toString(number);
    }
}
