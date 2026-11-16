package app.revanced.patches.bilibili.video.player.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.settings.patch.SettingsResourcePatch
import app.revanced.patches.bilibili.patcher.patch.MultiMethodBytecodePatch
import app.revanced.patches.bilibili.utils.exception
import app.revanced.patches.bilibili.utils.resolveOrNull
import app.revanced.patches.bilibili.video.player.fingerprints.*
import app.revanced.util.exception
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Remember playback speed",
    description = "记住播放速度变化",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ],
    dependencies = [SettingsResourcePatch::class]
)
object RememberPlaybackSpeedPatch : MultiMethodBytecodePatch(
    fingerprints = setOf(
        PlayerSettingCreateSpeedFingerprint,
        MenuFuncSegmentFingerprint,
    ),
    multiFingerprints = setOf(PlayerSpeedChooseWidgetFingerprint, PlaybackSpeedSettingFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        super.execute(context)
        MenuServiceCreateSpeedFingerprint.resolveOrNull(context)
        PlayerSpeedChooseWidgetFingerprint.result.ifEmpty {
            throw PlayerSpeedChooseWidgetFingerprint.exception
        }.forEach { r ->
            r.mutableMethod.addInstructions(
                0, """
                invoke-static {p1}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onPlaybackSpeedSelected(F)V
            """.trimIndent()
            )
        }
        PlayerSettingCreateSpeedFingerprint.result?.run {
            val lambdaStyle = mutableMethod.parameterTypes.size == 2
            val instructions = if (lambdaStyle) {
                """
                invoke-static {p1}, Ljava/lang/Float;->parseFloat(Ljava/lang/String;)F
                move-result v0
                invoke-static {v0}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onPlaybackSpeedSelected(F)V
                """.trimIndent()
            } else {
                val speedField = mutableClass.fields.first { it.type == "Ljava/lang/String;" }
                """
                iget-object v0, p0, $speedField
                invoke-static {v0}, Ljava/lang/Float;->parseFloat(Ljava/lang/String;)F
                move-result v0
                invoke-static {v0}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onPlaybackSpeedSelected(F)V
                """.trimIndent()
            }
            mutableMethod.addInstructions(0, instructions)
        } ?: run {
            if (SettingsResourcePatch.isPink)
                throw PlayerSettingCreateSpeedFingerprint.exception
        }
        MenuServiceCreateSpeedFingerprint.result?.run {
            val speedListField = mutableClass.fields.first { it.type == "Ljava/util/List;" }
            mutableMethod.addInstructions(
                0, """
                iget-object v0, p0, $speedListField
                invoke-interface {v0, p1}, Ljava/util/List;->get(I)Ljava/lang/Object;
                move-result-object v0
                check-cast v0, Ljava/lang/String;
                invoke-static {v0}, Ljava/lang/Float;->parseFloat(Ljava/lang/String;)F
                move-result v0
                invoke-static {v0}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onPlaybackSpeedSelected(F)V
            """.trimIndent()
            )
        } ?: println("[Remember playback speed] skipped menu service hook: MenuServiceCreateSpeedFingerprint not found")
        PlaybackSpeedSettingFingerprint.result.ifEmpty {
            throw PlaybackSpeedSettingFingerprint.exception
        }.forEach { r ->
            val instructions = r.mutableMethod.implementation!!.instructions
            val (index, register) = instructions.withIndex().firstNotNullOf { (index, inst) ->
                if (inst.opcode == Opcode.INVOKE_DIRECT && inst.getReference<MethodReference>().let {
                        it.returnType == "F" && it.parameterTypes == listOf("I")
                    }) {
                    index + 2 to (instructions[index + 1] as OneRegisterInstruction).registerA
                } else null
            }
            r.mutableMethod.addInstructions(
                index, """
                invoke-static {v$register}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onPlaybackSpeedSelected(F)V
            """.trimIndent()
            )
        }
        MenuFuncSegmentFingerprint.result?.mutableMethod?.addInstructions(
            0, """
            invoke-static {p1}, Lapp/revanced/bilibili/patches/PlaybackSpeedPatch;->onPlaybackSpeedSelected(F)V
        """.trimIndent()
        )
    }
}
