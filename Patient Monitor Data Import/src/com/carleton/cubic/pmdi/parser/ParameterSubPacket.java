package com.carleton.cubic.pmdi.parser;


import java.io.UnsupportedEncodingException;

public class ParameterSubPacket {
    private String code;
    private String status;
    private byte[] value;

    public void setValue(byte[] valueBytes)
    {
        this.value = valueBytes;
    }

    public String getAsciiValue() {
        String asciiString = null;
        try {
            asciiString = new String(value, "UTF-8");
            asciiString = asciiString.trim();
        } catch (UnsupportedEncodingException e) {
            //TODO:
            e.printStackTrace();
        }
        return asciiString;
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
