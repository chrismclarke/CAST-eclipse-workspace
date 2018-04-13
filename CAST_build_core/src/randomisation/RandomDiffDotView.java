package randomisation;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class RandomDiffDotView extends DotPlotView {
//	static public final String RANDOM_DIFF_VIEW = "randomDiff";
	
	static final private Color kPaleRedColor = new Color(0xFFEEEE);
	
//	private double absLimit;
	private double lowLimit, highLimit;
	
	public RandomDiffDotView(DataSet summaryData, XApplet applet, NumCatAxis numAxis,
																													double lowLimit, double highLimit) {
		super(summaryData, applet, numAxis, 1.0);
		this.lowLimit = lowLimit;
		this.highLimit = highLimit;
	}
	
	public RandomDiffDotView(DataSet summaryData, XApplet applet, NumCatAxis numAxis, double absLimit) {
		this(summaryData, applet, numAxis, -absLimit, absLimit);
	}
	
	protected int groupIndex(int itemIndex) {
		double x = getNumVariable().doubleValueAt(itemIndex);
		return (x <= lowLimit || x >= highLimit) ? 5 : 10;
	}
	
	private void drawBackground(Graphics g) {
		int lowPos = axis.numValToRawPosition(lowLimit);
		int highPos = axis.numValToRawPosition(highLimit);
		
		Point p0 = translateToScreen(lowPos, 0, null);
		Point p1 = translateToScreen(highPos, 0, null);
		
		g.setColor(kPaleRedColor);
		
		g.fillRect(0, 0, p0.x, getSize().height);
		g.fillRect(p1.x, 0, getSize().width - highPos, getSize().height);
		
		g.setColor(Color.red);
		g.drawLine(p0.x, 0, p0.x, getSize().height);
		g.drawLine(p1.x, 0, p1.x, getSize().height);
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
}