package resid;

import java.awt.*;

import dataView.*;
import axis.*;

import regnView.*;


public class LSLabelScatterView extends LSScatterView {
//	static public final String LS_LABEL_SCATTER_PLOT = "lsLabelScatterPlot";
	
	static final private Color kPaleRed = new Color(0xFFCCCC);
	
	static final private int kRightLabelBorder = 7;
	static final private int kBottomLabelBorder = 4;
	static final private int kTextRowGap = 3;
	
	private int specialIndex;
	private LabelValue topLabel, bottomLabel;
	
	private double actualY;		//	Replaced in yVar by NaN later
	
	public LSLabelScatterView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey,
						int specialIndex, LabelValue topLabel, LabelValue bottomLabel) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lineKey);
		this.specialIndex = specialIndex;
		this.topLabel = topLabel;
		this.bottomLabel = bottomLabel;
		actualY = ((NumVariable)getVariable(yKey)).doubleValueAt(specialIndex);
	}
	
	public void paintView(Graphics g) {
		drawlabel(g);
		super.paintView(g);
	}
	
	private void drawlabel(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumVariable xVar = (NumVariable)getVariable(xKey);
		
		boolean deleted = Double.isNaN(yVar.doubleValueAt(specialIndex));
		double x = xVar.doubleValueAt(specialIndex);
		
		g.setColor(deleted ? kPaleRed : Color.red);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int maxWidth = Math.max(topLabel.stringWidth(g), bottomLabel.stringWidth(g));
		
		topLabel.drawRight(g, getSize().width - kRightLabelBorder - maxWidth,
										getSize().height - kBottomLabelBorder - 2 * descent - ascent - kTextRowGap);
		bottomLabel.drawRight(g, getSize().width - kRightLabelBorder - maxWidth,
																							getSize().height - kBottomLabelBorder - descent);
		
		int vertPos = yAxis.numValToRawPosition(actualY);
		int horizPos = axis.numValToRawPosition(x);
		Point p =  translateToScreen(horizPos, vertPos, null);
		
		g.drawLine(p.x, p.y, getSize().width - kRightLabelBorder - maxWidth / 2,
									getSize().height - kBottomLabelBorder - 2 * (descent + ascent + kTextRowGap));
		
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	protected boolean canDrag() {
		return false;
	}
}
	
