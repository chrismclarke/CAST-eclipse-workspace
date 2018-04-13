package cat;

import java.awt.*;

import dataView.*;
import axis.*;


public class BarStackView extends DataView {
	static final public int kTransitions = 100;
	static final private int kBarWidth = 30;

	static final private Color kCatColour[] = CatKey3View.kCatColour;
	
	private String catKey;
	private HorizAxis catAxis;
	private VertAxis propnAxis;
	
	public BarStackView(DataSet theData, XApplet applet,
																	String catKey, HorizAxis catAxis, VertAxis propnAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
		this.catAxis = catAxis;
		this.propnAxis = propnAxis;
	}
	
	public void paintView(Graphics g) {
		double p = getCurrentFrame() / (double)kTransitions;
		double q = 1.0 - p;
		
		Point p0 = null;
		Point p1 = null;
		
		int stackCentre = catAxis.getAxisLength() / 2;
		
		CatVariable y = (CatVariable)getVariable(catKey);
		int noOfCats = y.noOfCategories();
		double factor = 1.0 / y.noOfValues();
		
		int[] counts = y.getCounts();
		double startProb = 0.0;
		
		for (int i=0 ; i<noOfCats ; i++) {
			double thisProb = counts[i] * factor;
			double endProb = startProb + thisProb;
			
			g.setColor(kCatColour[i]);
			
			int barCentre = (int)Math.round(q * catAxis.catValToPosition(i) + p * stackCentre);
			double probLow = startProb * p;
			double probHigh = thisProb * q + endProb * p;
			
			int lowPos = propnAxis.numValToRawPosition(probLow);
			if (getCurrentFrame() == 0 || i == 0)
				lowPos --;					//		to avoid white space under bar
			int highPos = propnAxis.numValToRawPosition(probHigh);
			
			p0 = translateToScreen(barCentre - kBarWidth / 2, highPos, p0);
			p1 = translateToScreen(barCentre + kBarWidth / 2, lowPos, p1);
			
			g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y));
			
			startProb = endProb;
		}
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

}