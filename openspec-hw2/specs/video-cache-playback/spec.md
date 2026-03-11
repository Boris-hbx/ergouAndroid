## ADDED Requirements

### Requirement: 视频磁盘缓存
系统 SHALL 使用 ExoPlayer SimpleCache 将已下载的视频数据缓存到本地磁盘，缓存上限为 200MB，采用 LRU 淘汰策略。

#### Scenario: 首次播放视频
- **WHEN** 用户首次点击播放某个功法视频
- **THEN** 系统从远程服务器下载视频数据，同时写入磁盘缓存，缓冲足够后开始播放

#### Scenario: 再次播放已缓存的视频
- **WHEN** 用户播放一个之前已观看过的视频
- **THEN** 系统从本地磁盘缓存读取数据，无需网络请求，首帧显示时间 < 0.5秒

#### Scenario: 缓存空间超限
- **WHEN** 缓存写入新数据导致总大小超过 200MB
- **THEN** 系统自动按 LRU 策略清除最久未访问的缓存，使总大小回到上限以内

### Requirement: ExoPlayer 单实例复用
系统 SHALL 在养生页面生命周期内维护单个 ExoPlayer 实例，切换视频时复用该实例而非重新创建。

#### Scenario: 切换到不同视频
- **WHEN** 用户正在播放视频 A，点击播放视频 B
- **THEN** 系统停止视频 A，使用同一播放器实例加载并播放视频 B

#### Scenario: 收起视频后重新打开
- **WHEN** 用户收起当前视频，稍后再点击同一或不同视频
- **THEN** 系统复用已有播放器实例，不创建新的 ExoPlayer

#### Scenario: 离开养生页面
- **WHEN** 用户导航离开养生功法页面
- **THEN** 系统释放 ExoPlayer 实例及相关资源

### Requirement: 快速缓冲策略
系统 SHALL 配置自定义 LoadControl，降低初始缓冲阈值以加快首帧显示。

#### Scenario: 视频首次加载
- **WHEN** ExoPlayer 开始加载一个新的视频
- **THEN** 初始缓冲量 SHALL 不超过 1秒（DefaultLoadControl 默认 2.5秒），使视频更快开始播放

### Requirement: 加载状态指示
系统 SHALL 在视频缓冲期间显示加载指示器（CircularProgressIndicator）。

#### Scenario: 视频正在缓冲
- **WHEN** 用户点击播放视频且 ExoPlayer 处于缓冲状态
- **THEN** 视频区域显示居中的 CircularProgressIndicator

#### Scenario: 缓冲完成开始播放
- **WHEN** ExoPlayer 缓冲完成，视频开始播放
- **THEN** 加载指示器消失，视频画面正常显示

### Requirement: 网络不可用时的错误处理
系统 SHALL 在网络不可用且缓存未命中时向用户显示错误提示。

#### Scenario: 无网络且无缓存
- **WHEN** 用户点击播放视频，但设备无网络且该视频未被缓存
- **THEN** 系统显示 Snackbar "无法加载视频，请检查网络连接"，播放按钮恢复可点击状态
