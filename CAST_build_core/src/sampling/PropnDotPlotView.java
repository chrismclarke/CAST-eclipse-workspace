package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import imageGroups.TickCrossImages;
import coreGraphics.*;


public class PropnDotPlotView extends DotPlotView {
	private PropnDotPlotView linkedView;
	private FreqVariable freqVar;
	private boolean popNotSamp;
	private ValueSlider theValueSlider;
	
	public PropnDotPlotView(DataSet theData, XApplet applet,
								NumCatAxis theAxis, PropnDotPlotView linkedView, String freqKey,
								boolean popNotSamp, ValueSlider theValueSlider) {
		super(theData, applet, theAxis, 1.0);
		this.linkedView = linkedView;
		freqVar = (freqKey == null) ? null : (FreqVariable)theData.getVariable(freqKey);
		this.popNotSamp = popNotSamp;
		this.theValueSlider = theValueSlider;
	}
	
	public void setLinkedView(PropnDotPlotView linkedView) {
		this.linkedView = linkedView;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		int f = (freqVar == null) ? 1 : ((FreqValue)freqVar.valueAt(index)).intValue;
		
		if (!popNotSamp && f == 0)
			return null;
		else
			return super.getScreenPoint(index, theVal, thePoint);
	}
	
	private void drawBackground(Graphics g) {
		double topValue = theValueSlider.getSliderValue();
		try {
			int horizPos = axis.numValToPosition(topValue);
			int rightScreenPos = translateToScreen(horizPos, 0, null).x;
			
			g.setColor(Color.yellow);
			g.fillRect(0, 0, rightScreenPos, getSize().height);
			g.setColor(getForeground());
		} catch (AxisException e) {
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		if (popNotSamp && freqVar == null) {
			Point questionCentre = translateToScreen(getDisplayHeight() / 2,
																					getDisplayWidth() / 2, null);
			
			g.drawImage(TickCrossImages.question,
									questionCentre.x - TickCrossImages.question.getWidth(this) / 2,
									questionCentre.y - TickCrossImages.question.getHeight(this) / 2, this);
		}
		else
			super.paintView(g);
	}
	
	protected void checkJittering() {
		if (jittering == null && linkedView.jittering != null)
			jittering = linkedView.jittering;
		
		super.checkJittering();
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
