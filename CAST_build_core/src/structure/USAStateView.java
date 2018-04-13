package structure;

import java.awt.*;

import dataView.*;
import images.*;


public class USAStateView extends DataView {
	
	static final private String kStatePictFile = "stateInfo/USAStates.gif";
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	static final private int kUSPictWidth = 400;
	static final private int kUSPictHeight = 244;
	
	static final private int kMinHitDist = 100;
	static final private int kHiliteRadius = 4;
	
	private Image statePict;
	private boolean loadedPicture = false;
	
	private int[] xCoord, yCoord;
	
	public USAStateView(DataSet theData, XApplet applet, String xCoordKey, String yCoordKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		if (!loadedPicture) {
			MediaTracker tracker = new MediaTracker(applet);
				statePict = CoreImageReader.getImage(kStatePictFile);
			tracker.addImage(statePict, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
			loadedPicture = true;
		}
		NumVariable xVar = (NumVariable)theData.getVariable(xCoordKey);
		NumVariable yVar = (NumVariable)theData.getVariable(yCoordKey);
		int nStates = xVar.noOfValues();
		xCoord = new int[nStates];
		yCoord = new int[nStates];
		
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ye = yVar.values();
		int index = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			xCoord[index] = (int)Math.round(xe.nextDouble());
			yCoord[index] = (int)Math.round(ye.nextDouble());
			index ++;
		}
	}
	
	public void paintView(Graphics g) {
		g.drawImage(statePict, 0, 0, this);
		
		g.setColor(Color.red);
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while(fe.hasMoreFlags()) {
			boolean nextSel = fe.nextFlag();
			
			if (nextSel)
				g.fillOval(xCoord[index] - kHiliteRadius, yCoord[index] - kHiliteRadius,
																							2 * kHiliteRadius + 1, 2 * kHiliteRadius + 1);
			else
				g.drawLine(xCoord[index], yCoord[index], xCoord[index], yCoord[index]);
			index++;
		}
	}


//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		return new Dimension(kUSPictWidth, kUSPictHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}


//-----------------------------------------------------------------------------------
		
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<xCoord.length ; i++) {
			int xDist = xCoord[i] - x;
			int yDist = yCoord[i] - y;
			int dist = xDist*xDist + yDist*yDist;
			if (!gotPoint) {
				gotPoint = true;
				minIndex = i;
				minDist = dist;
			}
			else if (dist < minDist) {
				minIndex = i;
				minDist = dist;
			}
		}
		if (gotPoint && minDist < kMinHitDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
}
	
