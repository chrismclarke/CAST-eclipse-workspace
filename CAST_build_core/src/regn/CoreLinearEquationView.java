package regn;

import java.awt.*;

import dataView.*;
import linMod.*;


abstract public class CoreLinearEquationView extends EquationView {
	static protected final int kLeftRightBorder = 3;
	static protected final int kYEqualsGap = 6;
	static protected final int kParamXGap = 6;
	static final private int kYSubscriptExtra = 3;		//		since tail of mu-sub(y) drops too low
	
	protected String yName;
	
	protected int linearWidth, baseline;
	
	private int maxParamWidth[];
	protected Dimension paramSize[];
	
	private boolean hasIntercept = true;
	
	public CoreLinearEquationView(DataSet theData, XApplet applet, String yName) {
		super(theData, applet);
		if (yName == null)
			RegnImages.loadRegn(applet);
		this.yName = yName;
	}
	
	public void setYName(String yName) {
		this.yName = yName;
		reinitialise();
	}
	
	public void setHasIntercept(boolean hasIntercept) {
		this.hasIntercept = hasIntercept;
	}

//--------------------------------------------------------------------------------
	
	abstract protected String getExplanName(int paramIndex);
	abstract protected Image getExplanImage(int paramIndex);
	abstract protected Color getParamColor(int paramIndex);
	abstract protected NumValue getParamValue(int paramIndex);
	abstract protected int getNoOfParams();
	abstract protected int getMaxParamValueWidth(Graphics g, int paramIndex);
	abstract protected boolean doDrawParameter(int paramIndex);
	
	protected int getExplanWidth(Graphics g, int paramIndex) {
		String xName = getExplanName(paramIndex);
		return (xName == null) ? (kParamXGap + RegnImages.kXParamWidth)
										: g.getFontMetrics().stringWidth(xName);
	}
	
	protected int getResponseWidth(Graphics g) {
		return (yName == null) ? RegnImages.kYParamWidth + kYEqualsGap
										: g.getFontMetrics().stringWidth(yName + " = ");
	}
	
	protected int drawExplan(Graphics g, int paramIndex, int horizPos, int baseline) {
		String xName = getExplanName(paramIndex);
		if (xName == null) {
			Image xImage = getExplanImage(paramIndex);
			horizPos += kParamXGap;
			g.drawImage(xImage, horizPos, baseline - RegnImages.kXParamHeight, this);
			horizPos += RegnImages.kXParamWidth;
		}
		else {
			g.drawString(" " + xName, horizPos, baseline);
			horizPos += g.getFontMetrics().stringWidth(" " + xName);
		}
		return horizPos;
	}
	
	protected int drawResponse(Graphics g, int horizPos, int baseline) {
		if (yName == null) {
			g.drawImage(RegnImages.yMean, horizPos, baseline - RegnImages.kYParamAscent, this);
			horizPos += RegnImages.kYParamWidth + kYEqualsGap;
		}
		else {
			String startString = yName + " = ";
			g.drawString(startString, horizPos, baseline);
			horizPos += g.getFontMetrics().stringWidth(startString);
		}
		return horizPos;
	}
	
	protected void findLinearPartSize(Graphics g) {
		maxParamWidth = new int[getNoOfParams()];
		for (int i=0 ; i<maxParamWidth.length ; i++)
			maxParamWidth[i] = getMaxParamValueWidth(g, i);
		
		FontMetrics fm = g.getFontMetrics();
		
		paramSize = new Dimension[maxParamWidth.length];
		for (int i=0 ; i<maxParamWidth.length ; i++)
			paramSize[i] = getValueSize(fm, maxParamWidth[i]);
		linearWidth = paramSize[0].width;
		if (!hasIntercept)
			linearWidth += getExplanWidth(g, 1);
		for (int i=1 ; i<maxParamWidth.length ; i++) {
			linearWidth += fm.stringWidth(" +  ") + paramSize[i].width;
			linearWidth += getExplanWidth(g, hasIntercept ? i : (i+1));
		}
	}
	
	protected boolean parameterSelected(int paramIndex) {
		return false;
	}
	
	protected int drawLinearPart(Graphics g, int horizPos, int baseline) {
		FontMetrics fm = g.getFontMetrics();
		Color oldColor = g.getColor();
		
		NumValue intercept = getParamValue(0);
		Color interceptColor = getParamColor(0);
		if (interceptColor != null)
			g.setColor(interceptColor);
		if (intercept == null)
			drawParameter(g, null, maxParamWidth[0], horizPos, baseline);
		else
			drawParameter(g, intercept.toString(), maxParamWidth[0], horizPos, baseline,
																								parameterSelected(0));
		horizPos += paramSize[0].width;
		
		g.setColor(oldColor);
		if (!hasIntercept)
			horizPos = drawExplan(g, 1, horizPos, baseline);
		
		for (int i=1 ; i<maxParamWidth.length ; i++)
			if (doDrawParameter(i)) {
				g.drawString(" + ", horizPos, baseline);
				horizPos += fm.stringWidth(" + ");
				
				Color paramColor = getParamColor(i);
				if (paramColor != null)
					g.setColor(paramColor);
				
				NumValue param = getParamValue(i);
				if (param == null)
					drawParameter(g, null, maxParamWidth[i], horizPos, baseline);
				else
					drawParameter(g, param.toString(), maxParamWidth[i], horizPos, baseline,
																								parameterSelected(i));
				
				horizPos += paramSize[i].width;
				
				g.setColor(oldColor);
				horizPos = drawExplan(g, hasIntercept ? i : (i+1), horizPos, baseline);
			}
		
		return horizPos;
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			findLinearPartSize(g);
			
			int leftWidth = getResponseWidth(g);
			modelWidth = leftWidth + 2 * kLeftRightBorder + linearWidth;
			
			modelHeight = paramSize[0].height;
			if (yName == null)
				modelHeight += kYSubscriptExtra;
			baseline = getValueBaseline(g.getFontMetrics());
			return true;
		}
		else
			return false;
	}
	
	public int paintModel(Graphics g) {
//		Color oldColor = g.getColor();
//		FontMetrics fm = g.getFontMetrics();
		int horizPos = kLeftRightBorder;
		
		horizPos = drawResponse(g, horizPos, baseline);
		horizPos = drawLinearPart(g, horizPos, baseline);
		
		return horizPos;
	}
}
