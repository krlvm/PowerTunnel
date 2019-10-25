package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;

public class WhitelistFrame extends UserListFrame {

    @Override
    public String type() {
        return "Whitelist";
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
