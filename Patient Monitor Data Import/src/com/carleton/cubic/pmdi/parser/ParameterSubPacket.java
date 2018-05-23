package com.carleton.cubic.pmdi.parser;


public class ParameterSubPacket {
    private String code;
    private String status;
    private String asciiValue;

    public String getAsciiValue() {
        return asciiValue;
    }

    public void setAsciiValue(String asciiValue) {
        this.asciiValue = asciiValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String codeByteHigh, String codeByteLow) {
        this.code = codeByteHigh;
        if(codeByteLow != null)
        {
            this.code += codeByteLow.substring(2); //Trims the 0x part
        }
    }
}
