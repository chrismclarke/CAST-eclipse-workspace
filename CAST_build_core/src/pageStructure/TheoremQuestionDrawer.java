package pageStructure;

import java.awt.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;

import utils.*;
import ebook.*;


public class TheoremQuestionDrawer extends CoreDrawer {
	static final private Color kTheoremTitleBackground = new Color(0xB3DBEB);
	static final private Color kExerciseTitleBackground = new Color(0xAFE9B8);
	
	static final public int kBaseTitleFontSize = 20;
	static final private int kBaseNoteFontSize = 13;
	
	
	static final private String kTitlePattern = "<p class=['\"](theoremTitle|questionTitle)['\"]>\\s*(.*?)\\s*</p>(.*)";
	static final private String kNotePattern = "(^.*?)<p class=['\"](theoremNote|questionNote)['\"]>\\s*(.*?)\\s*</p>";
	
	
	private int blockType;
	private String titleString = null;
	private String note = null;
	
	@SuppressWarnings("unused")
	private Color theoremBackgroundColor;
	
	public TheoremQuestionDrawer(String htmlString, String dirString, BookFrame theBookFrame, int blockType, Map namedApplets) {
		this.blockType = blockType;
		theoremBackgroundColor = getBackgroundColor(ExpandingBlockDrawer.getTheoremQuestionStyleSheet(blockType));
		
		Pattern titlePattern = Pattern.compile(kTitlePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher titleMatcher = titlePattern.matcher(htmlString);
		if (titleMatcher.find()) {
			titleString = titleMatcher.group(2);
			htmlString = titleMatcher.group(3);
		}
		
		Pattern notePattern = Pattern.compile(kNotePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher noteMatcher = notePattern.matcher(htmlString);
		if (noteMatcher.find()) {
			htmlString = noteMatcher.group(1);
			note = noteMatcher.group(3);
		}
		
		addChild(new HtmlDrawer(htmlString, dirString, ExpandingBlockDrawer.getTheoremQuestionStyleSheet(blockType),
																																					theBookFrame, namedApplets));
	}
	
	private Color getTitleBackgroundColor() {
		return (blockType == ExpandingBlockDrawer.THEOREM) ? kTheoremTitleBackground : kExerciseTitleBackground;
	}
	
	
	public JPanel createPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		thePanel.setOpaque(false);
			
		if (titleString != null) {
			JPanel titlePanel = new JPanel();
			titlePanel.setBackground(getTitleBackgroundColor());
			titlePanel.setLayout(new BorderLayout(0, 0));
			Border spacingBorder = BorderFactory.createEmptyBorder(5, 10, 5, 0);
			titlePanel.setBorder(spacingBorder);
			
				JLabel title = new JLabel(titleString, JLabel.LEFT);
				title.setForeground(Color.black);
				Font titleFont = new Font(kSerifFontName, Font.BOLD|Font.ITALIC, scaledSize(kBaseTitleFontSize));
				title.setFont(titleFont);
			titlePanel.add("Center", title);
			
			thePanel.add(titlePanel);
		}
		
			JPanel theoremPanel = getChild(0).createPanel();
		thePanel.add(theoremPanel);
		
		if (note != null) {
			JPanel notePanel = new JPanel();
			notePanel.setBackground(getTitleBackgroundColor());
			notePanel.setLayout(new BorderLayout(0, 0));
			Border spacingBorder = BorderFactory.createEmptyBorder(5, 10, 5, 10);
			notePanel.setBorder(spacingBorder);
			
				JLabel noteLabel = new JLabel(note, JLabel.CENTER);
				noteLabel.setForeground(Color.black);
				Font noteFont = new Font(kSerifFontName, Font.BOLD|Font.ITALIC, kBaseNoteFontSize);
				noteLabel.setFont(noteFont);
			notePanel.add("Center", noteLabel);
			
			thePanel.add(notePanel);
		}
		
		return thePanel;
	}
}
