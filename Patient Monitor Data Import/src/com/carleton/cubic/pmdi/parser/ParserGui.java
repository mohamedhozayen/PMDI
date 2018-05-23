package com.carleton.cubic.pmdi.parser;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParserGui {

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
                    ParserGui window = new ParserGui();
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
    public ParserGui() {
        initialize();
    }

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
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    filePath = chooser.getSelectedFile().getPath();
                }

                String fileCreationDate = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss").format(new Date());
                File inputFile = new File(filePath);
                File outputFile = new File(inputFile.getName() + "_pmdi_parsed_" + fileCreationDate + ".csv");

                try {
                    btnSelectFileTo.setText("Parsing " + inputFile.getName() + " ....");
                    btnSelectFileTo.setEnabled(false);
                    Parser.parse(inputFile, outputFile);
                    JOptionPane.showMessageDialog(frame, "Parsing Complete. \n Output file stored at: " + outputFile.getAbsolutePath());

                } catch (java.io.IOException ioException) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ioException.printStackTrace(pw);
                    String sStackTrace = sw.toString();
                    JOptionPane.showMessageDialog(frame, "Parsing Error: \n" + sStackTrace);
                    ioException.printStackTrace();

                }
                finally {
                    btnSelectFileTo.setText("Select File to be Parsed and start parsing\r\n");
                    btnSelectFileTo.setEnabled(true);
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
