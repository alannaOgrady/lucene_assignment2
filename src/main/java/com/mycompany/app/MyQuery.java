package com.mycompany.app;

public class MyQuery {
    private int queryNumber;
    private String title;
    private String description;
    private String narrative;


    public MyQuery(int queryNum, String qTitle, String qDescription, String qNarrative) {
        queryNumber = queryNum;
        title = qTitle;
        description = qDescription;
        narrative = qNarrative;
    }

    public int getQueryNum() {
        return queryNumber;
    }

    public String getQueryTitle() {
        return title;
    }

    public String getQueryDescription() {
        return description;
    }

    public String getQueryNarrative() {
        return narrative;
    }
}
