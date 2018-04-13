package graphics;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class VertNoZeroBarView extends DataView {
//	static final public String VERT_NO_ZERO_BAR_VIEW = "vertNoZeroBarView";
	
	static final private Color kGridColor = new Color(0xDDDDDD);
	static final private Color kBarColor = new Color(0x000099);
	
	static final private int kHorizZig = 12;
	static final private int kVertZag = 5;
	static final private int kValueGap = 4;
	
	private String yKey;
	private HorizAxis catAxis;
	private VertAxis yAxis;
	
	private int xCoord[] = new int[15];
	private int yCoord[] = new int[15];
	
	public VertNoZeroBarView(DataSet theData, XApplet applet, String yKey, HorizAxis catAxis, VertAxis yAxis) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
		this.catAxis = catAxis;
		this.yAxis = yAxis;
	}
	
	private void drawGrid(Graphics g) {
		g.setColor(kGridColor);
		Point p = null;
		Enumeration e = yAxis.getLabels().elements();
		while (e.hasMoreElements()) {
			AxisLabel nextLabel = (AxisLabel)e.nextElement();
			double labelValue = ((NumValue)nextLabel.label).toDouble();
			int y = yAxis.numValToRawPosition(labelValue);
			p = translateToScreen(0, y, p);
			g.drawLine(0, p.y, getSize().width, p.y);
		}
	}
	
	public void paintView(Graphics g) {
		drawGrid(g);
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		
		int halfBarWidth = getSize().width / (nVals * 6);
		
		Point topCenter = null;
		for (int i=0 ; i<nVals ; i++) {
			int x = catAxis.catValToPosition(i);
			int y = yAxis.numValToRawPosition(yVar.doubleValueAt(i));
			topCenter = translateToScreen(x, y, topCenter);
			g.setColor(kBarColor);
			if (yAxis.minOnAxis == 0.0)
				g.fillRect(topCenter.x - halfBarWidth, topCenter.y, 2 * halfBarWidth, getSize().height - topCenter.y);
			else {
				xCoord[0] = xCoord[1] = xCoord[5] = xCoord[6] = xCoord[14] = topCenter.x + halfBarWidth;
				xCoord[2] = xCoord[4] = topCenter.x + halfBarWidth + kHorizZig;
				xCoord[3] = topCenter.x + halfBarWidth - kHorizZig;
				xCoord[7] = xCoord[8] = xCoord[12] = xCoord[13] = topCenter.x - halfBarWidth;
				xCoord[9] = xCoord[11] = topCenter.x - halfBarWidth + kHorizZig;
				xCoord[10] = topCenter.x - halfBarWidth - kHorizZig;
				
				yCoord[0] = yCoord[13] = yCoord[14] = getSize().height;
				yCoord[1] = yCoord[12] = getSize().height - kVertZag;
				yCoord[2] = yCoord[11] = getSize().height - 2 * kVertZag;
				yCoord[3] = yCoord[10] = getSize().height - 4 * kVertZag;
				yCoord[4] = yCoord[9] = getSize().height - 6 * kVertZag;
				yCoord[5] = yCoord[8] = getSize().height - 7 * kVertZag;
				yCoord[6] = yCoord[7] = topCenter.y;
				
				g.fillPolygon(xCoord, yCoord, 15);
			}
			g.setColor(Color.black);
			yVar.valueAt(i).drawCentred(g, topCenter.x, topCenter.y - kValueGap);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}