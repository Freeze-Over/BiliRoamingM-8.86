package app.revanced.patches.bilibili.video.player.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.findMutableMethodOf
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction22c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Force hardware codec",
    description = "\u5f3a\u5236\u786c\u4ef6\u89e3\u7801",
    compatiblePackages = [
        CompatiblePackage(name = "tv.danmaku.bili"),
        CompatiblePackage(name = "tv.danmaku.bilibilihd"),
        CompatiblePackage(name = "com.bilibili.app.in")
    ]
)
object HwCodecPatch : BytecodePatch() {
    private const val ENABLE_HW_CODEC_FIELD =
        "Ltv/danmaku/ijk/media/player/IjkMediaConfigParams;->mEnableHwCodec:Z"

    private fun isSetOptionBundle(inst: Instruction): Boolean {
        if (inst.opcode != Opcode.INVOKE_INTERFACE) return false
        val mr = (inst as? ReferenceInstruction)?.reference as? MethodReference ?: return false
        return mr.returnType == "V" && mr.parameterTypes == listOf("I", "Landroid/os/Bundle;")
    }

    private fun isHwCodecAccess(inst: Instruction): Boolean {
        if (inst.opcode != Opcode.IGET_BOOLEAN && inst.opcode != Opcode.IPUT_BOOLEAN) return false
        return (inst as? Instruction22c)?.reference?.toString() == ENABLE_HW_CODEC_FIELD
    }

    private fun isTargetInstruction(inst: Instruction) = isSetOptionBundle(inst) || isHwCodecAccess(inst)

    override fun execute(context: BytecodeContext) {
        var patchedMethods = 0
        var patchedInstructions = 0
        for (classDef in context.classes) {
            val candidates: List<Method> = classDef.methods.filter { method ->
                method.implementation?.instructions?.any(::isTargetInstruction) == true
            }
            if (candidates.isEmpty()) continue
            val mutableClass = context.proxy(classDef).mutableClass
            for (method in candidates) {
                val mutableMethod = mutableClass.findMutableMethodOf(method)
                val instructions = mutableMethod.getInstructions()
                val positions = mutableListOf<Int>()
                val snippets = mutableListOf<String>()
                for (index in instructions.indices) {
                    val inst = instructions[index]
                    if (isSetOptionBundle(inst)) {
                        val register = (inst as? FiveRegisterInstruction)?.registerE ?: continue
                        positions.add(index)
                        snippets.add(
                            "invoke-static {v" + register +
                                "}, Lapp/revanced/bilibili/patches/HwCodecPatch;->printOptionBundle(Landroid/os/Bundle;)V"
                        )
                        continue
                    }
                    if (!isHwCodecAccess(inst)) continue
                    val register = (inst as Instruction22c).registerA
                    val snippet = "invoke-static {v" + register +
                        "}, Lapp/revanced/bilibili/patches/HwCodecPatch;->enableHwCodec(Z)Z\n" +
                        "move-result v" + register
                    if (inst.opcode == Opcode.IGET_BOOLEAN) {
                        // option read: rewrite the loaded value
                        positions.add(index + 1)
                        snippets.add(snippet)
                    } else {
                        // option stored: force the value before the store
                        positions.add(index)
                        snippets.add(snippet)
                    }
                }
                if (positions.isEmpty()) continue
                for (i in positions.indices.reversed()) {
                    mutableMethod.addInstructions(positions[i], snippets[i])
                }
                patchedInstructions += positions.size
                patchedMethods++
            }
        }
        println("[Force hardware codec] patched $patchedInstructions instructions in $patchedMethods methods")
        if (patchedInstructions == 0) {
            println("[Force hardware codec] skipped: no mEnableHwCodec access found")
        }
    }
}
