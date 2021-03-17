package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3

/**
 * Rozvrh of the Bakaláři API v3
 */
data class Rozvrh3 (
    val hours: List<Hour3>,
    val days: List<Day3>,
    val classes: List<Class3>,
    val groups: List<Group3>,
    val subjects: List<Subject3>,
    val teachers: List<Teacher3>,
    val rooms: List<Room3>,
    val cycles: List<Cycle3>,
)