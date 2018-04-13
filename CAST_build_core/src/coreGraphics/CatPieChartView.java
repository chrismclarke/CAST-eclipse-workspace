package coreGraphics;

import java.awt.*;

import dataView.*;
import imageGroups.*;


public class CatPieChartView extends DataView {
	
//	static final private double kDegToRad = Math.PI / 180;
	
	static final public Color catColor[] = {new Color(0x006600), new Color(0xFF3333),
											 new Color(0x0066FF), new Color(0xFF6600), new Color(0xCC66FF),
											 new Color(0x990099), new Color(0x009999), new Color(0xFF9966),
											 new Color(0x66FF00), new Color(0x666666)};
	
	protected String catKey;
	
	protected int radius, left, top, cx, cy;
	private boolean initialised = false;
	
	public CatPieChartView(DataSet theData, XApplet applet, String catKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.catKey = catKey;
	}
	
	public void setVariableKey(String catKey, XApplet applet) {
		this.catKey = catKey;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			radius = Math.min(getSize().height, getSize().width) / 2 - 1;
			left = getSize().width / 2 - radius;
			top = getSize().height / 2 - radius;
			cx = left + radius;
			cy = top + radius;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		if (catKey == null) {
			TickCrossImages.loadCrossAndTick(getApplet());
			int qnHoriz = cx - TickCrossIcon.kAnswerSize / 2;
			int qnVert = cy - TickCrossIcon.kAnswerSize / 2;
			g.drawImage(TickCrossImages.question, qnHoriz, qnVert, this);
		}
		else {
			CatVariableInterface variable = (CatVariableInterface)getVariable(catKey);
			int noOfCats = variable.noOfCategories();
				
			double cum[] = new double[noOfCats];
			
			if (variable instanceof CatVariable) {
				int[] count = ((CatVariable)variable).getCounts();
				cum[0] = count[0];
				for (int i=1 ; i<count.length ; i++)
					cum[i] = cum[i-1] + count[i];
			}
			else {
				double[] prob = ((CatDistnVariable)variable).getProbs();
				cum[0] = prob[0];
				for (int i=1 ; i<prob.length ; i++)
					cum[i] = cum[i-1] + prob[i];
			}
			double total = cum[noOfCats - 1];
			
			int startAngle = 90;
			for (int i=0 ; i<noOfCats ; i++) {
				int endAngle = 90 - (int)Math.round((cum[i] * 360) / total);
				if (endAngle < 0)
					endAngle += 360;
				int degrees = endAngle - startAngle;
				if (degrees > 0)
					degrees -= 360;
				if (cum[i] == total && (i == 0 || cum[i-1] == 0))
					degrees = -360;
				if (degrees < 0) {
					g.setColor(catColor[i]);
					g.fillArc(left, top, 2 * radius, 2 * radius, startAngle, degrees);
				}
				startAngle = endAngle;
			}
		}
		
		g.setColor(getForeground());
		g.drawOval(left, top, 2 * radius, 2 * radius);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}