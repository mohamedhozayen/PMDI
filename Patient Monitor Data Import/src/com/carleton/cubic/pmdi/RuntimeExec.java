package com.carleton.cubic.pmdi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RuntimeExec {
	public StreamWrapper getStreamWrapper(InputStream is, String type){
		return new StreamWrapper(is, type);
	}
	private class StreamWrapper extends Thread {
		InputStream is = null;
		String type = null;          
		String message = null;

		public String getMessage() {
			return message;
		}

		StreamWrapper(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				StringBuffer buffer = new StringBuffer();
				String line = null;
				while ( (line = br.readLine()) != null) {
					buffer.append(line);//.append("\n");
				}
				message = buffer.toString();
			} catch (IOException ioe) {
				ioe.printStackTrace();  
			}
		}
	}

	public RuntimeExec() {
	}


	public String action() {
		Runtime rt = Runtime.getRuntime();
		//RuntimeExec rte = new RuntimeExec();
		StreamWrapper error, output = null;
		//String [] command =  {"cmd.exe /c dir"};
		try {
			Process proc = rt.exec(String.format("cmd.exe /c mode"));
			error = getStreamWrapper(proc.getErrorStream(), "ERROR");
			output = getStreamWrapper(proc.getInputStream(), "OUTPUT");
			int exitVal = 0;

			error.start();
			output.start();
			error.join(3000);
			output.join(3000);
			exitVal = proc.waitFor();
			//System.out.println("Output: "+ output.message + "\nError: "+error.message);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return output.message;
	}
	
	/*
	 * parse COMXX - won't work if COMXXX (three numbers)!!
	 */
	public ArrayList<String> parseCOM(String s) {
		ArrayList<String> r = new ArrayList<String>();

		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == 'C') {
				if(s.charAt(i+1) == 'O' && s.charAt(i+2) == 'M' &&i < s.length()) {
					r.add(s.substring(i, i+5).replace(":", ""));
				}
			}
		}
		return r;
	}

	// this is where the action is
	public static void main(String[] args) {

		RuntimeExec e = new RuntimeExec();
		System.out.println(e.parseCOM(e.action()).get(0));
	}
}
