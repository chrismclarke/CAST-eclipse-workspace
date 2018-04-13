package transform;

import java.awt.*;

import dataView.*;
import axis.*;


public class StanineDotPlotView extends TwinAxisDotPlotView {
	static final private Color kShadeGray = new Color(0xDDDDDD);
	static final private Color kDigitGray = new Color(0xBBBBBB);
	static final private Color kShadeColor[] = {kShadeGray, Color.white, kShadeGray,
									Color.white, kShadeGray, Color.white, kShadeGray, Color.white, kShadeGray};
	
	private NumCatAxis zAxis;
	private Font digitFont = new Font(XApplet.FONT, Font.BOLD, 36);
	
	public StanineDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, NumCatAxis zAxis,
																										double initialJittering) {
		super(theData, applet, theAxis, initialJittering);
		this.zAxis = zAxis;
	}
	
	public StanineDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, NumCatAxis zAxis) {
		this(theData, applet, theAxis, zAxis, 0.3);
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		int lowPos;
		int highPos = 0;
		double highVal = -2.25;
		Point p = null;
		
		FontMetrics fm = g.getFontMetrics();
		int baseline = (getSize().height + fm.getAscent() - fm.getDescent()) / 2;
		NumValue digit = new NumValue(0.0, 0);
		Font oldFont = g.getFont();
		g.setFont(digitFont);
		
		for (int i=0 ; i<9 ; i++) {
			lowPos = highPos;
			if (i == 8)
				highPos = getSize().width;
			else {
				highVal += 0.5;
				p = translateToScreen(zAxis.numValToRawPosition(highVal), 0, p);
				highPos = p.x;
			}
			
			g.setColor(kShadeColor[i]);
			g.fillRect(lowPos, 0, (highPos - lowPos), getSize().width);
			
			g.setColor(kDigitGray);
			digit.setValue(i + 1);
			digit.drawCentred(g, (highPos + lowPos) / 2, baseline);
		}
		g.setFont(oldFont);
	}

}
	
