package ru.krlvm.powertunnel.enums;

public enum SNITrick {

    ERASE,
    SPOIL;

    public static SNITrick fromID(int id) {
        switch (id) {
            case 1: {
                return ERASE;
            }
            case 2: {
                return SPOIL;
            }
            default: {
                return null;
            }
        }
    }
}