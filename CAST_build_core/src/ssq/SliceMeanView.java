package ssq;

import java.awt.*;

import dataView.*;
import axis.*;

import multivar.SliceDotPlotView;


public class SliceMeanView extends SliceDotPlotView {
	static final private int kMeanLineWidth = 20;
	static final private int kLabelLeftRight = 5;
	static final private int kLabelBottom = 2;
	
	private String labelKey;
	
	public SliceMeanView(DataSet theData, XApplet applet, NumCatAxis theAxis,
												double minSelect, double maxSelect, String yKey, String labelKey) {
		super(theData, applet, theAxis, 0.0, minSelect, maxSelect, yKey, null);
		if (vertNotHoriz)
			setViewBorder(new Insets(5, 0, 5, kArrowBorder));
		else
			setViewBorder(new Insets(0, 5, kArrowBorder, 5));
		this.labelKey = labelKey;
	}
	
	public int minDisplayWidth() {
//		int wid = kArrowBorder + kMeanLineWidth;
		int wid = kMeanLineWidth;
		
		Graphics g = getGraphics();
		
		if (vertNotHoriz) {
			LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
			int maxLabelWidth = labelVar.getMaxWidth(g);
			wid += maxLabelWidth + 2 * kLabelLeftRight;
		}
		else {
			FontMetrics fm = g.getFontMetrics();
			wid += fm.getAscent() + fm.getDescent() + kLabelBottom;
		}
		
		return wid;
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		
		if (vertNotHoriz) {
			int labelStart = getSize().width - kArrowBorder - kLabelLeftRight - labelVar.getMaxWidth(g);
			int baselineOffset = g.getFontMetrics().getAscent() / 2;
			
			ValueEnumeration ye = yVar.values();
			ValueEnumeration le = labelVar.values();
			FlagEnumeration fe = getSelection().getEnumeration();
			int index = 0;
			Point p = null;
			while (ye.hasMoreValues()) {
				NumValue nextVal = (NumValue)ye.nextValue();
				Value nextLabel = le.nextValue();
				boolean nextSel = fe.nextFlag();
				p = getScreenPoint(index, nextVal, p);
				if (p != null) {
					g.setColor(nextSel ? Color.red : Color.black);
					g.drawLine(0, p.y, labelStart - kLabelLeftRight, p.y);
					
					nextLabel.drawRight(g, labelStart, p.y + baselineOffset);
				}
				index++;
			}
		}
		else {
			int baseline = getSize().height - kArrowBorder - g.getFontMetrics().getDescent();
			ValueEnumeration ye = yVar.values();
			ValueEnumeration le = labelVar.values();
			FlagEnumeration fe = getSelection().getEnumeration();
			int index = 0;
			Point p = null;
			
			while (ye.hasMoreValues()) {
				NumValue nextVal = (NumValue)ye.nextValue();
				Value nextLabel = le.nextValue();
				boolean nextSel = fe.nextFlag();
				p = getScreenPoint(index, nextVal, p);
				if (p != null) {
					g.setColor(nextSel ? Color.red : Color.black);
					g.drawLine(p.x, 0, p.x, kMeanLineWidth);
					
					nextLabel.drawCentred(g, p.x, baseline);
				}
				index++;
			}
		}
	}
}
	
