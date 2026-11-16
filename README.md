# 哔哩漫游M（8.86 适配版）

> [!IMPORTANT]
>
> 本仓库基于 [sakarie9/BiliRoamingM](https://github.com/sakarie9/BiliRoamingM) v1.23.3.8，
> 额外适配 **哔哩哔哩 8.86.0（versionCode 8860400）**：该版本里 12 个补丁的锚点失效，
> 这里逐个重新定位后 **79 个补丁全部可以打上**（实测 0 失败）。

## 8.86 上改了什么

| 补丁 | 8.86 的变化与处理 |
|---|---|
| Video default quality | 手指纹改匹配 `"quality settings:"`（`"get free data failed"` 已删除） |
| Cache redirect | `OnOgvDownloadFingerprint` 改为可选，由 Theseus 指纹覆盖 `OgvDownloadService.j(Context)` |
| BL route intercept | 按类名定位 `RouteRequest$Builder`，`RouteRequest` 构造器接受 1 个 `Uri` 参数 |
| Remember playback speed | 兼容 lambda 形态 `(widget, String)`，取 `p1` 直接 `Float.parseFloat` |
| Force hardware codec | 放弃单指纹，全量扫描 `mEnableHwCodec` 的读写点；类型匹配改用 format 接口 |
| Force comment time navigable | 改用寄存器追踪找 `seekEnabled`（8.86 在 `toString()` 开头一次性读完所有字段） |
| Subtitle import/save | `recordSelectedSubtitle` 变成 `(ZZ)V` |
| Auto like / Live room auto float / Moss / Pegasus banner | 8.86 目标不存在时优雅跳过，不再中断整个补丁流程 |
| Bili layout adjust | `string/theseus_playlist_default_order` 已删，改用字面量 |
| Remove vip section | `id/mine_vip_layout` 改名为 `mine_vip_layout_refactoring` |
| JSON / Pegasus / Player | `Dm`/`DmAdvert`/`FeedAdInfo` 迁到 `com.bilibili.adcommon.data.model`；`onOnlineInfoChanged` 改 `Object` 参数 |
| 构建 | 私有 GitHub Packages 的 patcher 取不到时，回退到 `revanced-cli` 内嵌的 `19.3.1.1` |

## 构建

```bash
# 依赖：JDK 17、Android SDK（build-tools 36 / platform 35）、NDK 26.3
./gradlew distDev
# 产物：build/BiliRoamingM-<version>.apk  与  build/BiliRoamingM-<version>.jar
```

打补丁（以 8.86 官方包为例）：

```bash
java -jar revanced-cli.jar patch   --merge integrations.apk   --patch-bundle patches.jar   --signing-levels 1,2,3   bilibili_8.86.0.apk
```

> 若 `:patches` 报 `Could not resolve kofua.app.revanced:revanced-patcher`，
> 说明上游私有仓库不可访问；本仓库的 `patches/build.gradle.kts` 已支持
> 通过 `../libs/patcher-api.jar`（从 revanced-cli 抽出）离线编译。

---


> [!IMPORTANT]
>
> 基于 [BiliRoamingX](https://github.com/BiliRoamingX/BiliRoamingX)，去除番剧解锁、大会员画质试用等功能
>
> 此 Fork 针对 Play 版本测试，仅进行维护性更新。

> [!NOTE]
>
> **不提供** 预构建包
>
> **请勿在公开场合分享**，随时跑路

> [!IMPORTANT]
>
> ```plain
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
> FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
> IN THE SOFTWARE.
> ```

基于 ReVanced 实现的B站 Android 客户端增强模块。模块设置完美融入 APP 设置，功能丰富，自定义程度高。
得益于实现方式，对 APP 性能几乎没有影响，流畅、迅速、启动快。支持粉版、Play 版及 HD 版。

## 📖 主要功能

- 自由移除页面组件
- 自定义直播、视频默认清晰度
- 自定义播放速度
- 字幕样式调整，翻译、保存及导入
- 调整 APP 显示大小
- 自由复制评论及视频信息
- 双指缩放视频填充屏幕
- 调用外部下载器保存视频
- 加回频道功能
- 自动领取B币券
- 分享链接净化
- 推荐、热门、动态过滤
- 开屏页背景色跟随深色模式

## 💻 源码构建

```shell
git clone --recurse-submodules https://github.com/sakarie9/BiliRoamingM.git
cd BiliRoamingM
./gradlew dist
```

- Windows 系统上使用 `gradlew.bat` 命令而不是 `./gradlew`
- 构建产物在 `build` 目录下

## ⬇️ 下载使用

- 参照 [revanced-cli](https://github.com/ReVanced/revanced-cli/tree/main/docs) 文档打包
  1. 下载定制版 [revanced-cli.jar](https://github.com/zjns/revanced-cli/releases/latest)
  2. 从 [releases](https://github.com/sakarie9/BiliRoamingM/releases/latest) 下载 `integrations.apk` 和 `patches.jar`
  3. 执行终端命令 `java -jar revanced-cli.jar patch --merge integrations.apk --patch-bundle patches.jar --signing-levels 1,2,3 bilibili.apk`

> [!NOTE]
>
> 若获取到的 Bilibili 为 apkm, xapk 或 apks 等格式，需要使用 [APKEditor](https://github.com/REAndroid/APKEditor) 等工具将其合并为 apk 再执行上述打包命令。

## 📃 Licence

[![GitHub](https://img.shields.io/github/license/sakarie9/BiliRoamingM?style=for-the-badge)](https://github.com/sakarie9/BiliRoamingM/blob/main/LICENSE)
