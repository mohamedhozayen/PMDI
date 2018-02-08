package main;

import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;

public class ParserPMDIGUI {

	private JFrame frame;
	private JButton btnSelectFileTo;
	private String filePath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ParserPMDIGUI window = new ParserPMDIGUI();
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
	public ParserPMDIGUI() {
		initialize();
	}

	Runnable backgroundParsing = new Runnable() {
		public void run() {
		}
	};
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		btnSelectFileTo = new JButton("Select File to be Parsed and start parsing\r\n");
		btnSelectFileTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				//FileNameExtensionFilter filter = new FileNameExtensionFilter(
					//	"JPG & GIF Images", "jpg", "gif");
				//chooser.setFileFilter(filter);
				String path = "C:\\Users\\Mohamed Hozayen\\Downloads\\reexperimentsetup";
			    chooser.setCurrentDirectory(new File(path));
			    
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					filePath = chooser.getSelectedFile().getPath();
				}

				ParsePMDI parser = new ParsePMDI();

				String fileReadPath = filePath;

				String fileReadName = new File(fileReadPath).getName();;
				parser.setFileWrite(parser.createTextFile(fileReadName).getPath());
				try {
					parser.read(fileReadPath);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGap(62)
						.addComponent(btnSelectFileTo)
						.addContainerGap(66, Short.MAX_VALUE))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGap(99)
						.addComponent(btnSelectFileTo)
						.addContainerGap(116, Short.MAX_VALUE))
				);
		frame.getContentPane().setLayout(groupLayout);
	}
}
