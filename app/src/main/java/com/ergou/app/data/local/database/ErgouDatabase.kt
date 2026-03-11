package com.ergou.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ergou.app.data.local.dao.MemoryDao
import com.ergou.app.data.local.dao.MessageDao
import com.ergou.app.data.local.dao.PersonDao
import com.ergou.app.data.local.dao.ReminderDao
import com.ergou.app.data.local.dao.SessionDao
import com.ergou.app.data.local.dao.SoulDao
import com.ergou.app.data.local.entity.MemoryEntity
import com.ergou.app.data.local.entity.MessageEntity
import com.ergou.app.data.local.entity.PersonEntity
import com.ergou.app.data.local.entity.ReminderEntity
import com.ergou.app.data.local.entity.SessionEntity
import com.ergou.app.data.local.entity.SoulEvolutionLogEntity
import com.ergou.app.data.local.entity.SoulStateEntity

@Database(
    entities = [
        SessionEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        PersonEntity::class,
        ReminderEntity::class,
        SoulStateEntity::class,
        SoulEvolutionLogEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class ErgouDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun personDao(): PersonDao
    abstract fun reminderDao(): ReminderDao
    abstract fun soulDao(): SoulDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS tasks")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS soul_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        classicalRatio REAL NOT NULL DEFAULT 0.9,
                        warmthLevel REAL NOT NULL DEFAULT 0.3,
                        verbosityLevel REAL NOT NULL DEFAULT 0.3,
                        proactivityLevel REAL NOT NULL DEFAULT 0.2,
                        trustLevel REAL NOT NULL DEFAULT 0.1,
                        relationshipStage TEXT NOT NULL DEFAULT 'stranger',
                        totalInteractions INTEGER NOT NULL DEFAULT 0,
                        lastUpdatedAt INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS soul_evolution_log (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        parameterName TEXT NOT NULL DEFAULT '',
                        oldValue REAL NOT NULL DEFAULT 0,
                        newValue REAL NOT NULL DEFAULT 0,
                        reason TEXT NOT NULL DEFAULT '',
                        triggerSessionId INTEGER NOT NULL DEFAULT 0,
                        triggerType TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS routines")
                db.execSQL("DROP TABLE IF EXISTS expenses")
                db.execSQL("DROP TABLE IF EXISTS english_scenarios")
                db.execSQL("DROP TABLE IF EXISTS health_records")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN feedback INTEGER DEFAULT NULL")
            }
        }
    }
}
