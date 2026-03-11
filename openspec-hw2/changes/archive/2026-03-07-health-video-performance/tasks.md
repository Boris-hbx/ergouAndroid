## 1. 依赖配置

- [x] 1.1 在 `libs.versions.toml` 添加 `media3-datasource` 依赖声明
- [x] 1.2 在 `app/build.gradle.kts` 添加 `implementation(libs.media3.datasource)`

## 2. 视频缓存初始化

- [x] 2.1 在 `HealthViewModel` companion object 中创建 `SimpleCache` 单例（200MB LRU，缓存目录 `cacheDir/video_cache`）
- [x] 2.2 创建 `CacheDataSource.Factory` 包装 `DefaultHttpDataSource.Factory`，接入 SimpleCache

## 3. ExoPlayer 单实例复用

- [x] 3.1 在 `HealthViewModel` 中用 Application Context 创建 ExoPlayer（lazy init），配置 CacheDataSource 和自定义 LoadControl
- [x] 3.2 配置 `DefaultLoadControl`：minBuffer=15s, maxBuffer=30s, playbackBuffer=500ms, rebuffer=1s
- [x] 3.3 在 `HealthViewModel.onCleared()` 中释放 ExoPlayer
- [x] 3.4 实现 `playVideo(url)` 方法：停止当前播放 → setMediaItem → prepare → playWhenReady=true
- [x] 3.5 实现 `stopVideo()` 方法：stop + clearMediaItems
- [x] 3.6 在 Koin 模块中确保 HealthViewModel 能接收 Application Context

## 4. 加载状态与错误处理

- [x] 4.1 添加 `Player.Listener` 监听 `onPlaybackStateChanged`，更新 `isVideoBuffering` 状态到 UiState
- [x] 4.2 添加 `Player.Listener.onPlayerError` 监听，网络错误时发送 Snackbar 消息"无法加载视频，请检查网络连接"，重置播放状态

## 5. UI 层改造

- [x] 5.1 重写 `InlineVideoPlayer` composable：不再创建 ExoPlayer，从 ViewModel 获取 exoPlayer 实例
- [x] 5.2 在视频区域添加 loading overlay：`isVideoBuffering` 为 true 时显示居中 CircularProgressIndicator
- [x] 5.3 更新 `toggleVideo()` 逻辑：调用 viewModel.playVideo/stopVideo 而非切换 playingVideoItemId
- [x] 5.4 确保收起视频卡片时调用 stopVideo

## 6. 手动测试

- [ ] 6.1 首次播放视频：确认加载指示器显示，视频播放后消失
- [ ] 6.2 收起后再次播放同一视频：确认秒开（缓存命中）
- [ ] 6.3 切换不同视频：确认上一个视频停止，新视频正常播放
- [ ] 6.4 退出养生页面再进入：确认无内存泄漏（Logcat 无 ExoPlayer 相关警告）
- [ ] 6.5 飞行模式下播放未缓存视频：确认错误提示显示
- [ ] 6.6 飞行模式下播放已缓存视频：确认正常播放
