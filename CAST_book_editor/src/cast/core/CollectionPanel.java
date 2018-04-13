package cast.core;

import java.awt.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

import javax.swing.*;

import cast.utils.*;


public class CollectionPanel extends JPanel {
	static final private Font kCollectionTitleFont = new Font("SansSerif", Font.BOLD | Font.ITALIC, 24);
	static private Font kCollectionTitleLinkFont;
	static {
		Map attributes = kCollectionTitleFont.getAttributes();
//		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
		kCollectionTitleLinkFont = kCollectionTitleFont.deriveFont(attributes);
	}
	static final public Color kCollectionTitleColor = Color.black;
	static final public Color kCollectionTitleHiliteColor = new Color(0x660000);
	
	static final private String kCollectionGroupPattern = "<p class=\"heading\">(<a href=[^>]*>)?(.*?)(<img[^>]*>\\s*</a>)?</p>\\s*(.*?)\\s*"
													+ "<div.*?class=\"collection\".*?<div class=\"collectionList\">(.*?)</div>\\s*</div>";
	static final private String kBookPattern = "addBook\\('(.*?)', '(.*?)', '(.*?)', '(.*?)'\\);";
	
	private GroupHeadingPanel collectionHeading;
	private JPanel mainPanel;
	
	public CollectionPanel(String collectionName, String collectionFileName, File castDir,
																															final BookChoiceWindow choiceWindow) {
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
			
			collectionHeading = new GroupHeadingPanel(collectionName, kCollectionTitleFont,
																	kCollectionTitleLinkFont, kCollectionTitleColor, kCollectionTitleHiliteColor) {
								protected void doClickAction() {
									choiceWindow.expandCollection(CollectionPanel.this);
								}
							};
		add("North", collectionHeading);
			
//			JLabel collectionLabel = new JLabel(collectionName, JLabel.LEFT);
//			collectionLabel.setFont(kCollectionTitleFont);
//			collectionLabel.setForeground(kCollectionTitleColor);
//		add("North", collectionLabel);
		
		File collectionFile = castDir;
		String[] path = collectionFileName.split("/");
		for (int i=0 ; i<path.length ; i++)
			collectionFile = new File(collectionFile, path[i]);
		
		if (collectionFileName.endsWith(".html"))
			mainPanel = createHtmlMainPanel(collectionFile, castDir, choiceWindow);
		else
			mainPanel = createJsMainPanel(collectionFile, castDir, choiceWindow);
		
		add("Center", mainPanel);
		
		collectionHeading.setLinkedPanel(mainPanel);
	}
	
	private JPanel createHtmlMainPanel(File collectionFile, File castDir, BookChoiceWindow choiceWindow) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		thePanel.setOpaque(false);
		
		String collectionHtml = HtmlHelper.getFileAsString(collectionFile);
		Pattern groupPattern = Pattern.compile(kCollectionGroupPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher groupMatcher = groupPattern.matcher(collectionHtml);
		while (groupMatcher.find()) {
			String linkString = groupMatcher.group(1);
			String groupTitle = groupMatcher.group(2);
			String descriptionHtml = groupMatcher.group(4);
			String ebooksString = groupMatcher.group(5);
			boolean unlinkedGroup = linkString == null || linkString.length() == 0;
			
			thePanel.add(new BookGroupPanel(groupTitle, descriptionHtml, ebooksString, unlinkedGroup,
																											castDir, choiceWindow));
		}
		return thePanel;
	}
	
	private JPanel createJsMainPanel(File collectionFile, File castDir, BookChoiceWindow choiceWindow) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		thePanel.setOpaque(false);
		
		String collectionHtml = HtmlHelper.getFileAsString(collectionFile);
		
		Vector groups = new Vector();
		Pattern bookPattern = Pattern.compile(kBookPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher bookMatcher = bookPattern.matcher(collectionHtml);
		while (bookMatcher.find()) {
			String groupName = bookMatcher.group(2);
			if (!groups.contains(groupName))
				groups.add(groupName);
		}
		
		for (int i=0 ; i<groups.size(); i++) {
			String groupName = (String)groups.elementAt(i);
			String htmlString = "";
			
			Matcher bookMatcher2 = bookPattern.matcher(collectionHtml);
			while (bookMatcher2.find()) {
				String bookId = bookMatcher2.group(1);
				String bookGroupName = bookMatcher2.group(2);
				String bookTitle = bookMatcher2.group(3);
				String bookDescription = bookMatcher2.group(4);
				if (bookGroupName.equals(groupName))
					htmlString += "<dt><a href='javascript:startEbook(\"" + bookId + "\")' class=\"startEbook\">" + bookTitle + "</a></dt><dd>" + bookDescription + "</dd>\n";
			}
			thePanel.add(new BookGroupPanel(groupName, null, htmlString, true, castDir, choiceWindow));
		}
		return thePanel;
	}
	
	public void setExpanded(boolean expanded) {
		collectionHeading.setExpanding(!expanded);
		mainPanel.setVisible(expanded);
	}
	
}
