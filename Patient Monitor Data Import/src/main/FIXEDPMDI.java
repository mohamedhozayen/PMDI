package main;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.comm.CommPortIdentifier;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class FIXEDPMDI {

	private static ReadWrite read = new ReadWrite();
	private JFrame frame;
	private JPanel panel_2;
	private JPanel panel_1;
	private JPanel panel_4;
	private JPanel panel_3;
	private JPanel panel;
	private JTextArea textArea;
	private JLabel lblNewLabel;
	private JComboBox<String> comboBox;
	private JButton BtnOpenPort;
	private JButton btnTestConnection;
	private JButton btnSelectDirectory;
	private JButton btnStartRecording;
	private JButton btnClear;
	private Thread mainThread;
	private Thread viewThread;

	private static byte [] msg = new byte [] {0x00, (byte) 0xA5, 0x02, 0x00, 0x77, (byte) 0x1E};
	private JScrollPane scrollPane;
	String startmsg = " PMDI imports data from a patient monitor type:\n"
			+ " 	Infinity Delta XL\n\n"
			+ " To start recording, select a port and open this port\n"
			+ " Further intstructions are provided when done so\n\n"
			+ " Note: a serial cable must be connected before the app launches\n"
			+ " if not, connect the cable and restart the app"
			+ "\n\n";
	private JButton btnAbout;
	private JButton btnCurrentDirectory;
	private JTextField HRalarm;
	private JPanel panel_5;
	private JPanel panel_6;
	private JPanel panel_7;
	private JPanel panel_8;
	private JPanel panel_9;
	private JPanel panel_10;
	private JTextField HRdisplay;
	private JPanel panel_11;
	private JTextField SPOalarm;
	private JPanel panel_12;
	private JTextField SPOdisplay;
	private JPanel panel_13;
	private JTextField PLsalarm;
	private JPanel panel_14;
	private JTextField PLsdisplay;
	private JPanel panel_15;
	private JTextField RRalram;
	private JPanel panel_16;
	private JTextField RRdisplay;
	private JPanel panel_17;
	private JTextField textField_8;
	private JTextField txtSpo;
	private JTextField txtPls;
	private JTextField txtRr;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FIXEDPMDI window = new FIXEDPMDI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FIXEDPMDI() {
		initialize();
		createEvents();
		comboBoxAddItems();
	}


	private void createEvents(){
		BtnOpenPort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(BtnOpenPort.getText() == "Open Port") {
					String sel = (String)comboBox.getSelectedItem();
					if(sel == comboBox.getItemAt(0)){
						textArea.append("\nNo serial port is selected\n");
					}else if(read.openPort(sel)) {
						textArea.append("\n"+sel + " - open success\n"
								+ "1. Test connection\n"
								+ "2. Select direcotry where data to be saved\n"
								+ "3. Start Recording \n");
						comboBox.setEnabled(false);
						btnSelectDirectory.setEnabled(true);
						btnCurrentDirectory.setEnabled(true);
						btnStartRecording.setEnabled(true);
						btnTestConnection.setEnabled(true); 
						BtnOpenPort.setText("Close Port");
					}else {
						textArea.append(sel + "\n open failed");
					}
				}else {//close port
					read.loopControl(false);
					read.closePort();
					comboBox.setEnabled(true);
					btnSelectDirectory.setEnabled(false);
					btnCurrentDirectory.setEnabled(false);
					btnStartRecording.setEnabled(false);
					btnTestConnection.setEnabled(false); 
					BtnOpenPort.setText("Open Port");
					textArea.append("\n\n" + read.getPortNum() + " close success\n");
				}

			}
		});

		btnTestConnection.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String s = read.writeMessage(msg, 1);
				if(s.equals("")) {//test failed
					textArea.setText("\nTest connection fail!\n\n"
							+ "No data received\n"
							+ "Check other ports\n"
							+ "Monitor might be connected to another port\n");
				}else {
					textArea.setText("\nTest connection success!\n\n");
					textArea.append(s.substring(0, 80) + " ...\n");
				}

			}
		});

		btnSelectDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				String s = "";
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//chooser.setCurrentDirectory(new File(path));
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					s = chooser.getSelectedFile().getPath();
				}
				read.setpath(s);
				textArea.append("\n\nThe selected directory is\n" + s);
			}
		});

		btnStartRecording.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if(btnStartRecording.getText() == "Start Recording") {
					btnSelectDirectory.setEnabled(false);
					btnTestConnection.setEnabled(false);
					read.loopControl(true);
					mainThread = new Thread(new Runnable() {
						public void run() {
							read.loopMessage(msg);
						}
					});
					viewThread = new Thread(new Runnable(){
						public void run() {
							int i = 0;//to clean text area
							while(mainThread.isAlive()) {
								try {Thread.sleep(1100);} catch (InterruptedException e) {e.printStackTrace();}
								textArea.append(read.getShortCopy() + "...\n");
								read.clearShortCopy();
								i++;
								if(i>9) {
									textArea.setText("...\n");
									i=0;
								}
							}
						}
					});
					textArea.setText("");
					mainThread.start();
					viewThread.start();
					btnStartRecording.setText("Stop Recording");
				}else {//stop recording
					btnSelectDirectory.setEnabled(true);
					btnTestConnection.setEnabled(true);
					read.loopControl(false);
					btnStartRecording.setText("Start Recording");
					textArea.append("Data is saved at " + read.getFilepath() +"\n");
				}
			}
		});

		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		});
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText(startmsg);
			}
		});

		btnCurrentDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(read.getpath().equals("")) {
					textArea.append("\n\nNo directory selected, the dedault one is\n"
							+ read.getdefaultpath());

					textArea.append("\nData will be saved in a PMDI_Data folder when recording starts");
					textArea.append("\nPress Select Directory if you wish to change the default one\n");
				}else {
					textArea.append("\n\n" + read.getpath());
					if(read.getFilepath().equals("")) {
						textArea.append("\nData will be saved in a PMDI_Data folder when recording starts");
					}else {
						textArea.append("\\PMDI_Data\n");
					}

				}
			}
		});
	}

	private void comboBoxAddItems(){
		comboBox.removeAllItems();
		comboBox.addItem("select");
		CommPortIdentifier portId;
		Enumeration<?> portList;
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
		}else {
			BtnOpenPort.setEnabled(true);
		}
		if(comboBox.getItemCount() == 2){
			comboBox.setSelectedIndex(1);
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir")
				+"\\images\\PMDI_Icon_colour.png"));
		frame.setTitle("PMDI - Patient Monitor Data Import");
		frame.setSize(1100,750);

		//Centers the GUI Frame in the middle of the screen
		frame.setLocationRelativeTo(null);

		panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		panel_2 = new JPanel();
		panel_2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(panel, GroupLayout.DEFAULT_SIZE, 1058, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
										.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
										.addGap(36)
										.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 509, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 183, GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
								.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))
						.addContainerGap())
				);
		
		panel_5 = new JPanel();
		panel_5.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		panel_6 = new JPanel();
		panel_6.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		panel_11 = new JPanel();
		
		SPOalarm = new JTextField();
		SPOalarm.setText("---");
		SPOalarm.setSelectedTextColor(Color.BLACK);
		SPOalarm.setHorizontalAlignment(SwingConstants.CENTER);
		SPOalarm.setFont(new Font("Tahoma", Font.BOLD, 35));
		SPOalarm.setEnabled(false);
		SPOalarm.setDisabledTextColor(Color.BLACK);
		SPOalarm.setColumns(5);
		SPOalarm.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_11.add(SPOalarm);
		
		panel_12 = new JPanel();
		
		SPOdisplay = new JTextField();
		SPOdisplay.setText("---");
		SPOdisplay.setSelectedTextColor(Color.BLACK);
		SPOdisplay.setHorizontalAlignment(SwingConstants.CENTER);
		SPOdisplay.setFont(new Font("Tahoma", Font.BOLD, 35));
		SPOdisplay.setEnabled(false);
		SPOdisplay.setDisabledTextColor(Color.BLACK);
		SPOdisplay.setColumns(5);
		SPOdisplay.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_12.add(SPOdisplay);
		GroupLayout gl_panel_6 = new GroupLayout(panel_6);
		gl_panel_6.setHorizontalGroup(
			gl_panel_6.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_6.createSequentialGroup()
					.addContainerGap(30, Short.MAX_VALUE)
					.addComponent(panel_12, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(panel_11, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(21))
		);
		gl_panel_6.setVerticalGroup(
			gl_panel_6.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panel_6.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_6.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_12, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_11, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(22, Short.MAX_VALUE))
		);
		panel_6.setLayout(gl_panel_6);
		
		panel_7 = new JPanel();
		panel_7.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		panel_13 = new JPanel();
		
		PLsalarm = new JTextField();
		PLsalarm.setText("---");
		PLsalarm.setSelectedTextColor(Color.GREEN);
		PLsalarm.setHorizontalAlignment(SwingConstants.CENTER);
		PLsalarm.setFont(new Font("Tahoma", Font.BOLD, 35));
		PLsalarm.setEnabled(false);
		PLsalarm.setDisabledTextColor(Color.GREEN);
		PLsalarm.setColumns(5);
		PLsalarm.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_13.add(PLsalarm);
		
		panel_14 = new JPanel();
		
		PLsdisplay = new JTextField();
		PLsdisplay.setText("---");
		PLsdisplay.setSelectedTextColor(Color.GREEN);
		PLsdisplay.setHorizontalAlignment(SwingConstants.CENTER);
		PLsdisplay.setFont(new Font("Tahoma", Font.BOLD, 35));
		PLsdisplay.setEnabled(false);
		PLsdisplay.setDisabledTextColor(Color.GREEN);
		PLsdisplay.setColumns(5);
		PLsdisplay.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_14.add(PLsdisplay);
		GroupLayout gl_panel_7 = new GroupLayout(panel_7);
		gl_panel_7.setHorizontalGroup(
			gl_panel_7.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_7.createSequentialGroup()
					.addContainerGap(30, Short.MAX_VALUE)
					.addComponent(panel_14, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(panel_13, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(21))
		);
		gl_panel_7.setVerticalGroup(
			gl_panel_7.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_7.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_7.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_14, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_13, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(22, Short.MAX_VALUE))
		);
		panel_7.setLayout(gl_panel_7);
		
		panel_8 = new JPanel();
		panel_8.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		panel_15 = new JPanel();
		
		RRalram = new JTextField();
		RRalram.setText("---");
		RRalram.setSelectedTextColor(Color.GREEN);
		RRalram.setHorizontalAlignment(SwingConstants.CENTER);
		RRalram.setFont(new Font("Tahoma", Font.BOLD, 35));
		RRalram.setEnabled(false);
		RRalram.setDisabledTextColor(Color.GREEN);
		RRalram.setColumns(5);
		RRalram.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_15.add(RRalram);
		
		panel_16 = new JPanel();
		
		RRdisplay = new JTextField();
		RRdisplay.setText("---");
		RRdisplay.setSelectedTextColor(Color.GREEN);
		RRdisplay.setHorizontalAlignment(SwingConstants.CENTER);
		RRdisplay.setFont(new Font("Tahoma", Font.BOLD, 35));
		RRdisplay.setEnabled(false);
		RRdisplay.setDisabledTextColor(Color.GREEN);
		RRdisplay.setColumns(5);
		RRdisplay.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_16.add(RRdisplay);
		GroupLayout gl_panel_8 = new GroupLayout(panel_8);
		gl_panel_8.setHorizontalGroup(
			gl_panel_8.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_8.createSequentialGroup()
					.addContainerGap(30, Short.MAX_VALUE)
					.addComponent(panel_16, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(panel_15, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(21))
		);
		gl_panel_8.setVerticalGroup(
			gl_panel_8.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_8.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_8.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_16, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_15, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_8.setLayout(gl_panel_8);
		
		panel_17 = new JPanel();
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_17, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 391, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 391, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 391, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 391, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(29, Short.MAX_VALUE))
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addGap(32)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_17, GroupLayout.PREFERRED_SIZE, 412, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_panel_2.createSequentialGroup()
							.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(28, Short.MAX_VALUE))
		);
		
		textField_8 = new JTextField();
		textField_8.setFont(new Font("Tahoma", Font.PLAIN, 16));
		textField_8.setText("HR");
		textField_8.setEditable(false);
		textField_8.setColumns(10);
		
		txtSpo = new JTextField();
		txtSpo.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtSpo.setText("SpO2");
		txtSpo.setEditable(false);
		txtSpo.setColumns(10);
		
		txtPls = new JTextField();
		txtPls.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtPls.setText("PLs");
		txtPls.setEditable(false);
		txtPls.setColumns(10);
		
		txtRr = new JTextField();
		txtRr.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtRr.setText("RR");
		txtRr.setEditable(false);
		txtRr.setColumns(10);
		GroupLayout gl_panel_17 = new GroupLayout(panel_17);
		gl_panel_17.setHorizontalGroup(
			gl_panel_17.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_17.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_17.createParallelGroup(Alignment.LEADING)
						.addComponent(textField_8, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtSpo, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtPls, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtRr, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_panel_17.setVerticalGroup(
			gl_panel_17.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_17.createSequentialGroup()
					.addGap(37)
					.addComponent(textField_8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(84)
					.addComponent(txtSpo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 84, Short.MAX_VALUE)
					.addComponent(txtPls, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(87)
					.addComponent(txtRr, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(32))
		);
		panel_17.setLayout(gl_panel_17);
		
		panel_9 = new JPanel();
		
		panel_10 = new JPanel();
		
		HRdisplay = new JTextField();
		HRdisplay.setText("---");
		HRdisplay.setSelectedTextColor(Color.BLUE);
		HRdisplay.setHorizontalAlignment(SwingConstants.CENTER);
		HRdisplay.setFont(new Font("Tahoma", Font.BOLD, 35));
		HRdisplay.setEnabled(false);
		HRdisplay.setDisabledTextColor(Color.BLUE);
		HRdisplay.setColumns(5);
		HRdisplay.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_10.add(HRdisplay);
		GroupLayout gl_panel_5 = new GroupLayout(panel_5);
		gl_panel_5.setHorizontalGroup(
			gl_panel_5.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_5.createSequentialGroup()
					.addContainerGap(64, Short.MAX_VALUE)
					.addComponent(panel_10, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel_9, GroupLayout.PREFERRED_SIZE, 163, GroupLayout.PREFERRED_SIZE)
					.addGap(21))
		);
		gl_panel_5.setVerticalGroup(
			gl_panel_5.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_5.createSequentialGroup()
					.addContainerGap(22, Short.MAX_VALUE)
					.addGroup(gl_panel_5.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_10, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_9, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		
		HRalarm = new JTextField();
		panel_9.add(HRalarm);
		HRalarm.setText("---");
		HRalarm.setSelectedTextColor(Color.BLUE);
		HRalarm.setHorizontalAlignment(SwingConstants.CENTER);
		HRalarm.setFont(new Font("Tahoma", Font.BOLD, 35));
		HRalarm.setEnabled(false);
		HRalarm.setDisabledTextColor(Color.BLUE);
		HRalarm.setColumns(5);
		HRalarm.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_5.setLayout(gl_panel_5);
		panel_2.setLayout(gl_panel_2);



		lblNewLabel = new JLabel("Communication window:\r\n");

		btnClear = new JButton("clear");

		scrollPane = new JScrollPane();

		btnAbout = new JButton("about");


		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
										.addComponent(btnAbout)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnClear))
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 484, GroupLayout.PREFERRED_SIZE))
						.addContainerGap())
				);
		gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
										.addComponent(btnClear)
										.addComponent(btnAbout)))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 396, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(17, Short.MAX_VALUE))
				);

		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEnabled(false);
		textArea.setEditable(false);
		textArea.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		textArea.setDisabledTextColor(Color.BLACK);
		textArea.setFont(new Font("Tahoma", Font.BOLD, 15));

		textArea.append(startmsg);
		panel_1.setLayout(gl_panel_1);

		panel_3 = new JPanel();

		panel_4 = new JPanel();
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
						.addGap(24))
				);
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
										.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
										.addGap(1))
								.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
						.addGap(23))
				);

		btnSelectDirectory = new JButton("Select Directory");
		btnStartRecording = new JButton("Start Recording");
		BtnOpenPort = new JButton("Open Port");
		btnTestConnection = new JButton("Test Connection");
		btnSelectDirectory.setEnabled(false);
		btnStartRecording.setEnabled(false);
		BtnOpenPort.setEnabled(false);
		btnTestConnection.setEnabled(false);

		btnCurrentDirectory = new JButton("Current Directory");

		btnCurrentDirectory.setEnabled(false);

		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
		gl_panel_4.setHorizontalGroup(
				gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_4.createSequentialGroup()
						.addGap(23)
						.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
								.addComponent(btnStartRecording, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panel_4.createSequentialGroup()
										.addComponent(btnSelectDirectory, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnCurrentDirectory, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(90, Short.MAX_VALUE))
				);
		gl_panel_4.setVerticalGroup(
				gl_panel_4.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_4.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
								.addComponent(btnCurrentDirectory, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panel_4.createSequentialGroup()
										.addComponent(btnSelectDirectory, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
										.addGap(26)
										.addComponent(btnStartRecording, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(37, Short.MAX_VALUE))
				);
		panel_4.setLayout(gl_panel_4);

		comboBox = new JComboBox<String>();


		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
				gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_3.createSequentialGroup()
										.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
										.addComponent(btnTestConnection, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
										.addGap(38))
								.addGroup(gl_panel_3.createSequentialGroup()
										.addComponent(BtnOpenPort, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
										.addContainerGap(309, Short.MAX_VALUE))))
				);
		gl_panel_3.setVerticalGroup(
				gl_panel_3.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_3.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
								.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnTestConnection, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
						.addGap(28)
						.addComponent(BtnOpenPort, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(35, Short.MAX_VALUE))
				);
		panel_3.setLayout(gl_panel_3);
		panel.setLayout(gl_panel);
		frame.getContentPane().setLayout(groupLayout);
	}
}
