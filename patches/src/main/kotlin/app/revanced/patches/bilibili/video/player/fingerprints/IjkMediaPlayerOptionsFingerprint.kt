package app.revanced.patches.bilibili.video.player.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c

object IjkMediaPlayerOptionsFingerprint : MethodFingerprint(
    returnType = "Ltv/danmaku/ijk/media/player/IjkMediaConfigParams;",
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any { inst ->
            inst.opcode == Opcode.IGET_BOOLEAN && (inst as? Instruction22c)?.reference?.toString()
                ?.contains("IjkMediaConfigParams;->mEnableHwCodec:Z") == true
        } == true
    },
)
