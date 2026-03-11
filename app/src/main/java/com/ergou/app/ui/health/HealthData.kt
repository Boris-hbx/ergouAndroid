package com.ergou.app.ui.health

/**
 * Complete health/wellness data ported from Next frontend (health-data.js).
 * Includes 八段锦, 易筋经, 站桩, 经络穴位 with full descriptions,
 * meridian associations, acupoint details, insights, and video URLs.
 */
object HealthData {
    private const val VIDEO_BASE = "https://next-boris.fly.dev/assets/videos"

    val categories = listOf(baduanjin(), yijinjing(), zhanzhung(), meridians())

    // ── 八段锦 ──

    private fun baduanjin() = HealthCategory(
        id = "baduanjin",
        name = "八段锦",
        description = "八段锦是一种传统养生功法，由八个动作组成，简单易学，适合各年龄段。",
        intro = "源自宋代，距今已有800余年历史。八段锦以8个连贯动作疏通全身经络、调和气血，" +
                "动作柔和缓慢，适合各年龄段人群。每日练习一遍约15-20分钟，坚持可增强体质、改善亚健康。" +
                "练习时注意呼吸配合动作，动作宜缓不宜急。",
        items = listOf(
            HealthItem(
                id = "bdj_00", name = "预备式",
                description = "两脚开立，与肩同宽，两臂自然下垂，周身放松，呼吸自然。",
                benefits = "调整呼吸；放松身心；进入练功状态",
                benefitsList = listOf("调整呼吸", "放松身心", "进入练功状态"),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-00-prep.mp4"
            ),
            HealthItem(
                id = "bdj_01", name = "双手托天理三焦",
                description = "双手交叉上托，掌心朝天，全身充分伸展，调理三焦气机。吸气时上托，呼气时还原，动作缓慢均匀。",
                benefits = "疏通三焦经气；拉伸脊柱及两侧肌群；改善肩颈僵硬；调理气血运行",
                meridians = "三焦经、肺经、心包经、督脉",
                keyPoints = "外关(SJ-5)、中府(LU-1)、内关(PC-6)、百会(DU-20)",
                benefitsList = listOf("疏通三焦经气", "拉伸脊柱及两侧肌群", "改善肩颈僵硬", "调理气血运行"),
                meridianDetails = listOf(
                    MeridianDetail("三焦经", "primary", "双手上托直接拉伸三焦经"),
                    MeridianDetail("肺经", "primary", "扩胸展臂刺激肺经"),
                    MeridianDetail("心包经", "primary", "手臂内侧伸展刺激心包经"),
                    MeridianDetail("督脉", "secondary", "脊柱伸展间接刺激督脉")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-01.mp4"
            ),
            HealthItem(
                id = "bdj_02", name = "左右开弓似射雕",
                description = "两臂平展如拉弓射箭，马步站立，左右交替。扩胸展肩，锻炼上肢及胸背肌群，宣通肺气。",
                benefits = "扩展胸廓，宣发肺气；增强上肢及胸背肌力；改善呼吸功能；刺激心肺经络",
                meridians = "肺经、大肠经、心经、心包经",
                keyPoints = "中府(LU-1)、合谷(LI-4)、神门(HT-7)、内关(PC-6)、尺泽(LU-5)",
                benefitsList = listOf("扩展胸廓，宣发肺气", "增强上肢及胸背肌力", "改善呼吸功能", "刺激心肺经络"),
                meridianDetails = listOf(
                    MeridianDetail("肺经", "primary", "扩胸展臂拉伸肺经"),
                    MeridianDetail("大肠经", "primary", "手臂外展刺激大肠经"),
                    MeridianDetail("心经", "primary", "内侧手臂伸展刺激心经"),
                    MeridianDetail("心包经", "secondary", "间接刺激心包经")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-02.mp4"
            ),
            HealthItem(
                id = "bdj_03", name = "调理脾胃须单举",
                description = "一手上撑，一手下按，上下对拉拔伸脊柱，左右交替。牵拉腹腔对脾胃进行按摩，促进消化。",
                benefits = "调理脾胃升降气机；拉伸腹部及体侧肌群；改善消化功能；增强脊柱柔韧性",
                meridians = "脾经、胃经、肝经、胆经",
                keyPoints = "三阴交(SP-6)、足三里(ST-36)、中脘(RN-12)、太冲(LR-3)",
                benefitsList = listOf("调理脾胃升降气机", "拉伸腹部及体侧肌群", "改善消化功能", "增强脊柱柔韧性"),
                meridianDetails = listOf(
                    MeridianDetail("脾经", "primary", "拉伸体侧直接刺激脾经"),
                    MeridianDetail("胃经", "primary", "腹部牵拉刺激胃经"),
                    MeridianDetail("肝经", "secondary", "体侧伸展间接刺激肝经"),
                    MeridianDetail("胆经", "secondary", "侧屈动作间接刺激胆经")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-03.mp4"
            ),
            HealthItem(
                id = "bdj_04", name = "五劳七伤往后瞧",
                description = "头部缓慢向后转动，目视斜后方，左右交替。通过颈部旋转刺激颈椎及背部经络，防治颈椎劳损。",
                benefits = "缓解颈椎疲劳；增强颈部灵活性；刺激背部督脉及膀胱经；改善头部血液循环",
                meridians = "膀胱经、小肠经、督脉、胆经",
                keyPoints = "百会(DU-20)、后溪(SI-3)、风池(GB-20)、大椎(DU-14)",
                benefitsList = listOf("缓解颈椎疲劳", "增强颈部灵活性", "刺激背部督脉及膀胱经", "改善头部血液循环"),
                meridianDetails = listOf(
                    MeridianDetail("膀胱经", "primary", "颈部后转刺激膀胱经"),
                    MeridianDetail("小肠经", "primary", "颈肩旋转刺激小肠经"),
                    MeridianDetail("督脉", "primary", "脊柱微旋刺激督脉"),
                    MeridianDetail("胆经", "secondary", "头部侧转间接刺激胆经")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-04.mp4"
            ),
            HealthItem(
                id = "bdj_05", name = "摇头摆尾去心火",
                description = "马步站立，俯身旋转摇头摆尾，左右交替。通过大幅度的脊柱扭转和头部摆动，泻心火，交心肾。",
                benefits = "清泻心火；交通心肾；增强腰腿力量；改善脊柱柔韧性",
                meridians = "心经、肾经、督脉、膀胱经",
                keyPoints = "神门(HT-7)、涌泉(KI-1)、百会(DU-20)、命门(DU-4)、肾俞(BL-23)",
                benefitsList = listOf("清泻心火", "交通心肾", "增强腰腿力量", "改善脊柱柔韧性"),
                meridianDetails = listOf(
                    MeridianDetail("心经", "primary", "摇头泻心火"),
                    MeridianDetail("肾经", "primary", "摆尾强肾气"),
                    MeridianDetail("督脉", "primary", "脊柱旋转刺激督脉"),
                    MeridianDetail("膀胱经", "secondary", "俯身牵拉间接刺激膀胱经")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-05.mp4"
            ),
            HealthItem(
                id = "bdj_06", name = "两手攀足固肾腰",
                description = "双手沿腰背向下推按至足部，再沿腿后侧上行。前屈后仰交替，强化肾腰功能，疏通足太阳膀胱经。",
                benefits = "强腰固肾；疏通膀胱经；增强腰部柔韧性；改善肾功能",
                meridians = "肾经、膀胱经、督脉",
                keyPoints = "涌泉(KI-1)、肾俞(BL-23)、委中(BL-40)、命门(DU-4)",
                benefitsList = listOf("强腰固肾", "疏通膀胱经", "增强腰部柔韧性", "改善肾功能"),
                meridianDetails = listOf(
                    MeridianDetail("肾经", "primary", "前屈攀足直接刺激肾经"),
                    MeridianDetail("膀胱经", "primary", "背部及腿后侧伸展刺激膀胱经"),
                    MeridianDetail("督脉", "secondary", "脊柱前屈后仰间接刺激督脉")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-06.mp4"
            ),
            HealthItem(
                id = "bdj_07", name = "攒拳怒目增气力",
                description = "马步站立，双拳紧握置于腰侧，左右交替冲拳，怒目圆睁。激发肝气，增强气力。",
                benefits = "疏泄肝气；增强上肢力量；激发全身阳气；改善气血运行",
                meridians = "肝经、胆经、大肠经、胃经",
                keyPoints = "太冲(LR-3)、阳陵泉(GB-34)、合谷(LI-4)、足三里(ST-36)",
                benefitsList = listOf("疏泄肝气", "增强上肢力量", "激发全身阳气", "改善气血运行"),
                meridianDetails = listOf(
                    MeridianDetail("肝经", "primary", "怒目瞪眼直接激发肝气"),
                    MeridianDetail("胆经", "primary", "握拳冲拳刺激胆经"),
                    MeridianDetail("大肠经", "secondary", "冲拳运动间接刺激大肠经"),
                    MeridianDetail("胃经", "secondary", "马步站立间接刺激胃经")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-07.mp4"
            ),
            HealthItem(
                id = "bdj_08", name = "背后七颠百病消",
                description = "双脚并立，提踵颠足，全身放松震动。通过脚跟有节奏的颠振刺激足底涌泉穴及足三阳经，调和全身气血。",
                benefits = "振奋全身阳气；刺激足底涌泉穴；改善全身血液循环；缓解疲劳",
                meridians = "肾经、膀胱经、胃经、督脉、脾经",
                keyPoints = "涌泉(KI-1)、至阴(BL-67)、足三里(ST-36)、百会(DU-20)",
                benefitsList = listOf("振奋全身阳气", "刺激足底涌泉穴", "改善全身血液循环", "缓解疲劳"),
                meridianDetails = listOf(
                    MeridianDetail("肾经", "primary", "颠足刺激足底涌泉穴（肾经起点）"),
                    MeridianDetail("膀胱经", "primary", "提踵刺激膀胱经"),
                    MeridianDetail("胃经", "primary", "足前部着力刺激胃经"),
                    MeridianDetail("督脉", "secondary", "脊柱震动间接刺激督脉"),
                    MeridianDetail("脾经", "secondary", "足部着力间接刺激脾经")
                ),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-08.mp4"
            ),
            HealthItem(
                id = "bdj_09", name = "收势",
                description = "两臂缓慢下落，呼吸自然，全身放松。",
                benefits = "收功养气；恢复平静；巩固练功效果",
                benefitsList = listOf("收功养气", "恢复平静", "巩固练功效果"),
                videoUrl = "$VIDEO_BASE/baduanjin/bdj-09-closing.mp4"
            )
        )
    )

    // ── 易筋经 ──

    private fun yijinjing() = HealthCategory(
        id = "yijinjing",
        name = "易筋经",
        description = "易筋经相传为达摩所创，侧重筋骨锻炼，动作舒展有力。",
        intro = "相传由禅宗初祖达摩所创，是少林寺传统内功功法。易筋经意为「改变筋骨」，" +
                "通过12个动作强化筋骨、疏通气血，侧重肌肉和韧带的拉伸锻炼。适合有一定运动基础的人群，" +
                "动作较八段锦更注重力量感。练习时注意动静结合，刚柔并济。",
        items = listOf(
            HealthItem(
                id = "yjj_01", name = "韦驮献杵第一势",
                description = "双手合十于胸前，宁心静气，调匀呼吸。此为易筋经起势，通过合掌聚气安定心神，贯通心包经与心经。",
                benefits = "宁心安神；调匀呼吸；贯通心包经与心经；聚集内气于膻中",
                meridians = "心包经、心经、任脉",
                keyPoints = "劳宫(PC-8)、神门(HT-7)、膻中(RN-17)",
                benefitsList = listOf("宁心安神", "调匀呼吸", "贯通心包经与心经", "聚集内气于膻中"),
                meridianDetails = listOf(
                    MeridianDetail("心包经", "primary", "合掌聚气刺激心包经"),
                    MeridianDetail("心经", "primary", "手少阴心经通过掌心内侧贯通"),
                    MeridianDetail("任脉", "secondary", "膻中穴位于任脉，合掌间接刺激")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-01.mp4"
            ),
            HealthItem(
                id = "yjj_02", name = "韦驮献杵第二势",
                description = "双臂侧平举，掌心向上，展臂扩胸。充分伸展上肢内外侧经络，宣通肺气，疏理三焦。",
                benefits = "宣通肺气；疏理三焦；增强上肢力量；扩展胸廓",
                meridians = "肺经、大肠经、三焦经",
                keyPoints = "中府(LU-1)、合谷(LI-4)、外关(SJ-5)",
                benefitsList = listOf("宣通肺气", "疏理三焦", "增强上肢力量", "扩展胸廓"),
                meridianDetails = listOf(
                    MeridianDetail("肺经", "primary", "展臂扩胸直接拉伸肺经"),
                    MeridianDetail("大肠经", "primary", "手臂外展刺激大肠经"),
                    MeridianDetail("三焦经", "secondary", "上肢外侧伸展间接刺激三焦经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-02.mp4"
            ),
            HealthItem(
                id = "yjj_03", name = "韦驮献杵第三势",
                description = "双臂上举托天，掌心朝上，全身充分伸展。拉伸三焦经，贯通督脉，振奋阳气。",
                benefits = "拉伸三焦经；贯通督脉；振奋全身阳气；伸展脊柱",
                meridians = "三焦经、督脉、膀胱经、胃经",
                keyPoints = "外关(SJ-5)、百会(DU-20)、大椎(DU-14)、足三里(ST-36)",
                benefitsList = listOf("拉伸三焦经", "贯通督脉", "振奋全身阳气", "伸展脊柱"),
                meridianDetails = listOf(
                    MeridianDetail("三焦经", "primary", "双手上托直接拉伸三焦经"),
                    MeridianDetail("督脉", "primary", "脊柱伸展贯通督脉"),
                    MeridianDetail("膀胱经", "secondary", "背部伸展间接刺激膀胱经"),
                    MeridianDetail("胃经", "secondary", "腹部拉伸间接刺激胃经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-03.mp4"
            ),
            HealthItem(
                id = "yjj_04", name = "摘星换斗势",
                description = "一手上举一手按腰，身体侧弯，左右交替。如摘星辰换北斗，拉伸体侧胆经，强肾固腰。",
                benefits = "拉伸体侧胆经；强肾固腰；增强脊柱侧弯柔韧性；疏泄肝胆之气",
                meridians = "胆经、肾经、肝经、膀胱经",
                keyPoints = "阳陵泉(GB-34)、太溪(KI-3)、肾俞(BL-23)、期门(LR-14)",
                benefitsList = listOf("拉伸体侧胆经", "强肾固腰", "增强脊柱侧弯柔韧性", "疏泄肝胆之气"),
                meridianDetails = listOf(
                    MeridianDetail("胆经", "primary", "侧弯直接拉伸胆经"),
                    MeridianDetail("肾经", "primary", "手按腰部刺激肾经"),
                    MeridianDetail("肝经", "secondary", "体侧伸展间接刺激肝经"),
                    MeridianDetail("膀胱经", "secondary", "腰部动作间接刺激膀胱经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-04.mp4"
            ),
            HealthItem(
                id = "yjj_05", name = "倒拽九牛尾势",
                description = "弓步站立，前手握拳伸出，后手握拳回拽，如倒拽牛尾。左右交替，强化上肢筋骨，疏通大肠经与小肠经。",
                benefits = "强化上肢筋骨；疏通大肠经与小肠经；增强臂力与握力；锻炼腰腿力量",
                meridians = "大肠经、小肠经、肺经、膀胱经",
                keyPoints = "合谷(LI-4)、后溪(SI-3)、尺泽(LU-5)、委中(BL-40)",
                benefitsList = listOf("强化上肢筋骨", "疏通大肠经与小肠经", "增强臂力与握力", "锻炼腰腿力量"),
                meridianDetails = listOf(
                    MeridianDetail("大肠经", "primary", "前拳伸出刺激大肠经"),
                    MeridianDetail("小肠经", "primary", "后拳回拽刺激小肠经"),
                    MeridianDetail("肺经", "secondary", "手臂内侧伸展间接刺激肺经"),
                    MeridianDetail("膀胱经", "secondary", "弓步牵拉间接刺激膀胱经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-05.mp4"
            ),
            HealthItem(
                id = "yjj_06", name = "出爪亮翅势",
                description = "双手前推如鹰爪，指尖用力张开，再收回腋下。反复推出收回，强化手三阳经筋力，如雄鹰亮翅。",
                benefits = "强化手三阳经筋力；增强指力与臂力；疏通大肠经与三焦经；改善上肢气血循环",
                meridians = "大肠经、三焦经、肺经、小肠经",
                keyPoints = "合谷(LI-4)、外关(SJ-5)、列缺(LU-7)、后溪(SI-3)",
                benefitsList = listOf("强化手三阳经筋力", "增强指力与臂力", "疏通大肠经与三焦经", "改善上肢气血循环"),
                meridianDetails = listOf(
                    MeridianDetail("大肠经", "primary", "推掌张指直接刺激大肠经"),
                    MeridianDetail("三焦经", "primary", "手指张开拉伸三焦经"),
                    MeridianDetail("肺经", "secondary", "推掌时内侧间接刺激肺经"),
                    MeridianDetail("小肠经", "secondary", "手指伸张间接刺激小肠经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-06.mp4"
            ),
            HealthItem(
                id = "yjj_07", name = "九鬼拔马刀势",
                description = "一手从头后绕至对侧耳部，另一手从背后上探，扭转脊柱。左右交替，强化肩背筋骨，疏通小肠经与督脉。",
                benefits = "强化肩背筋骨；疏通小肠经与督脉；增强脊柱旋转灵活性；改善肩关节活动度",
                meridians = "小肠经、督脉、膀胱经、三焦经",
                keyPoints = "小海(SI-9)、大椎(DU-14)、大杼(BL-11)、臑会(SJ-14)",
                benefitsList = listOf("强化肩背筋骨", "疏通小肠经与督脉", "增强脊柱旋转灵活性", "改善肩关节活动度"),
                meridianDetails = listOf(
                    MeridianDetail("小肠经", "primary", "手臂后绕直接拉伸小肠经"),
                    MeridianDetail("督脉", "primary", "脊柱扭转刺激督脉"),
                    MeridianDetail("膀胱经", "secondary", "背部扭转间接刺激膀胱经"),
                    MeridianDetail("三焦经", "secondary", "上臂外旋间接刺激三焦经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-07.mp4"
            ),
            HealthItem(
                id = "yjj_08", name = "三盘落地势",
                description = "宽步站立，深蹲马步，双手下按至膝侧。上中下三盘同时下沉，强化脾胃肾三脏，增强下肢力量。",
                benefits = "强化脾胃肾三脏；增强下肢力量；培补下元之气；改善消化与泌尿功能",
                meridians = "脾经、胃经、肾经、肝经、任脉",
                keyPoints = "三阴交(SP-6)、足三里(ST-36)、涌泉(KI-1)、气海(RN-6)",
                benefitsList = listOf("强化脾胃肾三脏", "增强下肢力量", "培补下元之气", "改善消化与泌尿功能"),
                meridianDetails = listOf(
                    MeridianDetail("脾经", "primary", "深蹲牵拉脾经"),
                    MeridianDetail("胃经", "primary", "马步站立刺激胃经"),
                    MeridianDetail("肾经", "primary", "下蹲蓄气强化肾经"),
                    MeridianDetail("肝经", "secondary", "大腿内侧伸展间接刺激肝经"),
                    MeridianDetail("任脉", "secondary", "下丹田蓄气间接刺激任脉")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-08.mp4"
            ),
            HealthItem(
                id = "yjj_09", name = "青龙探爪势",
                description = "一手从腰侧经胸前向对侧伸出，同时转腰，如青龙探爪。左右交替，疏通肝胆经络，活络腰部。",
                benefits = "疏通肝胆经络；活络腰部；增强腰部旋转灵活性；促进气血运行",
                meridians = "肝经、胆经、脾经、心包经",
                keyPoints = "太冲(LR-3)、阳陵泉(GB-34)、三阴交(SP-6)、内关(PC-6)",
                benefitsList = listOf("疏通肝胆经络", "活络腰部", "增强腰部旋转灵活性", "促进气血运行"),
                meridianDetails = listOf(
                    MeridianDetail("肝经", "primary", "转腰探爪直接刺激肝经"),
                    MeridianDetail("胆经", "primary", "体侧旋转拉伸胆经"),
                    MeridianDetail("脾经", "secondary", "腰腹旋转间接刺激脾经"),
                    MeridianDetail("心包经", "secondary", "手臂前伸间接刺激心包经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-09.mp4"
            ),
            HealthItem(
                id = "yjj_10", name = "卧虎扑食势",
                description = "弓步前扑，双手撑地，身体前倾如虎扑食。再弓背上拱，锻炼督脉与膀胱经，强化腰背力量。",
                benefits = "强化腰背力量；锻炼督脉与膀胱经；增强上肢支撑力；改善脊柱柔韧性",
                meridians = "督脉、膀胱经、胃经、大肠经",
                keyPoints = "命门(DU-4)、肾俞(BL-23)、足三里(ST-36)、曲池(LI-11)",
                benefitsList = listOf("强化腰背力量", "锻炼督脉与膀胱经", "增强上肢支撑力", "改善脊柱柔韧性"),
                meridianDetails = listOf(
                    MeridianDetail("督脉", "primary", "弓背拱腰直接刺激督脉"),
                    MeridianDetail("膀胱经", "primary", "背部伸展拉伸膀胱经"),
                    MeridianDetail("胃经", "secondary", "弓步前扑间接刺激胃经"),
                    MeridianDetail("大肠经", "secondary", "双手撑地间接刺激大肠经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-10.mp4"
            ),
            HealthItem(
                id = "yjj_11", name = "打躬势",
                description = "双手抱头，弯腰深深前屈如打躬。拉伸整条膀胱经与督脉，刺激头顶百会穴及足底涌泉穴。",
                benefits = "拉伸膀胱经全程；刺激督脉；增强腰部与腿部后侧柔韧性；改善脑部血液循环",
                meridians = "膀胱经、督脉、肾经、胆经",
                keyPoints = "委中(BL-40)、百会(DU-20)、涌泉(KI-1)、风池(GB-20)",
                benefitsList = listOf("拉伸膀胱经全程", "刺激督脉", "增强腰部与腿部后侧柔韧性", "改善脑部血液循环"),
                meridianDetails = listOf(
                    MeridianDetail("膀胱经", "primary", "前屈直接拉伸膀胱经全程"),
                    MeridianDetail("督脉", "primary", "弯腰前屈拉伸督脉"),
                    MeridianDetail("肾经", "secondary", "深度前屈间接刺激肾经"),
                    MeridianDetail("胆经", "secondary", "抱头动作间接刺激胆经")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-11.mp4"
            ),
            HealthItem(
                id = "yjj_12", name = "掉尾势",
                description = "双手攀足前屈，起身后微微后仰。前屈时拉伸膀胱经与肾经，后仰时刺激督脉与任脉，为易筋经收势。",
                benefits = "拉伸膀胱经与肾经；疏通督脉与任脉；增强腰部柔韧性；调和全身气血",
                meridians = "膀胱经、肾经、督脉、任脉",
                keyPoints = "委中(BL-40)、涌泉(KI-1)、命门(DU-4)、气海(RN-6)",
                benefitsList = listOf("拉伸膀胱经与肾经", "疏通督脉与任脉", "增强腰部柔韧性", "调和全身气血"),
                meridianDetails = listOf(
                    MeridianDetail("膀胱经", "primary", "前屈攀足拉伸膀胱经"),
                    MeridianDetail("肾经", "primary", "足底用力刺激肾经"),
                    MeridianDetail("督脉", "secondary", "后仰间接刺激督脉"),
                    MeridianDetail("任脉", "secondary", "后仰伸展间接刺激任脉")
                ),
                videoUrl = "$VIDEO_BASE/yijinjing/yjj-12.mp4"
            )
        )
    )

    // ── 站桩 ──

    private fun zhanzhung() = HealthCategory(
        id = "zhanzhung",
        name = "站桩",
        description = "站桩是传统内功基础，通过静立姿势培养内气，强健体质。",
        intro = "站桩是中国传统武术和养生的根基功法，通过保持特定站立姿势来培养内气、锻炼意念。" +
                "初学者建议从5分钟开始，逐步增加到20-30分钟。站桩重在放松和意守，" +
                "膝盖不可超过脚尖，呼吸自然。适合各年龄段，对改善体质、增强下肢力量效果显著。",
        items = listOf(
            HealthItem(
                id = "zz_00", name = "普通站立（基准对照）",
                description = "日常随意站立姿势，无特殊意念或骨骼排列要求。与站桩相比，缺乏\"松沉\"的内在要领——" +
                        "没有百会上领、尾闾内收、含胸拔背等结构对齐，呼吸为浅层胸式呼吸，肌肉紧张分布不均。",
                benefits = "维持基本直立姿态；作为站桩效果的基准对照",
                meridians = "膀胱经、胃经",
                keyPoints = "涌泉(KI-1)",
                benefitsList = listOf("维持基本直立姿态", "作为站桩效果的基准对照"),
                meridianDetails = listOf(
                    MeridianDetail("膀胱经", "secondary", "维持直立姿态，脊旁肌群被动参与"),
                    MeridianDetail("胃经", "secondary", "下肢承重，胃经沿线被动受力")
                ),
                insights = listOf(
                    InsightItem("意念", "无特定意念引导，注意力分散于外界"),
                    InsightItem("骨骼排列", "习惯性姿态，脊柱常有前倾或侧弯代偿"),
                    InsightItem("松沉", "肌肉紧张分布不均，肩颈常不自觉耸起"),
                    InsightItem("呼吸", "浅层胸式呼吸，横膈膜运动幅度小")
                )
            ),
            HealthItem(
                id = "zz_01", name = "混元桩",
                description = "双臂环抱于胸前如抱球，含胸拔背。自然呼吸，渐进为腹式呼吸，意守丹田。",
                benefits = "调和气血，培元固本；增强上肢及肩背耐力；改善呼吸深度与均匀性；安神定志，提升专注力",
                meridians = "任脉、督脉、心包经、肺经",
                keyPoints = "气海(RN-6)、百会(DU-20)、劳宫(PC-8)、关元(RN-4)",
                benefitsList = listOf("调和气血，培元固本", "增强上肢及肩背耐力", "改善呼吸深度与均匀性", "安神定志，提升专注力"),
                meridianDetails = listOf(
                    MeridianDetail("任脉", "primary", "含胸收腹激活任脉"),
                    MeridianDetail("督脉", "primary", "拔背伸脊刺激督脉"),
                    MeridianDetail("心包经", "secondary", "环抱姿势牵拉心包经"),
                    MeridianDetail("肺经", "secondary", "扩胸含胸间接刺激肺经")
                ),
                insights = listOf(
                    InsightItem("意念", "意守丹田（RN-6），引导气沉入下腹"),
                    InsightItem("骨骼排列", "百会上领、尾闾内收、含胸拔背，任督二脉形成完整回路"),
                    InsightItem("松沉", "系统性放松肩、肘、腕，重力下沉至脚底涌泉（KI-1）"),
                    InsightItem("呼吸", "腹式深呼吸，吸气小腹微鼓，呼气自然内收")
                ),
                videoUrl = "$VIDEO_BASE/zhanzhuang/zz-01.mp4"
            ),
            HealthItem(
                id = "zz_02", name = "抱球桩",
                description = "双臂在腹前环抱，掌心向内对脐。腹式呼吸为主，意注小腹。",
                benefits = "培补脾胃元气；温养肾气；促进腹部气血运行；增强下肢稳定性",
                meridians = "任脉、脾经、肾经、胃经",
                keyPoints = "神阙(RN-8)、气海(RN-6)、三阴交(SP-6)、太溪(KI-3)",
                benefitsList = listOf("培补脾胃元气", "温养肾气", "促进腹部气血运行", "增强下肢稳定性"),
                meridianDetails = listOf(
                    MeridianDetail("任脉", "primary", "掌心对脐激活任脉"),
                    MeridianDetail("脾经", "primary", "意注腹部培补脾经"),
                    MeridianDetail("肾经", "secondary", "站桩沉气温养肾经"),
                    MeridianDetail("胃经", "secondary", "腹前环抱间接刺激胃经")
                ),
                insights = listOf(
                    InsightItem("意念", "意注神阙（肚脐），内视下丹田区域"),
                    InsightItem("骨骼排列", "手掌对脐形成\"气场环\"，任脉前侧通道被激活"),
                    InsightItem("松沉", "重心略低于混元桩，气更容易沉入下焦"),
                    InsightItem("呼吸", "深腹式呼吸，呼气时手掌微有内合之意")
                ),
                videoUrl = "$VIDEO_BASE/zhanzhuang/zz-02.mp4"
            ),
            HealthItem(
                id = "zz_03", name = "降气桩",
                description = "双手掌心向下置于腹前如按浮球。吸气时意守丹田，呼气时意念引气沿腿内侧降至涌泉。",
                benefits = "引气下行，降虚火；增强肾气与下肢力量；促进消化吸收；安定心神，缓解焦虑",
                meridians = "肾经、胃经、任脉、膀胱经",
                keyPoints = "涌泉(KI-1)、足三里(ST-36)、气海(RN-6)、肾俞(BL-23)",
                benefitsList = listOf("引气下行，降虚火", "增强肾气与下肢力量", "促进消化吸收", "安定心神，缓解焦虑"),
                meridianDetails = listOf(
                    MeridianDetail("肾经", "primary", "引气至涌泉刺激肾经"),
                    MeridianDetail("胃经", "primary", "掌按腹前刺激胃经"),
                    MeridianDetail("任脉", "secondary", "意守丹田间接激活任脉"),
                    MeridianDetail("膀胱经", "secondary", "气沉下行间接刺激膀胱经")
                ),
                insights = listOf(
                    InsightItem("意念", "呼气时意念引气从丹田沿腿内侧降至涌泉（KI-1）"),
                    InsightItem("骨骼排列", "掌心向下对地，形成向下的引导力线"),
                    InsightItem("松沉", "全身有意识地\"放下\"，特别适合虚火上浮者"),
                    InsightItem("呼吸", "呼气略长于吸气，助气下行")
                ),
                videoUrl = "$VIDEO_BASE/zhanzhuang/zz-03.mp4"
            ),
            HealthItem(
                id = "zz_04", name = "扶按桩",
                description = "双手在身体两侧微微下按如按桌面。自然呼吸，呼气时加强下按意念。",
                benefits = "疏通手三阳经；增强肩臂力量；调理气机升降；改善肩背僵硬",
                meridians = "大肠经、三焦经、小肠经、督脉",
                keyPoints = "曲池(LI-11)、外关(SJ-5)、后溪(SI-3)、大椎(DU-14)",
                benefitsList = listOf("疏通手三阳经", "增强肩臂力量", "调理气机升降", "改善肩背僵硬"),
                meridianDetails = listOf(
                    MeridianDetail("大肠经", "primary", "手臂外展下按刺激大肠经"),
                    MeridianDetail("三焦经", "primary", "手臂侧张刺激三焦经"),
                    MeridianDetail("小肠经", "secondary", "肩背张力间接刺激小肠经"),
                    MeridianDetail("督脉", "secondary", "挺脊立身间接刺激督脉")
                ),
                insights = listOf(
                    InsightItem("意念", "呼气时加强双手下按之意，如按水中浮球"),
                    InsightItem("骨骼排列", "双臂外展打开腋下，疏通手三阳经走行路线"),
                    InsightItem("松沉", "肩松坠肘，力达掌根而非指尖"),
                    InsightItem("呼吸", "自然呼吸，呼气配合下按动作的意念")
                ),
                videoUrl = "$VIDEO_BASE/zhanzhuang/zz-04.mp4"
            ),
            HealthItem(
                id = "zz_05", name = "提抱桩",
                description = "双臂上提至胸前高度，掌心向上如托物。自然呼吸，吸气时微有上提之意。",
                benefits = "提升脾胃清气；宣发肺气；增强上肢及胸部肌力；改善中气不足",
                meridians = "脾经、肺经、胃经、心包经",
                keyPoints = "三阴交(SP-6)、中府(LU-1)、足三里(ST-36)、内关(PC-6)",
                benefitsList = listOf("提升脾胃清气", "宣发肺气", "增强上肢及胸部肌力", "改善中气不足"),
                meridianDetails = listOf(
                    MeridianDetail("脾经", "primary", "上提之意培补脾经升清之力"),
                    MeridianDetail("肺经", "primary", "展胸托掌宣发肺经"),
                    MeridianDetail("胃经", "secondary", "间接刺激胃经促进运化"),
                    MeridianDetail("心包经", "secondary", "掌心朝上间接刺激心包经")
                ),
                insights = listOf(
                    InsightItem("意念", "吸气时微有上提之意，如双掌托起轻盈之物"),
                    InsightItem("骨骼排列", "掌心朝天，劳宫穴（PC-8）对天，接引清气"),
                    InsightItem("松沉", "上提而不耸肩，沉肩坠肘与上托形成矛盾统一"),
                    InsightItem("呼吸", "吸气稍深配合上提意念，呼气自然放松")
                ),
                videoUrl = "$VIDEO_BASE/zhanzhuang/zz-05.mp4"
            ),
            HealthItem(
                id = "zz_06", name = "马步桩",
                description = "两脚宽开深蹲，大腿接近水平。自然呼吸，意守丹田，忌憋气。",
                benefits = "强化下肢肌力与耐力；增强膝关节稳定性；壮腰固肾；培养根基之力",
                meridians = "胃经、脾经、胆经、肾经、膀胱经",
                keyPoints = "足三里(ST-36)、血海(SP-10)、阳陵泉(GB-34)、太溪(KI-3)、委中(BL-40)",
                benefitsList = listOf("强化下肢肌力与耐力", "增强膝关节稳定性", "壮腰固肾", "培养根基之力"),
                meridianDetails = listOf(
                    MeridianDetail("胃经", "primary", "深蹲刺激大腿前侧胃经"),
                    MeridianDetail("脾经", "primary", "大腿内侧受力刺激脾经"),
                    MeridianDetail("胆经", "primary", "宽步站立拉伸胆经"),
                    MeridianDetail("肾经", "secondary", "沉腰坐胯间接温养肾经"),
                    MeridianDetail("膀胱经", "secondary", "腰背挺直间接刺激膀胱经")
                ),
                insights = listOf(
                    InsightItem("意念", "意守丹田，忌憋气——呼吸自然不与蹲姿对抗"),
                    InsightItem("骨骼排列", "宽步深蹲，大腿近水平，膝不超脚尖。对下肢三阴三阳经施加最强刺激"),
                    InsightItem("松沉", "腰胯放松下坐，非肌肉硬撑——\"坐胯\"而非\"蹲腿\""),
                    InsightItem("呼吸", "自然呼吸为主，切忌在深蹲时闭气")
                ),
                videoUrl = "$VIDEO_BASE/zhanzhuang/zz-06.mp4"
            ),
            HealthItem(
                id = "zz_07", name = "无极桩",
                description = "双手自然下垂，最简朴的站桩。自然呼吸，不加任何意念，体悟无极之境。",
                benefits = "回归自然，放松身心；沟通任督二脉；培养整体气感；适合初学者入门",
                meridians = "督脉、任脉、膀胱经、肾经",
                keyPoints = "百会(DU-20)、气海(RN-6)、涌泉(KI-1)、命门(DU-4)",
                benefitsList = listOf("回归自然，放松身心", "沟通任督二脉", "培养整体气感", "适合初学者入门"),
                meridianDetails = listOf(
                    MeridianDetail("督脉", "primary", "自然站立挺脊激活督脉"),
                    MeridianDetail("任脉", "primary", "松腹自然呼吸激活任脉"),
                    MeridianDetail("膀胱经", "secondary", "脊柱两侧放松间接刺激膀胱经"),
                    MeridianDetail("肾经", "secondary", "足踏实地间接温养肾经")
                ),
                insights = listOf(
                    InsightItem("意念", "不加特定意念，体悟\"无极\"——但这种\"无念\"本身就是高级的内观状态"),
                    InsightItem("骨骼排列", "外形近似普通站立，但百会上领、下颌微收、尾闾内敛，脊柱如线贯珠"),
                    InsightItem("松沉", "全身系统性放松，逐节检查：从头顶松到脚底，与普通站立的\"散\"截然不同"),
                    InsightItem("呼吸", "自然呼吸不加干预，但因身体结构对齐，呼吸自然变深变匀"),
                    InsightItem("与普通站立的本质区别", "普通站立是无意识的习惯姿态；无极桩是有意识地\"归于无极\"——用最少的肌肉维持结构，让气血自然流通。两者外形差异仅在毫厘之间，对经络的激活程度却有天壤之别")
                ),
                videoUrl = "$VIDEO_BASE/zhanzhuang/zz-07.mp4"
            )
        )
    )

    // ── 经络穴位 ──

    private fun meridians() = HealthCategory(
        id = "meridians",
        name = "经络穴位",
        description = "人体有十二正经和奇经八脉，了解经络走向有助于养生保健。",
        intro = "中医认为人体有十二正经和奇经八脉，经络是气血运行的通道。了解常用经络和穴位，" +
                "可以通过按摩、艾灸等方式进行日常保健。每个穴位按压以酸胀感为度，每次3-5分钟，" +
                "力度适中。孕妇、皮肤破损处、肿瘤部位禁止按摩。",
        items = listOf(
            HealthItem(
                id = "mer_LU", name = "手太阴肺经",
                description = "肺经起于中焦，下络大肠，上行穿膈属肺，循上肢内侧前缘至拇指端。主治咳嗽、气喘、胸闷、咽喉肿痛等肺系病证。",
                benefits = "宣肺理气；止咳平喘；清热利咽；通调水道",
                meridians = "肺经（手太阴）",
                keyPoints = "中府(LU-1)：宣肺理气、止咳平喘；尺泽(LU-5)：清肺泻火、降逆止咳；" +
                        "列缺(LU-7)：宣肺解表、通经活络；太渊(LU-9)：补肺益气、止咳化痰；少商(LU-11)：清热利咽、开窍醒神",
                benefitsList = listOf("宣肺理气", "止咳平喘", "清热利咽", "通调水道"),
                meridianDetails = listOf(MeridianDetail("肺经", "primary", "五行属金，阴经，手太阴"))
            ),
            HealthItem(
                id = "mer_LI", name = "手阳明大肠经",
                description = "大肠经起于食指末端，经手背沿前臂外侧上行至肩、颈，止于对侧鼻翼旁。主治头面五官疾患、热病及上肢外侧前缘痛。",
                benefits = "疏风解表；通络镇痛；清泄肺气；调理肠胃",
                meridians = "大肠经（手阳明）",
                keyPoints = "合谷(LI-4)：疏风解表、通络镇痛（止痛要穴）；曲池(LI-11)：清热解表、调和气血；迎香(LI-20)：祛风通窍、理气止痛",
                benefitsList = listOf("疏风解表", "通络镇痛", "清泄肺气", "调理肠胃"),
                meridianDetails = listOf(MeridianDetail("大肠经", "primary", "五行属金，阳经，手阳明"))
            ),
            HealthItem(
                id = "mer_ST", name = "足阳明胃经",
                description = "胃经从眼下起，沿面颊、颈前、胸腹下行至下肢前侧，止于第二趾端。为多气多血之经，主治胃肠病、头面口齿病。",
                benefits = "健脾和胃；调肠理气；通经活络；扶正培元",
                meridians = "胃经（足阳明）",
                keyPoints = "天枢(ST-25)：调肠胃、理气消食；足三里(ST-36)：健脾和胃、扶正培元（保健要穴）；丰隆(ST-40)：健脾化痰、和胃降逆",
                benefitsList = listOf("健脾和胃", "调肠理气", "通经活络", "扶正培元"),
                meridianDetails = listOf(MeridianDetail("胃经", "primary", "五行属土，阳经，足阳明，多气多血"))
            ),
            HealthItem(
                id = "mer_SP", name = "足太阴脾经",
                description = "脾经从大趾内侧起，沿下肢内侧上行至腹、胸。主治脾胃病、妇科病。脾为后天之本，气血生化之源。",
                benefits = "健脾利湿；调补肝肾；行气活血；统摄血液",
                meridians = "脾经（足太阴）",
                keyPoints = "三阴交(SP-6)：健脾利湿、调补肝肾（妇科要穴）；阴陵泉(SP-9)：健脾利湿、通利小便；血海(SP-10)：活血化瘀、调经统血",
                benefitsList = listOf("健脾利湿", "调补肝肾", "行气活血", "统摄血液"),
                meridianDetails = listOf(MeridianDetail("脾经", "primary", "五行属土，阴经，足太阴，后天之本"))
            ),
            HealthItem(
                id = "mer_HT", name = "手少阴心经",
                description = "心经从心系起，出于腋下，沿上臂内侧后缘下行至小指端。主治心、胸、神志病。心为君主之官。",
                benefits = "宁心安神；理气通络；清心泻热；开窍醒神",
                meridians = "心经（手少阴）",
                keyPoints = "少海(HT-3)：理气通络、益心安神；神门(HT-7)：宁心安神、通经活络（安神要穴）；少冲(HT-9)：清热熄风、醒神开窍",
                benefitsList = listOf("宁心安神", "理气通络", "清心泻热", "开窍醒神"),
                meridianDetails = listOf(MeridianDetail("心经", "primary", "五行属火，阴经，手少阴，君主之官"))
            ),
            HealthItem(
                id = "mer_SI", name = "手太阳小肠经",
                description = "小肠经从小指尺侧起，经手背、前臂外侧上行至肩后、颈侧，止于耳前。主治头面五官病、热病、神志病。",
                benefits = "清心安神；通经活络；聪耳开窍；舒筋利节",
                meridians = "小肠经（手太阳）",
                keyPoints = "后溪(SI-3)：清心安神、通督脉（八脉交会穴）；听宫(SI-19)：聪耳开窍、宁神定志",
                benefitsList = listOf("清心安神", "通经活络", "聪耳开窍", "舒筋利节"),
                meridianDetails = listOf(MeridianDetail("小肠经", "primary", "五行属火，阳经，手太阳"))
            ),
            HealthItem(
                id = "mer_BL", name = "足太阳膀胱经",
                description = "膀胱经为人体最长经脉，从内眼角起经头顶至颈后，沿脊柱两旁下行至足小趾。背部俞穴与五脏六腑对应，是调理全身的重要经脉。",
                benefits = "主一身之表；调节水液代谢；舒筋通络；散风清热",
                meridians = "膀胱经（足太阳）",
                keyPoints = "攒竹(BL-2)：清热明目；肾俞(BL-23)：补肾益气、强腰壮骨；" +
                        "委中(BL-40)：舒筋通络、散瘀活血（腰背要穴）；昆仑(BL-60)：舒筋活络、强腰膝；至阴(BL-67)：正胎催产、清头明目",
                benefitsList = listOf("主一身之表", "调节水液代谢", "舒筋通络", "散风清热"),
                meridianDetails = listOf(MeridianDetail("膀胱经", "primary", "五行属水，阳经，足太阳，人体最长经脉"))
            ),
            HealthItem(
                id = "mer_KI", name = "足少阴肾经",
                description = "肾经从足底起，沿下肢内侧后缘上行至腹、胸。肾为先天之本，主藏精、主水、主纳气。",
                benefits = "补肾益阴；壮阳强腰；滋阴降火；镇静安神",
                meridians = "肾经（足少阴）",
                keyPoints = "涌泉(KI-1)：苏厥开窍、滋阴降火（急救要穴）；太溪(KI-3)：补肾益阴、壮阳强腰（补肾要穴）；照海(KI-6)：滋阴清热、调经止带",
                benefitsList = listOf("补肾益阴", "壮阳强腰", "滋阴降火", "镇静安神"),
                meridianDetails = listOf(MeridianDetail("肾经", "primary", "五行属水，阴经，足少阴，先天之本"))
            ),
            HealthItem(
                id = "mer_PC", name = "手厥阴心包经",
                description = "心包经从胸中起，沿上臂内侧中线下行至中指端。心包代心受邪，主治心胸病、胃病、神志病。",
                benefits = "宁心安神；理气止痛；和胃降逆；清心泻热",
                meridians = "心包经（手厥阴）",
                keyPoints = "内关(PC-6)：宁心安神、理气止痛（止呕要穴）；劳宫(PC-8)：清心泻热、开窍醒神；中冲(PC-9)：清心泻热、开窍醒神",
                benefitsList = listOf("宁心安神", "理气止痛", "和胃降逆", "清心泻热"),
                meridianDetails = listOf(MeridianDetail("心包经", "primary", "五行属火，阴经，手厥阴，代心受邪"))
            ),
            HealthItem(
                id = "mer_SJ", name = "手少阳三焦经",
                description = "三焦经从无名指端起，经手背、前臂背面上行至肩、颈后、耳后，止于眉梢。三焦主通调水道。",
                benefits = "清热解表；通经活络；疏散风热；聪耳通窍",
                meridians = "三焦经（手少阳）",
                keyPoints = "外关(SJ-5)：清热解表、通经活络；翳风(SJ-17)：聪耳通窍、散风泻热；丝竹空(SJ-23)：清头明目、散骨镇惊",
                benefitsList = listOf("清热解表", "通经活络", "疏散风热", "聪耳通窍"),
                meridianDetails = listOf(MeridianDetail("三焦经", "primary", "五行属火，阳经，手少阳，主通调水道"))
            ),
            HealthItem(
                id = "mer_GB", name = "足少阳胆经",
                description = "胆经从外眼角起，经头侧、颈侧、肩部至胁肋，沿下肢外侧下行至第四趾端。胆主决断。",
                benefits = "疏风清热；明目益聪；祛风活络；强健腰膝",
                meridians = "胆经（足少阳）",
                keyPoints = "风池(GB-20)：疏风清热、明目益聪（治头痛要穴）；肩井(GB-21)：祛风活络、消肿散结；" +
                        "环跳(GB-30)：祛风化湿、强健腰膝；阳陵泉(GB-34)：舒筋活络、清利肝胆（筋会）",
                benefitsList = listOf("疏风清热", "明目益聪", "祛风活络", "强健腰膝"),
                meridianDetails = listOf(MeridianDetail("胆经", "primary", "五行属木，阳经，足少阳，主决断"))
            ),
            HealthItem(
                id = "mer_LR", name = "足厥阴肝经",
                description = "肝经从大趾背起，沿下肢内侧中间上行至阴部、少腹、胁肋。肝主疏泄、藏血、主筋。",
                benefits = "疏肝理气；平肝熄风；清头明目；活血化瘀",
                meridians = "肝经（足厥阴）",
                keyPoints = "太冲(LR-3)：疏肝理气、平肝熄风（疏肝要穴）；曲泉(LR-8)：清利湿热、舒筋活络；期门(LR-14)：疏肝理气、健脾和胃",
                benefitsList = listOf("疏肝理气", "平肝熄风", "清头明目", "活血化瘀"),
                meridianDetails = listOf(MeridianDetail("肝经", "primary", "五行属木，阴经，足厥阴，主疏泄藏血"))
            ),
            HealthItem(
                id = "mer_RN", name = "任脉",
                description = "任脉起于会阴，沿腹胸正中线上行至下颌。任脉为\"阴脉之海\"，总任一身之阴经。",
                benefits = "培元固本；补益下焦；和胃健脾；宽胸理气",
                meridians = "任脉（奇经八脉）",
                keyPoints = "关元(RN-4)：培元固本、补益下焦（强壮要穴）；气海(RN-6)：补气益元、温阳固脱；" +
                        "中脘(RN-12)：和胃健脾、降逆利水（胃之募穴）；膻中(RN-17)：宽胸理气、降逆止呕（气会）",
                benefitsList = listOf("培元固本", "补益下焦", "和胃健脾", "宽胸理气"),
                meridianDetails = listOf(MeridianDetail("任脉", "primary", "奇经八脉，阴脉之海，总任一身之阴经"))
            ),
            HealthItem(
                id = "mer_DU", name = "督脉",
                description = "督脉起于尾骨，沿脊柱正中上行过头顶至上唇。督脉为\"阳脉之海\"，总督一身之阳经。",
                benefits = "补肾壮阳；清热解表；开窍醒脑；升阳固脱",
                meridians = "督脉（奇经八脉）",
                keyPoints = "命门(DU-4)：补肾壮阳、强腰膝（生命之门）；大椎(DU-14)：清热解表、益气壮阳（诸阳之会）；" +
                        "百会(DU-20)：开窍醒脑、升阳固脱（百脉之会）；人中(DU-26)：醒神开窍、清热熄风（急救要穴）",
                benefitsList = listOf("补肾壮阳", "清热解表", "开窍醒脑", "升阳固脱"),
                meridianDetails = listOf(MeridianDetail("督脉", "primary", "奇经八脉，阳脉之海，总督一身之阳经"))
            )
        )
    )
}
