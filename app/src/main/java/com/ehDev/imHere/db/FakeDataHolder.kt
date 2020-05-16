package com.ehDev.imHere.db

import java.util.Date

object FakeDataHolder {

    val login = arrayOf(
        "strontium2001@mail.ru", "elizaveta.ezhova01@mail.ru", "admin",
        "000", "001", "010", "011", "020", "021", "030", "031",
        "040", "041", "050", "051", "060", "061", "070", "071",
        "100", "101", "110", "111", "120", "121", "130", "131",
        "140", "141", "150", "151", "160", "161", "170", "171",
        "200", "201", "210", "211", "220", "221", "230", "231",
        "240", "241", "250", "251", "260", "261", "270", "271",
        "300", "301", "310", "311", "320", "321", "330", "331",
        "340", "341", "350", "351", "360", "361", "370", "371",
        "400", "401", "410", "411", "420", "421", "430", "431",
        "440", "441", "450", "451", "460", "461", "470", "471",
        "500", "501", "510", "511", "520", "521", "530", "531",
        "540", "541", "550", "551", "560", "561", "570", "571"
    )

    val password = intArrayOf(
        "Elzahaggard13@".hashCode(), "Elzahaggard13!".hashCode(), "admin".hashCode(),
        "000".hashCode(), "001".hashCode(), "010".hashCode(), "011".hashCode(), "020".hashCode(), "021".hashCode(),
        "030".hashCode(), "031".hashCode(),
        "040".hashCode(), "041".hashCode(), "050".hashCode(), "051".hashCode(), "060".hashCode(), "061".hashCode(),
        "070".hashCode(), "071".hashCode(),
        "100".hashCode(), "101".hashCode(), "110".hashCode(), "111".hashCode(), "120".hashCode(), "121".hashCode(),
        "130".hashCode(), "131".hashCode(),
        "140".hashCode(), "141".hashCode(), "150".hashCode(), "151".hashCode(), "160".hashCode(), "161".hashCode(),
        "170".hashCode(), "171".hashCode(),
        "200".hashCode(), "201".hashCode(), "210".hashCode(), "211".hashCode(), "220".hashCode(), "221".hashCode(),
        "230".hashCode(), "231".hashCode(),
        "240".hashCode(), "241".hashCode(), "250".hashCode(), "251".hashCode(), "260".hashCode(), "261".hashCode(),
        "270".hashCode(), "271".hashCode(),
        "300".hashCode(), "301".hashCode(), "310".hashCode(), "311".hashCode(), "320".hashCode(), "321".hashCode(),
        "330".hashCode(), "331".hashCode(),
        "340".hashCode(), "341".hashCode(), "350".hashCode(), "351".hashCode(), "360".hashCode(), "361".hashCode(),
        "370".hashCode(), "371".hashCode(),
        "400".hashCode(), "401".hashCode(), "410".hashCode(), "411".hashCode(), "420".hashCode(), "421".hashCode(),
        "430".hashCode(), "431".hashCode(),
        "440".hashCode(), "441".hashCode(), "450".hashCode(), "451".hashCode(), "460".hashCode(), "461".hashCode(),
        "470".hashCode(), "471".hashCode(),
        "500".hashCode(), "501".hashCode(), "510".hashCode(), "511".hashCode(), "520".hashCode(), "521".hashCode(),
        "530".hashCode(), "531".hashCode(),
        "540".hashCode(), "541".hashCode(), "550".hashCode(), "551".hashCode(), "560".hashCode(), "561".hashCode(),
        "570".hashCode(), "571".hashCode()
    )

    val filter = arrayOf(
        "001", "001", "000",
        "000", "001", "010", "011", "020", "021", "030", "031",
        "040", "041", "050", "051", "060", "061", "070", "071",
        "100", "101", "110", "111", "120", "121", "130", "131",
        "140", "141", "150", "151", "160", "161", "170", "171",
        "200", "201", "210", "211", "220", "221", "230", "231",
        "240", "241", "250", "251", "260", "261", "270", "271",
        "300", "301", "310", "311", "320", "321", "330", "331",
        "340", "341", "350", "351", "360", "361", "370", "371",
        "400", "401", "410", "411", "420", "421", "430", "431",
        "440", "441", "450", "451", "460", "461", "470", "471",
        "500", "501", "510", "511", "520", "521", "530", "531",
        "540", "541", "550", "551", "560", "561", "570", "571"
    )

    var institutions = arrayOf("IRIT-RTF", "GUK", "TEPLOFAK", "VSHEM", "STROYFAK", "HIMFAK")

    var latitude = doubleArrayOf(
        56.840751353604674, 56.843914249505055, 56.8424766026804, 56.843110347677786, 56.84516994481896,
        56.84218319858506
    )
    var longitude = doubleArrayOf(
        60.650839805603034, 60.65374732017518, 60.655345916748054, 60.65316796302796, 60.65071105957032,
        60.649048089981086
    )

    //TODO допилить данные
    var date = arrayOf(Date(2020, 5, 18, 12, 0),
            Date(2020, 5, 19, 12, 0),
            Date(2020, 5, 20, 12, 0),
            Date(2020, 5, 21, 12, 0))

    var lecturer = arrayOf("","","","")

    var auditorium = arrayOf("","","","")

    var type = arrayOf("Лекция", "Онлайн-курс", "Пркатика", "Лекция")

    var number = intArrayOf(3, 3, 3, 3)
}