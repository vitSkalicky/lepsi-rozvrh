package cz.vitskalicky.lepsirozvrh.model.rozvrh

import cz.vitskalicky.lepsirozvrh.model.GroupConverters

/**
 * When changing structure don't forget to update [GroupConverters]
 */
data class RozvrhGroup(
        val id: String,
        val name: String,
        val abbrev: String
)