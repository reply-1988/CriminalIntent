package com.example.jingj.criminalintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jingj on 2018/3/11.
 */

public class Crime {
    private UUID mId;
    private String mTitle;
    private boolean mSolved;
    private Date mDate;
    private int mPolice;

    public Crime(){
        this(UUID.randomUUID());
    }

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public int getPolice() {
        return mPolice;
    }

    public void setPolice(int police) {
        mPolice = police;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
