package exper;

import java.awt.*;

import dataView.*;

import survey.*;
import experProg.*;


abstract public class CoreMovePictView extends SamplePictView {
	static final private int kFinalFrame = 30;
	
	static final private int nCols(DataSet data, String rowTreatKey) {
		CatVariable rowTreatVar = (CatVariable)data.getVariable(rowTreatKey);
		int nRows = rowTreatVar.noOfCategories();
		return rowTreatVar.noOfValues() / nRows;
	}
	
	static final private int nRows(DataSet data, String rowTreatKey) {
		return ((CatVariable)data.getVariable(rowTreatKey)).noOfCategories();
	}
	
	protected String permKey, rowTreatKey, colTreatKey;
	private CoreMultiFactorApplet applet;
	
	private int oldPerm[] = null;
	private boolean doingAnimation = false;
	
	public CoreMovePictView(DataSet data, CoreMultiFactorApplet applet, long randomSeed, String permKey,
											String rowTreatKey, String colTreatKey) {
		super(data, applet, 0, 0, randomSeed, nRows(data, rowTreatKey), nCols(data, rowTreatKey), 1, 0, 0);
		this.permKey = permKey;
		this.rowTreatKey = rowTreatKey;
		this.colTreatKey = colTreatKey;
		this.applet = applet;
		rememberPermutation();
	}
	
	public void rememberPermutation() {
		if (oldPerm == null)
			oldPerm = new int[rows * cols];
		
		NumVariable permVar = (NumVariable)getVariable(permKey);
		for (int i=0 ; i<rows*cols ; i++)
			oldPerm[i] = (int)Math.round(permVar.doubleValueAt(i));
	}
	
	
	abstract protected void drawFooting(Graphics g, int leftBorder);
	abstract protected int getFootingHt(Graphics g);
	abstract protected int drawLefting(Graphics g, int bottomBorder);
	abstract protected void drawBackground(Graphics g, int tableLeft, int tableBottom);
	
	abstract protected int getPictureIndex(int i);
																//	Allows same picture drawn several times for paired data
	
	
	private int getX(int newIndex, int oldIndex, Dimension d) {
		int newColIndex = newIndex % cols;
		int newXPos = (2 * newColIndex + 1) * d.width / (2 * cols);
		int oldColIndex = oldIndex % cols;
		int oldXPos = (2 * oldColIndex + 1) * d.width / (2 * cols);
		return (newXPos * getCurrentFrame() + oldXPos * (kFinalFrame - getCurrentFrame())) / kFinalFrame;
	}
	
	private int getY(int newIndex, int oldIndex, Dimension d) {
		int newRowIndex = newIndex / cols;
		int newYPos = (2 * newRowIndex + 1) * d.height / (2 * rows);
		
		if (newIndex == oldIndex) {
			int sign = (newRowIndex == 0) ? 1 : -1;
			int maxDist = d.height / (4 * rows);
			int frame = Math.min(getCurrentFrame(), kFinalFrame - getCurrentFrame());
			
			return newYPos + sign * maxDist * frame / kFinalFrame;
		}
		else {
			int oldRowIndex = oldIndex / cols;
			int oldYPos = (2 * oldRowIndex + 1) * d.height / (2 * rows);
			
			return (newYPos * getCurrentFrame() + oldYPos * (kFinalFrame - getCurrentFrame())) / kFinalFrame;
		}
	}
	
	private Point getCenter(int index, NumVariable perm, Point topLeft, Dimension d,
																																		Point tempPoint) {
		if (tempPoint == null)
			tempPoint = new Point(0, 0);
		
		int newIndex = (int)Math.round(perm.doubleValueAt(index));
		int oldIndex = oldPerm[index];
		
		tempPoint.x = topLeft.x + getX(newIndex, oldIndex, d);
		tempPoint.y = topLeft.y + getY(newIndex, oldIndex, d);
		
		return tempPoint;
	}
	
	
	public void paintView(Graphics g) {
		Font mainFont = g.getFont();
		g.setFont(new Font(mainFont.getName(), Font.BOLD, mainFont.getSize()));
		
		int tableBottom = getFootingHt(g);
		int tableLeft = drawLefting(g, tableBottom);
		drawFooting(g, tableLeft);
		
		g.setFont(mainFont);
		
		drawBackground(g, tableLeft, tableBottom);
		g.setColor(getForeground());
		
		CatVariable v = getCatVariable();
		Value successVal = v.getLabel(0);
		
		int innerTop = kTopBottomBorder;
		int innerBottom = getSize().height - tableBottom - kTopBottomBorder;
		
		int innerLeft = tableLeft + kLeftRightBorder;
		int innerRight = getSize().width - kLeftRightBorder;
		
		Point topLeft = new Point(innerLeft, innerTop);
		Dimension d = new Dimension(innerRight - innerLeft, innerBottom - innerTop);
		Point p = null;
		
		NumValue index = new NumValue(0.0, 0);
		NumVariable permVar = (NumVariable)getVariable(permKey);
		
		for (int i=0 ; i<rows*cols ; i++) {
			p = getCenter(i, permVar, topLeft, d, p);
			
			int pictureIndex = getPictureIndex(i);
			drawPicture(g, p.x, p.y, pictureIndex, false, v.valueAt(pictureIndex) == successVal);
			drawIndex(g, p.x, p.y, i, index);
		}
		
		if (getCurrentFrame() == kFinalFrame && doingAnimation) {
			doingAnimation = false;
			rememberPermutation();
			applet.finishAnimation();
		}
	}

//-----------------------------------------------------------------------------------

	public void doSampleAnimation() {
		doingAnimation = true;
		animateFrames(1, kFinalFrame - 1, 20, null);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
