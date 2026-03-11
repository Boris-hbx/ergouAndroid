package com.ergou.app.ui.health

data class Pt(val x: Float, val y: Float)

data class BodyOutlineSide(
    val head: List<Pt>,
    val neckLeft: List<Pt>, val neckRight: List<Pt>,
    val torsoLeft: List<Pt>, val torsoRight: List<Pt>,
    val armLeftOuter: List<Pt>, val armLeftInner: List<Pt>,
    val armRightOuter: List<Pt>, val armRightInner: List<Pt>,
    val legLeftOuter: List<Pt>, val legLeftInner: List<Pt>,
    val legRightOuter: List<Pt>, val legRightInner: List<Pt>
)

data class Acupoint(
    val id: String,
    val name: String,
    val pinyin: String,
    val positionFront: Pt? = null,
    val positionBack: Pt? = null,
    val isKey: Boolean = false,
    val functions: List<String> = emptyList(),
    val indication: String = ""
)

data class Meridian(
    val id: String,
    val name: String,
    val shortName: String,
    val organ: String,
    val element: String,
    val yinYang: String,
    val limbType: String,
    val direction: String,
    val color: Long,
    val pathFront: List<Pt>,
    val pathBack: List<Pt>,
    val acupoints: List<Acupoint>,
    val description: String
)

object MeridianData {

    private fun f(x: Float, y: Float) = Pt(x, y)

    fun mirror(pts: List<Pt>): List<Pt> = pts.map { Pt(1f - it.x, it.y) }

    // ── Front body outline ──────────────────────────────────────────────

    private val frontHead = listOf(
        f(0.50f,0.000f),f(0.53f,0.003f),f(0.56f,0.012f),f(0.58f,0.025f),
        f(0.585f,0.040f),f(0.58f,0.055f),f(0.575f,0.065f),f(0.56f,0.080f),
        f(0.54f,0.093f),f(0.52f,0.098f),f(0.50f,0.100f),f(0.48f,0.098f),
        f(0.46f,0.093f),f(0.44f,0.080f),f(0.425f,0.065f),f(0.42f,0.055f),
        f(0.415f,0.040f),f(0.42f,0.025f),f(0.44f,0.012f),f(0.47f,0.003f)
    )
    private val frontNeckLeft = listOf(
        f(0.46f,0.100f),f(0.45f,0.110f),f(0.44f,0.120f),f(0.43f,0.130f)
    )
    private val frontNeckRight = mirror(frontNeckLeft)
    private val frontTorsoLeft = listOf(
        f(0.36f,0.150f),f(0.37f,0.180f),f(0.38f,0.220f),f(0.39f,0.270f),
        f(0.41f,0.320f),f(0.42f,0.360f),f(0.42f,0.380f),f(0.43f,0.400f)
    )
    private val frontTorsoRight = mirror(frontTorsoLeft)
    private val frontArmLeftOuter = listOf(
        f(0.36f,0.150f),f(0.34f,0.170f),f(0.32f,0.200f),f(0.29f,0.240f),
        f(0.27f,0.280f),f(0.26f,0.310f),f(0.24f,0.350f),f(0.22f,0.390f),
        f(0.21f,0.410f),f(0.22f,0.430f)
    )
    private val frontArmLeftInner = listOf(
        f(0.38f,0.160f),f(0.37f,0.180f),f(0.35f,0.210f),f(0.33f,0.250f),
        f(0.31f,0.280f),f(0.30f,0.310f),f(0.28f,0.350f),f(0.27f,0.390f),
        f(0.26f,0.410f),f(0.26f,0.430f)
    )
    private val frontArmRightOuter = mirror(frontArmLeftOuter)
    private val frontArmRightInner = mirror(frontArmLeftInner)
    private val frontLegLeftOuter = listOf(
        f(0.43f,0.400f),f(0.42f,0.430f),f(0.41f,0.470f),f(0.40f,0.520f),
        f(0.40f,0.560f),f(0.40f,0.610f),f(0.40f,0.650f),f(0.40f,0.700f),
        f(0.40f,0.750f),f(0.40f,0.810f),f(0.41f,0.870f),f(0.40f,0.900f),
        f(0.38f,0.920f)
    )
    private val frontLegLeftInner = listOf(
        f(0.48f,0.400f),f(0.48f,0.430f),f(0.48f,0.470f),f(0.47f,0.520f),
        f(0.47f,0.560f),f(0.47f,0.610f),f(0.47f,0.650f),f(0.47f,0.700f),
        f(0.46f,0.750f),f(0.46f,0.810f),f(0.45f,0.870f),f(0.45f,0.900f),
        f(0.45f,0.920f)
    )
    private val frontLegRightOuter = mirror(frontLegLeftOuter)
    private val frontLegRightInner = mirror(frontLegLeftInner)

    // ── Back body outline ───────────────────────────────────────────────

