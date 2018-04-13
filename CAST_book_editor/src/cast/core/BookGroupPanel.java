package cast.core;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;


public class BookGroupPanel extends JPanel {
	static final public Color kGroupTitleColor = new Color(0x006600);
	static final public Color kGroupTitleHiliteColor = new Color(0x00AA00);
	static final private Font kGroupTitleFont = new Font("SansSerif", Font.BOLD, 18);
	static private Font kGroupTitleLinkFont;
	static {
		Map attributes = kGroupTitleFont.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
		kGroupTitleLinkFont = kGroupTitleFont.deriveFont(attributes);
	}
	
	static final private Font kGroupDescriptionFont = new Font("SansSerif", Font.PLAIN, 14);
	static private Font kBookNameFont = new Font("SansSerif", Font.BOLD, 14);
	static {
		Map attributes = kBookNameFont.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		kBookNameFont = kBookNameFont.deriveFont(attributes);
	}
	static final public Color kBookNameColor = new Color(0x000066);
	static final public Color kBookNameHiliteColor = new Color(0x0000FF);
	
//	static final private Font kBookDescriptionFont = new Font("SansSerif", Font.PLAIN, 14);
	
	static final private String kEbooksPattern = "<dt>.*?(startEbook|startModule)\\(\"([^\"]*).*?>([^<]*)<.*?<dd>(.*?)</dd>";
	static final private String kClickInfoPattern = "<p class=\"clickInfo\">(.*?)</p>";
	static final private Font kClickInfoFont = new Font("SansSerif", Font.BOLD, 14);
	
	
//	private BookChoiceWindow choiceWindow;
	
	private JPanel bookListPanel;
	
	public BookGroupPanel(String groupTitle, String descriptionHtml, String ebooksString, boolean unlinkedGroup,
																											File castDir, final BookChoiceWindow choiceWindow) {
//		this.choiceWindow = choiceWindow;
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(10, 25, 5, 0));
		
		GroupHeadingPanel groupHeading = new GroupHeadingPanel(groupTitle, kGroupTitleFont,
																											kGroupTitleLinkFont, kGroupTitleColor, kGroupTitleHiliteColor) {
								protected void doClickAction() {
									bookListPanel.setVisible(!bookListPanel.isVisible());
								}
							};
		add(groupHeading);
		groupHeading.setExpanding(!unlinkedGroup);
		
		if (descriptionHtml != null && descriptionHtml.length() > 0) {
			String descriptionHtmlNarrow = BookChoiceWindow.changeToHtml(descriptionHtml, 500);
			JLabel descriptionLabel = new JLabel(descriptionHtmlNarrow, JLabel.LEFT);
			descriptionLabel.setFont(kGroupDescriptionFont);
			descriptionLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 0));
			
			add(descriptionLabel);
		}
		
			bookListPanel = new JPanel();
			bookListPanel.setOpaque(false);
			
			Border lineBorder = BorderFactory.createLineBorder(Color.black);
			Border outerBorder = BorderFactory.createEmptyBorder(0, 30, 0, 0);
			Border innerBorder = BorderFactory.createEmptyBorder(5, 8, 5, 8);
			
			Border combinedBorder = new CompoundBorder(outerBorder, new CompoundBorder(lineBorder, innerBorder));
			bookListPanel.setBorder(combinedBorder);
			bookListPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
			
			Pattern bookPattern = Pattern.compile(kEbooksPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher bookMatcher = bookPattern.matcher(ebooksString);
			while (bookMatcher.find()) {
				final String shortBookName = bookMatcher.group(2);
				String displayBookName = bookMatcher.group(3);
				String description = bookMatcher.group(4);
				
				final JLabel bookNameLabel = new JLabel(displayBookName, JLabel.LEFT);
				bookNameLabel.setFont(kBookNameFont);
				bookNameLabel.setForeground(kBookNameColor);
				bookNameLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 2, 0));
				bookListPanel.add(bookNameLabel);
				
				bookNameLabel.addMouseListener(new MouseListener() {
									public void mouseReleased(MouseEvent e) {}
									public void mousePressed(MouseEvent e) {}
									public void mouseExited(MouseEvent e) {
										bookNameLabel.setForeground(kBookNameColor);
									}
									public void mouseEntered(MouseEvent e) {
										bookNameLabel.setForeground(kBookNameHiliteColor);
									}
									public void mouseClicked(MouseEvent e) {
										choiceWindow.startCAST(shortBookName);
									}
								});
				bookNameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				
				JLabel bookDescription = new JLabel(BookChoiceWindow.changeToHtml(description, 500));
				bookDescription.setBorder(BorderFactory.createEmptyBorder(2, 10, 4, 0));
				bookListPanel.add(bookDescription);
			}
			
		Pattern clickInfoPattern = Pattern.compile(kClickInfoPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher clickInfoMatcher = clickInfoPattern.matcher(ebooksString);
		if (clickInfoMatcher.find()) {
			String clickInfo = BookChoiceWindow.changeToHtml(clickInfoMatcher.group(1), 500);
			JLabel clickLabel = new JLabel(clickInfo, JLabel.LEFT);
			clickLabel.setForeground(Color.red);
			clickLabel.setFont(kClickInfoFont);
			bookListPanel.add(clickLabel);
		}
		add(bookListPanel);
		
		if (!unlinkedGroup) {
			bookListPanel.setVisible(false);
			groupHeading.setLinkedPanel(bookListPanel);
		}
	}
	
}
