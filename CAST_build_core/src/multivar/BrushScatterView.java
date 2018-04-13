package multivar;

import java.awt.*;

import dataView.*;
import axis.*;
import multivarProg.ScatterMatrixApplet;


public class BrushScatterView extends DataView {
//	static public final String BRUSH_SCATTER_PLOT = "brushScatterPlot";
	
	static public final Color groupBColor = CatKey2.groupBColor;
	static public final Color groupCColor = CatKey2.groupCColor;
	
	static final private int kBrushDist = 4;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private String xKey, yKey;
	private ScatterMatrixApplet theApplet;
	
	private Point crossPos[];
	private static final int kMinHitDist = 9;
	
	public BrushScatterView(DataSet theData, ScatterMatrixApplet applet, HorizAxis xAxis, VertAxis yAxis,
						String xKey, String yKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.xKey = xKey;
		this.yKey = yKey;
		theApplet = applet;
	}
	
	protected Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		if (Double.isNaN(xVal.toDouble()) || Double.isNaN(yVal.toDouble()))
			return null;
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = xAxis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	private void initialiseCrosses() {
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		int noOfVals = xVariable.noOfValues();
		crossPos = new Point[noOfVals];
		for (int i=0 ; i<noOfVals ; i++)
			crossPos[i] = getScreenPoint((NumValue)(xVariable.valueAt(i)),
											(NumValue)(yVariable.valueAt(i)), null);
	}
	
	private void drawSymbol(Graphics g, int index, boolean selected,
															CatVariable groupVariable) {
		if (crossPos[index] != null) {
			Color crossColor = Color.red;
			int groupIndex = 0;
			if (groupVariable != null)
				groupIndex = groupVariable.getItemCategory(index);
			if (!selected)
				crossColor = (groupIndex == 0) ? Color.black : (groupIndex == 1)
												? groupBColor : groupCColor;
			
			g.setColor(crossColor);
			switch (groupIndex) {
				case 0:
					drawCross(g, crossPos[index]);
					break;
				case 1:
					drawSquare(g, crossPos[index]);
					break;
				default:
					drawPlus(g, crossPos[index]);
					break;
			}
		}
	}
	
	private void drawAllCrosses(Graphics g) {
		CatVariable groupVariable = theApplet.canShowGroups() ? getCatVariable() : null;
		
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while (fe.hasMoreFlags()) {
			boolean nextSel = fe.nextFlag();
			drawSymbol(g, index, nextSel, groupVariable);
			index++;
		}	
	}
	
	public void paintView(Graphics g) {
		if (crossPos == null)
			initialiseCrosses();
		
		if (currentSelection != null && brushPos != null) {
			g.setColor(Color.pink);
			g.fillRect(brushPos.x - kBrushDist, brushPos.y - kBrushDist, 2 * kBrushDist,
																				2 * kBrushDist);
			g.setColor(getForeground());
		}
		
		drawAllCrosses(g);
	}

//-----------------------------------------------------------------------------------
		
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(xKey) || key.equals(yKey))
			crossPos = null;
		super.doChangeVariable(g, key);
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		crossPos = null;
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return !theApplet.doingBrush();
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	private boolean doingBrush;
	private boolean currentSelection[] = null;
	private DragPosInfo brushPos;
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height) {
			brushPos = null;
			return null;
		}
		
		if (crossPos == null)
			initialiseCrosses();
		
		if (doingBrush) {
			brushPos = new DragPosInfo(x, y);
			return brushPos;
		}
		else {
			int minIndex = -1;
			int minDist = 0;
			boolean gotPoint = false;
			for (int i=0 ; i<crossPos.length ; i++)
				if (crossPos[i] != null) {
					int xDist = crossPos[i].x - x;
					int yDist = crossPos[i].y - y;
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
	
	protected PositionInfo getInitialPosition(int x, int y) {
		doingBrush = theApplet.doingBrush();
		return getPosition(x, y);
	}
	
	private boolean addBrushSelection(DragPosInfo selectionInfo) {
		if (selectionInfo != null) {
			boolean changed = false;
			for (int i=0 ; i<crossPos.length ; i++)
				if (crossPos[i] != null) {
					int xDist = crossPos[i].x - selectionInfo.x;
					int yDist = crossPos[i].y - selectionInfo.y;
//					int dist = xDist*xDist + yDist*yDist;
					if (xDist >= -kBrushDist && xDist <= kBrushDist && yDist >= -kBrushDist
																		&& yDist <= kBrushDist)
						if (!currentSelection[i]) {
							currentSelection[i] = true;
							changed = true;
						}
				}
			return changed;
		}
		else
			return false;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (doingBrush)
			currentSelection = new boolean[((NumVariable)getVariable(xKey)).noOfValues()];
		
		if (doingBrush) {
			addBrushSelection((DragPosInfo)startInfo);
			if (!getData().setSelection(currentSelection))
				repaint();
		}
		else {
			getData().clearSelection();
			if (startInfo != null) {
				int listHitIndex = ((IndexPosInfo)startInfo).itemIndex;
				getData().setSelection(listHitIndex);
			}
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (doingBrush) {
			if (toPos == null)
				repaint();
			else {
				boolean changedSelection = addBrushSelection((DragPosInfo)toPos);
				if (changedSelection)
					getData().setSelection(currentSelection);
				else
					repaint();
			}
		}
		else {
			if (fromPos != null) {
				int listHitIndex = ((IndexPosInfo)fromPos).itemIndex;
				getData().setSelection(listHitIndex);
			}
			if (toPos != null) {
				int listHitIndex = ((IndexPosInfo)toPos).itemIndex;
				getData().setSelection(listHitIndex);
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		brushPos = null;
		if (doingBrush)
			repaint();
		else
			getData().clearSelection();
		currentSelection = null;
	}
	
}
	
