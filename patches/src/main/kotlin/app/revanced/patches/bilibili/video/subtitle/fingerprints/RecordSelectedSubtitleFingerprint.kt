package app.revanced.patches.bilibili.video.subtitle.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object RecordSelectedSubtitleFingerprint : MethodFingerprint(
    returnType = "V",
    customFingerprint = { methodDef, classDef ->
        classDef.type == "Ltv/danmaku/biliplayerv2/service/interact/biz/InteractLayerService;" &&
            methodDef.name == "recordSelectedSubtitle"
    },
)
