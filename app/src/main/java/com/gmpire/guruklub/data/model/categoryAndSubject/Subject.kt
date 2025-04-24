package com.gmpire.guruklub.data.model.categoryAndSubject

import com.gmpire.guruklub.data.local_db.dto.SubjectDTO


class Subject : BaseItem() {
    companion object {
        fun toSubjectDTOs(subjects: List<Subject>): List<SubjectDTO> {
            var SubjectDTOs = arrayListOf<SubjectDTO>()
            subjects.forEach { SubjectDTOs.add(it.toSubjectDTO()) }
            return SubjectDTOs
        }
    }

    fun toSubjectDTO(): SubjectDTO {
        return SubjectDTO(
            0, id, name, isSelected
        )
    }
}