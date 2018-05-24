package com.carleton.cubic.pmdi.parser;


import java.util.*;

public class ParameterPacket {

    private static final Map<String,String> parameterIdLabelMap = createIdLabelMap();

    private static Map<String, String> createIdLabelMap() {
        Map<String, String> result = new HashMap<>();
        result.put("0x0100", "HR");
        result.put("0x6000", "RESP");
        result.put("0x6400", "SPO2");
        result.put("0x6500", "PLS");
        result.put("0x9600", "ETCO2");
        result.put("0x6600", "RRC");
        return Collections.unmodifiableMap(result);
    }

    private HashMap<String,ParameterSubPacket> parameterLabelValueMap ;
    private ArrayList<ParameterSubPacket> subPackets;
    private String requestTime;

    public ParameterPacket()
    {
        parameterLabelValueMap = new HashMap<>();
        subPackets = new ArrayList<>();
    }

    public void addSubPacket(ParameterSubPacket subPacket)
    {
        subPackets.add(subPacket);
        String parameterLabel = parameterIdLabelMap.get(subPacket.getCode());
        if(parameterLabel != null)
        {
            parameterLabelValueMap.put(parameterLabel, subPacket);
        }
    }

    public ParameterSubPacket getSubPacketByLabel(String label)
    {
        return parameterLabelValueMap.get(label);
    }

    public List<String> getSupportedLabels()
    {
        List<String> labelsList = new ArrayList<>(parameterIdLabelMap.values());
        Collections.sort(labelsList);
        return labelsList;
    }

    public List<String> getValuesForSupportedLabels()
    {
        List<String> values = new ArrayList<>();
        List<String> sortedLabels = getSupportedLabels();
        for(String label:sortedLabels)
        {
            ParameterSubPacket p = parameterLabelValueMap.get(label);
            if(p != null) {
                values.add(parameterLabelValueMap.get(label).getAsciiValue());
            }else {
                values.add("N/A");
            }
        }
        return values;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }
}
