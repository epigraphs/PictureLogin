package me.Lythrilla.picturelogin.config;

import java.io.File;
import me.Lythrilla.picturelogin.PictureLogin;

public class FallbackPicture {
    private final PictureLogin plugin;

    public FallbackPicture(PictureLogin plugin) {
        this.plugin = plugin;
    }

    public File get() {
        String FALLBACK_PATH = String.valueOf(this.plugin.getDataFolder()) + File.separator + "fallback.png";
        File image = new File(FALLBACK_PATH);
        if (!image.exists()) {
            this.plugin.saveResource("fallback.png", false);
        }
        image = this.plugin.getConfigManager().getBoolean("fallback", true) ? new File(FALLBACK_PATH) : null;
        return image;
    }
}