    private val backHead = frontHead
    private val backNeckLeft = frontNeckLeft
    private val backNeckRight = mirror(backNeckLeft)
    private val backTorsoLeft = listOf(
        f(0.36f,0.150f),f(0.35f,0.180f),f(0.34f,0.200f),f(0.36f,0.220f),
        f(0.38f,0.250f),f(0.39f,0.280f),f(0.41f,0.320f),f(0.42f,0.360f),
        f(0.42f,0.380f),f(0.43f,0.400f)
    )
    private val backTorsoRight = mirror(backTorsoLeft)
    private val backArmLeftOuter = frontArmLeftOuter
    private val backArmLeftInner = frontArmLeftInner
    private val backArmRightOuter = mirror(backArmLeftOuter)
    private val backArmRightInner = mirror(backArmLeftInner)
    private val backLegLeftOuter = frontLegLeftOuter
    private val backLegLeftInner = frontLegLeftInner
    private val backLegRightOuter = mirror(backLegLeftOuter)
    private val backLegRightInner = mirror(backLegLeftInner)

    // ── Public outline objects ──────────────────────────────────────────

    val front = BodyOutlineSide(
        head = frontHead,
        neckLeft = frontNeckLeft, neckRight = frontNeckRight,
        torsoLeft = frontTorsoLeft, torsoRight = frontTorsoRight,
        armLeftOuter = frontArmLeftOuter, armLeftInner = frontArmLeftInner,
        armRightOuter = frontArmRightOuter, armRightInner = frontArmRightInner,
        legLeftOuter = frontLegLeftOuter, legLeftInner = frontLegLeftInner,
        legRightOuter = frontLegRightOuter, legRightInner = frontLegRightInner
    )

    val back = BodyOutlineSide(
        head = backHead,
        neckLeft = backNeckLeft, neckRight = backNeckRight,
        torsoLeft = backTorsoLeft, torsoRight = backTorsoRight,
        armLeftOuter = backArmLeftOuter, armLeftInner = backArmLeftInner,
        armRightOuter = backArmRightOuter, armRightInner = backArmRightInner,
        legLeftOuter = backLegLeftOuter, legLeftInner = backLegLeftInner,
        legRightOuter = backLegRightOuter, legRightInner = backLegRightInner
    )

    // ── 14 Meridians ───────────────────────────────────────────────────

