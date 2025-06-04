package technicfan.lanlock;

import java.util.ArrayList;
import java.util.Map;

public class LANLockConfig {
    private boolean enabled;
    private boolean useUuid;
    private ArrayList<Map<String, String>> whitelist;

    public LANLockConfig() {
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

    public ArrayList<Map<String, String>> whitelist() {
        return whitelist;
    }

    public void setWhitelist(ArrayList<Map<String, String>> newList) {
        whitelist = newList;
    }
}

