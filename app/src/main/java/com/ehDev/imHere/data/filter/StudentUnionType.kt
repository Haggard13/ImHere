package com.ehDev.imHere.data.filter

enum class StudentUnionType(val description: String) {

    ALL_STUDENTS("Все студенты"),
    IN_STUDENT_UNION("Состоит в союзе студентов"),
    NOT_IN_STUDENT_UNION("Не состоит в союзе студентов");

    companion object {

        fun findStudentUnionInfoByDescription(requiredStudentUnionInfo: String) = values().find {
            it.description == requiredStudentUnionInfo
        } ?: ALL_STUDENTS
    }

    fun isStudentUnionInfoCorrect(requiredInfo: String): Boolean {

        val requiredStudentUnionType = findStudentUnionInfoByDescription(requiredInfo)

        return (requiredStudentUnionType == this) || (requiredStudentUnionType == ALL_STUDENTS)
    }
}