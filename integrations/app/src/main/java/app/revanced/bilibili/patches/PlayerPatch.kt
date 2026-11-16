package app.revanced.bilibili.patches

import androidx.annotation.Keep
import app.revanced.bilibili.utils.blkvPrefs

object PlayerPatch {
    @Keep
    @JvmStatic
    fun onOnlineInfoChanged(info: Any?): Boolean {
        return !blkvPrefs.getBoolean("pref_player_full_screen_online_num_key", true)
    }
}
