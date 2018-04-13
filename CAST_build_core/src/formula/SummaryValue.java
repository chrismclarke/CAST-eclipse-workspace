package formula;

import java.awt.*;

import valueList.*;


public class SummaryValue extends FormulaPanel {
	static final private int kHeadingGap = 2;
	
	private ValueView value;
	private String headingString;
	private Image headingImage;
	private int imageWidth, imageHeight;
	
	private int headingHeight, headingWidth, valueWidth;
	
	public SummaryValue(ValueView value, String headingString, FormulaContext context) {
		super(context);
		
		this.value = value;
		this.headingString = headingString;
		value.setFont(context.getFont());
		value.setForeground(context.getColor());
		add(value);
	}
	
	public SummaryValue(ValueView value, FormulaContext context) {
		this(value, null, context);
	}
	
	public SummaryValue(ValueView value, Image headingImage,
														int imageWidth, int imageHeight, FormulaContext context) {
		super(context);
		
		this.value = value;
		this.headingImage = headingImage;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		value.setFont(context.getFont());
		value.setForeground(context.getColor());
		add(value);
	}
	
/*
	protected void setParent(FormulaPanel parent) {
		super.setParent(parent);
		value.lockBackground(parent.getBackground());
	}
*/
	
	public void reinitialise() {
		value.invalidate();
		super.reinitialise();
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			headingHeight = 0;
			headingWidth = 0;
			if (headingString != null) {
				headingHeight = ascent + descent + kHeadingGap;
				headingWidth = g.getFontMetrics().stringWidth(headingString);
			}
			else if (headingImage != null) {
				headingHeight = imageHeight + kHeadingGap;
				headingWidth = imageWidth;
			}
			Dimension minValueSize = value.getMinimumSize();
			
			valueWidth = minValueSize.width;
			layoutWidth = Math.max(valueWidth, headingWidth);
			int valueAscent = value.getLabelBaseline(value.getGraphics());
			layoutAscent = valueAscent + headingHeight;
			layoutDescent = minValueSize.height - valueAscent;
			return true;
		}
		else
			return false;
	}
	
	public void layoutContainer(Container parent) {
		initialise(getGraphics());
		value.setBounds((getSize().width - valueWidth) / 2, headingHeight, valueWidth, getSize().height - headingHeight);
	}
	
	protected void paintAroundItems(Graphics g) {
		if (headingString != null)
			g.drawString(headingString, (getSize().width - headingWidth) / 2, ascent);
		else if (headingImage != null)
			g.drawImage(headingImage, (getSize().width - headingWidth) / 2, 0, this);
	}
	
	protected double evaluateFormula() {
		return value.getValue();
	}
}