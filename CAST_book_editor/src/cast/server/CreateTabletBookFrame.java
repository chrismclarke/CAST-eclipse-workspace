package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import cast.utils.*;


public class CreateTabletBookFrame extends JFrame {
	static final private Color kBackgroundColor = new Color(0xeeeeff);
	
	private JButton copyCoreButton;
	private JCheckBox copyVideosCheck;
	private JComboBox bookChoice;
	private JLabel finishedLabel;
	private CastProgressBar copyStageProgress, copyItemProgress;
	private File castSourceDir, castDestDir;
	
	private CreateTabletBookTask copyTask;
	
	public CreateTabletBookFrame(File castSourceDir) {
		super("Create copy of CAST with only one e-book for tablet");
		this.castSourceDir = castSourceDir;
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				JPanel bookChoicePanel = new JPanel();
				bookChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
				
					JLabel bookLabel = new JLabel("Book:", JLabel.RIGHT);
				bookChoicePanel.add(bookLabel);
					
					bookChoice = new JComboBox();
					File[] bookFile = castSourceDir.listFiles( new FilenameFilter() {
																								public boolean accept(File dir, String name) {
																									return name.startsWith("book_");
																								}
																						});
					for (int i=0 ; i<bookFile.length ; i++) {
						String fileName = bookFile[i].getName();
						String bookName = fileName.substring(5, fileName.length() - 5);
						bookChoice.addItem(bookName);
					}
					bookChoice.setSelectedItem("public");
					bookLabel.setLabelFor(bookChoice);
				bookChoicePanel.add(bookChoice);
				
			topPanel.add(bookChoicePanel);
		
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																												VerticalLayout.VERT_TOP, 0));
					copyVideosCheck = new JCheckBox("Copy videos");
				buttonPanel.add(copyVideosCheck);
					
					copyCoreButton = new JButton("Create copy of CAST...");
					copyCoreButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											File destDir = chooseCopyLocation();
											if (destDir != null) {
												castDestDir = new File(destDir, "CAST");
												if (castDestDir.exists()) {
													Object[] options = {"Delete it", "Cancel"};
													int n = JOptionPane.showOptionDialog(CreateTabletBookFrame.this, "A CAST folder already exists in this location.",
																				"Delete existing CAST folder?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
																				null, options, options[1]);
													if (n == JOptionPane.YES_OPTION)
														deleteDir(castDestDir);
													else
														return;
												}
												finishedLabel.setText("Finding book contents...");
												String book = (String)bookChoice.getSelectedItem();
												boolean copyVideos = copyVideosCheck.isSelected();
												copyFiles(book, copyVideos);
											}
										}
									});
				buttonPanel.add(copyCoreButton);
				
			topPanel.add(buttonPanel);
		
		add("North", topPanel);
		
			JPanel messagePanel = new JPanel();
			messagePanel.setLayout(new FixedSizeLayout(200, 40));
				finishedLabel = new JLabel("", Label.LEFT);
				finishedLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
				finishedLabel.setForeground(Color.blue);
			messagePanel.add(finishedLabel);
		add("Center", messagePanel);
			
			JPanel progressPanel = new JPanel();
			progressPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, 10));
			
				copyStageProgress = new CastProgressBar("Copy: stage progress");
			progressPanel.add(copyStageProgress);
			
				copyItemProgress = new CastProgressBar("Copy: item progress");
			progressPanel.add(copyItemProgress);
			
		add("South", progressPanel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
	}
	
	
	private void copyFiles(String book, boolean copyVideos) {
		copyTask = new CreateTabletBookTask(castSourceDir, castDestDir, book,	copyVideos,
																						finishedLabel, copyStageProgress, copyItemProgress);
		copyTask.execute();
	}
	
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
					return false;
			}
		}
		return dir.delete();
  }
	
	private File chooseCopyLocation() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select folder in which \"CAST\" folder will be created");
		fc.setFileHidingEnabled(true);

		int result = fc.showOpenDialog(this);
	
		switch (result) {
			case JFileChooser.APPROVE_OPTION:
				return fc.getSelectedFile();
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
		}
		return null;
	}
		
}
