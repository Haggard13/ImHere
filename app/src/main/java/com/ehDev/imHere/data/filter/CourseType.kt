package com.ehDev.imHere.data.filter

enum class CourseType(val description: String) {

    ALL_COURSES("Все курсы"),
    FIRST("Первый"),
    SECOND("Второй"),
    THIRD("Третий"),
    FOURTH("Четвертый"),
    FIFTH("Пятый"),
    SIXTH("Шестой");

    companion object {

        fun findCourseByDescription(requiredCourse: String) = values().find {
            it.description == requiredCourse
        } ?: ALL_COURSES
    }

    fun isCourseCorrect(requiredCourse: String): Boolean {

        val requiredCourseType = findCourseByDescription(requiredCourse)

        return (requiredCourseType == this) || (requiredCourseType == ALL_COURSES)
    }
}