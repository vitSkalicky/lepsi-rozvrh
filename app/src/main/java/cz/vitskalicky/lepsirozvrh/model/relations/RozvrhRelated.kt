package cz.vitskalicky.lepsirozvrh.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import cz.vitskalicky.lepsirozvrh.model.rozvrh.Rozvrh
import cz.vitskalicky.lepsirozvrh.model.rozvrh.RozvrhCaption

data class RozvrhRelated(
        @Embedded val rozvrh: Rozvrh,

        @Relation(
                entity = RozvrhCaption::class,
                parentColumn = "id",
                entityColumn = "rozvrh"
        )
        val captions: List<RozvrhCaption>,

        @Relation(
                entity = DayRelated::class,
                parentColumn = "id",
                entityColumn = "rozvrh"
        )
        val days: List<DayRelated>
)