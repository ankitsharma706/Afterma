package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.database.daos.*
import com.example.data.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        MoodEntry::class,
        TherapySession::class,
        RecipeEntity::class,
        LearningArticle::class,
        MindfulnessSession::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun therapySessionDao(): TherapySessionDao
    abstract fun recipeDao(): RecipeDao
    abstract fun learningArticleDao(): LearningArticleDao
    abstract fun mindfulnessSessionDao(): MindfulnessSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "afterma_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch {
                // Populate default data using raw SQL
                // Recipes
                db.execSQL(
                    """
                    INSERT INTO recipes (title, category, prepTime, calories, benefits, ingredients, instructions, isFavorite, imageResName)
                    VALUES (
                        'Ayurvedic Restorative Kitchari',
                        'Postpartum Recovery',
                        '25 mins',
                        '320 kcal',
                        'Gentle on the digestive tract, promotes internal tissue healing.',
                        'Split yellow mung dal, Basmati rice, Ghee, Ginger, Turmeric, Cumin, Fennel seeds',
                        'Rinse rice and dal. Sauté spices in ghee, add rice and dal, add water, simmer for 20 minutes until soft.',
                        0,
                        'ic_launcher_foreground'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO recipes (title, category, prepTime, calories, benefits, ingredients, instructions, isFavorite, imageResName)
                    VALUES (
                        'Lactation Boosting Fennel Tea',
                        'Lactation Support',
                        '10 mins',
                        '45 kcal',
                        'Stimulates prolactin production, supports infant digestion via breastmilk.',
                        'Fennel seeds, Fenugreek seeds, Cumin seeds, Water, Honey',
                        'Boil water, add seeds, steep for 10 minutes, strain and serve warm with honey.',
                        0,
                        'ic_launcher_foreground'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO recipes (title, category, prepTime, calories, benefits, ingredients, instructions, isFavorite, imageResName)
                    VALUES (
                        'Calming Ashwagandha Golden Milk',
                        'Sleep & Calm',
                        '10 mins',
                        '150 kcal',
                        'Calms the nervous system, supports adrenal health, improves postpartum sleep.',
                        'Almond milk, Ashwagandha powder, Turmeric, Cinnamon, Black pepper, Maple syrup',
                        'Heat almond milk. Whisk in spices and maple syrup, simmer gently for 5 minutes, drink before bedtime.',
                        0,
                        'ic_launcher_foreground'
                    )
                    """.trimIndent()
                )

                // Learning Articles
                db.execSQL(
                    """
                    INSERT INTO learning_articles (title, category, author, readTime, snippet, content, isSaved, imageResName)
                    VALUES (
                        'Physical Recovery: The First Six Weeks',
                        'Physical Recovery',
                        'Sarah Gallagher, PT',
                        '5 min read',
                        'Understanding pelvic floor healing, uterine involution, and safe movement patterns postpartum.',
                        'During the first six weeks postpartum, your body undergoes massive changes. The uterus shrinks back to its normal size (involution), and pelvic organs shift back. Focus on pelvic floor rest, gentle diaphragmatic breathing, and avoid heavy lifting. Rest is the most active form of recovery right now.',
                        0,
                        'ic_launcher_foreground'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO learning_articles (title, category, author, readTime, snippet, content, isSaved, imageResName)
                    VALUES (
                        'Mental Wellness: Navigating the Baby Blues and PMADs',
                        'Mental Wellness',
                        'Dr. Evelyn Ross',
                        '7 min read',
                        'Distinguishing between the normal hormonal transition of baby blues and postpartum depression/anxiety.',
                        'Hormonal shifts in the first two weeks can cause ''baby blues'' in up to 80% of new mothers. However, if feelings of intense anxiety, sadness, irritability, or intrusive thoughts persist past two weeks, you may be experiencing a Postpartum Mood and Anxiety Disorder (PMAD). These are common, highly treatable, and not your fault. Reach out to a professional or helpline.',
                        0,
                        'ic_launcher_foreground'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO learning_articles (title, category, author, readTime, snippet, content, isSaved, imageResName)
                    VALUES (
                        'Safe Motherhood: When to Seek Immediate Medical Attention',
                        'Safe Motherhood',
                        'Afterma Clinical Team',
                        '4 min read',
                        'Essential warning signs every postpartum mother needs to know for safe recovery.',
                        'While recovery has many discomforts, certain symptoms require immediate medical care. Seek emergency help if you experience: heavy bleeding (soaking a pad in an hour), a fever of 100.4°F or higher, severe headaches or vision changes, sudden swelling in legs/face, thoughts of self-harm, or severe chest pain/shortness of breath. Trust your maternal instincts.',
                        0,
                        'ic_launcher_foreground'
                    )
                    """.trimIndent()
                )

                // Therapy Sessions
                db.execSQL(
                    """
                    INSERT INTO therapy_sessions (providerName, providerTitle, specialty, avatarUrl, appointmentDate, appointmentTime, sessionNotes, status, isBooked, durationMinutes)
                    VALUES (
                        'Dr. Evelyn Ross',
                        'Perinatal Psychotherapist',
                        'Mental Wellness',
                        '',
                        '2026-05-25',
                        '10:00 AM',
                        'A safe space to discuss emotional transitions, birth trauma, and relationship shifts.',
                        'Available',
                        0,
                        50
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO therapy_sessions (providerName, providerTitle, specialty, avatarUrl, appointmentDate, appointmentTime, sessionNotes, status, isBooked, durationMinutes)
                    VALUES (
                        'Maya Patel',
                        'IBCLC Lactation Consultant',
                        'Lactation Support',
                        '',
                        '2026-05-26',
                        '02:00 PM',
                        'Guidance on latching, milk supply optimization, and pumping schedules.',
                        'Available',
                        0,
                        50
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO therapy_sessions (providerName, providerTitle, specialty, avatarUrl, appointmentDate, appointmentTime, sessionNotes, status, isBooked, durationMinutes)
                    VALUES (
                        'Sarah Gallagher',
                        'Womens Health Physiotherapist',
                        'Physical Recovery',
                        '',
                        '2026-05-27',
                        '11:00 AM',
                        'Assessment and guidance for pelvic floor rehabilitation and abdominal wall healing.',
                        'Available',
                        0,
                        50
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
