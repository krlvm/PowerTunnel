package ru.krlvm.powertunnel.frames;

public enum ServerStatus {

    NOT_RUNNING("not running"),
    STARTING("is starting"),
    RUNNING("is running"),
    STOPPING("is stopping");

    String display;

    ServerStatus(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}
