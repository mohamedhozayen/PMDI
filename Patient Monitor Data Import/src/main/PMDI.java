package main;


import java.awt.*;
import java.awt.EventQueue;

import javax.comm.CommPortIdentifier;

import java.awt.Color;
import java.awt.Font;
import java.awt.Button;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.util.*;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionEvent;
import java.awt.Window.Type;
import java.awt.Toolkit;


import java.awt.Color;
import java.awt.Panel;
import java.awt.Choice;

import javax.swing.border.BevelBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class PMDI{

	private JFrame frmSer;
	private Button avialablePortsButton;
	private static ReadWrite read = new ReadWrite();
	JTextArea textArea;
	Thread readThread;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					PMDI window = new PMDI();
					window.frmSer.setVisible(true);

					//					ImageIcon img1 = new ImageIcon("PMDI_Icon1.png");
					//					ImageIcon img2 = new ImageIcon("PMDI_Icon1_larger.png");
					//					List<Image> icons = new ArrayList<Image>();
					//					icons.add(img1.getImage());
					//					icons.add(img2.getImage());
					//					window.setIconImages(icons);


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}



	static byte [] msg4 = new byte [] {0x00, (byte) 0xA5, 0x02, 0x00, 0x77, (byte) 0x1E};
	private JLabel selectSerialPort;

	private List<String> portIdList = new ArrayList<String>();
	private JLabel openLabel;
	private JButton btnOpen;
	private JLabel lblCloseSelectedPort;
	private JButton btnClose;
	private JTextArea msg;
	static byte [] byteMsg = new byte [] {0x00, (byte) 0xA5, 0x02, 0x00, 0x77, (byte) 0x1E};
	private JMenuBar menuBar;
	private JMenu mnHelp;
	private JMenu mnFile;
	private JMenuItem mntmExit;
	private JMenuItem mntmAbout;

	private static JComboBox comboBox;

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss");
	static String filename;
	private JButton btnStartTest;
	private JButton btnStopTest;

	private int ThreadSleepTime = 0;


	/**
	 * Create the application.
	 */
	public PMDI() {
		//read = new ReadWrite();
		initialize();
		createEvents();
	}


	Thread loop;
	Thread display;
	private static ArrayList<String> HR = new ArrayList<>();

	Runnable backgroundThread = new Runnable() {
		public void run() {
			read.loopMessage(msg4);
		}
	};

	Runnable checkThreads = new Runnable() {
		public void run() {
			while(true) {
				while(loop.isAlive() && !read.IsException()){
					green.setBackground(new Color(0,255,0));
					green.setText("Exporting Data ...");
				}
				green.setBackground(new Color(255,255,255));
				green.setText("not reading at the moment");
				if(read.IsException()|| read.fileWriteException()) {
					frmSer.setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir")
							+"\\images\\red.png"));
				}
				try{
					TimeUnit.MILLISECONDS.sleep(350);
				}catch (InterruptedException e) {e.printStackTrace();}
				frmSer.setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir")
						+"\\images\\PMDI_Icon_colour.png"));
				try{
					TimeUnit.MILLISECONDS.sleep(350);
				}catch (InterruptedException e) {e.printStackTrace();}
				
			}
		}
	};

	Runnable displayCurrentValues = new Runnable() {
		public void run() {

			while(loop.isAlive()){
				//HRDisplay.setFont(font);
				try{
					TimeUnit.SECONDS.sleep(1);
				}catch (InterruptedException e) {e.printStackTrace();}
				if(read.IsException()){
					HRview.setText("---");
					SpO2view.setText("---");
					PLsview.setText("---");
//					//JOptionPane.showMessageDialog(null,"Issue with connection");
//					frmSer.setIconImage(Toolkit.getDefaultToolkit().getImage(
//							"C:\\Users\\Mohamed Hozayen\\workspace\\Patient Monitor Data Export\\images\\red.png"));
				}
				HRview.setText(read.decodeHR());
				SpO2view.setText(read.decodeSpO2());
				PLsview.setText(read.decodePLs());
			}//end while
			if(!loop.isAlive()){
				HRview.setText("---");
				SpO2view.setText("---");
				PLsview.setText("---");
			}
		}
	};

	public static boolean isNumber(String string) {
		try {
			Long.parseLong(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	private JTextArea green;
	private JTextField HRview;
	Font font;
	private JTextField SpO2view;
	private JTextField PLsview;
	private JTextField txtSpo;
	private JTextField txtPls;
	private JTextField txtHr;
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSer = new JFrame();
		frmSer.setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir")
				+"\\images\\PMDI_Icon_colour.png"));
		frmSer.setTitle("Patient Monitor Data Import App - PMDI");
		frmSer.setBounds(100, 100, 937, 546);
		frmSer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		selectSerialPort = new JLabel("Select Serial Port");
		String[] empty = new String[] {"Select"};

		comboBox = new JComboBox();
		comboBoxAddItems();

		openLabel = new JLabel("Open Selected Port");

		btnOpen = new JButton("Open");

		lblCloseSelectedPort = new JLabel("Close Selected Port");

		btnClose = new JButton("Close");
		btnClose.setEnabled(false);

		msg = new JTextArea();
		msg.setText("Every 1 second send in hex  \r\n 00 A5 02 00 77 1E");
		msg.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		msg.setEditable(false);

		btnStartTest = new JButton("Start Recording");
		btnStartTest.setEnabled(false);


		btnStopTest = new JButton("Stop Recording");
		btnStopTest.setEnabled(false);

		green = new JTextArea();
		green.setEditable(false);
		green.setText("Not reading at the moment");
		green.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		green.setColumns(green.getColumns());

		HRview = new JTextField();
		HRview.setDisabledTextColor(Color.BLUE);
		HRview.setForeground(new Color(0, 0, 255));
		HRview.setEnabled(false);
		HRview.setSelectedTextColor(new Color(0, 255, 0));
		HRview.setHorizontalAlignment(SwingConstants.CENTER);
		HRview.setSelectionColor(Color.GREEN);
		HRview.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		HRview.setText("---");
		HRview.setFont(new Font("Tahoma", Font.BOLD, 35));


	
		//HRview.setHorizontalAlignment(JTextField.CENTER);

		SpO2view = new JTextField();
		SpO2view.setDisabledTextColor(Color.black);
		SpO2view.setSelectedTextColor(new Color(0, 255, 0));
		SpO2view.setText("---");
		SpO2view.setHorizontalAlignment(SwingConstants.CENTER);
		SpO2view.setFont(new Font("Tahoma", Font.BOLD, 35));
		SpO2view.setEnabled(false);
		SpO2view.setColumns(5);
		SpO2view.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		PLsview = new JTextField();
		PLsview.setDisabledTextColor(Color.GREEN);
		PLsview.setText("---");
		PLsview.setHorizontalAlignment(SwingConstants.CENTER);
		PLsview.setFont(new Font("Tahoma", Font.BOLD, 35));
		PLsview.setEnabled(false);
		PLsview.setColumns(5);
		PLsview.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		txtSpo = new JTextField();
		txtSpo.setEditable(false);
		txtSpo.setText("SpO2");
		txtSpo.setColumns(10);

		txtPls = new JTextField();
		txtPls.setEditable(false);
		txtPls.setText("PLs");
		txtPls.setColumns(10);

		txtHr = new JTextField();
		txtHr.setEditable(false);
		txtHr.setText("HR");
		txtHr.setColumns(10);


		GroupLayout groupLayout = new GroupLayout(frmSer.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(38)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(openLabel)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnOpen))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(selectSerialPort)
							.addGap(28)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblCloseSelectedPort)
									.addGap(18)
									.addComponent(btnClose))
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
									.addComponent(msg, GroupLayout.PREFERRED_SIZE, 215, GroupLayout.PREFERRED_SIZE)
									.addGroup(groupLayout.createSequentialGroup()
										.addComponent(btnStartTest)
										.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnStopTest))
									.addComponent(green, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addGap(430)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(txtPls, 0, 0, Short.MAX_VALUE)
								.addComponent(txtSpo, 0, 0, Short.MAX_VALUE)
								.addComponent(txtHr, GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(SpO2view, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
								.addComponent(PLsview, GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
								.addComponent(HRview))))
					.addGap(97))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(38)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(83)
									.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
										.addComponent(openLabel)
										.addComponent(btnOpen)))
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
									.addComponent(selectSerialPort)
									.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addPreferredGap(ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblCloseSelectedPort)
								.addComponent(btnClose)
								.addComponent(txtHr, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(51))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(180)
							.addComponent(HRview, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)))
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(msg, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtSpo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(SpO2view, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnStartTest)
								.addComponent(btnStopTest))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(green, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(36)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(PLsview, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtPls, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
					.addGap(79))
		);
		frmSer.getContentPane().setLayout(groupLayout);

		menuBar = new JMenuBar();
		frmSer.setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmExit = new JMenuItem("Exit");

		mnFile.add(mntmExit);

		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);

	}//end initialize


	private void createEvents(){
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int ret = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?");
				if(ret == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}
		});

		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String current = (String) comboBox.getSelectedItem();
				if(current != comboBox.getItemAt(0)){
					try{
						read.openPort(current);
						JOptionPane.showMessageDialog(null, "open port " + current);
						btnOpen.setEnabled(false);
						btnClose.setEnabled(true);
						comboBox.setEnabled(false);
						btnStartTest.setEnabled(true);
					}catch(Exception e1){JOptionPane.showMessageDialog(null,
							"open port " + current + " is unsuccessful \n"
									+ "Maybe the port is being used by another app "
									+ "or check connection \n");}
				}else if(comboBox.getItemCount() == 1){
					JOptionPane.showMessageDialog(null, "No serial Port detected");
				}else{
					JOptionPane.showMessageDialog(null, "No serial Port Selected");
				}

			}
		});

		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				read.closePort();
				btnOpen.setEnabled(true);
				btnClose.setEnabled(false);
				comboBox.setEnabled(true);
				btnStartTest.setEnabled(false);
				btnStopTest.setEnabled(false);
			}
		});

		btnStartTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				read.loopControl(true);

				loop = new Thread(backgroundThread);
				loop.start();

				btnStopTest.setEnabled(true);
				btnStartTest.setEnabled(false);

				new Thread(displayCurrentValues).start();
				new Thread(checkThreads).start();

				JOptionPane.showMessageDialog(null, "Exported data is being saved at \n"
						+read.getFilepaths());
			}
		});

		btnStopTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//t.interrupt();
				read.loopControl(false);//this terminate the while loop
				btnStartTest.setEnabled(true);
				btnStopTest.setEnabled(false);
			}
		});
	}


	private static void comboBoxAddItems(){
		comboBox.addItem("select");
		CommPortIdentifier portId;
		Enumeration portList;
		portList = CommPortIdentifier.getPortIdentifiers();	
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				comboBox.addItem(portId.getName());
			}//end if
		}//end while
		if(comboBox.getItemCount()==1){
			comboBox.removeItemAt(0);
			comboBox.addItem("No serial port detected");
		}else if(comboBox.getItemCount() == 2){
			comboBox.setSelectedIndex(1);
		}
	}
}//end class GUI
