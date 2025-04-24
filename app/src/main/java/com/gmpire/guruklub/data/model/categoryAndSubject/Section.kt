package com.gmpire.guruklub.data.model.categoryAndSubject

import com.gmpire.guruklub.data.local_db.dto.SectionDTO

open class Section(
    var subject_id: String? = "",
    var subject_name: String? = ""
) : BaseItem() {
    companion object {
        fun toSectionDTOs(sections: List<Section>): List<SectionDTO> {
            var sectionDTOs = arrayListOf<SectionDTO>()
            sections.forEach { sectionDTOs.add(it.toSectionDTO()) }
            return sectionDTOs
        }
    }

    fun toSectionDTO(): SectionDTO {
        return SectionDTO(
            0, id, name, isSelected, subject_id, subject_name
        )
    }

}