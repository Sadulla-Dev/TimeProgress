package com.example.yearprogress.utils

import androidx.annotation.StringRes
import com.example.yearprogress.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

const val UZ_LIFE_EXPECTANCY = 75.1
const val DEFAULT_CUSTOM_LIFE_EXPECTANCY = UZ_LIFE_EXPECTANCY

enum class WeekStartDay {
    MONDAY,
    SUNDAY
}

data class LifeExpectancyPreset(
    val id: String,
    @StringRes val labelRes: Int,
    val years: Double
)

data class AchievementExample(
    val requestedAge: Int,
    val actualAge: Int,
    val name: String,
    val achievementUz: String,
    val achievementEn: String,
    val achievementRu: String
) {
    fun localizedAchievement(language: String): String {
        return when (language.lowercase()) {
            "uz" -> achievementUz
            "ru" -> achievementRu
            else -> achievementEn
        }
    }
}

private data class ExactAchievement(
    val age: Int,
    val name: String,
    val achievementUz: String,
    val achievementEn: String,
    val achievementRu: String
)

val lifeExpectancyPresets = listOf(
    LifeExpectancyPreset("uzbekistan", R.string.life_preset_uzbekistan, 75.1),
    LifeExpectancyPreset("usa", R.string.life_preset_usa, 77.5),
    LifeExpectancyPreset("japan", R.string.life_preset_japan, 84.5),
    LifeExpectancyPreset("south_korea", R.string.life_preset_south_korea, 84.3),
    LifeExpectancyPreset("custom", R.string.life_preset_custom, -1.0),
)

