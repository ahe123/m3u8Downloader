package com.ahery.utils;

public class Ts implements Comparable<Ts> {
    private String file;
    private float seconds;

    public Ts(String file,float seconds){
        this.file=file;
        this.seconds=seconds;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public float getSeconds() {
        return seconds;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    /**
     * 获取时间
     */
    public long getLongDate() {
        try {
            return Long.parseLong(file.substring(0, file.lastIndexOf(".")));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return file + " (" + seconds + "sec)";
    }

    @Override
    public int compareTo(Ts o) {
        return file.compareTo(o.file);
    }
}
