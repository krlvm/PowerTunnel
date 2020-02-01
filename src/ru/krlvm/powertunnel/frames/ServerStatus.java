package ru.krlvm.powertunnel.frames;

public enum ServerStatus {

    NOT_RUNNING("n't running"),
    STARTING(" starting"),
    RUNNING(" running"),
    STOPPING(" stopping");

    String display;

    ServerStatus(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}
