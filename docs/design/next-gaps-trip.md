# Next API Gaps — Trip 模块

## 已修复的问题

### 1. API 路由错误 (NextApiService.kt)
- `updateTripItem` 和 `deleteTripItem` 原 URL: `/api/trips/{tripId}/items/{itemId}`
- Next 后端实际路由: `/api/trips/items/{itemId}` (不含 tripId)
- **已修复**: 更新 URL 但保留 tripId 参数以兼容现有调用方

### 2. DTO 字段不匹配
- `NextReimburseSummary`: 后端返回 Int 计数 (total/pending/submitted/approved/rejected/na)，ergou 原来用 Double 金额且缺少 submitted/na
- `NextTripItem`: 默认 type 应为 "misc" (非 "other")，默认 reimburseStatus 应为 "pending" (非 "none")，缺少 sortOrder/photoCount/createdAt/updatedAt
- `NextTrip`: 缺少 isOwner 字段
- **已修复**: 所有 DTO 已对齐

### 3. 费用类型枚举不匹配
- ergou 原来用: transport/hotel/meal/conference/other
- Next 后端用: flight/train/hotel/taxi/meal/meeting/telecom/misc
- **已修复**: 所有类型映射已更新

## 功能差距 (尚未实现)

### 1. 照片管理
- Next 支持为费用项上传/删除收据照片
- API: `POST /trips/items/{itemId}/photos`, `DELETE /trips/photos/{photoId}`
- ergou 暂不支持照片功能

### 2. 协作者管理
- Next 支持行程分享 (owner/editor/viewer 角色)
- API: `POST /trips/{id}/collaborators`, `DELETE /trips/{id}/collaborators/{uid}`
- ergou 暂不支持协作

### 3. 导出功能
- Next 支持导出 XLSX、照片 ZIP、打包 Bundle
- API: `GET /trips/{id}/export/xlsx|photos|bundle`
- ergou 暂不支持导出

### 4. AI 票据分析
- Next 支持拍照/粘贴文字自动识别费用项
- API: `POST /trips/analyze`
- ergou 暂不支持

### 5. 行程排序
- Next 费用项支持 sort_order 拖拽排序
- ergou 目前按 sortOrder 显示但不支持手动调整

## 数据模型对比

### Next 后端完整 TripItem 类型枚举
flight | train | hotel | taxi | meal | meeting | telecom | misc

### Next 后端完整报销状态枚举
pending | submitted | approved | rejected | na

### Next 后端权限矩阵
| 操作 | Owner | Editor | Viewer |
|------|-------|--------|--------|
| 查看行程 | Y | Y | Y |
| 编辑行程 | Y | N | N |
| 创建费用 | Y | Y | N |
| 编辑费用(全) | Y | N | N |
| 编辑报销状态 | Y | Y | N |
| 删除费用 | Y | N | N |
| 上传照片 | Y | Y | N |
| 删除照片 | Y | N | N |
