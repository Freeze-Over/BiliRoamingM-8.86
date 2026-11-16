package app.revanced.patches.bilibili.video.player.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

/**
 * bilibili 8.86 moved the speed selection into a Kotlin lambda
 * `PlayerSettingFunctionWidget2.c1(PlayerSettingFunctionWidget2, String)`, which receives the
 * selected speed as a String parameter instead of reading a field of the enclosing widget.
 * Older versions use a no-argument method. Both shapes are accepted here and the patch body
 * picks the register that holds the speed string.
 */
object PlayerSettingCreateSpeedFingerprint : MethodFingerprint(
    strings = listOf("option", "speed", "value"),
    customFingerprint = { methodDef, _ ->
        methodDef.name != "<clinit>" && methodDef.parameterTypes.let {
            it.isEmpty() || (it.size == 2 && it[1] == "Ljava/lang/String;")
        }
    }
)
