package com.onlinerptrans.data

data class DictionaryEntry(
    val russian: String,
    val arabic: String,
    val darija: String,
    val category: DictionaryCategory
)

enum class DictionaryCategory(val labelAr: String) {
    DOCUMENTS("الوثائق"),
    VEHICLES("السيارات"),
    JOBS("المهن"),
    ACTIONS("الأفعال"),
    PLACES("الأماكن"),
    MONEY("المال"),
    CRIMINAL("الجريمة"),
    COMMUNICATION("التواصل")
}

object GamerDictionary {

    val entries: List<DictionaryEntry> = listOf(

        // ── Documents ─────────────────────────────────────────────────────────
        DictionaryEntry("Паспорт",          "جواز السفر",     "باسبور",          DictionaryCategory.DOCUMENTS),
        DictionaryEntry("Лицензия",         "رخصة القيادة",   "بيرمي",           DictionaryCategory.DOCUMENTS),
        DictionaryEntry("Документы",        "الوثائق",        "الوثائق",         DictionaryCategory.DOCUMENTS),
        DictionaryEntry("Удостоверение",    "بطاقة التعريف",  "بطاقة التعريف",   DictionaryCategory.DOCUMENTS),
        DictionaryEntry("Страховка",        "التأمين",        "ليسيرونص",        DictionaryCategory.DOCUMENTS),
        DictionaryEntry("Регистрация",      "التسجيل",        "التسجيل",         DictionaryCategory.DOCUMENTS),

        // ── Vehicles ──────────────────────────────────────────────────────────
        DictionaryEntry("Машина",           "السيارة",        "طوموبيل",         DictionaryCategory.VEHICLES),
        DictionaryEntry("Автомобиль",       "السيارة",        "السيارة",         DictionaryCategory.VEHICLES),
        DictionaryEntry("Мотоцикл",         "الدراجة النارية","الموتو",          DictionaryCategory.VEHICLES),
        DictionaryEntry("Грузовик",         "الشاحنة",        "الكاميون",        DictionaryCategory.VEHICLES),
        DictionaryEntry("Вертолёт",         "الهليكوبتر",     "الهليكوبتر",      DictionaryCategory.VEHICLES),
        DictionaryEntry("Самолёт",          "الطائرة",        "الطيارة",         DictionaryCategory.VEHICLES),
        DictionaryEntry("Лодка",            "القارب",         "الباطو",          DictionaryCategory.VEHICLES),
        DictionaryEntry("Номерной знак",    "لوحة الترقيم",   "بلاكا",           DictionaryCategory.VEHICLES),

        // ── Jobs ──────────────────────────────────────────────────────────────
        DictionaryEntry("Полиция",          "الشرطة",         "البوليس",         DictionaryCategory.JOBS),
        DictionaryEntry("Медик",            "المسعف",         "الطبيب",          DictionaryCategory.JOBS),
        DictionaryEntry("Механик",          "الميكانيكي",     "الميكانيسيان",    DictionaryCategory.JOBS),
        DictionaryEntry("Таксист",          "سائق التاكسي",   "تاكسيور",         DictionaryCategory.JOBS),
        DictionaryEntry("Бандит",           "المجرم",         "الشرير",          DictionaryCategory.JOBS),
        DictionaryEntry("Мэр",              "العمدة",         "المير",           DictionaryCategory.JOBS),
        DictionaryEntry("Судья",            "القاضي",         "القاضي",          DictionaryCategory.JOBS),
        DictionaryEntry("Адвокат",          "المحامي",        "المحامي",         DictionaryCategory.JOBS),

        // ── Actions ───────────────────────────────────────────────────────────
        DictionaryEntry("Руки вверх",       "ارفع يديك",      "يدّيك فوق",       DictionaryCategory.ACTIONS),
        DictionaryEntry("Стой",             "قف",             "وقف",             DictionaryCategory.ACTIONS),
        DictionaryEntry("Выйди из машины",  "اخرج من السيارة","خرج من السيارة",  DictionaryCategory.ACTIONS),
        DictionaryEntry("Следуй за мной",   "اتبعني",         "جي معي",          DictionaryCategory.ACTIONS),
        DictionaryEntry("Ложись",           "استلقِ على الأرض","خوات فالأرض",    DictionaryCategory.ACTIONS),
        DictionaryEntry("Задержан",         "موقوف",          "معتقل",           DictionaryCategory.ACTIONS),
        DictionaryEntry("Обыск",            "التفتيش",        "التفتيش",         DictionaryCategory.ACTIONS),
        DictionaryEntry("Арест",            "الاعتقال",       "الاعتقال",        DictionaryCategory.ACTIONS),

        // ── Places ────────────────────────────────────────────────────────────
        DictionaryEntry("Больница",         "المستشفى",       "سبيطار",          DictionaryCategory.PLACES),
        DictionaryEntry("Тюрьма",           "السجن",          "الحبس",           DictionaryCategory.PLACES),
        DictionaryEntry("Банк",             "البنك",          "البنك",           DictionaryCategory.PLACES),
        DictionaryEntry("Магазин",          "المتجر",         "الحانوت",         DictionaryCategory.PLACES),
        DictionaryEntry("Заправка",         "محطة الوقود",    "محطة البنزين",    DictionaryCategory.PLACES),
        DictionaryEntry("Полицейский участок","مركز الشرطة", "الكومسيريا",      DictionaryCategory.PLACES),
        DictionaryEntry("Дом",              "المنزل",         "الدار",           DictionaryCategory.PLACES),
        DictionaryEntry("Аэропорт",         "المطار",         "المطار",          DictionaryCategory.PLACES),

        // ── Money ─────────────────────────────────────────────────────────────
        DictionaryEntry("Деньги",           "المال",          "الفلوس",          DictionaryCategory.MONEY),
        DictionaryEntry("Зарплата",         "الراتب",         "السالار",         DictionaryCategory.MONEY),
        DictionaryEntry("Штраф",            "الغرامة",        "الأمندا",         DictionaryCategory.MONEY),
        DictionaryEntry("Налог",            "الضريبة",        "الإيمبو",         DictionaryCategory.MONEY),
        DictionaryEntry("Кредит",           "القرض",          "الكريدي",         DictionaryCategory.MONEY),

        // ── Criminal ──────────────────────────────────────────────────────────
        DictionaryEntry("Оружие",           "السلاح",         "السلاح",          DictionaryCategory.CRIMINAL),
        DictionaryEntry("Наркотики",        "المخدرات",       "الدروكا",         DictionaryCategory.CRIMINAL),
        DictionaryEntry("Кража",            "السرقة",         "السرقة",          DictionaryCategory.CRIMINAL),
        DictionaryEntry("Ограбление",       "النهب",          "الفاطو",          DictionaryCategory.CRIMINAL),
        DictionaryEntry("Перестрелка",      "تبادل إطلاق النار","ضرب ديال الرصاص", DictionaryCategory.CRIMINAL),
        DictionaryEntry("Наручники",        "الأصفاد",        "الكوتشيفة",       DictionaryCategory.CRIMINAL),

        // ── Communication ─────────────────────────────────────────────────────
        DictionaryEntry("Помогите",         "ساعدوني",        "عاوني",           DictionaryCategory.COMMUNICATION),
        DictionaryEntry("Где ты?",          "أين أنت؟",       "فين نتا؟",        DictionaryCategory.COMMUNICATION),
        DictionaryEntry("Иду к тебе",       "أنا قادم إليك",  "أنا جاي ليك",     DictionaryCategory.COMMUNICATION),
        DictionaryEntry("Подожди",          "انتظر",          "تسنى",            DictionaryCategory.COMMUNICATION),
        DictionaryEntry("Всё в порядке",    "كل شيء بخير",    "كلشي مزيان",      DictionaryCategory.COMMUNICATION),
        DictionaryEntry("Договорились",     "اتفقنا",         "اتفاقنا",         DictionaryCategory.COMMUNICATION),
        DictionaryEntry("Нет",              "لا",             "لا",              DictionaryCategory.COMMUNICATION),
        DictionaryEntry("Да",               "نعم",            "إيه",             DictionaryCategory.COMMUNICATION)
    )

    fun search(query: String): List<DictionaryEntry> {
        if (query.isBlank()) return entries
        val q = query.trim().lowercase()
        return entries.filter { entry ->
            entry.russian.lowercase().contains(q) ||
            entry.arabic.contains(q) ||
            entry.darija.contains(q)
        }
    }

    fun byCategory(category: DictionaryCategory): List<DictionaryEntry> =
        entries.filter { it.category == category }
}
