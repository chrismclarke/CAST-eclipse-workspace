package inference;

import java.awt.*;

import dataView.*;
import axis.*;


public class KernelEstimateView extends DataView {
//	static public final String KERNELPLOT = "kernelPlot";
	
	static final private int kTopBorder = 8;
	
	protected HorizAxis horizAxis;
	
	private double height[];
	private int kernelWidth;			//		pixels
	private double targetArea, actualArea;
	
//	private int outlineX[];
//	private int outlineY[];
//	private int pointsUsed;
	
	public KernelEstimateView(DataSet theData, XApplet applet, HorizAxis horizAxis, int initialKernelWidth) {
		super(theData, applet, new Insets(kTopBorder, 0, 0, 0));
		
		this.horizAxis = horizAxis;
		setKernelWidth(initialKernelWidth);
		targetArea = actualArea;
	}
	
	public void setKernelWidth(int kernelWidth) {
		this.kernelWidth = kernelWidth;
		height = new double[kernelWidth + 1];
		double invMaxHt = 1.0 / (kernelWidth * kernelWidth);
		for (int i=0 ; i<=kernelWidth ; i++)
			height[i] = 1.0 - (i * i) * invMaxHt;
		
		actualArea = height[0];
		for (int i=1 ; i<height.length ; i++)
			actualArea += 2.0 * height[i];
		actualArea *= getNumVariable().noOfValues();
		
		repaint();
	}
	
/*
	private void addPointToPoly(int x, int y) {
		outlineX[pointsUsed] = x;
		outlineY[pointsUsed ++] = y;
	}
*/
	
	public void paintView(Graphics g) {
		double overallHt[] = new double[horizAxis.getAxisLength()];
		double htBeforeSelection[] = null;
		int selectedPos = 0;
		
		NumVariable variable = getNumVariable();
		
		ValueEnumeration e = variable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			boolean nextSel = fe.nextFlag();
			int horizPos = horizAxis.numValToRawPosition(nextVal);
			if (nextSel && htBeforeSelection == null) {		//		only one selected point allowed
				htBeforeSelection = new double[overallHt.length];
				System.arraycopy(overallHt, 0, htBeforeSelection, 0, overallHt.length);
				selectedPos = horizPos;
			}
			int startPos = Math.max(0, horizPos - kernelWidth);
			int endPos = Math.min(overallHt.length - 1, horizPos + kernelWidth);
			for (int i=startPos ; i<endPos ; i++)
				overallHt[i] += height[Math.abs(i - horizPos)];
		}
		
		double maxHeight = 0.0;
		double heightFactor = targetArea / actualArea;
		for (int i=0 ; i<overallHt.length ; i++) {
			overallHt[i] *= heightFactor;
			if (htBeforeSelection != null)
				htBeforeSelection[i] *= heightFactor;
			if (overallHt[i] > maxHeight)
				maxHeight = overallHt[i];
		}
		double displayFactor = (getSize().height - getViewBorder().bottom - getViewBorder().top)
																			/ Math.max(maxHeight, 1.0);
		g.setColor(Color.lightGray);
		int xOffset = getViewBorder().left;
		int yBottom = getSize().height - getViewBorder().bottom;
		
		for (int i=0 ; i<overallHt.length ; i++)
			g.drawLine(xOffset + i, (int)Math.round(yBottom - displayFactor * overallHt[i]),
											xOffset + i, yBottom);
		
		if (htBeforeSelection != null) {
			g.setColor(Color.red);
			int startPos = Math.max(0, selectedPos - kernelWidth);
			int endPos = Math.min(overallHt.length - 1, selectedPos + kernelWidth);
			for (int i=startPos ; i<endPos ; i++) {
				double extraHt = height[Math.abs(i - selectedPos)] * heightFactor;
				int bottomHt = (int)Math.round(displayFactor * htBeforeSelection[i]);
				int topHt = (int)Math.round(displayFactor * (htBeforeSelection[i] + extraHt));
				if (bottomHt < topHt)
					g.drawLine(xOffset + i, yBottom - topHt, xOffset + i, yBottom - bottomHt - 1);
			}
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
	