private val exactAchievementExamples = listOf(
    ExactAchievement(
        age = 5,
        name = "Wolfgang Amadeus Mozart",
        achievementUz = "Yevropa saroylarida chiqish qilib, bolalar dahosi sifatida tanildi.",
        achievementEn = "Performed in European courts as a child prodigy.",
        achievementRu = "Выступал при европейских дворах как вундеркинд."
    ),
    ExactAchievement(
        age = 14,
        name = "Bobby Fischer",
        achievementUz = "Eng yosh shaxmat grossmeysterlaridan biriga aylandi.",
        achievementEn = "Became one of the youngest chess grandmasters.",
        achievementRu = "Стал одним из самых молодых гроссмейстеров."
    ),
    ExactAchievement(
        age = 15,
        name = "Louis Braille",
        achievementUz = "Ko‘zi ojizlar uchun Braille yozuv tizimini yaratdi.",
        achievementEn = "Created the Braille reading system.",
        achievementRu = "Создал систему чтения Брайля."
    ),
    ExactAchievement(
        age = 17,
        name = "Malala Yousafzai",
        achievementUz = "Nobel Tinchlik mukofotining eng yosh sovrindoriga aylandi.",
        achievementEn = "Became the youngest Nobel Peace Prize winner.",
        achievementRu = "Стала самой молодой лауреаткой Нобелевской премии мира."
    ),
    ExactAchievement(
        age = 19,
        name = "Mark Zuckerberg",
        achievementUz = "Facebook'ni ishga tushirdi.",
        achievementEn = "Launched Facebook.",
        achievementRu = "Запустил Facebook."
    ),
    ExactAchievement(
        age = 20,
        name = "Alexander the Great",
        achievementUz = "Makedoniya qiroli bo‘lib, dunyoni zabt etishni boshladi.",
        achievementEn = "Became king of Macedon and began his conquests.",
        achievementRu = "Стал царём Македонии и начал завоевания."
    ),
    ExactAchievement(
        age = 22,
        name = "Lionel Messi",
        achievementUz = "Birinchi Ballon d'Or sovrinini qo‘lga kiritdi.",
        achievementEn = "Won his first Ballon d'Or.",
        achievementRu = "Выиграл свой первый Золотой мяч."
    ),
    ExactAchievement(
        age = 25,
        name = "Freddie Mercury",
        achievementUz = "Queen guruhini tuzdi.",
        achievementEn = "Founded the band Queen.",
        achievementRu = "Создал группу Queen."
    ),
    ExactAchievement(
        age = 28,
        name = "Steve Jobs",
        achievementUz = "Apple Macintosh'ni taqdim etdi.",
        achievementEn = "Introduced the Apple Macintosh.",
        achievementRu = "Представил Apple Macintosh."
    ),
    ExactAchievement(
        age = 32,
        name = "Thomas Edison",
        achievementUz = "Amaliy elektr lampochkasini yaratdi.",
        achievementEn = "Invented the practical light bulb.",
        achievementRu = "Создал практическую лампу накаливания."
    ),
    ExactAchievement(
        age = 35,
        name = "Walt Disney",
        achievementUz = "\"Snow White\" multfilmini chiqardi.",
        achievementEn = "Released Snow White.",
        achievementRu = "Выпустил «Белоснежку»."
    ),
    ExactAchievement(
        age = 36,
        name = "Jan Koum",
        achievementUz = "WhatsApp'ni ishga tushirdi.",
        achievementEn = "Launched WhatsApp.",
        achievementRu = "Запустил WhatsApp."
    ),
    ExactAchievement(
        age = 38,
        name = "Reid Hoffman",
        achievementUz = "LinkedIn'ni ishga tushirdi.",
        achievementEn = "Launched LinkedIn.",
        achievementRu = "Запустил LinkedIn."
    ),
    ExactAchievement(
        age = 40,
        name = "Elon Musk",
        achievementUz = "SpaceX orqali xususiy kosmik sanoatni rivojlantirdi.",
        achievementEn = "Advanced private space exploration with SpaceX.",
        achievementRu = "Развил частную космическую индустрию через SpaceX."
    ),
    ExactAchievement(
        age = 44,
        name = "Sam Walton",
        achievementUz = "Walmart’ni dunyodagi yirik savdo tarmoqlaridan biriga aylantirdi.",
        achievementEn = "Built Walmart into a retail giant.",
        achievementRu = "Сделал Walmart крупнейшей торговой сетью."
    ),
    ExactAchievement(
        age = 52,
        name = "Ray Kroc",
        achievementUz = "McDonald's’ni global brendga aylantirdi.",
        achievementEn = "Turned McDonald's into a global brand.",
        achievementRu = "Сделал McDonald's глобальным брендом."
    ),
    ExactAchievement(
        age = 62,
        name = "Colonel Sanders",
        achievementUz = "KFC’ni franchayzing orqali butun dunyoga yoydi.",
        achievementEn = "Expanded KFC worldwide through franchising.",
        achievementRu = "Распространил KFC по всему миру через франшизу."
    ),
    ExactAchievement(
        age = 75,
        name = "Nelson Mandela",
        achievementUz = "Janubiy Afrika prezidenti bo‘ldi.",
        achievementEn = "Became President of South Africa.",
        achievementRu = "Стал президентом Южной Африки."
    )
)
fun findAchievementExample(age: Int): AchievementExample? {
    val nearest = exactAchievementExamples.minByOrNull { abs(it.age - age) } ?: return null
    return AchievementExample(
        requestedAge = age,
        actualAge = nearest.age,
        name = nearest.name,
        achievementUz = nearest.achievementUz,
        achievementEn = nearest.achievementEn,
        achievementRu = nearest.achievementRu
    )
}

fun resolveLifeExpectancy(preferenceManager: PreferenceManager): Double {
    val presetId = preferenceManager.getLifeExpectancyPresetId()
    return if (presetId == "custom") {
        preferenceManager.getCustomLifeExpectancy()
    } else {
        lifeExpectancyPresets.firstOrNull { it.id == presetId }?.years
            ?: DEFAULT_CUSTOM_LIFE_EXPECTANCY
    }
}

fun weekBounds(now: LocalDateTime, weekStartDay: WeekStartDay): Pair<LocalDateTime, LocalDateTime> {
    val startOffset = when (weekStartDay) {
        WeekStartDay.MONDAY -> (now.dayOfWeek.value - 1).toLong()
        WeekStartDay.SUNDAY -> (now.dayOfWeek.value % 7).toLong()
    }
    val start = now.toLocalDate().atStartOfDay().minusDays(startOffset)
    val end = start.plusDays(7).minusSeconds(1)
    return start to end
}

fun lifeProgress(birthDate: LocalDate, lifeExpectancy: Double): Double {
    val now = LocalDate.now()
    val ageYears = ChronoUnit.DAYS.between(birthDate, now) / 365.25
    return (ageYears / lifeExpectancy).coerceIn(0.0, 1.0)
}
