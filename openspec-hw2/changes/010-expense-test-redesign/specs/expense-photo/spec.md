## ADDED Requirements

### Requirement: 照片上传边界测试
系统 SHALL 在照片上传流程中正确处理各种边界情况，包括文件大小限制、MIME 类型检测、并发上传等。

#### Scenario: 连续上传多张照片
- **WHEN** 用户为同一条账单连续添加 3 张照片
- **THEN** 每张均独立上传成功，照片列表显示 3 张缩略图

#### Scenario: 上传后立即删除再上传
- **WHEN** 用户上传一张照片，删除它，再上传另一张
- **THEN** 照片列表只显示最后上传的那张

#### Scenario: 详情页照片加载认证
- **WHEN** 用户进入含照片的账单详情页
- **THEN** 照片缩略图通过 session cookie 认证正确加载
- **THEN** 点击缩略图全屏查看也正确加载

#### Scenario: session 过期时照片加载失败
- **WHEN** 用户的 session 过期后查看照片
- **THEN** 系统显示加载失败的占位图，不崩溃

## MODIFIED Requirements

### Requirement: AI receipt parsing preview
The system SHALL support sending photos to Claude API (via ReceiptAnalyzer) for AI analysis. The system SHALL compress images (≤1920px, 80% JPEG quality) before encoding to base64. The system SHALL display a preview of parsed results and allow editing before saving.

#### Scenario: Successful AI parse (single photo)
- **WHEN** user selects a receipt photo and triggers AI analysis
- **THEN** system compresses image, sends base64 to Claude API
- **THEN** system displays parsed preview with merchant, amount, date, items, tags

#### Scenario: AI parse timeout
- **WHEN** the Claude API does not respond within 120 seconds
- **THEN** system displays "分析超时，请手动输入" and switches to manual input mode

#### Scenario: AI parse returns empty or invalid result
- **WHEN** the API returns non-JSON or malformed response
- **THEN** system displays "AI 返回格式异常，请重试"

#### Scenario: User edits parsed result before saving
- **WHEN** the preview is displayed
- **THEN** user SHALL be able to edit amount, date, notes, tags, and currency before confirming

#### Scenario: Parse succeeds but create fails
- **WHEN** AI parsing succeeds but POST /api/expenses fails
- **THEN** system preserves the parsed preview, user can retry save without re-analyzing

#### Scenario: Create succeeds but photo upload fails
- **WHEN** expense entry is created but photo upload fails
- **THEN** system displays warning "照片上传失败" but expense is saved
- **THEN** user can later manually add the photo from detail page
