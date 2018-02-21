package main;


import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.comm.*;

public class ReadWrite implements SerialPortEventListener{
	private static CommPortIdentifier portId;
	private static Enumeration portList;
	protected InputStream inputStream;
	protected static SerialPort serialPort;
	protected static OutputStream outputStream;

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss");

	private static List<String> portIdList = new ArrayList<String>();
	private ArrayList<Byte> buf = new ArrayList<>();

	static byte [] msg1 = new byte [] {0x00, (byte) 0xA5, 0x02, 0x00, 0x50, (byte) 0xF7};
	static byte [] msg2 = new byte [] {0x00, (byte) 0xA5, 0x02, 0x00, 0x56, (byte) 0xFD};
	static byte [] msg3 = new byte [] {0x00, (byte) 0xA5, 0x02, 0x00, 0x57, (byte) 0xFE};
	static byte [] msg4 = new byte [] {0x00, (byte) 0xA5, 0x02, 0x00, 0x77, (byte) 0x1E};


	private static String filename = "";

	public static void main(String[] args) {

		ReadWrite reader = new ReadWrite();
		reader.avialableSerialPorts(portIdList);
		reader.openPort("COM7");
		filename = createTextFile().getPath(); //-> included in write methods
		reader.writeMessage(msg4, 5);
		reader.closePort();
	}//end main

	public ReadWrite(){
	}

