package stemLeaf;

import java.awt.*;

import dataView.*;


public class SplitValueView extends DataView {
	static public final boolean FIXED = true;
	static public final boolean SPLITTING = false;
	
//	static final private int kLeftRightBorder = 20;
	static final private int kTopBottomBorder = 4;
	
	static final public int kSplitFrame = 20;
	
	private String kValuesString, kStemsString, kLeavesString, kDiscardString;
	
	private String xKey;
	private int leafDecimals;
	private boolean fixedNotSplitting;
	
	private boolean initialised = false;
	private int maxDecimals, ignoreChars;
	private int valuesWidth, stemsWidth, leavesWidth;
	private int digitWidth, dotWidth;
	private int ascent, descent, lineHt;
	private Font titleFont;
	private int leafRight;
	
	public SplitValueView(DataSet theData, XApplet applet, String xKey, int leafDecimals, 
																														boolean fixedNotSplitting) {
		super(theData, applet, null);
		kValuesString = applet.translate("Values");
		kStemsString = applet.translate("Stems");
		kLeavesString = applet.translate("Leaves");
		kDiscardString = applet.translate("Discard");
		
		setVariable(xKey, leafDecimals);
		this.fixedNotSplitting = fixedNotSplitting;
	}
	
	public void setVariable(String xKey, int leafDecimals) {
		this.xKey = xKey;
		this.leafDecimals = leafDecimals;
		initialised = false;
		repaint();
	}
	
	private int getGap() {
		return getCurrentFrame();
	}
	
	private void drawHeading(Graphics g, int startBaseline) {
		if (fixedNotSplitting)
			g.drawString(kValuesString, (getSize().width - valuesWidth) / 2, startBaseline + lineHt);
		else {
			int gap = getGap();
			g.setColor(Color.lightGray);
			g.drawString(kDiscardString, leafRight + gap, startBaseline + lineHt);
			
			g.setColor(Color.blue);
			g.drawString(kStemsString, leafRight - digitWidth - gap - stemsWidth, startBaseline + lineHt);
			
			g.setColor(Color.red);
			int leafCenter = leafRight - digitWidth / 2;
			g.drawString(kLeavesString, leafCenter - leavesWidth / 2, startBaseline);
			g.drawLine(leafCenter, startBaseline + 2, leafCenter, startBaseline + lineHt + 2);
			g.drawLine(leafCenter, startBaseline + lineHt + 2, leafCenter + 2, startBaseline + lineHt);
			g.drawLine(leafCenter, startBaseline + lineHt + 2, leafCenter - 2, startBaseline + lineHt);
		}
	}
	
	private void drawValue(Graphics g, NumValue val, int baseline) {
		if (baseline + descent >= getSize().height && baseline - ascent < getSize().height)
			return;
		
		String stemString = "";
		String leafString = "";
		String ignoreString = "";
		if (fixedNotSplitting || getCurrentFrame() == 0) {
			String valString = val.toString(maxDecimals);
			int leafIndex = valString.length() - ignoreChars - 1;
			if (leafIndex > 0)
				stemString = valString.substring(0, leafIndex);
			if (leafIndex > 0 && leafIndex < valString.length())
				leafString = valString.substring(leafIndex, leafIndex + 1);
			if (leafIndex < valString.length())
				ignoreString = valString.substring(leafIndex + 1);
		}
		else {
			int requiredDigits = ignoreChars + 2;
			double realVal = val.toDouble();
			for (int i=0 ; i<maxDecimals ; i++)
				realVal *= 10.0;
			int intVal = (int)Math.round(realVal);
			boolean neg = (intVal < 0);
			if (neg)
				intVal = -intVal;
			String digitString = Integer.toString(intVal);
			while (digitString.length() < requiredDigits)
				digitString = "0" + digitString;
			int leafIndex = digitString.length() - ignoreChars - 1;
			stemString = digitString.substring(0, leafIndex);
			while (stemString.length() > 1 && stemString.charAt(0) == '0')
				stemString = stemString.substring(1);
			if (neg)
				stemString = "-" + stemString;
			leafString = digitString.substring(leafIndex, leafIndex + 1);
			if (ignoreChars > 0)
				ignoreString = digitString.substring(leafIndex + 1);
		}
		
//		boolean showDashes = (baseline > getSize().height - lineHt);
		
		int gap = getGap();
		FontMetrics fm = g.getFontMetrics();
		if (!fixedNotSplitting)
			g.setColor(Color.lightGray);
//		if (showDashes && ignoreString.length() > 0)
//			drawDash(g, leafRight + gap + digitWidth / 2, baseline);
//		else
			g.drawString(ignoreString, leafRight + gap, baseline);
		
		if (!fixedNotSplitting)
			g.setColor(Color.blue);
//		if (showDashes)
//			drawDash(g, leafRight - digitWidth - gap - digitWidth / 2, baseline);
//		else
			g.drawString(stemString, leafRight - digitWidth - gap
													- fm.stringWidth(stemString), baseline);
		
		if (!fixedNotSplitting)
			g.setColor(Color.red);
//		if (showDashes)
//			drawDash(g, leafRight - digitWidth / 2, baseline);
//		else
			g.drawString(leafString, leafRight - digitWidth, baseline);
	}
	
/*
	private void drawDash(Graphics g, int horiz, int baseline) {
		int vert = baseline - ascent / 2;
		g.drawLine(horiz - 1, vert, horiz + 1, vert);
	}
*/
	
	public void paintView(Graphics g) {
		NumVariable v = (NumVariable)getVariable(xKey);
		if (!initialised) {
			maxDecimals = v.getMaxDecimals();
//			maxValueWidth = v.getMaxAlignedWidth(g, maxDecimals);
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			lineHt = fm.getHeight();
			digitWidth = fm.stringWidth("0");
			dotWidth = fm.stringWidth(".");
			
			ignoreChars = maxDecimals - leafDecimals;
			int ignoreDigitWidth = digitWidth * (maxDecimals - leafDecimals);
			if (maxDecimals > 0 && leafDecimals <= 0)	{	//		ignore decimal point also
				ignoreChars ++;
				ignoreDigitWidth += dotWidth;
			}
			int leafDigitWidth = digitWidth;
			int stemDigitWidth = digitWidth * (v.getMaxLeftDigits() + leafDecimals - 1);
			if (leafDecimals > 0)
				stemDigitWidth += dotWidth;
			leafRight = (getSize().width - ignoreDigitWidth + leafDigitWidth + stemDigitWidth) / 2;
			
			Font oldFont = g.getFont();
			titleFont = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize());
			g.setFont(titleFont);
			fm = g.getFontMetrics();
			valuesWidth = fm.stringWidth(kValuesString);
			stemsWidth = fm.stringWidth(kStemsString);
			leavesWidth = fm.stringWidth(kLeavesString);
//			ignoreWidth = fm.stringWidth(kDiscardString);
			g.setFont(oldFont);
			
			initialised = true;
		};
		
		int vert = ascent + kTopBottomBorder;
		Font oldFont = g.getFont();
		g.setFont(titleFont);
		drawHeading(g, vert);
		
		g.setFont(oldFont);
		g.setColor(getForeground());
//		FontMetrics fm = g.getFontMetrics();
		vert += (2 * lineHt + 2);
		ValueEnumeration ve = v.values();
		while (ve.hasMoreValues()) {
			drawValue(g, (NumValue)ve.nextValue(), vert);
			vert += lineHt;
		}
	}
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}