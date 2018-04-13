package bivarCat;

import java.awt.*;

import dataView.*;
import axis.*;


public class BinaryBarView extends TwoWayView {
//	static public final String BINARY_BAR_PLOT = "binaryBarPlot";
	
	static private final int kFinalFrame = 40;
	static private final int kFrameRate = 10;
	
	static private final int kBarWidth = 17;
	
	static private final Color kDarkRed = new Color(0xCC0000);
	static private final Color kBlueColor[] = new Color[kFinalFrame + 1];
	static {
		for (int i=0 ; i<=kFinalFrame ; i++) {
			int rg = 0xFF * i / kFinalFrame;
			kBlueColor[i] = new Color(rg, rg, 0xFF);
		}
	}
	
	static final public Color catColors[] = {kDarkRed, kBlueColor[0]};
	
	private VertAxis vertAxis;
	private NumCatAxis horizAxis;
	private CatKey2View theKey = null;
	
	private double maxProbScale;
	private boolean toZeroOneScale;
	
	private double p0[];
	
	public BinaryBarView(DataSet theData, XApplet applet, VertAxis vertAxis, NumCatAxis horizAxis,
																					String xKey, String yKey, double maxProbScale) {
		super(theData, applet, xKey, yKey);
		this.vertAxis = vertAxis;
		this.horizAxis = horizAxis;
		this.maxProbScale = maxProbScale;
		toZeroOneScale = true;
		setInitialFrame(kFinalFrame);
	}
	
	public void setLinkedKey(CatKey2View theKey) {
		this.theKey = theKey;
	}
	
	protected boolean initialise(CatVariable x, Variable y) {
		if (super.initialise(x, y)) {
			p0 = new double[xCounts.length];
			for (int i=0 ; i<xCounts.length ; i++)
				p0[i] = (double)jointCounts[i][0] / xCounts[i];
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise((CatVariable)getVariable(xKey), (CatVariable)getVariable(yKey));
		
		int currentFrame = getCurrentFrame();
		if (toZeroOneScale)
			currentFrame = kFinalFrame - currentFrame;
		double maxProb = 1.0 - currentFrame * (1.0 - maxProbScale) / kFinalFrame;
		
		if (vertAxis.maxOnAxis != maxProb) {
			vertAxis.maxOnAxis = maxProb;
			vertAxis.setPower(1.0);
		}
		
		Point p = new Point(0,0);
		for (int i=0 ; i<xCounts.length ; i++) {
			int xPos = horizAxis.catValToPosition(i);
			int y0Pos = vertAxis.numValToRawPosition(p0[i]);
			p = translateToScreen(xPos, y0Pos, p);
			
			g.setColor(kDarkRed);
			g.fillRect(p.x - kBarWidth / 2, p.y, kBarWidth, getSize().height - p.y);
			g.setColor(kBlueColor[currentFrame]);
			g.fillRect(p.x - kBarWidth / 2, 0, kBarWidth, p.y);
		}
		if (theKey != null)
			theKey.changeColor(1, kBlueColor[currentFrame]);
	}
	
	public void animateChange(boolean zeroOneScale) {
		toZeroOneScale = zeroOneScale;
		animateFrames(0, kFinalFrame, kFrameRate, null);
	}
	
	public void setDisplayType(int newMainGrouping, int newHeightType,
																						boolean newStacked) {
	}
}