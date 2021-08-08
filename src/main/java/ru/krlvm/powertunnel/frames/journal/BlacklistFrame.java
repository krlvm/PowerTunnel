package ru.krlvm.powertunnel.frames.journal;

import ru.krlvm.powertunnel.PowerTunnel;

public class BlacklistFrame extends UserListFrame {

    public BlacklistFrame() {
        super("Blacklist");
    }

    @Override
    protected void userActed(String address) {
        PowerTunnel.removeFromUserBlacklist(address);
    }

    @Override
    protected String[] getElements() {
        return PowerTunnel.getUserBlacklist().toArray(new String[0]);
    }
}
