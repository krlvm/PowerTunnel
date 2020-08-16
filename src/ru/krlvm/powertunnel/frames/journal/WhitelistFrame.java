package ru.krlvm.powertunnel.frames.journal;

import ru.krlvm.powertunnel.PowerTunnel;

public class WhitelistFrame extends UserListFrame {

    public WhitelistFrame() {
        super("Whitelist");
    }

    @Override
    protected void userActed(String address) {
        PowerTunnel.removeFromUserWhitelist(address);
    }

    @Override
    protected String[] getElements() {
        return PowerTunnel.getUserWhitelist().toArray(new String[0]);
    }
}
