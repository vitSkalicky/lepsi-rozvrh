package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhDay

data class RozvrhRelated(
        @Embedded val rozvrh: Rozvrh,

        @Relation(
                parentColumn = "id",
                entityColumn = "rozvrh",
                entity = RozvrhCaption::class
        )
        val captions: List<RozvrhCaption>,

        @Relation(
                parentColumn = "id",
                entityColumn = "rozvrh",
                entity = RozvrhDay::class
        )
        val days: List<DayRelated>
)