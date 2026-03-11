## Use Cases

### Use Case: 观看养生功法演示视频

**Primary Actor:** 用户
**Scope:** 二狗 Android App — 养生功法模块
**Level:** User goal

**Stakeholders and Interests:**
- 用户 — 快速流畅地观看功法演示视频，不想每次都等漫长的加载

**Preconditions:**
- 用户已进入养生功法页面
- 设备有网络连接（首次观看时）

**Success Guarantee (Postconditions):**
- 视频流畅播放，首帧显示时间 < 2秒（缓存命中时 < 0.5秒）
- 已观看的视频缓存到磁盘，下次观看无需重新下载
- 同一时间只有一个播放器实例运行

**Trigger:** 用户展开某个功法项目并点击"观看演示视频"

**Main Success Scenario:**
1. 用户展开一个功法条目，点击"观看演示视频"
2. 系统显示加载指示器，使用已缓存的播放器实例加载视频 URL
3. 系统检测到本地缓存命中，直接从磁盘读取视频数据
4. 视频开始播放，加载指示器消失
5. 用户观看完毕，点击"收起视频"或展开另一个功法的视频
6. 系统停止当前视频，释放当前 MediaItem（播放器实例保留复用）

**Extensions:**
- 3a. 缓存未命中（首次观看）：系统从远程服务器下载视频，同时写入磁盘缓存，播放在缓冲足够后开始
- 3b. 网络不可用且缓存未命中：系统显示"无法加载视频，请检查网络连接"提示，播放按钮恢复可点击状态
- 5a. 用户切换到另一个功法的视频：系统停止当前视频，复用同一播放器加载新视频
- 6a. 用户离开养生页面：系统释放播放器实例和相关资源

### Use Case: 管理视频缓存空间

**Primary Actor:** 系统（自动）
**Scope:** 二狗 Android App — 视频缓存
**Level:** Subfunction

**Stakeholders and Interests:**
- 用户 — 不希望视频缓存占满手机存储

**Preconditions:**
- 视频缓存目录已初始化

**Success Guarantee (Postconditions):**
- 缓存总大小不超过上限（200MB）
- 最久未使用的缓存自动清除

**Trigger:** 缓存写入新视频数据时检测到空间不足

**Main Success Scenario:**
1. 系统写入新的视频缓存数据
2. 系统检测缓存总大小接近上限
3. 系统按 LRU 策略清除最久未访问的缓存文件
4. 缓存大小回到上限以内

**Extensions:**
- 2a. 缓存未超限：无需清除，正常写入
