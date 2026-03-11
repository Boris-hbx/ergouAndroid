# 二狗人格系统重构 Spec

> **日期**: 2026年3月5日
> **状态**: 草案
> **触发**: 人设与灵魂演进内容散落在 6+ 个文件中，维护成本高，容易脱节

---

## 1. 现状分析：人格内容碎片化地图

当前二狗的"灵魂"分散在以下文件中：

| 文件 | 内容 | 问题 |
|------|------|------|
| `ErgouPrompt.kt` | 身份定义、性格描述(7条)、说话方式、行为准则、语气示例(5阶段×3-5句)、安全边界、引经据典规则 | **主要人设文本全在这里**，但以 Kotlin 字符串硬编码，改一句要 rebuild |
| `SoulStateEntity.kt` | 5个参数默认值 (classicalRatio=0.9, warmth=0.3, verbosity=0.3, proactivity=0.2, trust=0.1) | 默认值定义了"初始二狗"的人格起点 |
| `SoulRepositoryImpl.kt` | 参数上下限 (PARAM_BOUNDS)、关系阶段名称 (RELATIONSHIP_STAGES)、晋升阈值 (stranger→10次→acquaintance→50次→familiar...) | 演进规则硬编码在 companion object |
| `SoulEvolver.kt` | EVOLUTION_PROMPT — 告诉 LLM 怎么解读对话信号、怎么调参（每个参数的信号→delta映射） | 演进 prompt 也是硬编码字符串 |
| `SoulScreen.kt` | 参数中文名映射、阶段中文标签、参数描述文案 (describeXxx 函数，共5组) | UI 层又定义了一套文案 |
| `workshop/03-人设与品牌设计.md` | 完整人设设计文档（性格特征、语言风格、行为边界、System Prompt 框架） | **设计稿与实际代码已不同步** — 代码里的人格已从"毒舌损友"演变为"文人幕僚"，但设计稿还是旧版 |

### 典型痛点场景

- **改语气**：要同时改 `ErgouPrompt.buildToneExamples()` + `SoulScreen.describeXxx()` + 设计稿
- **加阶段**：要改 `SoulRepositoryImpl.RELATIONSHIP_STAGES` + `STAGE_THRESHOLDS` + `ErgouPrompt.buildToneExamples()` + `SoulScreen.RelationshipHeader()` + `SoulScreen.EvolutionLogItem()` + `SettingsViewModel`
- **调参数范围**：要改 `SoulRepositoryImpl.PARAM_BOUNDS` + `SoulStateEntity` 默认值 + `ErgouPrompt` 中对应的 when 分支
- **同步设计稿**：每次改完代码还要手动更新 `03-人设与品牌设计.md`，实际上没人做

---

## 2. 行业参考：OpenClaw soul.md 方案

