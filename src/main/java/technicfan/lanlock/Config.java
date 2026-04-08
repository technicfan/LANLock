package technicfan.lanlock;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private final boolean enabled;
    private final boolean useUuid;
    private final boolean sendNotification;
    private final List<PlayerEntry> whitelist;

    protected Config() {
        this.enabled = true;
        this.useUuid = true;
        this.sendNotification = true;
        this.whitelist = new ArrayList<>();
    }

    protected Config(
        boolean enabled,
        boolean useUuid,
        boolean sendNotification,
        List<PlayerEntry> whitelist
    ) {
        this.enabled = enabled;
        this.useUuid = useUuid;
        this.sendNotification = sendNotification;
        this.whitelist = List.copyOf(whitelist);
    }

    protected boolean enabled() {
        return enabled;
    }

    protected Config setEnabled(boolean newValue) {
        return new Config(newValue, useUuid, sendNotification, List.copyOf(whitelist));
    }

    protected boolean useUuid() {
        return useUuid;
    }

    protected Config setUseUuid(boolean newValue) {
        return new Config(enabled, newValue, sendNotification, List.copyOf(whitelist));
    }

    protected boolean sendNotification() {
        return sendNotification;
    }

    protected Config setSendNotification(boolean newValue) {
        return new Config(enabled, useUuid, newValue, List.copyOf(whitelist));
    }

    protected List<PlayerEntry> whitelist() {
        return List.copyOf(whitelist);
    }

    protected boolean whitelistContains(String id) {
		return whitelistPlayer(id) != null;
    }

    protected PlayerEntry whitelistPlayer(String id) {
		String keyQuery = id.contains("-") ? "uuid" : "name";
		for (PlayerEntry player : whitelist){
			if (player.get(keyQuery).equalsIgnoreCase(id)) {
				return player;
			}
		}
        return null;
    }
    
    protected Config addToWhitelist(PlayerEntry player) {
        if (whitelist.contains(player)) {
            return this;
        } else {
            ArrayList<PlayerEntry> temp = new ArrayList<>(whitelist);
            temp.add(player);
            return new Config(enabled, useUuid, sendNotification, temp.stream().sorted().toList());
        }
    }

    protected Config removeFromWhitelist(PlayerEntry player) {
        if (whitelist.contains(player)) {
            ArrayList<PlayerEntry> temp = new ArrayList<>(whitelist);
            temp.remove(player);
            return new Config(enabled, useUuid, sendNotification, temp);
        } else {
            return this;
        }
    }

    protected List<String> whitelistNames(boolean checkUuid) {
        if (checkUuid) {
            if (useUuid) {
                return whitelist.stream().filter(p -> !p.uuid.isEmpty()).map(p -> p.name).toList();
            } else {
                return whitelist.stream().map(p -> p.name).toList();
            }
        } else {
            return whitelist.stream().map(p -> p.name).map(String::toLowerCase).toList();
        }
    }

    protected List<String> whitelistOfflineNames() {
        return whitelist.stream().filter(p -> p.uuid.isEmpty()).map(p -> p.name.toLowerCase()).toList();
    }
}

