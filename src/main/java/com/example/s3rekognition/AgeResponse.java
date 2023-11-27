package com.example.s3rekognition;

import java.io.Serializable;

public class AgeResponse implements Serializable {
    public long getAmountInPicture() {
        return amountInPicture;
    }

    public void setAmountInPicture(long amountInPicture) {
        this.amountInPicture = amountInPicture;
    }

    public long getAmountUnderage() {
        return amountUnderage;
    }

    public void setAmountUnderage(long amountUnderage) {
        this.amountUnderage = amountUnderage;
    }

    public AgeResponse(String pictureName, long amountInPicture, long amountUnderage) {
        this.pictureName = pictureName;
        this.amountInPicture = amountInPicture;
        this.amountUnderage = amountUnderage;
    }

    private long amountInPicture;
    private long amountUnderage;

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    private String pictureName;
}
