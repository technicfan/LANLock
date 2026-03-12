package technicfan.lanlock.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

public class LANLockModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return parent -> {
            try {
                return LANLockConfigScreen.getScreen(parent);
            } catch (NoClassDefFoundError e) {
                // this will still show a configure button in modmenu
                // that will do nothing (but ig it's ok)
                return null;
            }
        };
    }
}
