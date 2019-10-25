package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;

public class BlacklistFrame extends UserListFrame {

    @Override
    public String type() {
        return "Blacklist";
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
