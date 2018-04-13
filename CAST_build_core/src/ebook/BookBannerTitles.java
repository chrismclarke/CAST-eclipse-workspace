package ebook;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.font.*;
import javax.swing.*;

import ebookStructure.*;


public class BookBannerTitles extends CoreBannerTitles {
	
	private JLabel ch, chNo, sect, page;
	private UiImage lineImage;
	
	public BookBannerTitles(BookFrame theWindowParam) {
		super(theWindowParam);
		
		Font chSecFont = new Font("Arial", Font.PLAIN, 24);
		chSecFont = chSecFont.deriveFont(Collections.singletonMap(TextAttribute.WEIGHT,
																												TextAttribute.WEIGHT_SEMIBOLD));
		
		Font pageFont = new Font("Arial", Font.PLAIN, 32);
		pageFont = pageFont.deriveFont(Collections.singletonMap(TextAttribute.WEIGHT,
																												TextAttribute.WEIGHT_SEMIBOLD));
													// semi-bold looks same as plain font unfortunately
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
			GridBagConstraints logoC = new GridBagConstraints();
			logoC.anchor = GridBagConstraints.WEST;
			logoC.fill = GridBagConstraints.NONE;
			logoC.gridheight = 4;
			logoC.gridwidth = 1;
			logoC.gridx = 0;
			logoC.gridy = 0;
			logoC.insets = new Insets(6,2,6,20);
			logoC.ipadx = logoC.ipady = 0;
			logoC.weightx = 0.0;
			logoC.weighty = 0.0;
			
			File structureDir = theWindowParam.getEbook().getStructureDir();
			File logoFile = new File(structureDir, "images/logo.png");
		UiImage logoImage = new UiImage(logoFile, true);
		add(logoImage);
			gbl.setConstraints(logoImage, logoC);
		
		
			GridBagConstraints chapterNameC = new GridBagConstraints();
			chapterNameC.anchor = GridBagConstraints.NORTHWEST;
			chapterNameC.fill = GridBagConstraints.NONE;
			chapterNameC.gridheight = 1;
			chapterNameC.gridwidth = 2;
			chapterNameC.gridx = 1;
			chapterNameC.gridy = 0;
			chapterNameC.insets = new Insets(0,0,0,0);
			chapterNameC.ipadx = chapterNameC.ipady = 0;
			chapterNameC.weightx = 1.0;
			chapterNameC.weighty = 0.0;
		
//		ch = new JLabel("One Numerical Variable", JLabel.LEFT);
		ch = new JLabel("", JLabel.LEFT);
		ch.setFont(chSecFont);
		ch.setForeground(kGreyTextColor);
		add(ch);
			gbl.setConstraints(ch, chapterNameC);
		
		
			GridBagConstraints chapterNoC = new GridBagConstraints();
			chapterNoC.anchor = GridBagConstraints.NORTHWEST;
			chapterNoC.fill = GridBagConstraints.NONE;
			chapterNoC.gridheight = 3;
			chapterNoC.gridwidth = 1;
			chapterNoC.gridx = 1;
			chapterNoC.gridy = 1;
			chapterNoC.insets = new Insets(0,20,0,20);
			chapterNoC.ipadx = chapterNoC.ipady = 0;
			chapterNoC.weightx = 0.0;
			chapterNoC.weighty = 0.0;
		
//		chNo = new JLabel("2", JLabel.LEFT);
		chNo = new JLabel("", JLabel.LEFT);
		chNo.setFont(new Font("Times New Roman", Font.PLAIN, 64));
		chNo.setForeground(kGreyTextColor);
		add(chNo);
			gbl.setConstraints(chNo, chapterNoC);
		
		
			GridBagConstraints sectionC = new GridBagConstraints();
			sectionC.anchor = GridBagConstraints.NORTHWEST;
			sectionC.fill = GridBagConstraints.NONE;
			sectionC.gridheight = 1;
			sectionC.gridwidth = 1;
			sectionC.gridx = 2;
			sectionC.gridy = 1;
			sectionC.insets = new Insets(0,0,0,0);
			sectionC.ipadx = sectionC.ipady = 0;
			sectionC.weightx = 1.0;
			sectionC.weighty = 0.0;
		
//		sect = new JLabel("3. Histograms and density", JLabel.LEFT);
		sect = new JLabel("", JLabel.LEFT);
		sect.setFont(chSecFont);
		sect.setForeground(kGreyTextColor);
		add(sect);
			gbl.setConstraints(sect, sectionC);
		
		
			GridBagConstraints pageC = new GridBagConstraints();
			pageC.anchor = GridBagConstraints.NORTHWEST;
			pageC.fill = GridBagConstraints.NONE;
			pageC.gridheight = 1;
			pageC.gridwidth = 1;
			pageC.gridx = 2;
			pageC.gridy = 2;
			pageC.insets = new Insets(2,30,2,0);
			pageC.ipadx = pageC.ipady = 0;
			pageC.weightx = 1.0;
			pageC.weighty = 0.0;
		
//		page = new JLabel("1. Density of values", JLabel.LEFT);
		page = new JLabel("", JLabel.LEFT);
		page.setFont(pageFont);
		page.setForeground(kBlackTextColor);
		add(page);
			gbl.setConstraints(page, pageC);
		
		
			GridBagConstraints lineC = new GridBagConstraints();
			lineC.anchor = GridBagConstraints.SOUTHWEST;
			lineC.fill = GridBagConstraints.NONE;
			lineC.gridheight = 1;
			lineC.gridwidth = 1;
			lineC.gridx = 2;
			lineC.gridy = 3;
			lineC.insets = new Insets(0,0,4,0);
			lineC.ipadx = lineC.ipady = 0;
			lineC.weightx = 0.0;
			lineC.weighty = 0.0;
			
		File lineFile = new File(structureDir, "images/pageUnderscore.png");
		lineImage = new UiImage(lineFile, false);
		add(lineImage);
			gbl.setConstraints(lineImage, lineC);
		
		updateTitles();
	}
	
	public void updateTitles() {
		DomElement e = theWindow.currentElement;
		String[] titles = e.getTitles();
		chNo.setText((titles[0] == null) ? "" : titles[0]);
		ch.setText((titles[1] == null) ? "" : titles[1]);
		sect.setText((titles[2] == null) ? "" : titles[2]);
		page.setText((titles[3] == null) ? "" : titles[3]);
		
		lineImage.setVisible(titles[3] != null);
	}
}