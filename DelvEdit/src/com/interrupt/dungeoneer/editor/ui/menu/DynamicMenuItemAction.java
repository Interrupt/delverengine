package com.interrupt.dungeoneer.editor.ui.menu;

public interface DynamicMenuItemAction {
    public boolean isDirty();
    public void initMenuItem(MenuItem item);
    public void updateMenuItem(MenuItem item);
}
