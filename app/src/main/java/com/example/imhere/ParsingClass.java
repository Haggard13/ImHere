package com.example.imhere;

import java.util.Date;

public class ParsingClass {
}

class ClassData {
    public String name;
    public String type;
    public Auditory auditory;
    public String lecturer;
    public Date date;
}
class Auditory {
    public String auditory;
    private String prefix;

    public Auditory(String auditory) {
        this.auditory = auditory;
        for (int i = 0; i < auditory.length(); i++) {
            if ((auditory.length() == 5 && i == 2) || (auditory.length() == 4 && i == 1)) break;
            prefix = prefix + auditory.charAt(i);
        }
    }
}
