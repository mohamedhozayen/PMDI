package com.carleton.cubic.pmdi.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {


    public static ParameterSubPacket parseSingleParameterSubPacket(List<String> subPacketBytes, String subPacketType)
    {
        ParameterSubPacket subPacket = new ParameterSubPacket();

        int statusByteIndex = 1;
        if(subPacketType.equals("0x77"))
        {
            subPacket.setCode(subPacketBytes.get(0), subPacketBytes.get(1));
            statusByteIndex = 2;
        }
        else
        {
            subPacket.setCode(subPacketBytes.get(0), null);
        }

        subPacket.setStatus(subPacketBytes.get(statusByteIndex));

        List<String> valueByteStrList = subPacketBytes.subList(statusByteIndex + 1, subPacketBytes.size());
        byte[] valueBytes = decodeHexByteStrList(valueByteStrList);
        subPacket.setValue(valueBytes);

        return subPacket;
    }


    public static ParameterResponse parseSingleParameterResponse(List<String> messageBytes, String requestTimeStr) {

        if(messageBytes.size() < 3)
        {
            return null; // Ignore bad messages that are saved during interruptions
        }

        int packetLength = Integer.decode(messageBytes.get(2) + messageBytes.get(1).substring(2));
        if(packetLength != messageBytes.size() - 3) // Length excludes sync byte and the length bytes
        {
            return null;
        }
        //TODO: Should checksum be validated

        ParameterResponse parameterResponse = new ParameterResponse();
        parameterResponse.setRequestTime(requestTimeStr);

        String parameterDataMessageType = messageBytes.get(3);
        String numberOfParameterSubPacketsStr = messageBytes.get(23);
        int numberOfParameterSubPackets = Integer.decode(numberOfParameterSubPacketsStr);
        int nextSubPacketLengthByteIndex = 24;
        for(int numSubPacket = 0; numSubPacket < numberOfParameterSubPackets; numSubPacket++)
        {
            int lengthOfSubPacket = Integer.decode(messageBytes.get(nextSubPacketLengthByteIndex));
            Byte numParametersByte = Byte.decode(messageBytes.get(nextSubPacketLengthByteIndex + 1));

            int numberOfParameters = numParametersByte & 0x07; // Right three bits indicate the number of parameters
            boolean hasTimeStamp = (numParametersByte & 0x08) != 0; // If third bit is set, then it has a timestamp

            int inSubPacketIndex = nextSubPacketLengthByteIndex + 7;
            for(int i=0; i<numberOfParameters; i++)
            {
                List<String> subPacketBytes = new ArrayList<>();
                inSubPacketIndex++;
                subPacketBytes.add(messageBytes.get(inSubPacketIndex)); //Code Byte
                if(parameterDataMessageType.equals("0x77"))
                {
                    inSubPacketIndex++;
                    subPacketBytes.add(messageBytes.get(inSubPacketIndex));
                }

                inSubPacketIndex++; //Status Byte
                subPacketBytes.add(messageBytes.get(inSubPacketIndex));

                inSubPacketIndex++; //Add a value byte
                String addedByte = messageBytes.get(inSubPacketIndex);
                subPacketBytes.add(addedByte);
                int maxRemainingValueBytes = 4;
                while(maxRemainingValueBytes > 0 && !addedByte.equals("0x00"))
                {
                    inSubPacketIndex++; //Add a value byte
                    addedByte = messageBytes.get(inSubPacketIndex);
                    subPacketBytes.add(addedByte);
                    maxRemainingValueBytes--;
                }

                if(hasTimeStamp)
                {
                    //TODO: Ignored timestamp for now
                    inSubPacketIndex += 5; //Skip 5 of the timestamp bytes
                }

                ParameterSubPacket subPacket = parseSingleParameterSubPacket(subPacketBytes, parameterDataMessageType);
                parameterResponse.addSubPacketParameter(subPacket);
            }

            nextSubPacketLengthByteIndex =  nextSubPacketLengthByteIndex + lengthOfSubPacket;

        }
        return parameterResponse;
    }

    public static void parse(File inputFile, File outputFile) throws IOException {

        try (
                BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile))
        ) {
            boolean headerWritten = false;
            String line = bufferedReader.readLine();
            int lineNumber = 1;
            while (line != null) {

                if(line.contains("Read data"))
                {
                    String requestTimeStr = line.split(", ")[0];
                    //Read subsequent lines until empty line or end of file
                    ArrayList<String> messageBytes = new ArrayList<>();
                    line = bufferedReader.readLine();
                    lineNumber++;
                    while(line != null && !line.isEmpty())
                    {
                        messageBytes.addAll(Arrays.asList(line.split(", ")));
                        line = bufferedReader.readLine();
                        lineNumber++;
                    }

                    ParameterResponse packet = parseSingleParameterResponse(messageBytes, requestTimeStr);
                    if(packet != null) {
                        if(!headerWritten)
                        {
                            List<String> labels = packet.getSupportedLabels();
                            String headerLine = "TIME,"+String.join(",", labels);
                            bufferedWriter.write(headerLine + System.lineSeparator());
                            headerWritten = true;
                        }
                        List<String> values = packet.getValuesForSupportedLabels();
                        String valuesLine = packet.getRequestTime() + ","+String.join(",", values);
                        bufferedWriter.write(valuesLine + System.lineSeparator());
                    }
                    else {
                        System.out.println("Warning: Skipped bad message at line number: " + lineNumber);
                    }
                }
                line = bufferedReader.readLine();
                lineNumber++;
            }

        }

    }

    private static byte[] decodeHexByteStrList(List<String> byteStrList)
    {
        byte[] bytes = new byte[byteStrList.size()];
        for(int i=0; i<byteStrList.size(); i++)
        {
            String b = byteStrList.get(i);
            int decimal = Integer.parseInt(b.substring(2), 16);
            bytes[i] = (byte)decimal;
        }
        return bytes;
    }

    public static void main(String[] args){

        if(args.length != 1)
        {
            System.out.println("ERROR: The CSV file path to parse should be specified as a first argument.");
            System.exit(1);
        }

        File exportedCsvFile = new File(args[0]);
        File outputFile = new File(exportedCsvFile.getName() + "_exported_" + System.currentTimeMillis() + ".csv");

        try {
            Parser.parse(exportedCsvFile, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
