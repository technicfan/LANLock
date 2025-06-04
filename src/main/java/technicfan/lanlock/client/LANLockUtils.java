package technicfan.lanlock.client;

import java.util.ArrayList;

class Config {
    private boolean enabled;
    private boolean useUuid;
    private ArrayList<Player> whitelist;

    public Config() {
        this.enabled = true;
        this.useUuid = true;
        this.whitelist = new ArrayList<>();
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean newValue) {
        enabled = newValue;
    }

    public boolean useUuid() {
        return useUuid;
    }

    public void setUseUuid(boolean newValue) {
        useUuid = newValue;
    }

    public ArrayList<Player> whitelist() {
        return whitelist;
    }

    public void setWhitelist(ArrayList<Player> newList) {
        whitelist = newList;
    }
}

class Player {
    private final String uuid;
    private final String name;

    public Player(String  uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String get(String field) {
        return switch (field) {
            case "uuid" -> uuid;
            case "name" -> name;
            default -> null;
        };
    }
}

