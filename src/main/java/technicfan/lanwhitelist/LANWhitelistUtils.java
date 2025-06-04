package technicfan.lanwhitelist;

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
    private final String name;
    private final String uuid;

    public Player(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String get(String field) {
        return switch (field) {
            case "name" -> name;
            case "uuid" -> uuid;
            default -> null;
        };
    }
}

