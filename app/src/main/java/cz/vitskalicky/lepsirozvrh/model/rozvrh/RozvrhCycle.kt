package cz.vitskalicky.lepsirozvrh.model.rozvrh

import cz.vitskalicky.lepsirozvrh.model.CycleConverters

/**
* When changing structure don't forget to update [CycleConverters]
*/
data class RozvrhCycle(
        val id: String,
        val name: String,
        val abbrev: String
)