    val meridians: List<Meridian> = listOf(

        // 1. LU 肺经
        Meridian(
            id = "LU", name = "手太阴肺经", shortName = "肺经",
            organ = "肺", element = "金", yinYang = "太阴",
            limbType = "hand", direction = "centrifugal",
            color = 0xFF94a3b8,
            pathFront = listOf(
                f(0.38f,0.22f),f(0.37f,0.235f),f(0.36f,0.25f),f(0.35f,0.27f),
                f(0.34f,0.285f),f(0.33f,0.30f),f(0.32f,0.315f),f(0.31f,0.33f),
                f(0.30f,0.345f),f(0.29f,0.36f),f(0.28f,0.38f),f(0.27f,0.395f),
                f(0.26f,0.41f),f(0.25f,0.425f),f(0.24f,0.44f),f(0.235f,0.45f),
                f(0.23f,0.46f),f(0.225f,0.47f),f(0.22f,0.48f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("LU-1","中府","Zhōng Fǔ", positionFront = f(0.38f,0.22f), isKey = true,
                    functions = listOf("宣肺理气","止咳平喘","肃降肺气"),
                    indication = "咳嗽、气喘、胸闷、胸痛、肩背痛"),
                Acupoint("LU-5","尺泽","Chǐ Zé", positionFront = f(0.28f,0.38f), isKey = true,
                    functions = listOf("清肺泻火","降逆止咳","舒筋活络"),
                    indication = "咳嗽、气喘、咯血、潮热、肘臂挛痛"),
                Acupoint("LU-7","列缺","Liè Quē", positionFront = f(0.25f,0.44f), isKey = true,
                    functions = listOf("宣肺解表","通经活络","通调任脉"),
                    indication = "头痛、项强、咳嗽、气喘、咽喉肿痛、口眼歪斜"),
                Acupoint("LU-9","太渊","Tài Yuān", positionFront = f(0.24f,0.46f), isKey = true,
                    functions = listOf("补肺益气","止咳化痰","通调血脉"),
                    indication = "咳嗽、气喘、无脉症、腕痛"),
                Acupoint("LU-11","少商","Shào Shāng", positionFront = f(0.22f,0.48f), isKey = true,
                    functions = listOf("清热利咽","开窍醒神","泻肺热"),
                    indication = "咽喉肿痛、鼻衄、高热、昏迷、癫狂")
            ),
            description = "肺经起于中焦，下络大肠，上行穿膈属肺，循上肢内侧前缘至拇指端。主治咳嗽、气喘、胸闷、咽喉肿痛等肺系病证。"
        ),

        // 2. LI 大肠经
        Meridian(
            id = "LI", name = "手阳明大肠经", shortName = "大肠经",
            organ = "大肠", element = "金", yinYang = "阳明",
            limbType = "hand", direction = "centripetal",
            color = 0xFFcbd5e1,
            pathFront = listOf(
                f(0.21f,0.48f),f(0.215f,0.47f),f(0.22f,0.46f),f(0.225f,0.45f),
                f(0.23f,0.44f),f(0.24f,0.425f),f(0.25f,0.41f),f(0.26f,0.395f),
                f(0.27f,0.38f),f(0.28f,0.365f),f(0.285f,0.35f),f(0.29f,0.335f),
                f(0.30f,0.32f),f(0.31f,0.30f),f(0.32f,0.28f),f(0.33f,0.26f),
                f(0.35f,0.24f),f(0.37f,0.22f),f(0.39f,0.20f),f(0.40f,0.185f),
                f(0.41f,0.17f),f(0.43f,0.155f),f(0.44f,0.14f),f(0.455f,0.125f),
                f(0.46f,0.115f),f(0.47f,0.105f),f(0.48f,0.10f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("LI-4","合谷","Hé Gǔ", positionFront = f(0.23f,0.44f), isKey = true,
                    functions = listOf("疏风解表","通络镇痛","清泄肺气"),
                    indication = "头痛、目赤肿痛、鼻衄、齿痛、面肿、发热恶寒"),
                Acupoint("LI-11","曲池","Qū Chí", positionFront = f(0.285f,0.35f), isKey = true,
                    functions = listOf("清热解表","疏经通络","调和气血"),
                    indication = "热病、上肢不遂、手臂肿痛、高血压"),
                Acupoint("LI-20","迎香","Yíng Xiāng", positionFront = f(0.48f,0.10f), isKey = true,
                    functions = listOf("祛风通窍","理气止痛"),
                    indication = "鼻塞、鼻衄、口眼歪斜、面痒浮肿")
            ),
            description = "大肠经起于食指末端，经手背沿前臂外侧上行至肩、颈，止于对侧鼻翼旁。主治头面五官疾患、热病及上肢外侧前缘痛。"
        ),

        // 3. ST 胃经
        Meridian(
            id = "ST", name = "足阳明胃经", shortName = "胃经",
            organ = "胃", element = "土", yinYang = "阳明",
            limbType = "foot", direction = "centrifugal",
            color = 0xFFfbbf24,
            pathFront = listOf(
                f(0.47f,0.07f),f(0.47f,0.08f),f(0.47f,0.09f),f(0.47f,0.10f),
                f(0.47f,0.11f),f(0.47f,0.12f),f(0.46f,0.14f),f(0.46f,0.16f),
                f(0.45f,0.18f),f(0.44f,0.20f),f(0.44f,0.24f),f(0.44f,0.28f),
                f(0.44f,0.32f),f(0.44f,0.36f),f(0.44f,0.40f),f(0.44f,0.44f),
                f(0.44f,0.46f),f(0.43f,0.50f),f(0.42f,0.54f),f(0.41f,0.58f),
                f(0.41f,0.60f),f(0.41f,0.62f),f(0.41f,0.65f),f(0.40f,0.68f),
                f(0.40f,0.70f),f(0.40f,0.73f),f(0.40f,0.76f),f(0.40f,0.79f),
                f(0.40f,0.82f),f(0.41f,0.85f),f(0.42f,0.88f),f(0.42f,0.90f),
                f(0.43f,0.92f),f(0.44f,0.94f),f(0.44f,0.96f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("ST-25","天枢","Tiān Shū", positionFront = f(0.44f,0.44f), isKey = true,
                    functions = listOf("调肠胃","理气行滞","消食化积"),
                    indication = "腹胀、肠鸣、泄泻、便秘、痢疾、月经不调"),
                Acupoint("ST-36","足三里","Zú Sān Lǐ", positionFront = f(0.41f,0.70f), isKey = true,
                    functions = listOf("健脾和胃","扶正培元","通经活络"),
                    indication = "胃痛、腹胀、呕吐、泄泻、下肢不遂、虚劳赢瘦"),
                Acupoint("ST-40","丰隆","Fēng Lóng", positionFront = f(0.40f,0.76f), isKey = true,
                    functions = listOf("健脾化痰","和胃降逆","开窍"),
                    indication = "头痛眩晕、咳嗽痰多、癫狂、下肢痿痹")
            ),
            description = "胃经从眼下起，沿面颊、颈前、胸腹下行至下肢前侧，止于第二趾端。为多气多血之经，主治胃肠病、头面口齿病。"
        ),

        // 4. SP 脾经
        Meridian(
            id = "SP", name = "足太阴脾经", shortName = "脾经",
            organ = "脾", element = "土", yinYang = "太阴",
            limbType = "foot", direction = "centripetal",
            color = 0xFFf59e0b,
            pathFront = listOf(
                f(0.42f,0.96f),f(0.42f,0.94f),f(0.42f,0.92f),f(0.43f,0.90f),
                f(0.44f,0.88f),f(0.44f,0.86f),f(0.45f,0.84f),f(0.45f,0.82f),
                f(0.45f,0.79f),f(0.45f,0.76f),f(0.46f,0.73f),f(0.46f,0.70f),
                f(0.46f,0.67f),f(0.46f,0.64f),f(0.47f,0.61f),f(0.47f,0.58f),
                f(0.47f,0.55f),f(0.47f,0.52f),f(0.47f,0.48f),f(0.47f,0.44f),
                f(0.46f,0.40f),f(0.46f,0.36f),f(0.45f,0.32f),f(0.44f,0.28f),
                f(0.42f,0.26f),f(0.40f,0.24f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("SP-6","三阴交","Sān Yīn Jiāo", positionFront = f(0.45f,0.84f), isKey = true,
                    functions = listOf("健脾利湿","调补肝肾","行气活血"),
                    indication = "肠鸣腹胀、泄泻、月经不调、带下、不孕、遗精"),
                Acupoint("SP-9","阴陵泉","Yīn Líng Quán", positionFront = f(0.46f,0.70f), isKey = true,
                    functions = listOf("健脾利湿","通利小便"),
                    indication = "腹胀、泄泻、水肿、黄疸、小便不利、膝痛"),
                Acupoint("SP-10","血海","Xuè Hǎi", positionFront = f(0.46f,0.64f), isKey = true,
                    functions = listOf("活血化瘀","调经统血","健脾利湿"),
                    indication = "月经不调、痛经、闭经、崩漏、瘾疹湿疹")
            ),
            description = "脾经从大趾内侧起，沿下肢内侧上行至腹、胸。主治脾胃病、妇科病。脾为后天之本，气血生化之源。"
        ),

        // 5. HT 心经
        Meridian(
            id = "HT", name = "手少阴心经", shortName = "心经",
            organ = "心", element = "火", yinYang = "少阴",
            limbType = "hand", direction = "centrifugal",
            color = 0xFFef4444,
            pathFront = listOf(
                f(0.35f,0.24f),f(0.34f,0.26f),f(0.33f,0.28f),f(0.32f,0.295f),
                f(0.31f,0.31f),f(0.30f,0.325f),f(0.29f,0.34f),f(0.28f,0.355f),
                f(0.275f,0.37f),f(0.27f,0.385f),f(0.265f,0.40f),f(0.26f,0.415f),
                f(0.255f,0.43f),f(0.25f,0.44f),f(0.245f,0.45f),f(0.24f,0.46f),
                f(0.235f,0.47f),f(0.22f,0.48f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("HT-3","少海","Shào Hǎi", positionFront = f(0.28f,0.355f), isKey = true,
                    functions = listOf("理气通络","益心安神"),
                    indication = "心痛、肘臂挛痛、头项痛、腋胁痛"),
                Acupoint("HT-7","神门","Shén Mén", positionFront = f(0.245f,0.45f), isKey = true,
                    functions = listOf("宁心安神","通经活络"),
                    indication = "心痛、心烦、惊悸、失眠、健忘、癫狂痫"),
                Acupoint("HT-9","少冲","Shào Chōng", positionFront = f(0.22f,0.48f), isKey = true,
                    functions = listOf("清热熄风","醒神开窍"),
                    indication = "心悸、心痛、胸胁痛、癫狂、热病、昏迷")
            ),
            description = "心经从心系起，出于腋下，沿上臂内侧后缘下行至小指端。主治心、胸、神志病。心为君主之官。"
        ),

        // 6. SI 小肠经
        Meridian(
            id = "SI", name = "手太阳小肠经", shortName = "小肠经",
            organ = "小肠", element = "火", yinYang = "太阳",
            limbType = "hand", direction = "centripetal",
            color = 0xFFf87171,
            pathFront = listOf(
                f(0.20f,0.48f),f(0.205f,0.47f),f(0.21f,0.46f),f(0.215f,0.45f),
                f(0.22f,0.44f),f(0.225f,0.43f),f(0.23f,0.42f),f(0.235f,0.41f),
                f(0.24f,0.40f),f(0.25f,0.39f),f(0.26f,0.375f),f(0.27f,0.36f),
                f(0.45f,0.085f)
            ),
            pathBack = listOf(
                f(0.27f,0.36f),f(0.28f,0.345f),f(0.29f,0.33f),f(0.30f,0.315f),
                f(0.31f,0.30f),f(0.32f,0.28f),f(0.33f,0.26f),f(0.34f,0.24f),
                f(0.35f,0.22f),f(0.37f,0.20f),f(0.38f,0.19f),f(0.40f,0.18f),
                f(0.42f,0.16f),f(0.44f,0.14f),f(0.46f,0.12f),f(0.47f,0.10f)
            ),
            acupoints = listOf(
                Acupoint("SI-3","后溪","Hòu Xī", positionFront = f(0.215f,0.45f), isKey = true,
                    functions = listOf("清心安神","通经活络","通督脉"),
                    indication = "头项强痛、腰背痛、手指挛痛、目赤、耳聋"),
                Acupoint("SI-19","听宫","Tīng Gōng", positionFront = f(0.45f,0.085f), isKey = true,
                    functions = listOf("聪耳开窍","宁神定志"),
                    indication = "耳鸣、耳聋、聤耳、齿痛、癫狂")
            ),
            description = "小肠经从小指尺侧起，经手背、前臂外侧上行至肩后、颈侧，止于耳前。主治头面五官病、热病、神志病。"
        ),

        // 7. BL 膀胱经
        Meridian(
            id = "BL", name = "足太阳膀胱经", shortName = "膀胱经",
            organ = "膀胱", element = "水", yinYang = "太阳",
            limbType = "foot", direction = "centrifugal",
            color = 0xFF1e3a5f,
            pathFront = listOf(
                f(0.48f,0.065f),f(0.48f,0.055f),f(0.48f,0.045f),f(0.48f,0.035f)
            ),
            pathBack = listOf(
                f(0.48f,0.035f),f(0.48f,0.025f),f(0.48f,0.02f),f(0.47f,0.04f),
                f(0.47f,0.06f),f(0.47f,0.08f),f(0.47f,0.10f),f(0.47f,0.12f),
                f(0.46f,0.14f),f(0.46f,0.17f),f(0.46f,0.20f),f(0.46f,0.23f),
                f(0.46f,0.26f),f(0.46f,0.29f),f(0.46f,0.32f),f(0.46f,0.35f),
                f(0.46f,0.38f),f(0.46f,0.41f),f(0.46f,0.44f),f(0.45f,0.47f),
                f(0.45f,0.50f),f(0.44f,0.53f),f(0.43f,0.56f),f(0.42f,0.59f),
                f(0.42f,0.62f),f(0.42f,0.65f),f(0.42f,0.68f),f(0.42f,0.71f),
                f(0.42f,0.74f),f(0.42f,0.77f),f(0.42f,0.80f),f(0.42f,0.83f),
                f(0.42f,0.86f),f(0.43f,0.88f),f(0.43f,0.90f),f(0.44f,0.92f),
                f(0.44f,0.94f),f(0.44f,0.96f)
            ),
            acupoints = listOf(
                Acupoint("BL-2","攒竹","Cuán Zhú", positionFront = f(0.48f,0.055f), isKey = true,
                    functions = listOf("清热明目","祛风通络"),
                    indication = "头痛、口眼歪斜、目赤肿痛、迎风流泪"),
                Acupoint("BL-23","肾俞","Shèn Shù", positionBack = f(0.46f,0.35f), isKey = true,
                    functions = listOf("补肾益气","强腰壮骨","利水"),
                    indication = "腰痛、遗精、阳痿、遗尿、月经不调、耳鸣"),
                Acupoint("BL-40","委中","Wěi Zhōng", positionBack = f(0.42f,0.65f), isKey = true,
                    functions = listOf("舒筋通络","散瘀活血","清热解毒"),
                    indication = "腰背疼痛、下肢痿痹、腹痛吐泻"),
                Acupoint("BL-60","昆仑","Kūn Lún", positionBack = f(0.43f,0.90f), isKey = true,
                    functions = listOf("舒筋活络","散风清热","强腰膝"),
                    indication = "头痛、项强、目眩、腰骶疼痛、足踝肿痛"),
                Acupoint("BL-67","至阴","Zhì Yīn", positionBack = f(0.44f,0.96f), isKey = true,
                    functions = listOf("正胎催产","清头明目","通经活络"),
                    indication = "头痛、目痛、鼻塞、鼻衄、胎位不正")
            ),
            description = "膀胱经为人体最长经脉，从内眼角起经头顶至颈后，沿脊柱两旁下行至足小趾。"
        ),

        // 8. KI 肾经
        Meridian(
            id = "KI", name = "足少阴肾经", shortName = "肾经",
            organ = "肾", element = "水", yinYang = "少阴",
            limbType = "foot", direction = "centripetal",
            color = 0xFF334155,
            pathFront = listOf(
                f(0.44f,0.96f),f(0.44f,0.94f),f(0.45f,0.92f),f(0.45f,0.90f),
                f(0.46f,0.88f),f(0.46f,0.86f),f(0.46f,0.83f),f(0.46f,0.80f),
                f(0.46f,0.77f),f(0.46f,0.74f),f(0.46f,0.71f),f(0.47f,0.68f),
                f(0.47f,0.65f),f(0.47f,0.62f),f(0.48f,0.58f),f(0.48f,0.54f),
                f(0.48f,0.50f),f(0.48f,0.46f),f(0.48f,0.42f),f(0.48f,0.38f),
                f(0.48f,0.34f),f(0.48f,0.30f),f(0.47f,0.26f),f(0.46f,0.22f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("KI-1","涌泉","Yǒng Quán", positionFront = f(0.44f,0.96f), isKey = true,
                    functions = listOf("苏厥开窍","滋阴降火","镇静安神"),
                    indication = "头顶痛、头晕、失音、小儿惊风、癫狂"),
                Acupoint("KI-3","太溪","Tài Xī", positionFront = f(0.45f,0.90f), isKey = true,
                    functions = listOf("补肾益阴","壮阳强腰","清虚热"),
                    indication = "耳鸣、耳聋、咽喉肿痛、齿痛、失眠、阳痿"),
                Acupoint("KI-6","照海","Zhào Hǎi", positionFront = f(0.46f,0.88f), isKey = true,
                    functions = listOf("滋阴清热","调经止带","宁神定志"),
                    indication = "咽喉干痛、目赤肿痛、月经不调、失眠")
            ),
            description = "肾经从足底起，沿下肢内侧后缘上行至腹、胸。肾为先天之本，主藏精、主水、主纳气。"
        ),

        // 9. PC 心包经
        Meridian(
            id = "PC", name = "手厥阴心包经", shortName = "心包经",
            organ = "心包", element = "火", yinYang = "厥阴",
            limbType = "hand", direction = "centrifugal",
            color = 0xFFdc2626,
            pathFront = listOf(
                f(0.37f,0.23f),f(0.36f,0.245f),f(0.35f,0.26f),f(0.34f,0.275f),
                f(0.33f,0.29f),f(0.32f,0.305f),f(0.31f,0.32f),f(0.30f,0.335f),
                f(0.29f,0.35f),f(0.28f,0.365f),f(0.275f,0.38f),f(0.27f,0.39f),
                f(0.26f,0.405f),f(0.25f,0.42f),f(0.24f,0.435f),f(0.235f,0.445f),
                f(0.23f,0.455f),f(0.225f,0.465f),f(0.22f,0.48f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("PC-6","内关","Nèi Guān", positionFront = f(0.25f,0.42f), isKey = true,
                    functions = listOf("宁心安神","理气止痛","和胃降逆"),
                    indication = "心痛、心悸、胸闷、胃痛、呕吐、眩晕、失眠"),
                Acupoint("PC-8","劳宫","Láo Gōng", positionFront = f(0.23f,0.455f), isKey = true,
                    functions = listOf("清心泻热","开窍醒神","消肿止痒"),
                    indication = "心痛、癫狂、口疮、口臭、中风昏迷"),
                Acupoint("PC-9","中冲","Zhōng Chōng", positionFront = f(0.22f,0.48f), isKey = true,
                    functions = listOf("清心泻热","开窍醒神"),
                    indication = "心痛、昏迷、舌强肿痛、热病、中暑")
            ),
            description = "心包经从胸中起，沿上臂内侧中线下行至中指端。心包代心受邪，主治心胸病、胃病、神志病。"
        ),

        // 10. SJ 三焦经
        Meridian(
            id = "SJ", name = "手少阳三焦经", shortName = "三焦经",
            organ = "三焦", element = "火", yinYang = "少阳",
            limbType = "hand", direction = "centripetal",
            color = 0xFFfb923c,
            pathFront = listOf(
                f(0.20f,0.48f),f(0.205f,0.47f),f(0.21f,0.46f),f(0.22f,0.45f),
                f(0.225f,0.44f),f(0.46f,0.065f)
            ),
            pathBack = listOf(
                f(0.225f,0.44f),f(0.23f,0.43f),f(0.235f,0.42f),f(0.24f,0.41f),
                f(0.25f,0.395f),f(0.26f,0.38f),f(0.27f,0.365f),f(0.28f,0.35f),
                f(0.285f,0.34f),f(0.29f,0.33f),f(0.30f,0.31f),f(0.31f,0.295f),
                f(0.32f,0.28f),f(0.33f,0.26f),f(0.34f,0.24f),f(0.36f,0.22f),
                f(0.38f,0.20f),f(0.40f,0.185f),f(0.42f,0.17f),f(0.44f,0.15f),
                f(0.45f,0.13f),f(0.46f,0.11f),f(0.47f,0.09f),f(0.47f,0.075f)
            ),
            acupoints = listOf(
                Acupoint("SJ-5","外关","Wài Guān", positionFront = f(0.225f,0.44f), isKey = true,
                    functions = listOf("清热解表","通经活络","疏散风热"),
                    indication = "热病、头痛、目赤肿痛、耳鸣耳聋、上肢痹痛"),
                Acupoint("SJ-17","翳风","Yì Fēng", positionBack = f(0.44f,0.11f), isKey = true,
                    functions = listOf("聪耳通窍","散风泻热"),
                    indication = "耳鸣、耳聋、口眼歪斜、面痛、牙关紧闭"),
                Acupoint("SJ-23","丝竹空","Sī Zhú Kōng", positionFront = f(0.46f,0.065f), isKey = true,
                    functions = listOf("清头明目","散骨镇惊"),
                    indication = "头痛、目眩、目赤肿痛、眼睑瞤动、齿痛")
            ),
            description = "三焦经从无名指端起，经手背、前臂背面上行至肩、颈后、耳后，止于眉梢。三焦主通调水道。"
        ),

        // 11. GB 胆经
        Meridian(
            id = "GB", name = "足少阳胆经", shortName = "胆经",
            organ = "胆", element = "木", yinYang = "少阳",
            limbType = "foot", direction = "centrifugal",
            color = 0xFF4ade80,
            pathFront = listOf(
                f(0.46f,0.065f),f(0.45f,0.055f),f(0.44f,0.045f),f(0.43f,0.04f),
                f(0.42f,0.05f),f(0.41f,0.06f),f(0.40f,0.055f),f(0.39f,0.045f),
                f(0.40f,0.06f),f(0.41f,0.075f),f(0.42f,0.085f)
            ),
            pathBack = listOf(
                f(0.42f,0.085f),f(0.43f,0.095f),f(0.43f,0.11f),f(0.42f,0.13f),
                f(0.40f,0.15f),f(0.38f,0.17f),f(0.37f,0.19f),f(0.36f,0.21f),
                f(0.38f,0.22f),f(0.39f,0.24f),f(0.39f,0.27f),f(0.39f,0.30f),
                f(0.38f,0.33f),f(0.38f,0.36f),f(0.37f,0.39f),f(0.37f,0.42f),
                f(0.37f,0.45f),f(0.37f,0.48f),f(0.38f,0.51f),f(0.38f,0.54f),
                f(0.38f,0.57f),f(0.38f,0.60f),f(0.38f,0.63f),f(0.38f,0.66f),
                f(0.38f,0.69f),f(0.38f,0.72f),f(0.38f,0.75f),f(0.38f,0.78f),
                f(0.39f,0.81f),f(0.39f,0.84f),f(0.40f,0.87f),f(0.41f,0.89f),
                f(0.42f,0.91f),f(0.42f,0.93f),f(0.43f,0.95f)
            ),
            acupoints = listOf(
                Acupoint("GB-20","风池","Fēng Chí", positionBack = f(0.43f,0.095f), isKey = true,
                    functions = listOf("疏风清热","明目益聪","通利官窍"),
                    indication = "头痛、眩晕、目赤肿痛、鼻渊、耳鸣、感冒"),
                Acupoint("GB-21","肩井","Jiān Jǐng", positionBack = f(0.40f,0.15f), isKey = true,
                    functions = listOf("祛风活络","消肿散结"),
                    indication = "肩背痹痛、上肢不遂、颈项强痛、乳痈"),
                Acupoint("GB-30","环跳","Huán Tiào", positionBack = f(0.38f,0.51f), isKey = true,
                    functions = listOf("祛风化湿","强健腰膝","通经活络"),
                    indication = "腰腿疼痛、下肢痿痹、半身不遂"),
                Acupoint("GB-34","阳陵泉","Yáng Líng Quán", positionBack = f(0.38f,0.66f), isKey = true,
                    functions = listOf("舒筋活络","清利肝胆","强健腰膝"),
                    indication = "下肢痿痹、膝肿痛、胁肋痛、口苦")
            ),
            description = "胆经从外眼角起，经头侧、颈侧、肩部至胁肋，沿下肢外侧下行至第四趾端。胆主决断。"
        ),

        // 12. LR 肝经
        Meridian(
            id = "LR", name = "足厥阴肝经", shortName = "肝经",
            organ = "肝", element = "木", yinYang = "厥阴",
            limbType = "foot", direction = "centripetal",
            color = 0xFF22c55e,
            pathFront = listOf(
                f(0.42f,0.96f),f(0.42f,0.94f),f(0.43f,0.92f),f(0.43f,0.90f),
                f(0.44f,0.88f),f(0.44f,0.86f),f(0.45f,0.83f),f(0.45f,0.80f),
                f(0.46f,0.77f),f(0.46f,0.74f),f(0.46f,0.71f),f(0.47f,0.68f),
                f(0.47f,0.65f),f(0.47f,0.62f),f(0.47f,0.59f),f(0.48f,0.56f),
                f(0.48f,0.53f),f(0.48f,0.50f),f(0.48f,0.47f),f(0.47f,0.44f),
                f(0.46f,0.40f),f(0.44f,0.36f),f(0.43f,0.32f),f(0.42f,0.28f),
                f(0.41f,0.25f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("LR-3","太冲","Tài Chōng", positionFront = f(0.43f,0.92f), isKey = true,
                    functions = listOf("疏肝理气","平肝熄风","清头目"),
                    indication = "头痛、眩晕、目赤肿痛、口歪、胁痛、月经不调"),
                Acupoint("LR-8","曲泉","Qū Quán", positionFront = f(0.47f,0.65f), isKey = true,
                    functions = listOf("清利湿热","舒筋活络"),
                    indication = "月经不调、带下、遗精、阳痿、膝痛、小便不利"),
                Acupoint("LR-14","期门","Qī Mén", positionFront = f(0.41f,0.25f), isKey = true,
                    functions = listOf("疏肝理气","健脾和胃","活血化瘀"),
                    indication = "胸胁胀痛、呕吐、呃逆、吞酸、腹胀")
            ),
            description = "肝经从大趾背起，沿下肢内侧中间上行至阴部、少腹、胁肋。肝主疏泄、藏血、主筋。"
        ),

        // 13. RN 任脉
        Meridian(
            id = "RN", name = "任脉", shortName = "任脉",
            organ = "任脉", element = "", yinYang = "阴",
            limbType = "trunk", direction = "centripetal",
            color = 0xFFa855f7,
            pathFront = listOf(
                f(0.50f,0.58f),f(0.50f,0.56f),f(0.50f,0.54f),f(0.50f,0.52f),
                f(0.50f,0.50f),f(0.50f,0.48f),f(0.50f,0.46f),f(0.50f,0.44f),
                f(0.50f,0.42f),f(0.50f,0.40f),f(0.50f,0.38f),f(0.50f,0.36f),
                f(0.50f,0.34f),f(0.50f,0.32f),f(0.50f,0.30f),f(0.50f,0.28f),
                f(0.50f,0.26f),f(0.50f,0.24f),f(0.50f,0.22f),f(0.50f,0.20f),
                f(0.50f,0.18f),f(0.50f,0.16f),f(0.50f,0.14f),f(0.50f,0.12f)
            ),
            pathBack = emptyList(),
            acupoints = listOf(
                Acupoint("RN-4","关元","Guān Yuán", positionFront = f(0.50f,0.52f), isKey = true,
                    functions = listOf("培元固本","补益下焦","回阳救逆"),
                    indication = "虚劳赢瘦、中风脱证、遗尿、遗精、月经不调"),
                Acupoint("RN-6","气海","Qì Hǎi", positionFront = f(0.50f,0.50f), isKey = true,
                    functions = listOf("补气益元","温阳固脱"),
                    indication = "虚脱、腹痛、泄泻、遗尿、遗精、月经不调"),
                Acupoint("RN-12","中脘","Zhōng Wǎn", positionFront = f(0.50f,0.40f), isKey = true,
                    functions = listOf("和胃健脾","降逆利水"),
                    indication = "胃痛、腹胀、呕吐、吞酸、泄泻、黄疸"),
                Acupoint("RN-17","膻中","Dàn Zhōng", positionFront = f(0.50f,0.28f), isKey = true,
                    functions = listOf("宽胸理气","降逆止呕","宣肺化痰"),
                    indication = "咳嗽、气喘、胸闷、胸痛、心悸、产妇乳少")
            ),
            description = "任脉起于会阴，沿腹胸正中线上行至下颌。任脉为\"阴脉之海\"，总任一身之阴经。"
        ),

        // 14. DU 督脉
        Meridian(
            id = "DU", name = "督脉", shortName = "督脉",
            organ = "督脉", element = "", yinYang = "阳",
            limbType = "trunk", direction = "centripetal",
            color = 0xFF3b82f6,
            pathFront = listOf(
                f(0.50f,0.04f),f(0.50f,0.05f),f(0.50f,0.06f),f(0.50f,0.07f),
                f(0.50f,0.08f),f(0.50f,0.09f),f(0.50f,0.10f)
            ),
            pathBack = listOf(
                f(0.50f,0.55f),f(0.50f,0.53f),f(0.50f,0.51f),f(0.50f,0.49f),
                f(0.50f,0.47f),f(0.50f,0.45f),f(0.50f,0.43f),f(0.50f,0.41f),
                f(0.50f,0.39f),f(0.50f,0.37f),f(0.50f,0.35f),f(0.50f,0.33f),
                f(0.50f,0.31f),f(0.50f,0.29f),f(0.50f,0.27f),f(0.50f,0.25f),
                f(0.50f,0.23f),f(0.50f,0.21f),f(0.50f,0.19f),f(0.50f,0.17f),
                f(0.50f,0.15f),f(0.50f,0.13f),f(0.50f,0.11f),f(0.50f,0.09f),
                f(0.50f,0.07f),f(0.50f,0.05f),f(0.50f,0.04f)
            ),
            acupoints = listOf(
                Acupoint("DU-4","命门","Mìng Mén", positionBack = f(0.50f,0.37f), isKey = true,
                    functions = listOf("补肾壮阳","强腰膝","固精止带"),
                    indication = "腰脊强痛、遗精、阳痿、带下、月经不调"),
                Acupoint("DU-14","大椎","Dà Zhuī", positionBack = f(0.50f,0.17f), isKey = true,
                    functions = listOf("清热解表","截疟止痫","益气壮阳"),
                    indication = "热病、疟疾、感冒、咳嗽、气喘、项强"),
                Acupoint("DU-20","百会","Bǎi Huì",
                    positionFront = f(0.50f,0.02f), positionBack = f(0.50f,0.02f), isKey = true,
                    functions = listOf("开窍醒脑","升阳固脱","平肝熄风"),
                    indication = "头痛、眩晕、中风不语、脱肛、癫狂、失眠"),
                Acupoint("DU-26","人中","Rén Zhōng", positionFront = f(0.50f,0.10f), isKey = true,
                    functions = listOf("醒神开窍","清热熄风","解痉止痛"),
                    indication = "昏迷、晕厥、中暑、中风、癫狂痫")
            ),
            description = "督脉起于尾骨，沿脊柱正中上行过头顶至上唇。督脉为\"阳脉之海\"，总督一身之阳经。"
        )
    )
}
