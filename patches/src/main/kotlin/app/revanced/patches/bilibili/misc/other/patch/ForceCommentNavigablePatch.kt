package app.revanced.patches.bilibili.misc.other.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bilibili.misc.other.fingerprints.CommentConfigFingerprint
import app.revanced.patches.bilibili.utils.classDescriptor
import app.revanced.patches.bilibili.utils.isAbstract
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22x
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Force comment time navigable",
    description = "允许分P及交互视频评论时间点可点击",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object ForceCommentNavigablePatch : BytecodePatch(fingerprints = setOf(CommentConfigFingerprint)) {
    override fun execute(context: BytecodeContext) {
        val primaryCommentMainFragmentType =
            "com.bilibili.app.comm.comment2.comments.view.PrimaryCommentMainFragment".classDescriptor
        val patchType = "app.revanced.bilibili.patches.ForceCommentNavigablePatch".classDescriptor
        val baseFragmentType = "com.bilibili.lib.ui.BaseFragment".classDescriptor
        context.findClass(primaryCommentMainFragmentType)?.mutableClass?.methods?.first {
            it.name == "onCreate" && it.parameterTypes == listOf("Landroid/os/Bundle;")
        }?.addInstructions(
            0, """
            invoke-static {p0}, $patchType->onPrimaryCommentMainFragmentCreate($baseFragmentType)V
        """.trimIndent()
        ) ?: throw PatchException("not found PrimaryCommentMainFragment")
        CommentConfigFingerprint.result?.run {
            val instructions = mutableMethod.getInstructions()
            val seekEnabledIndex = scanResult.stringsScanResult!!.matches
                .firstOrNull { it.string.contains("seekEnabled=") }?.index ?: -1
            if (seekEnabledIndex < 0) {
                println("[Force comment time navigable] skipped: no seekEnabled string")
                return
            }
            val appendIndex = instructions.indices.firstOrNull { i ->
                i > seekEnabledIndex && instructions[i].let { inst ->
                    inst.opcode == Opcode.INVOKE_VIRTUAL &&
                        inst.getReference<MethodReference>()?.parameterTypes == listOf("Z")
                }
            } ?: run {
                println("[Force comment time navigable] skipped: no seekEnabled append")
                return
            }
            val appendRegister = (instructions[appendIndex] as Instruction35c).registerD
            fun seekEnabledFieldValue(reg: Int): FieldReference? {
                for (i in (appendIndex - 1) downTo 0) {
                    val inst = instructions[i]
                    if (inst.opcode == Opcode.IGET_BOOLEAN && (inst as Instruction22c).registerA == reg)
                        return inst.getReference<FieldReference>()
                    if (inst.opcode == Opcode.MOVE_FROM16 && (inst as BuilderInstruction22x).registerA == reg) {
                        val src = inst.registerB
                        for (j in (i - 1) downTo 0) {
                            val inner = instructions[j]
                            if (inner.opcode == Opcode.IGET_BOOLEAN && (inner as Instruction22c).registerA == src)
                                return inner.getReference<FieldReference>()
                        }
                    }
                }
                return null
            }
            val seekEnabledField = seekEnabledFieldValue(appendRegister) ?: run {
                println("[Force comment time navigable] skipped: no seekEnabled field")
                return
            }
            val getter = mutableClass.methods.firstOrNull { m ->
                m.parameterTypes.isEmpty() && m.returnType == "Z" &&
                    m.implementation?.instructions?.firstOrNull()?.let { inst ->
                        inst.opcode == Opcode.IGET_BOOLEAN && inst.getReference<FieldReference>() == seekEnabledField
                    } == true
            } ?: run {
                println("[Force comment time navigable] skipped: no seekEnabled getter")
                return
            }
            getter.addInstructionsWithLabels(
                0, """
                invoke-static {}, $patchType->enabled()Z
                move-result v0
                if-eqz v0, :jump
                const/4 v0, 0x1
                return v0
                :jump
                nop
            """.trimIndent()
            )
        }
    }
}
