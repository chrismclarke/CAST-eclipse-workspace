package exercise2;

import java.awt.*;
import java.util.*;

import dataView.*;


public class DragStringsView extends CoreDragItemsView {
	
	static final public int TEXT_STRINGS = TEXT_LABELS + 1;
	
	private String textStrings[];
	
	public DragStringsView(DataSet theData, XApplet applet, String[] textStrings, int[] order) {
		super(theData, applet, order, TEXT_STRINGS, new Insets(0,5,0,5));
		
		this.textStrings = textStrings;
	}
	
	public void setTextStrings(String[] textStrings) {
		this.textStrings = textStrings;
	}

//----------------------------------------------------------------
	
	protected int noOfItems() {
		return textStrings.length;
	}
	
	protected void drawBackground(Graphics g) {
	}
	
	protected String getItemName(int index) {
		return "";
	}
	
	protected void drawOneItem(Graphics g, int index, int baseline, int height) {
		int width = getSize().width;
		FontMetrics fm = g.getFontMetrics();
		String[] lines = breakIntoLines(textStrings[index], fm, width);
		
		int lineHeight = fm.getAscent() + fm.getDescent() + fm.getLeading();
		int heightUsed = lines.length * lineHeight - fm.getLeading();
		
		int top = translateToScreen(0, baseline + (height + heightUsed) / 2, null).y;
		int lineBaseline = top + fm.getAscent();
		
		for (int i=0 ; i<lines.length ; i++) {
			g.drawString(lines[i], 0, lineBaseline);
			lineBaseline += lineHeight;
		}
	}
	
	private String[] breakIntoLines(String s, FontMetrics fm, int width) {
		String[] lines = {""};
		int currentLine = 0;
//		int currentPos = 0;
		
		StringTokenizer st = new StringTokenizer(s, " ");
		String testString = "";
		while (st.hasMoreTokens()) {
			String nextWord = st.nextToken();
			if (testString.length() > 0)
				testString += " ";
			testString += nextWord;
			if (fm.stringWidth(testString) > width) {
				currentLine ++;
				if (currentLine >= lines.length) {
					String tempLines[] = new String[lines.length + 1];
					System.arraycopy(lines, 0, tempLines, 0, lines.length);
					lines = tempLines;
					testString = nextWord;
				}
			}
			lines[currentLine] = testString;
		}
		return lines;
	}
}