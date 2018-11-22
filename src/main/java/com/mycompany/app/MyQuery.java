package com.mycompany.app;

public class MyQuery {
    private int queryNumber;
    private String title;
    private String description;
    private String relNarrative;
    private String nonRelNarrative;


    public MyQuery(int queryNum, String qTitle, String qDescription, String qRelNarrative, String qNonRelNarrative) {
        queryNumber = queryNum;
        title = qTitle;
        description = qDescription;
        relNarrative = qRelNarrative;
        nonRelNarrative = qNonRelNarrative;
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

    public String getRelevantQueryNarrative() {
        return relNarrative;
    }

    public String getNonRelevantQueryNarrative() {
        return nonRelNarrative;
    }
}
