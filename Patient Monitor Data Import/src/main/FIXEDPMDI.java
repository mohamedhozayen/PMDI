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
			+ " To start recording select a port and open this port\n"
			+ " Further intstructions are provided when done so\n\n";
	private JButton btnAbout;
	private JButton btnCurrentDirectory;

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
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGap(0, 505, Short.MAX_VALUE)
				);
		gl_panel_2.setVerticalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGap(0, 472, Short.MAX_VALUE)
				);
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
