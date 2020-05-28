package com.ehDev.imHere.data.filter

enum class InstitutionType(val description: String) {

    ALL_INSTITUTIONS("Все институты"),
    RTF("ИРИТ-РТФ"),
    ISA("ИСА"),
    UGI("УГИ"),
    FTI("ФТИ"),
    IENiM("ИЕНиМ"),
    INMiT("ИНМиТ"),
    InFO("ИнФО"),
    HTI("ХТИ");

    companion object {

        fun findInstituteByDescription(requiredInstitute: String) = values().find {
            it.description == requiredInstitute
        } ?: ALL_INSTITUTIONS
    }

    fun isInstituteCorrect(requiredInstitute: String): Boolean {

        val requiredInstituteType = findInstituteByDescription(requiredInstitute)

        return (requiredInstituteType == this) || (requiredInstituteType == ALL_INSTITUTIONS)
    }
}