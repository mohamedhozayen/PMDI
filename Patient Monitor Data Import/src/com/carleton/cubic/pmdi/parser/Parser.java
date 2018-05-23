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

        List<String> valueBytes = subPacketBytes.subList(statusByteIndex + 1, subPacketBytes.size());
        subPacket.setAsciiValue(hexByteStrListToAsciiString(valueBytes));

        return subPacket;
    }


    public static ParameterPacket parseSinglePacket(List<String> packetBytes, String requestTimeStr) {


        int packetLength = Integer.decode(packetBytes.get(2) + packetBytes.get(1).substring(2));
        if(packetLength != packetBytes.size() - 3) // Length excludes sync byte and the length bytes
        {
            return null;
        }
        //TODO: Should checksum be validated

        ParameterPacket packet = new ParameterPacket();
        packet.setRequestTime(requestTimeStr);

        String subPacketType = packetBytes.get(3);
        String numberOfParameterSubPacketsStr = packetBytes.get(23);
        int numberOfParameterSubPackets = Integer.decode(numberOfParameterSubPacketsStr);
        int nextSubPacketLengthByteIndex = 24;
        for(int numSubPacket = 0; numSubPacket < numberOfParameterSubPackets; numSubPacket++)
        {
            int lengthOfSubPacket = Integer.decode(packetBytes.get(nextSubPacketLengthByteIndex));
            int numberOfParameters = Integer.decode(packetBytes.get(nextSubPacketLengthByteIndex + 1));

            int inSubPacketIndex = nextSubPacketLengthByteIndex + 7;
            for(int i=0; i<numberOfParameters; i++)
            {
                List<String> subPacketBytes = new ArrayList<>();
                inSubPacketIndex++;
                subPacketBytes.add(packetBytes.get(inSubPacketIndex)); //Code Byte
                if(subPacketType.equals("0x77"))
                {
                    inSubPacketIndex++;
                    subPacketBytes.add(packetBytes.get(inSubPacketIndex));
                }

                inSubPacketIndex++; //Status Byte
                subPacketBytes.add(packetBytes.get(inSubPacketIndex));


                inSubPacketIndex++; //Add a value byte
                String addedByte = packetBytes.get(inSubPacketIndex);
                subPacketBytes.add(addedByte);
                int maxRemainingValueBytes = 4;
                while(maxRemainingValueBytes > 0 && !addedByte.equals("0x00"))
                {
                    inSubPacketIndex++; //Add a value byte
                    addedByte = packetBytes.get(inSubPacketIndex);
                    subPacketBytes.add(addedByte);
                    maxRemainingValueBytes--;
                }

                ParameterSubPacket subPacket = parseSingleParameterSubPacket(subPacketBytes, subPacketType);
                packet.addSubPacket(subPacket);
            }

            nextSubPacketLengthByteIndex =  nextSubPacketLengthByteIndex + lengthOfSubPacket;

        }
        return packet;
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
                    ArrayList<String> packetCodes = new ArrayList<>();
                    line = bufferedReader.readLine();
                    lineNumber++;
                    while(line != null && !line.isEmpty())
                    {
                        packetCodes.addAll(Arrays.asList(line.split(", ")));
                        line = bufferedReader.readLine();
                        lineNumber++;
                    }

                    ParameterPacket packet = parseSinglePacket(packetCodes, requestTimeStr);
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
                        System.out.println("Warning: Skipped bad packet at line number: " + lineNumber);
                    }
                }
                line = bufferedReader.readLine();
                lineNumber++;
            }

        }

    }

    private static String hexByteStrListToAsciiString(List<String> byteStrList)
    {
        StringBuilder valueStringBuilder = new StringBuilder();
        for(String b: byteStrList)
        {
            int decimal = Integer.parseInt(b.substring(2), 16); // Trims the 0x part and concerts to hexadecimal
            valueStringBuilder.append((char)decimal); // Append as ascii character
        }

        return valueStringBuilder.toString();
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
