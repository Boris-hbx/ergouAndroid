package com.ergou.app.data.repository

import com.ergou.app.data.remote.api.NextApiService
import com.ergou.app.util.NextAuthProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Calendar

/**
 * 建议话题引擎 — 三个固定位置，各有使命
 * 位置1: 长生（每天必出，时段相关的健康行动）
 * 位置2: 盲点（数据驱动，从记忆/routine中挖掘遗忘事项）
 * 位置3: 一问（成长叩问，按天轮转6个维度）
 *
 * 纯规则引擎，零 LLM 调用，瞬间返回。
 */
class SuggestionEngine(
    private val memoryRepository: MemoryRepository,
    private val nextApiService: NextApiService,
    private val nextAuthProvider: NextAuthProvider
) {

    suspend fun generate(): List<String> = coroutineScope {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val dayOfYear = now.get(Calendar.DAY_OF_YEAR)

        val healthDeferred = async { generateHealth(hour, dayOfYear) }
        val blindSpotDeferred = async { generateBlindSpot(dayOfYear) }
        val questionDeferred = async { generateQuestion(dayOfYear) }

        val result = listOf(
            healthDeferred.await(),
            blindSpotDeferred.await(),
            questionDeferred.await()
        )
        Timber.d("[Suggestion] 生成建议话题 hour=%d day=%d", hour, dayOfYear)
        result
    }

    // ── 位置1: 长生 ──

    private fun generateHealth(hour: Int, dayOfYear: Int): String {
        val pool = when {
            hour in 5..9 -> MORNING_HEALTH
            hour in 10..13 -> MIDDAY_HEALTH
            hour in 14..17 -> AFTERNOON_HEALTH
            hour in 18..21 -> EVENING_HEALTH
            else -> NIGHT_HEALTH
        }
        return pool[dayOfYear % pool.size]
    }

    // ── 位置2: 盲点 ──

    private suspend fun generateBlindSpot(dayOfYear: Int): String {
        try {
            // 优先级1: 查找人物事件（生日、纪念日关键词）
            val people = memoryRepository.searchMemories("生日")
            if (people.isNotEmpty()) {
                val m = people.first()
                return "有条记忆提到：${m.content.take(30)}，要不要看看？"
            }

            // 优先级2: 高权重记忆中的定期事项
            val importantMemories = memoryRepository.recallMemories(10)
            val periodicKeywords = listOf("体检", "续费", "过期", "到期", "年检", "保险")
            val periodicMemory = importantMemories.firstOrNull { m ->
                periodicKeywords.any { kw -> kw in m.content }
            }
            if (periodicMemory != null) {
                return "之前记过：${periodicMemory.content.take(30)}，记得处理"
            }

            // 优先级3: 久未提及的重要人物
            val allPeople = memoryRepository.getAllPeople().first()
            val now = System.currentTimeMillis()
            val thirtyDays = 30L * 24 * 60 * 60 * 1000
            val lostContact = allPeople.firstOrNull { p ->
                p.updatedAt > 0 && (now - p.updatedAt) > thirtyDays
            }
            if (lostContact != null) {
                return "好久没聊到${lostContact.name}了，要不问问近况？"
            }

            // 优先级4: 今日未完成的 routine（需已登录）
            val isLoggedIn = nextAuthProvider.isLoggedIn.first()
            if (isLoggedIn) {
                val routines = nextApiService.getRoutines().getOrNull() ?: emptyList()
                val unchecked = routines.firstOrNull { !it.completedToday }
                if (unchecked != null) {
                    return "今天「${unchecked.text}」还没打卡"
                }
            }
        } catch (e: Exception) {
            Timber.d("[Suggestion] 盲点查询异常: %s", e.message)
        }

        // 优先级5: 兜底盲点池
        return FALLBACK_BLIND_SPOTS[dayOfYear % FALLBACK_BLIND_SPOTS.size]
    }

    // ── 位置3: 一问 ──

    private fun generateQuestion(dayOfYear: Int): String {
        val faceIndex = dayOfYear % 6
        val pool = QUESTION_FACES[faceIndex]
        val questionIndex = (dayOfYear / 6) % pool.size
        return pool[questionIndex]
    }

    companion object {
        // ── 长生池 ──

        private val MORNING_HEALTH = listOf(
            "晨起站桩五分钟，胜过补药一大碗",
            "起床后先喝一杯温水，唤醒身体",
            "做一组八段锦，精神一整天",
            "早起拉伸十分钟，筋长一寸寿延十年",
            "出门前深呼吸三次，带着氧气上路",
            "今天试试走路上班/上学？",
            "早餐别忘了，空腹伤胃"
        )

        private val MIDDAY_HEALTH = listOf(
            "午饭吃到七分饱，下午不犯困",
            "饭后别立刻坐下，站着走几分钟",
            "中午有条件的话，闭眼休息15分钟",
            "喝杯水，上午可能忘了补水",
            "检查一下坐姿，肩膀放松、背挺直"
        )

        private val AFTERNOON_HEALTH = listOf(
            "午后困倦是身体要你闭眼五分钟",
            "站起来走走，久坐是慢性杀手",
            "眼睛累了吧？望远处20秒",
            "下午茶时间，来杯绿茶代替奶茶",
            "做几个颈椎操，低头族自救指南"
        )

        private val EVENING_HEALTH = listOf(
            "晚饭少吃点，胃也需要休息",
            "饭后散步半小时，消食又减压",
            "今晚泡个脚，解一天的乏",
            "睡前做五分钟拉伸，睡眠质量翻倍",
            "晚上少看手机，蓝光影响褪黑素"
        )

        private val NIGHT_HEALTH = listOf(
            "子时不寐，伤肝损目。放下手机吧",
            "深夜了，给自己一个入睡倒计时",
            "现在睡觉，明天精力值拉满",
            "熬夜一时爽，免疫力两行泪",
            "做三次深呼吸，然后闭眼"
        )

        // ── 兜底盲点池 ──

        private val FALLBACK_BLIND_SPOTS = listOf(
            "冰箱里有没有该扔的东西？",
            "护照什么时候到期？",
            "手机存储满了吗？该清理了",
            "有没有该回但忘了回的消息？",
            "订阅服务检查一下，有没有不用的？",
            "上次体检是什么时候？",
            "家里的滤芯/耗材该换了吗？",
            "有没有想学但一直没开始的东西？"
        )

        // ── 一问 · 六面 ──

        private val QUESTION_FACES = listOf(
            // 面0: 心
            listOf(
                "这周最想感谢谁？",
                "有没有什么话一直想说但没说出口？",
                "最近什么事让你感到平静？",
                "如果今天只剩一件事能做，你选什么？"
            ),
            // 面1: 智
            listOf(
                "最近有什么改变了你想法的事？",
                "如果重新选一次，你还会这样做吗？",
                "上次认真读完一本书是什么时候？",
                "有没有一个你以前深信不疑、现在动摇的观点？"
            ),
            // 面2: 事
            listOf(
                "这个月最重要的一件事是什么？做了吗？",
                "手头最该放下的一件事是什么？",
                "有没有一直拖着没开始的事？",
                "下周一定要完成的一件事是？"
            ),
            // 面3: 财
            listOf(
                "上个月花的钱，哪笔最值？哪笔最不值？",
                "如果给自己发一笔奖金，你想花在哪？",
                "有没有花钱买的东西其实根本没用过？",
                "下个月在哪里可以省一点？"
            ),
            // 面4: 趣
            listOf(
                "上次纯粹为了开心做的事是什么时候？",
                "最近有没有让你笑出声的事？",
                "如果这个周末什么都不用干，你想做什么？",
                "你有没有一个很久没碰的爱好？"
            ),
            // 面5: 人
            listOf(
                "如果今天只能见一个人，你会选谁？",
                "有没有一个人你一直想联系但没联系？",
                "最近谁给了你能量？",
                "你身边谁最需要一句鼓励？"
            )
        )
    }
}
