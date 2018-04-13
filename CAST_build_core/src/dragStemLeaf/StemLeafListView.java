package dragStemLeaf;

import java.awt.*;

import dataView.*;


abstract public class StemLeafListView extends DataView {
	public StemLeafListView(DataSet theData, XApplet applet) {
		super(theData, applet, null);
		standardFont = applet.getStandardFont();
	}
	

//-----------------------------------------------------------------------------------
	
	private boolean initialised = false;
	protected int stemPower, minStemPower, maxStemPower;
	protected int lineHt, ascent, leading, finalZeroWidth;
	protected int valueRightPos, headingHt, listBottom, listRight;
	private Font standardFont;
	protected Font theFont;
	
	private boolean disabled[];
	
	protected static final int kBorder = 4;
	private static final int kHeadingSpace = 4;
	private static final String kStemString = "stem";
	private static final String kLeafString = "leaf";
	private static final String kDiscardString = "discard";
	
	private static LabelValue stemLabel = new LabelValue(kStemString);
	private static LabelValue leafLabel = new LabelValue(kLeafString);
	private static LabelValue discardLabel = new LabelValue(kDiscardString);
	
	
	abstract protected int getMinStemPower(StemLeafVariable variable);
	
	abstract protected int getMaxStemPower(StemLeafVariable variable);
	
	abstract protected int getAnswerHt();
	
	private void initialise(Graphics g) {
		StemLeafVariable variable = (StemLeafVariable)getNumVariable();
		minStemPower = getMinStemPower(variable);
		stemPower = minStemPower;
		maxStemPower = getMaxStemPower(variable);
		
		int noOfValues = variable.noOfValues();
		
		theFont = standardFont;
		g.setFont(theFont);
		while (theFont.getSize() > 5) {
			FontMetrics fm = g.getFontMetrics();
			int maxDecimals = Math.max(variable.getMaxDecimals(), 1 - minStemPower);
			int maxValueWidth = variable.getMaxAlignedWidth(g, maxDecimals);
			if (variable.getMaxLeftDigits() == maxStemPower)	//	we must allow for extra leading zero
				maxValueWidth += fm.charWidth('0');
			
			int minDiscardOffset = (StemLeafValue.leafSeparators(g, maxDecimals, minStemPower)).y;
			int maxStemOffset = (StemLeafValue.leafSeparators(g, maxDecimals, maxStemPower)).x;
			
			maxValueWidth = Math.max(maxValueWidth, maxStemOffset + fm.stringWidth(kStemString));
			int discardLabelExtra = Math.max(0, fm.stringWidth(kDiscardString) - minDiscardOffset);
			
			if (minStemPower == 0)
				finalZeroWidth = fm.stringWidth(".0");
			else
				finalZeroWidth = fm.charWidth('0');
			
			lineHt = fm.getHeight();
			ascent = fm.getAscent();
			leading = fm.getLeading();
			headingHt = lineHt + ascent + kHeadingSpace;
			
			listBottom = kBorder + headingHt + lineHt * noOfValues;
			valueRightPos = kBorder + maxValueWidth;
			listRight = valueRightPos + discardLabelExtra + kBorder;
			if (getSize().height >= listBottom + getAnswerHt() + kBorder
																	&& getSize().width >= listRight + kBorder)
				break;
			theFont = new Font(theFont.getName(), theFont.getStyle(), theFont.getSize() - 1);
			g.setFont(theFont); 
		}
		
		initialised = true;
	}
	
	protected void disableValue(int item) {
		if (disabled == null)
			disabled = new boolean[((StemLeafVariable)getNumVariable()).noOfValues()];
		disabled[item] = true;
	}
	
	protected void drawBorder() {
		drawBorder(getGraphics());
	}
	
	protected void drawBorder(Graphics g) {
	}
	
	protected void drawAnswer(Graphics g) {
	}
	
	protected int leafHit(int x, int y, int maxVarDecimals) {
		int vert = y - (kBorder + headingHt);
		int hitItem = vert / lineHt;
		if (vert < 0 || vert >= getNumVariable().noOfValues() * lineHt
																	|| (disabled != null && disabled[hitItem]))
			return -1;
		
		int localRightPos = valueRightPos;
//		int drawDecs = 1 - minStemPower;
		int drawDecs = Math.max(maxVarDecimals, 1 - minStemPower);
		if (stemPower > minStemPower && minStemPower < 1) {
			localRightPos -= finalZeroWidth;
			drawDecs--;
		}
		
		Point separators = StemLeafValue.leafSeparators(getGraphics(), drawDecs, stemPower);
		int leafLeft = localRightPos - separators.x + 2;
		int leafRight = localRightPos - separators.y;
		
		if (x < leafLeft || x >= leafRight)
			return -1;
		else
			return hitItem; 
	}
	
	public void paintView(Graphics g) {
		if (!initialised)
			initialise(g);
		else
			g.setFont(theFont);
		
		StemLeafVariable variable = (StemLeafVariable)getNumVariable();
		
		int localRightPos = valueRightPos;
//		int drawDecs = 1 - minStemPower;
		int drawDecs = Math.max(variable.getMaxDecimals(), 1 - minStemPower);
		if (stemPower > minStemPower && minStemPower < 1) {
			localRightPos -= finalZeroWidth;
			drawDecs--;
		}
		
		Point separators = StemLeafValue.leafSeparators(g, drawDecs, stemPower);
		int line1 = localRightPos - separators.x;
		int line2 = localRightPos - separators.y;
		Color oldColor = g.getColor();
		
		g.setColor(Color.blue);
		leafLabel.drawCentred(g, (line1 + line2) / 2, kBorder + ascent);
		stemLabel.drawLeft(g, (line1 - 1), kBorder + lineHt + ascent - leading);
		discardLabel.drawRight(g, (line2 + 3), kBorder + lineHt + ascent - leading);
		
		g.setColor(Color.red);
		int lineTop = kBorder + headingHt - lineHt;
		int lineBottom = headingHt + variable.noOfValues() * lineHt;
		g.drawLine(line1, lineTop, line1, lineBottom);
		g.drawLine(line1 + 1, lineTop, line1 + 1, lineBottom);
		g.drawLine(line2, lineTop, line2, lineBottom);
		g.drawLine(line2 + 1, lineTop, line2 + 1, lineBottom);
		g.setColor(oldColor);
		
		ValueEnumeration e = variable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int vertPos = kBorder + headingHt + ascent;
		int i=0;
		while (e.hasMoreValues()) {
			StemLeafValue nextVal = (StemLeafValue) e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (disabled != null && disabled[i])
				g.setColor(Color.gray);
			nextVal.drawLeft(g, drawDecs, stemPower, localRightPos, vertPos, nextSel);
			if (disabled != null && disabled[i])
				g.setColor(oldColor);
			vertPos += lineHt;
			i++;
		}
		
		drawAnswer(g);
		drawBorder(g);
	}
}