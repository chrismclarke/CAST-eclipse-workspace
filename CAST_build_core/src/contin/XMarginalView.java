package contin;

import java.awt.*;

import dataView.*;


public class XMarginalView extends CoreTableView {
//	static public final String XMARGINVIEW = "xMargin";
	
	static final private int kPlusSize = 21;
	
	public XMarginalView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
	}
	
	protected Dimension getHeadingSize(Graphics g, CoreVariable yVar, CoreVariable xVar) {
		return new Dimension(0, 2 * (ascent + descent) + kYLabelBorder);
										//		height matches YConditionalView's heading height
	}
	
	protected int noOfTableCols() {
		return 1;
	}
	
	protected int noOfTableRows() {
		return nXCats;
	}
	
	protected void drawHeading(Graphics g, int horizCenter, CoreVariable yVar, CoreVariable xVar) {
	}
	
	protected double[] getSingleProbs(CoreVariable yVar, CoreVariable xVar) {
		return ((CatDistnVariable)xVar).getProbs();
	}
	
	protected int maxRowHeadingWidth(int maxXCatWidth) {
		return kPlusSize;
	}
	
	protected void drawLeftMargin(Graphics g, int boxTop, int boxLeft, int boxHeight,
																int nRows, CatVariableInterface xVar) {
		int crossY = boxTop + boxHeight / 2;
		
		g.setColor(Color.red);
		
		g.fillRect(0, crossY - 1, kPlusSize, 3);
		g.fillRect(kPlusSize / 2 - 1, crossY - kPlusSize / 2, 3, kPlusSize);
		
		g.setColor(getForeground());
	}
}
