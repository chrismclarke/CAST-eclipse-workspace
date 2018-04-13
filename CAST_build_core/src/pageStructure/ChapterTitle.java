package pageStructure;

import java.awt.*;
import java.io.*;
import java.util.regex.*;
import javax.swing.*;
//import javax.swing.text.*;

import ebook.*;
import ebookStructure.*;
import exercise2.*;


public class ChapterTitle extends JPanel {
	static final private int kBaseTitleFontSize = 36;
	
	static final private String kBlackPattern = "(.*?)<span .*?black.*?>(.*?)</span>(.*)";
	
//	private Style textBoldStyle, textBoldRedStyle;
	
	private MessageTextPane titleTextPane;
	
	public ChapterTitle(String titleHtml, boolean chapterNotModule, File coreDir) {
		setOpaque(false);
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));
		
			JPanel innerPanel = new JPanel();
			innerPanel.setOpaque(false);
			
			GridBagLayout gbl = new GridBagLayout();
			innerPanel.setLayout(gbl);
			
				GridBagConstraints leftStarC = new GridBagConstraints();
				leftStarC.anchor = GridBagConstraints.CENTER;
				leftStarC.fill = GridBagConstraints.NONE;
				leftStarC.gridheight = 1;
				leftStarC.gridwidth = 1;
				leftStarC.gridx = 0;
				leftStarC.gridy = 0;
				leftStarC.insets = new Insets(3,2,3,30);
				leftStarC.ipadx = leftStarC.ipady = 0;
				leftStarC.weightx = 0.0;
				leftStarC.weighty = 0.0;
				
			File structureDir = new File(coreDir, "structure");
			File starFile = new File(structureDir, "images/star.png");
			UiImage leftStarImage = new UiImage(starFile, true);
			leftStarImage.setPageScaling(true);
			innerPanel.add(leftStarImage);
				gbl.setConstraints(leftStarImage, leftStarC);
			
				GridBagConstraints titleC = new GridBagConstraints();
				titleC.anchor = GridBagConstraints.CENTER;
				titleC.fill = GridBagConstraints.NONE;
				titleC.gridheight = 1;
				titleC.gridwidth = 1;
				titleC.gridx = 1;
				titleC.gridy = 0;
				titleC.insets = new Insets(3,2,3,2);
				titleC.ipadx = titleC.ipady = 0;
				titleC.weightx = 1.0;
				titleC.weighty = 0.0;
				
			titleTextPane = new MessageTextPane(CoreDrawer.scaledSize(kBaseTitleFontSize), CoreDrawer.kSerifFontName, 0);
			titleTextPane.setOpaque(false);
			titleTextPane.setAlignment(false);
			innerPanel.add(titleTextPane);
				gbl.setConstraints(titleTextPane, titleC);
			
			
				GridBagConstraints rightStarC = new GridBagConstraints();
				rightStarC.anchor = GridBagConstraints.CENTER;
				rightStarC.fill = GridBagConstraints.NONE;
				rightStarC.gridheight = 1;
				rightStarC.gridwidth = 1;
				rightStarC.gridx = 2;
				rightStarC.gridy = 0;
				rightStarC.insets = new Insets(3,30,3,2);
				rightStarC.ipadx = rightStarC.ipady = 0;
				rightStarC.weightx = 0.0;
				rightStarC.weighty = 0.0;
				
			UiImage rightStarImage = new UiImage(starFile, true);
			rightStarImage.setPageScaling(true);
			innerPanel.add(rightStarImage);
				gbl.setConstraints(rightStarImage, rightStarC);
			
				GridBagConstraints lineC = new GridBagConstraints();
				lineC.anchor = GridBagConstraints.CENTER;
				lineC.fill = GridBagConstraints.NONE;
				lineC.gridheight = 1;
				lineC.gridwidth = 3;
				lineC.gridx = 0;
				lineC.gridy = 1;
				lineC.insets = new Insets(8,2,20,2);
				lineC.ipadx = lineC.ipady = 0;
				lineC.weightx = 0.0;
				lineC.weighty = 0.0;
				
			String imageName = chapterNotModule ? "chapterUnderline.gif" : "moduleUnderscore.gif";
			File lineFile = new File(structureDir, "images/" + imageName);
			UiImage underlineImage = new UiImage(lineFile, false);
			underlineImage.setPageScaling(true);
			innerPanel.add(underlineImage);
				gbl.setConstraints(underlineImage, lineC);
			
		add(innerPanel);
		
		setTitle(XmlHelper.decodeHtml(titleHtml, false));
	}
	
	
	private void setTitle(String titleHtml) {
		titleTextPane.setEditable(true);
		titleTextPane.selectAll();
		titleTextPane.replaceSelection("");

		titleHtml = titleHtml.replace("<br>", "\n");
		Pattern blackPattern = Pattern.compile(kBlackPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		while (true) {
			Matcher blackMatcher = blackPattern.matcher(titleHtml);
			if (!blackMatcher.find())
				break;
			titleTextPane.insertBoldRedText(blackMatcher.group(1));
			titleTextPane.insertBoldText(blackMatcher.group(2));
			titleHtml = blackMatcher.group(3);
		}
		titleTextPane.insertBoldRedText(titleHtml);
		titleTextPane.setEditable(false);
	}
}