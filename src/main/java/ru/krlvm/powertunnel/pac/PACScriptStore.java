package ru.krlvm.powertunnel.pac;

import ru.krlvm.powertunnel.data.DataStore;

public class PACScriptStore extends DataStore {

    public PACScriptStore() {
        super("powertunnel");
    }

    @Override
    public String getFileExtension() {
        return "pac";
    }
}
