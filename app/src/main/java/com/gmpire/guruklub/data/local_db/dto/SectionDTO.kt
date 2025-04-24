package com.gmpire.guruklub.data.local_db.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gmpire.guruklub.data.model.categoryAndSubject.Section

@Entity(tableName = "Section")
class SectionDTO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "localId")
    var localId: Int = 0,
    var id: String? = "",
    var name: String? = "",
    var isSelected: Boolean? = false,
    var subject_id: String? = "",
    var subject_name: String? = ""
) {

    companion object {
        fun toSections(sectionDTOs: List<SectionDTO>): List<Section> {
            var sections = arrayListOf<Section>()
            sectionDTOs.forEach { sections.add(it.toSection()) }
            return sections
        }
    }

    fun toSection(): Section {
        val section = Section()
        section.id = id
        section.name = name
        section.isSelected = isSelected ?: false
        section.subject_id = subject_id
        section.subject_name = subject_name
        return section
    }
}