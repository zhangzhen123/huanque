package com.julun.huanque.common.bean;

/**
 * Created by nirack on 16-11-7.
 */

public class ResponseError extends Exception{
    private Integer busiCode;
    private String busiMessage;

    public ResponseError (Integer busiCode, String busiMessage) {
        super (busiMessage);
        this.busiCode = busiCode;
        this.busiMessage = busiMessage;
    }

    public Integer getBusiCode () {
        return busiCode;
    }

    public void setBusiCode (Integer busiCode) {
        this.busiCode = busiCode;
    }

    public String getBusiMessage () {
        return busiMessage;
    }

    public void setBusiMessage (String busiMessage) {
        this.busiMessage = busiMessage;
    }
}
