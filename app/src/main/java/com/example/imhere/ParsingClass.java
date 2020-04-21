package com.example.imhere;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.*;

class ParsingClass {

    public static Dictionary<String, DayData> weekData;

    public static void scheduleParsing(Document doc) {
        Elements elements = doc.getElementsByAttribute("div#inner");
    }

    class DayData  {
        public Dictionary<Integer, ClassData> daySchedule;

        public DayData(ClassData... classDataArray){
            for (ClassData data:
                 classDataArray) {
                daySchedule.put(data.number, data);
            }
        }
    }

    class ClassData {
        public String name, type, lecturer;
        public Auditory auditory;
        public Date date;
        public int number;

        public ClassData(String name, String type, String lecturer, Auditory auditory, Date date, int number){
            this.name = name;
            this.type = type;
            this.lecturer = lecturer;
            this.auditory = auditory;
            this.date = date;
            this.number = number;
        }
    }

    class Auditory {
        public String auditory;
        public String prefix;

        public Auditory(String auditory){
            this.auditory = auditory;
            StringBuilder prefixBuilder = null;
            for (int i = 0; i < auditory.length(); i++){
                if (tryParseInt(auditory.charAt(i))) break;
                prefixBuilder.append(auditory.charAt(i));
            }
            prefix = prefixBuilder.toString();
        }

        private boolean tryParseInt(char value) {
            try {
                Integer.parseInt(String.valueOf(value));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}