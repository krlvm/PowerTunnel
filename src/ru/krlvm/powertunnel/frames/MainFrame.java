package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;

public abstract class MainFrame extends ControlFrame {

    public MainFrame() {
        setTitle(PowerTunnel.NAME);
    }

    public abstract void update();
}
