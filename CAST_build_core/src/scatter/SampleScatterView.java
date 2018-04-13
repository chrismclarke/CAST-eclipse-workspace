package scatter;

import java.awt.*;

import dataView.*;
import axis.*;


public class SampleScatterView extends DataView {
	static final public int START_ANIMATION_FRAME = 0;
	static final public int POPN_FRAME = 10;
	static final public int SAMP_FRAME = 2 * POPN_FRAME;
	static final public int FRAMES_PER_SEC = POPN_FRAME;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	protected String xKey, yKey;
	
	private Flags oldFlags;
	
	public SampleScatterView(DataSet theData, XApplet applet,
										HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.xKey = xKey;
		this.yKey = yKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	protected Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = xAxis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	private Color hiliteColor() {
		int blueGreen = (Math.abs(getCurrentFrame() - POPN_FRAME)) * 0xFF / POPN_FRAME;
		return new Color(0xFF, blueGreen, blueGreen);
	}
	
	private Color unselectedColor() {
		int rgb = Math.abs(getCurrentFrame() - POPN_FRAME) * 0xFF / POPN_FRAME;
		return new Color(rgb, rgb, rgb);
	}
	
	private Flags getActiveFlags() {
		return (getCurrentFrame() >= POPN_FRAME) ? getSelection() : oldFlags;
	}
	
	public void paintView(Graphics g) {
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		FlagEnumeration fe = getActiveFlags().getEnumeration();
		Point crossPos = null;
		
		if (getCurrentFrame() >= POPN_FRAME) {
			g.setColor(hiliteColor());
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				NumValue x = (NumValue)xe.nextValue();
				NumValue y = (NumValue)ye.nextValue();
				boolean nextSel = fe.nextFlag();
				crossPos = getScreenPoint(x, y, crossPos);
				if (crossPos != null && nextSel)
					drawCrossBackground(g, crossPos);
			}
		}
		
		g.setColor(unselectedColor());
		xe = xVariable.values();
		ye = yVariable.values();
		fe = getActiveFlags().getEnumeration();
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue x = (NumValue)xe.nextValue();
			NumValue y = (NumValue)ye.nextValue();
			boolean nextSel = fe.nextFlag();
			crossPos = getScreenPoint(x, y, crossPos);
			if (crossPos != null && !nextSel)
				drawCross(g, crossPos);
		}
		
		g.setColor(getForeground());
		xe = xVariable.values();
		ye = yVariable.values();
		fe = getActiveFlags().getEnumeration();
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue x = (NumValue)xe.nextValue();
			NumValue y = (NumValue)ye.nextValue();
			boolean nextSel = fe.nextFlag();
			crossPos = getScreenPoint(x, y, crossPos);
			if (crossPos != null && nextSel)
				drawCross(g, crossPos);
		}
	}
	
	public void rememberOldSelection() {
		Flags currentFlags = getSelection();
		if (oldFlags == null)
			oldFlags = new Flags(currentFlags.getNoOfFlags());
		oldFlags.setFlags(currentFlags.counts);
	}
	
	public void doSamplingAnimation(boolean[] newSelection) {
		int startFrame = (getCurrentFrame() == POPN_FRAME) ? POPN_FRAME : START_ANIMATION_FRAME;
		animateFrames(startFrame, SAMP_FRAME - startFrame, FRAMES_PER_SEC, null);
		getData().setSelection(newSelection);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
