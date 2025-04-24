package com.gmpire.guruklub.data.local_db.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject

@Entity(tableName = "Subject")
class SubjectDTO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "localId")
    var localId: Int = 0,
    var id: String? = "",
    var name: String? = "",
    var isSelected: Boolean? = false
) {

    companion object {
        fun toSubjects(subjectDTOs: List<SubjectDTO>): List<Subject> {
            var subjects = arrayListOf<Subject>()
            subjectDTOs.forEach { subjects.add(it.toSubject()) }
            return subjects
        }
    }

    fun toSubject(): Subject {
        val subject = Subject()
        subject.id = id
        subject.name = name
        subject.isSelected = isSelected ?: false
        return subject
    }

}