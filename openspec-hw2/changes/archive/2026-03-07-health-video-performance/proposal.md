## Why

养生功法页面的视频播放加载很慢。当前实现每次点击"观看演示视频"都会创建新的 ExoPlayer 并从远程服务器重新下载 MP4，没有任何磁盘缓存或播放器复用，导致用户每次都要等待完整的网络加载。

## What Changes

- 引入 ExoPlayer 磁盘缓存（SimpleCache），已观看的视频不再重复下载
- 复用单个 ExoPlayer 实例，切换视频时只替换 MediaItem 而非重建播放器
- 配置自定义 LoadControl，降低初始缓冲阈值以加快首帧显示
- 添加加载状态指示器（缓冲中显示 loading spinner）

## Capabilities

### New Capabilities
- `video-cache-playback`: 视频缓存与播放优化，包括磁盘缓存、播放器复用、缓冲策略配置、加载状态 UI

### Modified Capabilities

（无现有 spec 需修改）

## Impact

- **代码**: `HealthScreen.kt` 的 `InlineVideoPlayer` composable 重写；新增 ExoPlayer 缓存配置
- **依赖**: 已有 `media3-exoplayer` 和 `media3-ui`，可能需要额外引入 `media3-datasource` 用于缓存
- **存储**: 新增磁盘缓存目录（视频缓存），需考虑缓存大小上限（建议 200MB）
- **内存**: 单播放器复用模式减少内存占用（原来每个视频一个 ExoPlayer）
