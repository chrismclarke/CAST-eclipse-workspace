package pageStructure;

import java.awt.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.html.*;

import dataView.DataView;
import utils.*;
import ebook.*;


public class HeadedBlockDrawer extends CoreDrawer {
	static final public int DEFINITION = 0;
	static final public int EXAMPLE = 1;
	
	static final private Color kDefnBorderColor = new Color(0x0000FF);
	static final private Color kDefnTitleBackground = new Color(0xE0F6FF);
	static final private Color kDefnTitleBorderColor = DataView.mixColors(kDefnBorderColor, kDefnTitleBackground);
	static final private Color kDefnTitleTextColor = new Color(0x0000BB);
	
	static final private Color kExampleBorderColor = new Color(0x5B3300);
	static final private Color kExampleTitleBackground = new Color(0xF3E6D6);
	static final private Color kExampleTitleBorderColor = DataView.mixColors(kExampleBorderColor, kExampleTitleBackground, 0.3);
	static final private Color kExampleTitleTextColor = new Color(0x000000);
	
	static final private int kBaseTitleFontSize = 20;
	static final private int kBaseNoteFontSize = 13;
	
	
	static final private String kTitlePattern = "(^.*?)<p class=['\"](definitionTitle|exampleHeading)['\"][^>]*>\\s*(.*?)\\s*</p>(.*)";
	static final private String kNotePattern = "(^.*?)<p class=['\"]exampleNote['\"]>\\s*(.*?)\\s*</p>";
	
	
	private int blockType;
	private Vector titles = new Vector();
	private String note = null;
	
	public HeadedBlockDrawer(String htmlString, String dirString, BookFrame theBookFrame, int blockType,
																																													Map namedApplets) {
		this.blockType = blockType;
		Pattern titlePattern = Pattern.compile(kTitlePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher titleMatcher = titlePattern.matcher(htmlString);
		boolean firstMatch = true;
		while (true) {
			if (titleMatcher.find()) {
				String initialHtml = titleMatcher.group(1);
				if (firstMatch)
					firstMatch = false;
				else
					addChild(new HtmlDrawer(initialHtml, dirString, getMainStyleSheet(), theBookFrame, namedApplets));
				titles.add(titleMatcher.group(3));
				htmlString = titleMatcher.group(4);
				
				titleMatcher = titlePattern.matcher(htmlString);
			}
			else
				break;
		}
		
		Pattern notePattern = Pattern.compile(kNotePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher noteMatcher = notePattern.matcher(htmlString);
		if (noteMatcher.find()) {
			htmlString = noteMatcher.group(1);
			note = noteMatcher.group(2);
		}
		addChild(new HtmlDrawer(htmlString, dirString, getMainStyleSheet(), theBookFrame, namedApplets));
	}
	
	private Color getBorderColor() {
		return (blockType == DEFINITION) ? kDefnBorderColor : kExampleBorderColor;
	}
	
	private Color getTitleBackgroundColor() {
		return (blockType == DEFINITION) ? kDefnTitleBackground : kExampleTitleBackground;
	}
	
	private Color getTitleBorderColor() {
		return (blockType == DEFINITION) ? kDefnTitleBorderColor : kExampleTitleBorderColor;
	}
	
	private Color getTitleFontColor() {
		return (blockType == DEFINITION) ? kDefnTitleTextColor : kExampleTitleTextColor;
	}
	
	private StyleSheet getMainStyleSheet() {
		return (blockType == DEFINITION) ? getDefinitionStyleSheet() : getExampleStyleSheet();
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		thePanel.setOpaque(false);
			
			Border outerBorder = BorderFactory.createEmptyBorder(0, 30, 0, 30);
			Border innerBorder = BorderFactory.createLineBorder(getBorderColor());
		thePanel.setBorder(new CompoundBorder(outerBorder, innerBorder));
			
			int nDefns = Math.min(titles.size(), noOfChildren());
			for (int i=0 ; i<nDefns ; i++) {
				String titleString = (String)titles.elementAt(i);
				JPanel titlePanel = new JPanel();
				titlePanel.setBackground(getTitleBackgroundColor());
				titlePanel.setLayout(new BorderLayout(0, 0));
				Border underlineBorder = BorderFactory.createMatteBorder((i == 0) ? 0 : 1, 0, 1, 0, getTitleBorderColor());
				Border innerSpacingBorder = BorderFactory.createEmptyBorder(5, 10, 5, 0);
				titlePanel.setBorder(new CompoundBorder(underlineBorder, innerSpacingBorder));
				
					JLabel title = new JLabel(titleString, JLabel.LEFT);
					title.setForeground(getTitleFontColor());
					Font titleFont = new Font(kSerifFontName, Font.BOLD|Font.ITALIC, scaledSize(kBaseTitleFontSize));
					title.setFont(titleFont);
				titlePanel.add("Center", title);
				
				thePanel.add(titlePanel);
				
				JPanel definitionPanel = getChild(i).createPanel();
				definitionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
				thePanel.add(definitionPanel);
			}
		
		if (note != null) {
			JPanel notePanel = new JPanel();
			notePanel.setBackground(getTitleBackgroundColor());
			notePanel.setLayout(new BorderLayout(0, 0));
			Border noteUnderlineBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, getTitleBorderColor());
			Border noteInnerSpacingBorder = BorderFactory.createEmptyBorder(5, 10, 5, 10);
			notePanel.setBorder(new CompoundBorder(noteUnderlineBorder, noteInnerSpacingBorder));
			
				JLabel noteLabel = new JLabel(note, JLabel.CENTER);
				noteLabel.setForeground(getTitleFontColor());
				Font kNoteFont = new Font(kSerifFontName, Font.BOLD|Font.ITALIC, scaledSize(kBaseNoteFontSize));
				noteLabel.setFont(kNoteFont);
			notePanel.add("Center", noteLabel);
			
			thePanel.add(notePanel);
		}
		
		return thePanel;
	}
}
