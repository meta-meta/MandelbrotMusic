package com.generalprocessingunit.processing;

public interface BaseFunctionality {
    public void log(String label, Object msg);
    public void log(Object msg);
    public void playNote(int cursor, int note, boolean rest);
    public void mute();
    public void unmute();
}
