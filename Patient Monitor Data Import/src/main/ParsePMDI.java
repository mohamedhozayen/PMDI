package main;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParsePMDI {


	private static ArrayList<String> HR = new ArrayList<>();
	private static ArrayList<String> RR = new ArrayList<>();
	private static ArrayList<String> SPO2 = new ArrayList<>();
	private static ArrayList<String> PLs = new ArrayList<>();

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss");

	static String FileWrite;


	public ParsePMDI(){
	}

	/*
	 * Repeatedly prompt user for filename until a file with such a name exists
	 * and can be opened.
	 */
	private static String openFile() {

		BufferedReader keyboardReader = new BufferedReader(
				new InputStreamReader(System.in));

		String inFilePath = "";
		BufferedReader inFileReader;
		boolean pathsOK = false;

		while (!pathsOK) {
			try {
				System.out.print("Please enter the path for the input file: ");
				inFilePath = keyboardReader.readLine();
				inFileReader = new BufferedReader(new FileReader(inFilePath));
				pathsOK = true;
				inFileReader.close();
			} // try
			catch (IOException e) {
				System.out.println(e);
			} // catch I/O exception
		} // while
		return inFilePath;
	} // method openFiles


	/*
	 * Iterate over all strings in input file to determine whether the input
	 * string is a substring in any of these strings. Returns the number of
	 * times such a match exists.
	 */
	public int read(String filename)throws FileNotFoundException {

		int byteNum = 1;
		int lineNum = 0;
		int checkTotal = 0;

		int numMsg = 0;

		StringTokenizer tokens;
		String line, textword, byteMsg;

		String timestamp;

		int HRcounter = 0;
		int RRcounter = 0;
		int SPO2counter = 0;
		int PLscounter = 0;

		Boolean HRfound = false;
		Boolean RRfound = false;
		Boolean SPO2found = false;
		Boolean PLsfound = false;
		
		writeTextFile(FileWrite, "Date/Time, HR (bpm), RR (RPM), SPO2 (%), PLs (bpm) \n");

		// open file anew to ensure we start at the first character
		BufferedReader inFileReader = new BufferedReader(new FileReader(filename));

		try {
			while (true) {
			//for(int i = 0 ; i < 100; i++){


				line = inFileReader.readLine();

				if (line == null)
					break;

				//skip this line and get to the message
				if(line.contains("Read data")){
					if(byteNum != 1){
						if(byteNum-1 != checkTotal)
							System.out.println("Total number of bytes is not matching");
					}
					numMsg ++;
					byteNum = 1;
					timestamp = line.replaceAll(", Read data:", "");
					writeTextFile(FileWrite, timestamp+",");

					HRcounter = 0;
					RRcounter = 0;
					SPO2counter = 0;
					PLscounter = 0;

					//HRfound = false;
					RRfound = false;
					SPO2found = false;
					PLsfound = false;
					
					
					continue;
				}

				line = line.replaceAll("0x", " ").replaceAll(",", " ");

				tokens = new StringTokenizer(line);

				//check sync byte
				if(byteNum == 1){
					if(!tokens.nextToken().equals("A5")){
						System.out.println("Sync byte is missing!");
					}
					//get number of bytes within this message ####hex
					//2nd byte is low, 3rd is the high
					byteMsg = tokens.nextToken();
					byteMsg = tokens.nextToken() + byteMsg;
					checkTotal = hexToDec(byteMsg);
				}


				// for all the words in the line
				while (tokens.hasMoreTokens()) {
					textword = tokens.nextToken();
					byteNum++;

					//HR capture
					if(byteNum == (24+9-2)){
						while (HRcounter < 6) {
							try{
								byteNum++;
								HR.add(textword);
								HRcounter++;
								textword = tokens.nextToken();
							}catch(Exception e){
								byteNum--;
								break;
							};
						}
					}

					//RR capture
					if(textword.compareTo("60")==0 || RRcounter != 0){
						if(RRfound == false){
							while (RRcounter < 6) {
								try{
									byteNum++;
									RR.add(textword);
									RRcounter++;
									textword = tokens.nextToken();
								}catch(Exception e){
									byteNum--;
									break;
								};
							}
						}
					}
					//SPO2 capture
					if(textword.compareTo("64")==0 || SPO2counter != 0){
						if(SPO2found == false){
							while (SPO2counter < 6) {
								try{
									byteNum++;
									if(textword.compareTo("65") != 0){
										SPO2.add(textword);
									}else if(textword.compareTo("65") == 0){
										SPO2counter = 6;//done and SPO2 is unknown
										break;
									}else{
										SPO2counter++;
										byteNum--;
										break;
									}
									SPO2counter++;
									textword = tokens.nextToken();
								}catch(Exception e){
									byteNum--;
									break;
								};
							}
							//print(SPO2);
						}
					}
					//PLs rate capture
					if(textword.compareTo("65")== 0 || PLscounter != 0){
						if(PLsfound == false){
							while (PLscounter < 6) {
								if(textword.compareTo("90") == 0){//specific case
									PLscounter = 6;
									break;
								}
								try{
									byteNum++;
									PLs.add(textword);
									PLscounter++;
									textword = tokens.nextToken();
								}catch(Exception e){
									byteNum--;
									break;
								};
							}
						}
					}
					
				} // end while tokens
				if(HRcounter == 6){
					String HRvalue = decode(HR);
					writeTextFile(FileWrite, HRvalue + ",");
					//System.out.print(HRvalue+" ");
					//print(HR);
					HRcounter = 0;
					HR.clear();
				}

				if(RRcounter == 6){
					String RRvalue = decode(RR);
					writeTextFile(FileWrite, RRvalue + ",");
					//System.out.println(RRvalue + " ");
					//print(HR);
					RRcounter = 0;
					RR.clear();
					RRfound = true;
				}
				
				if(SPO2counter == 6){
					String SPO2value = decode(SPO2);
					writeTextFile(FileWrite, SPO2value + ",");
					//print(HR);
					SPO2counter = 0;
					SPO2.clear();
					//print(SPO2);
					SPO2found = true;
				}
				
				if(PLscounter == 6){
					String PLsvalue = decode(PLs);
					writeTextFile(FileWrite, PLsvalue + "\n");
					//print(HR);
					PLscounter = 0;
					//print(PLs);
					PLs.clear();
					PLsfound = true;
				}
			} // end while true
		} catch (IOException e) {
			System.out.println(e);
		} // catch I/O exception}
		try {
			inFileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return byteNum;
	}


	public static int hexToDec(String s){	
		int string = Integer.parseInt(s, 16);
		return string;
	}

	public static <T extends Iterable<T>> void print(List<String> portIdList2){
		for (String element : portIdList2){
			System.out.print(element+" ");
		}
		System.out.println();
	}

	
	private static boolean writeTextFile(String dir, List <String> lst){
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(FileWrite, true));){
			for(String s : lst){
				bw.append(s).append(",\n");
			}
			return true;

		} catch (IOException e) {e.printStackTrace();}
		return false;
	}

	private static boolean writeTextFile(String dir, String text){
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(FileWrite, true));){
			bw.write(text);
			//System.out.println("Done");
			return true;

		} catch (IOException e) {e.printStackTrace();}
		return false;
	}

	private static String decode(List <String> lst){
		String val = "";
		for(int i = 0; i < lst.size(); i++){
			if(isNumber(lst.get(i))){
				if(Integer.parseInt(lst.get(i)) >= 30 
						&& Integer.parseInt(lst.get(i)) <= 39){
					val += (Integer.parseInt(lst.get(i))-30);
				}else if(Integer.parseInt(lst.get(i)) == 12){//unkown value
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
		return val;
	}


	public static boolean isNumber(String string) {
		try {
			Long.parseLong(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public File createTextFile(String fileName){


		String dir = System.getProperty("user.dir");//get current paths of this project
		new File(dir+"\\Parsed Data").mkdir();//create Data folder it doesn't exist
		dir += "\\Parsed Data\\Parsed_" + fileName ;//create text file with current date/time to avoid overwriting

		try {
			File file = new File(dir);

			if (file.createNewFile()){
				System.out.println("File is created!");
			}else{
				System.out.println("File already exists.");
			}
			return file;
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}

	private static File FakecreateTextFile(){
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now).replace('/','_').replace(' ','-').replace(':','_');

		String dir = System.getProperty("user.dir");//get current paths of this project
		new File(dir+"\\Data").mkdir();//create Data folder it doesn't exist
		dir += "\\Parsed Data\\Parsed_" + date +".csv";//create text file with current date/time to avoid overwriting

		try {
			File file = new File(dir);

			if (file.createNewFile()){
				System.out.println("File is created!");
			}else{
				System.out.println("File already exists.");
			}
			return file;
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}
	
	public void setFileWrite (String s){
		FileWrite = s;
	}
	
	public static void main(String[] args){

		//short - C:\\session_2017_08_29-4_11_03.csv
		//long - C:\\session_2017_08_03-17_27_04.csv
		//C:\\session_2017_08_16-18_36_24.csv


		/*
		 *Testing code using fakecreatTextFile
		String fileRead = "C:\\session_2017_08_29-4_11_03.csv";
		FileWrite = FakecreateTextFile().getPath();
		try {
			read(fileRead);
		} catch (FileNotFoundException e) {e.printStackTrace();}

		 */

//		String fileReadName = "session_2017_08_16-18_36_24.csv";
//		String fileReadPath = "C:\\session_2017_08_16-18_36_24.csv";
//		FileWrite = createTextFile(fileReadName).getPath();
//		try {
//			read(fileReadPath);
//		} catch (FileNotFoundException e) {e.printStackTrace();}
		
		//print(HR);
	}

}