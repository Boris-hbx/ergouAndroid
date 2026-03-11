## Context

当前 `InlineVideoPlayer`（HealthScreen.kt:692-719）每次用户点击播放都会：
1. 用 `remember(videoUrl)` 创建全新的 `ExoPlayer.Builder(context).build()`
2. 调用 `setMediaItem` + `prepare()` + `playWhenReady = true`
3. 在 `DisposableEffect` 的 `onDispose` 中 `release()`

问题：
- 每次创建/销毁 ExoPlayer 开销大（~50ms + 内存分配）
- 视频完全从网络加载，无磁盘缓存（每次重复下载 ~5-30MB 的 MP4）
- 默认 `DefaultLoadControl` 初始缓冲 2.5s，首帧显示慢
- 无加载状态反馈，用户只看到空白区域

现有依赖：`media3-exoplayer` 1.5.1 和 `media3-ui` 1.5.1，尚未引入 `media3-datasource`。

## Goals / Non-Goals

**Goals:**
- 已观看的视频再次播放时秒开（磁盘缓存命中）
- 首次播放缩短首帧等待时间（自定义 LoadControl）
- 减少内存占用（单播放器复用）
- 缓冲期间有明确的加载指示

**Non-Goals:**
- 视频预加载（用户未点击的视频不预缓冲）
- 视频下载/离线保存功能（缓存是透明的，非用户主动操作）
- 视频缩略图/封面帧提取
- 全屏播放模式

## Decisions

### 1. 缓存方案：SimpleCache + CacheDataSource

**选择**: 使用 Media3 内置的 `SimpleCache` + `CacheDataSource.Factory`

**理由**:
- Media3 原生支持，无需额外框架
- `SimpleCache` 内置 LRU 淘汰（通过 `LeastRecentlyUsedCacheEvictor`），配置 200MB 上限
- `CacheDataSource` 自动处理 cache-miss 时的网络请求与本地写入

**替代方案**:
- OkHttp 拦截器缓存：无法利用 ExoPlayer 的分段缓存能力，对大文件效率低
- 自己实现文件缓存：重复造轮子，且需要处理并发、部分下载等复杂场景

**新增依赖**: `media3-datasource`（同版本 1.5.1）

### 2. 播放器生命周期：ViewModel 级别单实例

**选择**: 在 `HealthViewModel` 中持有 ExoPlayer 实例，通过 `onCleared()` 释放

**理由**:
- ViewModel 的生命周期与用户在养生页面的停留时间匹配
- 切换视频时只需 `setMediaItem` + `prepare()`，无需重建播放器
- 配置变更（旋转屏幕）时 ViewModel 存活，播放器不会中断

**替代方案**:
- Composable 级别（当前方案）：每次切换都重建，性能差
- Application 级别单例：播放器资源不会随页面退出释放，浪费内存

### 3. 缓冲策略：自定义 DefaultLoadControl

**选择**: `DefaultLoadControl.Builder` 设置:
- `setBufferDurationsMs(minBuffer=15s, maxBuffer=30s, playbackBuffer=500ms, playbackRebuffer=1s)`
- `playbackBuffer=500ms` 使首帧仅需 0.5s 缓冲即开始播放

**理由**: 养生视频是短视频（1-5分钟），快速开始播放比长时间缓冲更重要

### 4. 加载状态：监听 ExoPlayer playbackState

**选择**: 通过 `Player.Listener` 监听 `STATE_BUFFERING` / `STATE_READY` 状态，映射到 UI State

**实现**: ViewModel 中维护 `isVideoBuffering: Boolean`，Composable 据此显示/隐藏 CircularProgressIndicator

### 5. 错误处理：监听 Player.Listener.onPlayerError

**选择**: 捕获 `PlaybackException`，检测网络错误时显示 Snackbar

## Implementation Structure

```
HealthViewModel:
  + exoPlayer: ExoPlayer (lazy init, release in onCleared)
  + videoCache: SimpleCache (companion object 单例, 200MB LRU)
  + isVideoBuffering: Boolean (UI state)
  + playVideo(url: String) — setMediaItem + prepare
  + stopVideo() — stop + clearMediaItems

HealthScreen:
  InlineVideoPlayer:
  - 不再创建 ExoPlayer，从 ViewModel 获取
  - 显示 loading overlay when isVideoBuffering
  - AndroidView 只绑定 PlayerView ↔ viewModel.exoPlayer

build.gradle.kts:
  + implementation(libs.media3.datasource)

libs.versions.toml:
  + media3-datasource = { group = "androidx.media3", name = "media3-datasource", version.ref = "media3" }
```

## Risks / Trade-offs

- **[风险] SimpleCache 线程安全**: SimpleCache 是线程安全的，但必须确保整个 app 只有一个实例 → 使用 companion object + synchronized 初始化
- **[风险] 播放器在 ViewModel 中持有 Context**: ExoPlayer 需要 Context → 使用 Application Context 避免 Activity 泄漏
- **[权衡] 缓存空间 200MB**: 对于养生视频（约30个视频，每个5-30MB）基本够用，但如果视频很大可能只缓存部分 → 200MB 是合理的折中
- **[权衡] 快速缓冲 vs 流畅播放**: 500ms 起播可能在慢网络下造成频繁 rebuffer → 但 rebuffer 阈值设为 1s，应可缓解