	//prints available serial ports and store them portIdList 
	public void avialableSerialPorts (List<String> List){
		List.clear();
		CommPortIdentifier portId;
		Enumeration portList;
		portList = CommPortIdentifier.getPortIdentifiers();	
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				//System.out.println(portId.getName());
				List.add(portId.getName());
			}//end if
		}//end while
	}

	//to know available serial port, type 'mode' in the command line or from device manager in contol panel
	//example: portNumber = COM12
	//	public ReadWrite(String portNumber) {
	//		openPort(portNumber);
	//		setupPort();
	//	}

	String portUserName = "Patient Monitor App";

	public boolean openPort(String portNumber){
		try {
			portId = CommPortIdentifier.getPortIdentifier(portNumber);
			serialPort = (SerialPort) portId.open(portUserName, 20000);
		} catch (PortInUseException e) {
			System.out.println(portUserName + " is using this serial port");
			return false;
		} catch (NoSuchPortException e) {
			e.printStackTrace();
			return false;	
		}
		return setupPort();
	}

	public void closePort(){
		serialPort.close();
	}

	public String getPortNum() {
		return serialPort.getName();
	}


	public OutputStream getOutputStream(){
		return outputStream;
	}

	public boolean setupPort(){
		//set up for serial port
		try {
			serialPort.setSerialPortParams(19200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			//closePort();
			System.out.println(e);
			return false;
		}
		//output stream to write to serial port
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			//closePort();
			System.out.println(e);
			return false;
		}
		//event listener to serial port 
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			//closePort();
			System.out.println(e);
			return false;
		}
		//input stream to capture serial port messages 
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			//closePort();
			System.out.println(e);
			return false;
		}
		serialPort.notifyOnDataAvailable(true);
		return true;
	}

	protected Boolean infinteLoop = true;

	public Boolean getinfiniteLoop(){
		return infinteLoop;
	}

	public void loopControl(Boolean check){
		infinteLoop = check;
	}

	private String ReadData = new String("");

	private boolean exception = false;
	public boolean IsException() {
		return exception;
	}
	
	private String shortCopy = "";
	public String getShortCopy() {
		return shortCopy;
	}
	
	public void clearShortCopy() {
		shortCopy = "";
	}
	public synchronized String loopMessage(byte[] msg){
		filename = createTextFile().getPath();
		String readData;
		while(infinteLoop == true){
			try {

				HR.clear();
				SpO2.clear();
				PLs.clear();

				outputStream.write(msg);
				outputStream.flush();

				LocalDateTime now = LocalDateTime.now();
				//System.out.print(dtf.format(now));
				//System.out.print(" Written data: ");
				//System.out.println();

			} catch (IOException e) {
				exception = true;//System.out.println(e);
			}
			try{
				TimeUnit.SECONDS.sleep(1);
			}catch (InterruptedException e) {
				//closePort();
				e.printStackTrace();}

			if(!buf.isEmpty()){
				readData = dumpData(buf);
				writeTextFile(filename, readData);
				writeTextFile(filename, "\n\n");
				shortCopy = readData.substring(0, 80);
				buf.clear();
			}
		}
		return null;
	}


	public String getFilepath() {
		return filename;
	}

	private static  String path = "";
	public String setpath(String path) {
		this.path = path;
		return filename;
	}
	
	public String getpath() {
		return this.path;
	}
	public String writeMessage(byte[] msg, int NumberofLoops){
		//filename = createTextFile().getPath();
		String readData = "";
		for(int i = 0; i < NumberofLoops ; i++){
			try {

				outputStream.write(msg);
				outputStream.flush();

				LocalDateTime now = LocalDateTime.now();
				//System.out.print(dtf.format(now));
				//System.out.print(" Written data: ");
				//System.out.println();

			} catch (IOException e) {
				//closePort();
				System.out.println(e);}
			try{
				TimeUnit.SECONDS.sleep(1);
			}catch (InterruptedException e) {
				//closePort();
				e.printStackTrace();}

			if(!buf.isEmpty()){
				readData = dumpData(buf);
				ReadData = readData;//return the read data globally so it could be used in another program
				//writeTextFile(filename, readData + "\n\n");
				//writeTextFile(filename, "\n\n");
				buf.clear();
			}
		}
		return readData;
	}

	public void fillBuff(ArrayList<Byte> b){
		for(byte hex : buf){
			b.add(hex);
		}
	}

	public static boolean isNumber(String string) {
		try {
			Long.parseLong(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public String decodeHR(){
		String val = "";
		for(int i = 0; i < HR.size(); i++){
			if(isNumber(HR.get(i))){
				if(Integer.parseInt(HR.get(i)) >= 30 
						&& Integer.parseInt(HR.get(i)) <= 39){
					val += (Integer.parseInt(HR.get(i))-30);
				}else if(Integer.parseInt(HR.get(i)) == 12){//unkown value
					val = "---";
					return val;
				}else{
					continue;
				}
			}
		}
		if(val.compareTo("")==0){
			val = "---";
		}
		HR.clear();
		return val;
	}
	public String decodeSpO2(){
		String val = "";
		for(int i = 0; i < SpO2.size(); i++){
			if(isNumber(SpO2.get(i))){
				if(Integer.parseInt(SpO2.get(i)) >= 30 
						&& Integer.parseInt(SpO2.get(i)) <= 39){
					val += (Integer.parseInt(SpO2.get(i))-30);
				}else if(Integer.parseInt(SpO2.get(i)) == 12){//unkown value
					val = "---";
					return val;
				}else{
					continue;
				}
			}
		}
		if(val.compareTo("")==0){
			val = "---";
		}
		SpO2.clear();
		return val;
	}
	public String decodePLs(){
		String val = "";
		for(int i = 0; i < PLs.size(); i++){
			if(isNumber(PLs.get(i))){
				if(Integer.parseInt(PLs.get(i)) >= 30 
						&& Integer.parseInt(PLs.get(i)) <= 39){
					val += (Integer.parseInt(PLs.get(i))-30);
				}else if(Integer.parseInt(PLs.get(i)) == 12){//unkown value
					val = "---";
					return val;
				}else{
					continue;
				}
			}
		}
		if(val.compareTo("")==0){
			val = "---";
		}
		PLs.clear();
		return val;
	}
	private static ArrayList<String> HR = new ArrayList<>();
	private static ArrayList<String> SpO2 = new ArrayList<>();
	private static ArrayList<String> PLs = new ArrayList<>();
	int byteCtr = 0;
	int SpO2Ctr = 0;
	int PLsCtr = 0;
	boolean SpO2Fnd = false;
	boolean PLsFnd = false;

	public void serialEvent(SerialPortEvent event) {
		switch(event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:

			try {
				while (inputStream.available() > 0) {
					byte b = (byte) inputStream.read();
					//System.out.print(String.format("0x%02X", b)+" \n");
					if(b == (byte)0xA5){
						byteCtr = 0;
						SpO2Ctr = 0;
						PLsCtr = 0;
						SpO2Fnd = false;
						PLsFnd = false;
						//String h = String.format("0x%02X", b);
						//LocalDateTime now = LocalDateTime.now();
						//System.out.println(dtf.format(now) + "\n");
					}
					buf.add(b);
					byteCtr++;
					//System.out.println(byteCtr);
					//HR
					if(byteCtr >= 33 && byteCtr <= 38){
						String temp = String.format("0x%02X", b).replaceAll("0x", "");
						HR.add(temp);
						//System.out.println(temp);
					}
					//SpO2
					if(b == (byte)0x64 || SpO2Ctr != 0){
						if(SpO2Fnd == false){
							if(SpO2Ctr < 6){
								String temp = String.format("0x%02X", b).replaceAll("0x", "");
								//System.out.println(temp);
								SpO2.add(temp);
								SpO2Ctr++;
								//print(SpO2);			
							}else{
								SpO2Fnd = true;
								//print(SpO2);		
							}
						}
					}
					//PLs
					if(b == (byte)0x65 || PLsCtr != 0){
						if(PLsFnd == false){
							if(PLsCtr < 6){
								String temp = String.format("0x%02X", b).replaceAll("0x", "");
								PLs.add(temp);
								PLsCtr++;
								//print(SpO2);			
							}else{
								PLsFnd = true;
								//print(PLs);		
							}
						}
					}
				}
			} catch (IOException e) {
				//closePort();
				System.out.println(e);}
			break;
		}//end switch
	}//end serialEvent


	public String printByteArray(byte[] arr){
		String data = " ";
		for(byte b : arr){
			//System.out.print(String.format("0x%02X", b)+" ");
			data += String.format("0x%02X", b)+" ";
		}
		System.out.println();
		return data;
	}

	public static <T extends Iterable<T>> void print(ArrayList<String> spO22){
		for (Object element : spO22){
			System.out.print(element + " ");
		}
		System.out.println();
	}

	//print byte data
	private String dumpData(ArrayList<Byte> b){
		String data = "";
		int i = 0;
		LocalDateTime now = LocalDateTime.now();
		//System.out.print(dtf.format(now));
		//System.out.println(" Read data: ");

		data += dtf.format(now) + ", Read data: \n";

		for (Byte element : b){
			i++;
			data += String.format("0x%02X", element)+ ", ";
			//System.out.print(String.format("0x%02X", element)+" ");
			if(element == 0x01){
				//System.out.println();
			}
			if(i%16 == 0){
				data += "\n";
			}
		}
		//System.out.println();
		//System.out.println();

		return data;
	}

	static File file;

	public String getdefaultpath(){
		return System.getProperty("user.dir");
	}

	private static File createTextFile(){
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now).replace('/','_').replace(' ','-').replace(':','_');

		String dir ="";
		/*
		 * use current directory by default unless
		 * the user specified a directory to save data at
		 * by using setFilepath
		 */
		if(path.equals("")) {
			dir = System.getProperty("user.dir");//get current paths of this project
		}else {
			dir = path;
		}

		/*
		 * check if filename is already a directory to a csv file
		 * if not initialize it so
		 */
		new File(dir + "\\PMDI_Data").mkdir();//create Data folder it doesn't exist
		dir += "\\PMDI_Data\\session_" + date +".csv";//create text file with current date/time to avoid overwriting


		try {
			file = new File(dir);

			if (file.createNewFile()){
				//System.out.println("File is created!");
			}else{
				//System.out.println("File already exists.");
			}
			return file;
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}


	private static boolean wrtFileExpn = false;
	public boolean fileWriteException() {
		return wrtFileExpn;
	}
	private static boolean writeTextFile(String dir, String text){
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dir, true))) {
			bw.write(text);
			//System.out.println("Done");
			return true;

		} catch (IOException e) {
			wrtFileExpn = true;
			e.printStackTrace();}
		return false;
	}

	public String getReadData() {
		return ReadData;
	}

}//end SerialCommunication