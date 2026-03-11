## Why

当前记账功能只支持"添加"和"删除"，不能编辑已有记录。用户记错金额、备注或标签后只能删掉重建，体验差。同时 Next 后端已支持照片上传和 AI 解析收据（`parse-preview` + `photos` API），但 App 端完全没有对接，用户无法拍照记账。

这两个缺失是记账模块最高优先级的短板：编辑是基础 CRUD 补全，图片分析是记账的核心差异化能力。

## What Changes

- 新增记账详情/编辑页面（点击条目进入），支持修改金额、日期、备注、标签、币种
- 详情页展示 AI 解析的明细行（ExpenseItem）
- 新增照片上传功能（从相册选择或拍照），调用 Next 后端 `POST /api/expenses/:id/photos`
- 新增 AI 收据解析流程：选照片 → 调 `POST /api/expenses/parse-preview` → 预览结构化结果 → 用户确认/修改 → 保存
- 详情页展示已上传照片，支持查看和删除
- NextApiService 新增照片上传、删除、parse-preview 相关 API
- NextModels 新增 ExpensePhoto、ParsePreview 相关 DTO

## Capabilities

### New Capabilities
- `expense-edit`: 记账条目编辑功能，包括详情页 UI、编辑表单、更新 API 调用
- `expense-photo`: 记账照片管理，包括上传、查看、删除照片，以及 AI 收据解析预览流程

### Modified Capabilities
（无现有 spec 需要修改）

## Impact

- **UI 层**: 新增 `ExpenseDetailScreen`（详情/编辑页），修改 `ExpenseScreen`（条目点击导航）、`ErgouNavigation`（新路由）
- **API 层**: `NextApiService` 新增 4 个方法（uploadExpensePhoto, deleteExpensePhoto, parsePreview, getExpenseDetail with photos）
- **DTO 层**: `NextModels.kt` 新增 `NextExpensePhoto`、`NextParsePreview`、相关 Request/Response
- **依赖**: 需要 Coil 加载照片（已有依赖）、Android CameraX 或 ActivityResult API 拍照/选图
- **权限**: 需要 `CAMERA` 权限（拍照路径），相册选图不需额外权限
- **ViewModel**: 新增 `ExpenseDetailViewModel` 或扩展现有 `ExpenseViewModel`
