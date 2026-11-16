package app.revanced.patches.bilibili.utils

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.MethodFingerprint

fun MethodFingerprint.resolveOrNull(context: BytecodeContext): Boolean {
    if (result != null) return true
    for (classDef in context.classes) {
        for (method in classDef.methods) {
            if (resolve(context, method, classDef)) return true
        }
    }
    return false
}
