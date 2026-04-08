package technicfan.lanlock;

public class PlayerEntry implements Comparable<PlayerEntry> {
    public final String uuid;
    public final String name;

    public PlayerEntry(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String get(String key) {
        switch (key) {
            case "uuid": return uuid;
            case "name": return name;
            default: return null;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PlayerEntry) {
            PlayerEntry otherPlayer = (PlayerEntry) other;
            return uuid.isEmpty() ? name.equalsIgnoreCase(otherPlayer.name) : uuid.equalsIgnoreCase(otherPlayer.uuid);
        } else {
            return false;
        }
    }

    public int compareTo(PlayerEntry other) {
        return name.compareTo(other.name);
    }
}
