package exercise;

import java.awt.*;

import dataView.*;


public class TextCanvas extends XPanel {
	static final private int kTopBottomBorder = 3;
	static final private int kLeftRightBorder = 5;
	static final private int kLineGap = 2;
	
	private SymbolCoding coding[];
	private Value value[];
	private Value longestValues[];
	private String text, longestText;
	private int pixWidth;
	
	public TextCanvas(SymbolCoding coding[], String longestText, Value[] longestValues,
																																				int pixWidth) {
		this.coding = coding;
		this.longestText = longestText;
		this.pixWidth = pixWidth;
		this.longestValues = longestValues;
	}
	
	public void setText(String text, Value value[]) {
		this.text = text;
		this.value = value;
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		int spaceLength = fm.stringWidth(" ");
		
		TextTokenizer t = new TextTokenizer(longestText, coding, longestValues);
		
		int horizPos = kLeftRightBorder;
		int baseline = kTopBottomBorder + fm.getAscent();
		while (t.hasMoreWords()) {
			TextItem item = t.nextWord();
			int itemLength = item.getWidth(g);
			if (horizPos + itemLength + kLeftRightBorder > pixWidth) {
				baseline += fm.getAscent() + fm.getDescent() + kLineGap;
				horizPos = kLeftRightBorder;
			}
			else
				horizPos += spaceLength;
			horizPos += itemLength;
		}
		return new Dimension(pixWidth, baseline + fm.getDescent() + kTopBottomBorder);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public void paintComponent(Graphics g) {
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		super.paintComponent(g);
		
		g.setColor(Color.black);
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
		
		FontMetrics fm = g.getFontMetrics();
		int spaceLength = fm.stringWidth(" ");
		
		TextTokenizer t = new TextTokenizer(text, coding, value);
		
		int horizPos = kLeftRightBorder;
		int baseline = kTopBottomBorder + fm.getAscent();
		while (t.hasMoreWords()) {
			TextItem item = t.nextWord();
			int itemLength = item.getWidth(g);
			if (horizPos + itemLength + kLeftRightBorder > getSize().width) {
				baseline += fm.getAscent() + fm.getDescent() + kLineGap;
				horizPos = kLeftRightBorder;
				item.drawItem(g, horizPos, baseline, this);
			}
			else
				item.drawItem(g, horizPos, baseline, this);
			horizPos += itemLength + spaceLength;
		}
	}
}