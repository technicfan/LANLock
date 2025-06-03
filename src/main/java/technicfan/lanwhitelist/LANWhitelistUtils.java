package technicfan.lanwhitelist;

import java.util.ArrayList;

class Config {
    private boolean enabled;
    private boolean useUuid;
    private Whitelist whitelist;

    public Config() {
        this.enabled = true;
        this.useUuid = true;
        this.whitelist = new Whitelist();
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

    public Whitelist whitelist() {
        return whitelist;
    }

    public void setWhitelist(Whitelist newList) {
        whitelist = newList;
    }
}

class Whitelist extends ArrayList<Player> {
    public boolean contains(String value) {
        String keyQuery = value.contains("-") ? "uuid" : "name";
        for (Player player : this){
            if (player.get(keyQuery).equals(value)) {
                return true;
            }
        }
        return false;
    }

    public String getCounterPart(String value) {
        String keyQuery, keyResult;
        if (value.contains("-")) {
            keyQuery = "uuid";
            keyResult = "name";
        } else {
            keyQuery = "name";
            keyResult = "uuid";
        }
        for (Player player : this){
            if (player.get(keyQuery).equals(value)) {
                return player.get(keyResult);
            }
        }
        return null;
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

