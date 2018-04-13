package cast.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import cast.utils.*;


public class CopyCollectionFrame extends JFrame {
	static final private Color kBackgroundColor = new Color(0xeeeeff);
	
	private JButton copyCoreButton;
	private JComboBox collectionChoice;
	private JLabel finishedLabel;
	private CastProgressBar copyStageProgress, copyItemProgress;
	private File castSourceDir, castDestDir;
	
	private CopyCollectionTask copyTask;
	
	public CopyCollectionFrame(File castSourceDir) {
		super("Create copy of CAST with only core files");
		this.castSourceDir = castSourceDir;
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
//				JPanel fixedSizePanel = new JPanel();
//				fixedSizePanel.setLayout(new FixedSizeLayout(300, 1));
//			topPanel.add(fixedSizePanel);
			
				JPanel collectionPanel = new JPanel();
				collectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
				
					JLabel collectionLabel = new JLabel("Collection:", JLabel.RIGHT);
				collectionPanel.add(collectionLabel);
					
					collectionChoice = new JComboBox();
					File[] collectionFile = castSourceDir.listFiles( new FilenameFilter() {
																								public boolean accept(File dir, String name) {
																									return name.startsWith("collection_");
																								}
																						});
					for (int i=0 ; i<collectionFile.length ; i++) {
						String fileName = collectionFile[i].getName();
						String collectionName = fileName.substring(11, fileName.length() - 5);
						collectionChoice.addItem(collectionName);
					}
					collectionChoice.setSelectedItem("public");
					collectionLabel.setLabelFor(collectionChoice);
				collectionPanel.add(collectionChoice);
				
			topPanel.add(collectionPanel);
		
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					copyCoreButton = new JButton("Create copy of CAST...");
					copyCoreButton.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											File destDir = chooseCopyLocation();
											if (destDir != null) {
												castDestDir = new File(destDir, "CAST");
												if (castDestDir.exists()) {
													Object[] options = {"Replace it", "Cancel"};
													int n = JOptionPane.showOptionDialog(CopyCollectionFrame.this, "A CAST folder already exists in this location.",
																				"Replace existing CAST folder?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
																				null, options, options[1]);
													if (n == JOptionPane.YES_OPTION)
														deleteDir(castDestDir);
													else
														return;
												}
												finishedLabel.setText("Finding book contents...");
												String collection = (String)collectionChoice.getSelectedItem();
												copyFiles(collection);
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
	
	
	private void copyFiles(String collection) {
		File coreDir = new File(castSourceDir, "core");
		File booksDir = new File(coreDir, "collections");
		File coreBooksFile = new File(booksDir, collection + "_books.text");
		
		HashSet<String> bookNames = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(coreBooksFile));
			String bookName;
			while ((bookName = br.readLine()) != null) {
				bookNames.add(bookName);
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("Cannot find file with core books");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Cannot read file with core books");
			e.printStackTrace();
		}
		
		copyTask = new CopyCollectionTask(castSourceDir, castDestDir, collection, bookNames,	
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
