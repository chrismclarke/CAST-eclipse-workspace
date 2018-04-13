package cast.pageEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;


import cast.server.*;
import cast.utils.*;

	
class Trans {
	String entity;
	String utf8;
	
	Trans(String entity, String utf8) {
		this.entity = entity;
		this.utf8 = utf8;
	}
}

public class PageEditFrame extends JFrame {
	static final private Color kPageBackground = new Color(0xFFFFCC);
	static final private Color kFileChoiceBackground = new Color(0xCCCCFF);
	
	static ArrayList<Trans> trans = new ArrayList<Trans>();
	static {
		trans.add(new Trans("&Auml;", "\u00C4"));
		trans.add(new Trans("&auml;", "\u00E4"));
		trans.add(new Trans("&Ouml;", "\u00D6"));
		trans.add(new Trans("&ouml;", "\u00F6"));
		trans.add(new Trans("&Uuml;", "\u00DC"));
		trans.add(new Trans("&uuml;", "\u00FC"));
		trans.add(new Trans("&szlig;", "\u00DF"));
		trans.add(new Trans("&nbsp;", "\u00A0"));
		trans.add(new Trans("&sup2;", "\u00B2"));
		trans.add(new Trans("&plusmn;", "\u00B1"));
		trans.add(new Trans("&plusmn;", "\u00B1"));
		trans.add(new Trans("&ldquo;", "\u201C"));
		trans.add(new Trans("&rdquo;", "\u201D"));
		trans.add(new Trans("&bdquo;", "\u201E"));
		trans.add(new Trans("&mdash;", "\u2014"));
		trans.add(new Trans("&ndash;", "\u2013"));
		trans.add(new Trans("&times;", "\u00D7"));
		trans.add(new Trans("&hellip;", "\u2026"));
		trans.add(new Trans("&acute;", "\u00B4"));
	}
	
	private File[] htmlFiles;
	
	private JComboBox fileChoice;
	private int currentFileIndex;
	
	private HtmlContainerElement pageElement;
	private JScrollPane scrollPane = null;
	
	public PageEditFrame(final File[] htmlFiles, final JButton callButton) {
		this.htmlFiles = htmlFiles;
		
		setBackground(kPageBackground);
		setLayout(new BorderLayout(0, 0));
		
		JPanel fileChoicePanel = new JPanel();
		fileChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		fileChoicePanel.setBackground(kFileChoiceBackground);
		fileChoicePanel.setBorder(BorderFactory.createMatteBorder(0, 10, 0, 5, kFileChoiceBackground));
			JLabel choiceLabel = new JLabel("File to edit:", JLabel.LEFT);
			fileChoice = new JComboBox();
			fileChoice.setMaximumRowCount(htmlFiles.length);
			for (int i=0 ; i<htmlFiles.length ; i++)
				fileChoice.addItem(htmlFiles[i].getParentFile().getName() + "/" + htmlFiles[i].getName());
			choiceLabel.setLabelFor(fileChoice);
			
			fileChoice.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	int fileIndex = fileChoice.getSelectedIndex();
																	if (fileIndex != currentFileIndex)
																		if (queryUpdateFile()) {
																			currentFileIndex = fileChoice.getSelectedIndex();
																			changeFile(htmlFiles[currentFileIndex]);
																		}
																}
														});
			
		fileChoicePanel.add(choiceLabel);
		fileChoicePanel.add(fileChoice);
		
		add("North", fileChoicePanel);
		
		currentFileIndex = 0;
		changeFile(htmlFiles[0]);
		
		addWindowListener(new WindowAdapter() {
										public void windowClosing(WindowEvent e) {
											if (queryUpdateFile()) {
												if (callButton != null)
													callButton.setEnabled(true);
												dispose();
											}
										}
									});
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		setVisible(true);
		toFront();
	}
	
	private boolean queryUpdateFile() {
		if (pageElement.hasChanged()) {
			Object[] options = {"Save", "Don't save", "Cancel"};
			int result = JOptionPane.showOptionDialog(PageEditFrame.this, "Save changes to page?", "Save?",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);

			switch (result) {
				case JOptionPane.YES_OPTION:
					saveCurrentFile();
					return true;
				case JOptionPane.NO_OPTION:
					return true;
				case JOptionPane.CANCEL_OPTION:
				default:
					fileChoice.setSelectedIndex(currentFileIndex);
					return false;
			}
		}
		else
			return true;
	}
	
	private void saveCurrentFile() {
		File f = htmlFiles[currentFileIndex];
		String newHtml = pageElement.getHtml();
		
		try {
			File dir = f.getParentFile();
			String fileName = f.getName();
			File outFile = new File(dir, fileName + "__X");
			
			OutputStream out = new FileOutputStream(outFile);
			Writer w = new OutputStreamWriter(out, "UTF-8");
			w.write(newHtml);
			w.flush();
			w.close();
			
			f.delete();
			outFile.renameTo(f);
		} catch (Exception e) {
			System.out.println("Error saving HTML file: " + f.toString());
			e.printStackTrace();
		}
	}
	
	private void changeFile(File htmlFile) {
		if (scrollPane != null)
			remove(scrollPane);
		
		String htmlString = HtmlHelper.getFileAsString(htmlFile);
		htmlString = decodeEntities(htmlString);
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		contentPane.setBorder(padding);
		
		scrollPane = new JScrollPane(contentPane);
		scrollPane.setPreferredSize(new Dimension(700, 800));
		
		boolean isSystemAdviceFile = htmlFile.getName().equals(DatesHash.kSystemAdviceFileName);
		pageElement = new HtmlContainerElement(htmlString, contentPane, isSystemAdviceFile);
		
		add("Center", scrollPane);
		
		pack();
	}
	
	private String decodeEntities(String s) {
		for (Trans t : trans)
			s = s.replaceAll(t.entity, t.utf8);
		return s;
	}
	
}