[OpenClaw](https://openclawsoul.org/) 的 SOUL.md 是目前 AI agent 人格管理的标杆方案：

### 核心思路
- **一个 Markdown 文件**定义完整人格（身份、性格、说话方式、边界、语气示例）
- Agent 启动时**直接读文件注入 system prompt**
- 改人格 = 改文件，不碰代码
- "If you can write a document, you can create a soul."

### soul.md 典型结构
```
# Core Truths       — 核心信条
# Boundaries        — 边界规则
# Vibe              — 语气调性
# Continuity        — 跨会话一致性
```

### 为什么 OpenClaw 可以用纯 Markdown

| 特性 | OpenClaw | 二狗 |
|------|----------|------|
| 人格模型 | **静态** — 写什么就是什么 | **动态** — 5 个参数随对话自动演进 |
| 语气 | 固定一套 | 5 个关系阶段 × 参数值区间 → 不同文本组合 |
| prompt 构建 | 文件内容直接注入 | 根据 soulState 参数 **条件拼接** 不同段落 |
| 运行环境 | CLI agent / 浏览器 | 编译型 Android app（改文件要 rebuild） |
| 参数范围/阈值 | 不存在 | 需要类型安全的 Float range、Int threshold |

### 关键洞察

> OpenClaw 的 soul.md 本质是一个 **声明式人格描述**——告诉 LLM "你是谁"。
> 二狗的人格系统是 **声明 + 逻辑** 的混合体——不仅告诉 LLM "你是谁"，还根据动态参数决定 **现在你是哪个版本的你**。

纯 Markdown 能表达"你是谁"，但无法表达"当 warmthLevel > 0.8 时你多说一句关心的话"这种条件逻辑。

---

## 3. 选型决策：SoulConfig.kt（集中常量 + 条件逻辑）

### 3.1 方案对比

| 方案 | 做法 | 优点 | 缺点 |
|------|------|------|------|
| **A. soul.md (assets)** | Markdown/YAML 放 assets，启动时解析 | 非技术人员可编辑；改文件不改代码 | 需要写解析器；条件逻辑无法表达；改完还是要 rebuild（Android app） |
| **B. SoulConfig.kt** | 一个 Kotlin object 集中所有人设文本和配置常量 | 零额外依赖；IDE 跳转；类型安全；条件逻辑自然表达 | 还是代码，非技术人员不好直接编辑 |
| **C. DataStore/JSON** | JSON config 存 DataStore，运行时读取 | 热更新；用户可自定义 | 过度设计；序列化复杂度高；类型安全差 |

### 3.2 选择方案 B 的理由

1. **二狗是单人开发项目** — 不存在"非技术人员编辑人格"的需求
2. **条件逻辑是核心** — 人格文本和参数值强绑定，Kotlin 的 when 表达最自然
3. **零额外依赖** — 不需要写 Markdown 解析器或 YAML parser
4. **向前兼容** — 未来要做"用户自定义人格"，从 SoulConfig 抽到 DataStore/JSON 是自然演进
5. **编译期安全** — 参数名拼错、阶段名写错，编译时就能发现

### 3.3 soul.md 理念的保留

虽然不用 Markdown 文件，但 OpenClaw 的 **声明式组织理念** 应该保留：
- SoulConfig 的代码组织像文档一样可读
- 每个 section 有清晰注释，读 SoulConfig 就能理解完整人格
- `workshop/03-人设与品牌设计.md` 不再维护具体 prompt 文本（避免脱节），改为引用 SoulConfig

---

## 4. SoulConfig 设计

### 4.1 文件位置

```
com.ergou.app/
└── data/
    └── soul/
        └── SoulConfig.kt    ← 新建
```

放 `data/soul/` 而非 `util/`，因为它是人格系统的核心定义，不是通用工具。

### 4.2 内容结构

```kotlin
object SoulConfig {

    // ═══════════════════════════════════════════
    // 一、身份定义（不随参数变化）
    // ═══════════════════════════════════════════
    val IDENTITY = "..."
    val CLASSICS = "..."
    val MEMORY_COMMANDS = "..."
    val SAFETY_BOUNDARIES = "..."

    // ═══════════════════════════════════════════
    // 二、性格特征（固定 + 参数条件段）
    // ═══════════════════════════════════════════
    val BASE_TRAITS = listOf(
        "冷静克制：...",
        "专业精准：...",
        "务实导向：...",
        ...
    )
    // warmth 条件段、trust 条件段...

    // ═══════════════════════════════════════════
    // 三、说话方式（classicalRatio + verbosity 驱动）
    // ═══════════════════════════════════════════
    // classicalRatio 分档文本、verbosity 分档文本...

    // ═══════════════════════════════════════════
    // 四、行为准则（proactivity 驱动）
    // ═══════════════════════════════════════════
    val BASE_BEHAVIORS = listOf(...)
    // proactivity 分档文本...

    // ═══════════════════════════════════════════
    // 五、语气示例（按关系阶段）
    // ═══════════════════════════════════════════
    val TONE_EXAMPLES: Map<String, List<String>>

    // ═══════════════════════════════════════════
    // 六、关系阶段定义
    // ═══════════════════════════════════════════
    data class RelationshipStage(
        val key: String,          // "stranger"
        val label: String,        // "初识"
        val threshold: Int,       // 晋升所需互动次数
        val order: Int            // 排序序号
    )
    val STAGES: List<RelationshipStage>

    // ═══════════════════════════════════════════
    // 七、参数定义
    // ═══════════════════════════════════════════
    data class ParameterDef(
        val key: String,          // "classicalRatio"
        val label: String,        // "文白比例"
        val default: Float,       // 0.9f
        val range: ClosedFloatingPointRange<Float>,  // 0.6f..1.0f
        val describe: (Float) -> String   // UI 描述文案
    )
    val PARAMETERS: List<ParameterDef>

    // ═══════════════════════════════════════════
    // 八、演进规则（给 SoulEvolver 的 prompt 模板）
    // ═══════════════════════════════════════════
    val EVOLUTION_PROMPT_TEMPLATE = "..."
    data class EvolutionSignal(
        val parameter: String,
        val trigger: String,      // "太文了/说人话"
        val delta: Float          // -0.03
    )
    val EVOLUTION_SIGNALS: List<EvolutionSignal>
    const val MAX_DAILY_AUTO_ADJUSTMENTS = 10
    const val MAX_DELTA = 0.05f
}
```

### 4.3 谁引用 SoulConfig

| 消费方 | 引用内容 | 当前文件 |
|--------|---------|---------|
| `ErgouPrompt` | IDENTITY, CLASSICS, SAFETY, BASE_TRAITS, warmth/trust 条件段, 说话方式, 行为准则, TONE_EXAMPLES | 不再硬编码文本，只做拼接逻辑 |
| `SoulRepositoryImpl` | PARAMETERS (range, default), STAGES (threshold), MAX_DAILY/MAX_DELTA | 不再定义 companion object 常量 |
| `SoulEvolver` | EVOLUTION_PROMPT_TEMPLATE, EVOLUTION_SIGNALS, MAX_DAILY/MAX_DELTA | 不再硬编码 prompt |
| `SoulScreen` | PARAMETERS (label, describe), STAGES (label) | 不再定义 describeXxx 函数和阶段映射 |
| `SettingsViewModel` | STAGES (label) | 不再硬编码阶段中文名 |

---

## 5. 实施步骤

### Step 1: 创建 SoulConfig.kt
- 从 6 个文件中提取所有人设文本和配置常量
- 组织成上述结构
- 单元测试：验证 PARAMETERS 的 default 值在 range 内、STAGES 按 threshold 递增

### Step 2: ErgouPrompt 重构
- 所有文本常量改为引用 SoulConfig
- buildPersonality/buildSpeakingStyle/buildBehavior/buildToneExamples 逻辑不变，只是文本来源变了
- 产出的 prompt 应与重构前完全一致（可写对比测试）

### Step 3: SoulRepositoryImpl 重构
- PARAM_BOUNDS → `SoulConfig.PARAMETERS.associate { it.key to it.range }`
- RELATIONSHIP_STAGES → `SoulConfig.STAGES.map { it.key }`
- STAGE_THRESHOLDS → `SoulConfig.STAGES.associate { it.key to it.threshold }`
- MAX_DAILY/MAX_DELTA → SoulConfig 常量

### Step 4: SoulEvolver 重构
- EVOLUTION_PROMPT → SoulConfig.EVOLUTION_PROMPT_TEMPLATE + EVOLUTION_SIGNALS 动态生成
- MAX_DAILY → SoulConfig

### Step 5: SoulScreen 重构
- describeXxx 函数 → `SoulConfig.PARAMETERS.find { it.key == "xxx" }!!.describe(value)`
- 阶段映射 → `SoulConfig.STAGES.find { it.key == stage }?.label`
- 参数中文名 → `ParameterDef.label`

### Step 6: 验证与清理
- gradlew assembleDebug 编译通过
- 对比重构前后 buildSystemPrompt 输出一致
- 更新 `workshop/03-人设与品牌设计.md`：移除具体 prompt 文本，注明"人格文本由 SoulConfig.kt 统一管理"

---

## 6. 未来演进路径

```
当前                    近期可选                   远期可选
SoulConfig.kt    →    SoulConfig + DataStore    →    soul.json + 用户编辑器
(编译期常量)            (用户可调个别参数)            (完全自定义人格)
```

- **近期**：把 SoulConfig 中用户可能想调的参数（如语气正式度）暴露到设置页，存 DataStore，SoulConfig 提供默认值
- **远期**：如果要做"人格商店"或"自定义人格"，把 SoulConfig 序列化为 JSON，提供可视化编辑器

---

## 7. 不做的事

- **不做 assets 文件** — Android app 改 assets 也要 rebuild，和改 Kotlin 文件一样
- **不做 YAML/TOML 解析** — 过度设计，引入不必要的依赖
- **不做 soul.md** — 纯 Markdown 无法表达条件逻辑，二狗的人格是动态的
- **不拆多文件** — 一个 SoulConfig.kt 足够，拆成 soul_identity.kt + soul_traits.kt + ... 是过早抽象
- **不自动同步设计稿** — `03-人设与品牌设计.md` 改为设计意图文档，不再维护具体 prompt 内容
