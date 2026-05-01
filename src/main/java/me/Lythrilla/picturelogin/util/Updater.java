package me.Lythrilla.picturelogin.util;

import java.util.logging.Logger;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.LanguageManager;
import me.Lythrilla.picturelogin.util.Translate;

public class Updater {
    public Updater(Logger log, String currentVersion) {
        String USER_AGENT = "PictureLogin Plugin";
        try {
            if (PictureLogin.getInstance() != null && PictureLogin.getInstance().getConfigManager() != null) {
                LanguageManager langManager = PictureLogin.getInstance().getConfigManager().getLanguageManager();
                if (langManager != null) {
                    log.info(langManager.getMessage("log_update_check_disabled").replace("%version%", currentVersion));
                } else {
                    log.info("PictureLogin " + currentVersion + " - Update check disabled");
                }
            } else {
                log.info("PictureLogin " + currentVersion + " - Update check disabled");
            }
        }
        catch (Exception e) {
            log.warning(Translate.tl("error_update_check"));
        }
    }
}
