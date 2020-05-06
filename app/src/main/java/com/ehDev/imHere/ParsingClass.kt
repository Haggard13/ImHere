package com.ehDev.imHere

import org.jsoup.nodes.Document
import java.util.*

internal object ParsingClass {

    var weekData: Dictionary<String, DayData>? = null
    fun scheduleParsing(doc: Document) {
        val elements = doc.getElementsByAttribute("div#inner")
    }

    internal class DayData(vararg classDataArray: ClassData) {
        var daySchedule: Dictionary<Int, ClassData>? = null

        init {
            for (data in classDataArray) {
                daySchedule!!.put(data.number, data)
            }
        }
    }

    internal class ClassData(var name: String, var type: String, var lecturer: String, var auditory: Auditory, var date: Date, var number: Int)

    internal class Auditory(val auditory: String) {
        var prefix: String = auditory.split('0', '1','2','3','4','5','6', '7', '8','9')[0]
    }
}