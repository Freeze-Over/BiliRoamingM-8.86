package app.revanced.patches.bilibili.misc.other.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

/**
 * The "Builder(targetUri=" log string of older versions no longer exists in bilibili 8.86,
 * so match the builder constructor itself. The class name has been stable for years.
 */
object BLRouteBuilderFingerprint : MethodFingerprint(
    returnType = "V",
    customFingerprint = { methodDef, classDef ->
        classDef.type == "Lcom/bilibili/lib/blrouter/RouteRequest\$Builder;" &&
                methodDef.name == "<init>" &&
                methodDef.parameterTypes.let { it.size == 1 && it[0] == "Landroid/net/Uri;" }
    }
)
