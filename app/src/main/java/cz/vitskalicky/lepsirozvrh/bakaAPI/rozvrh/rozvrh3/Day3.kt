package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3

data class Day3 (
    val atoms: List<Atom3>,
    val dayOfWeek: Int,
    val date: String,
    val dayDescription: String,
    val dayType: String,
)