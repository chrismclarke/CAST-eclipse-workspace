package pageStructure;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;

import ebook.*;


public class ImageChoiceDrawer extends CoreDrawer {
	static final private String kWidthPattern = "width=\"(\\d*)\"";
	static final private String kHeightPattern = "height=\"(\\d*)\"";
	static final private String kCountPattern = "nImages=\"(\\d*)\"";
	static final private String kImagePattern = "#(.*?)#(.*?)#";
	
	private String htmlString;
	private String dirString;
	private UiImage theImage;
	private File coreDir;
	
	private boolean isLecturing;
	
	public ImageChoiceDrawer(String htmlString, String dirString, BookFrame theBookFrame) {
		this.htmlString = htmlString;
		this.dirString = dirString;
		
		isLecturing = theBookFrame.getEbook().isLecturingVersion();
		coreDir = theBookFrame.getEbook().getCoreDir();
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		
		Pattern widthPattern = Pattern.compile(kWidthPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Pattern heightPattern = Pattern.compile(kHeightPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Pattern countPattern = Pattern.compile(kCountPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Pattern imagePattern = Pattern.compile(kImagePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		
		Matcher widthMatcher = widthPattern.matcher(htmlString);
		Matcher heightMatcher = heightPattern.matcher(htmlString);
		Matcher countMatcher = countPattern.matcher(htmlString);
		if (widthMatcher.find() && heightMatcher.find() && countMatcher.find()) {
			@SuppressWarnings("unused")
			int width = Integer.parseInt(widthMatcher.group(1));
			@SuppressWarnings("unused")
			int height = Integer.parseInt(heightMatcher.group(1));
			int nImages = Integer.parseInt(countMatcher.group(1));
			
			String[] uiNames = new String[nImages];
			File[] uiFiles = new File[nImages];
			Matcher imageMatcher = imagePattern.matcher(htmlString);
			for (int i=0 ; i<nImages ; i++)
				if (imageMatcher.find()) {
					String prefix = imageMatcher.group(1);
					uiNames[i] = imageMatcher.group(2);
					uiFiles[i] = new File(coreDir, dirString + "/images/" + prefix + ".gif");
				}
			
			thePanel.setLayout(new BorderLayout(0, 15));
				
				JPanel bottomPanel = new JPanel();
				bottomPanel.setOpaque(false);
				bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					theImage = new UiImage(uiFiles, false);
					theImage.setPageScaling(true);
					theImage.setOpaque(false);
				bottomPanel.add(theImage);
				
			thePanel.add("Center", bottomPanel);
				
				JPanel topPanel = new JPanel();
				topPanel.setOpaque(false);
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				topPanel.setBorder(BorderFactory.createEmptyBorder(isLecturing ? 20 : 0, 20, isLecturing ? 20 : 0, 0));
					
					final JComboBox imageChoice = new JComboBox();
						Font menuFont = imageChoice.getFont();
						int size = menuFont.getSize();
						int scaledSize = scaledSize(size);
						if (size != scaledSize)
							imageChoice.setFont(menuFont.deriveFont(scaledSize));
					for (int i=0 ; i<uiNames.length ; i++)
						imageChoice.addItem(uiNames[i]);
					imageChoice.setMaximumRowCount(40);
					
					imageChoice.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent e) { 
														int selectedIndex = imageChoice.getSelectedIndex();
														theImage.setImage(selectedIndex);
													}
					});
				topPanel.add(imageChoice);
				
			thePanel.add("North", topPanel);
		}
		else {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			thePanel.setBackground(Color.red);
			thePanel.add(new JLabel("Error: image choice badly formatted in HTML", JLabel.LEFT));
		}
		
		return thePanel;
	}
	
	
	public int getMinimumWidth() {
		return theImage.getPreferredSize().width;
	}
}
