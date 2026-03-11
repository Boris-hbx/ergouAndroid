## Why

记账功能存在多个 bug，涉及新建、编辑、删除、照片上传、AI 分析等多个流程。当前缺乏系统性的测试用例覆盖，导致回归问题频出。需要重新设计完整的测试用例矩阵，覆盖所有用户操作路径和边界情况，确保功能稳定可靠。

## What Changes

- 设计完整的手动测试用例集，覆盖记账功能所有操作路径
- 覆盖 CRUD 基础操作（新建/编辑/删除账单）
- 覆盖照片相关流程（单张上传、多张同一账单、多张不同账单）
- 覆盖 AI 分析流程（Claude 解析收据、解析失败降级、编辑解析结果）
- 覆盖编辑已有账单的各种组合（新增照片、替换照片、修改字段、重新分析）
- 覆盖删除流程（滑动删除、撤销、确认删除）
- **所有测试用例必须是端到端（E2E）用例**：从用户操作入口到最终数据验证，完整走通整条链路
- 使用 3 组真实测试数据验证：
  - Group 1：T&T 超市同一张收据的 4 张照片（$235.76），测试多照片合并分析
  - Group 2：5 张不同收据（Structube $607.94/Costco Gas $84.63/Food Basics $19.56/IKEA $44.89/兰州拉面 $75.77），测试批量独立分析
  - Group 3：全部 9 张照片混合（group1 的 4 张同一收据 + group2 的 5 张不同收据），测试最复杂场景——混合照片中 AI 需识别哪些属于同一单据、哪些是不同单据

## Capabilities

### New Capabilities
- `expense-test-cases`: 记账功能完整测试用例矩阵，包含操作步骤、预期结果、测试数据映射

### Modified Capabilities
- `expense-photo`: 补充照片上传/替换/删除的边界测试场景
- `expense-batch-scan`: 补充批量扫描的组合测试场景（同一单据 vs 不同单据）
- `expense-edit`: 补充编辑已有账单时的各种组合操作测试

## Impact

- 测试覆盖：`ui/expense/` 下所有 Screen 和 ViewModel
- 数据依赖：`data/testdata/group1/`（4 张）、`data/testdata/group2/`（5 张）、`data/testdata/group3/`（9 张混合）
- API 覆盖：NextApiService 的 expenses 相关所有端点（CRUD + photos + parse-preview + summary）
- 工具覆盖：add_expense / delete_expense / query_expenses / expense_summary 四个 Tool
- 外部依赖：Claude API（ReceiptAnalyzer）、Next 后端 API
