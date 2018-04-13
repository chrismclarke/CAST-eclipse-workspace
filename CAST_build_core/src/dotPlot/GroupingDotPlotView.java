package dotPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import random.*;



public class GroupingDotPlotView extends StackedDotPlotView {
	static final private int kMaxVertJitter = 30;
	static final public int kEndFrame = 100;
	
	static final private Color kBandColor = new Color(0xEEEEEE);
	
//	static public final String GROUPED_DOTPLOT = "groupedDotPlot";
	
	private boolean colorCrosses = false;
	private boolean stackedNotJittered = false;
	
	private VertAxis groupAxis;
	private CatVariable groupingVariable;
	
	private int groupVertPos[] = null;
	
	public GroupingDotPlotView(DataSet theData, XApplet applet, NumCatAxis numAxis, VertAxis groupAxis) {
		super(theData, applet, numAxis);
		this.groupAxis = groupAxis;
		setViewBorder(new Insets(5, 5, 0, 5));
	}
	
	public void setColorCrosses(boolean colorCrosses) {
		this.colorCrosses = colorCrosses;
		repaint();
	}
	
	public void stackCrosses(boolean stackedNotJittered) {
		this.stackedNotJittered = stackedNotJittered;
		repaint();
	}
	
	protected boolean initialise() {
		if (super.initialise()) {
			int classSize = getClassSize();
//			int noOfClasses = getNoOfClasses();
			int nGroups = groupingVariable.noOfCategories();
			
			int nValues = horizPos.length;
			groupVertPos = new int[nValues];
			
			int maxHorizPos = 0;
			for (int i=0 ; i<horizPos.length ; i++)
				maxHorizPos = Math.max(maxHorizPos, horizPos[i]);
			
			for (int group = 0 ; group<nGroups ; group++) {
				int minIndex[] = new int[maxHorizPos + 1];
				for (int i=0 ; i<nValues ; i++)
					if (group == groupingVariable.getItemCategory(i)) {
						int vertIndex = minIndex[horizPos[i]];
						groupVertPos[i] = vertIndex * classSize;
						minIndex[horizPos[i]] ++;
					}
			}
			return true;
		}
		else
			return false;
	}
	
	protected void checkJittering() {
		initialiseJittering();
		int dataLength = getNumVariable().noOfValues();
		if (currentJitter > 0 && (jittering == null || jittering.length != dataLength)) {
			RandomBits generator = new RandomBits(14, dataLength);			//	between 0 and 2^14 = 16384
			jittering = generator.generate();
		}
	}
	
	protected int groupIndex(int itemIndex) {
		if (colorCrosses)
			return groupingVariable.getItemCategory(itemIndex);
		else
			return 0;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		int groupIndex = groupingVariable.getItemCategory(index);
		
		Point ungroupedPoint, groupedPoint;
		if (stackedNotJittered) {
			ungroupedPoint = super.getScreenPoint(index, theVal, thePoint);
			ungroupedPoint.y -= getCrossPix() + 2;
			int groupOffset = groupAxis.catValToPosition(groupIndex) - groupAxis.catValToPosition(0);
			groupedPoint = translateToScreen(horizPos[index], groupVertPos[index] + groupOffset, null);
			groupedPoint.y -= getCrossPix() + 2;
		}
		else {
			int horizPos = axis.numValToRawPosition(theVal.toDouble());
			int vertPos = (currentJitter > 0 && jittering != null && index < jittering.length) ? ((currentJitter * jittering[index]) >> 14) : 0;
			int groupZeroOffset = groupAxis.catValToPosition(0) - currentJitter / 2;
			ungroupedPoint = translateToScreen(horizPos, vertPos + groupZeroOffset, thePoint);
			int groupOffset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2;
			groupedPoint = translateToScreen(horizPos, vertPos + groupOffset, null);
		}
		
		if (ungroupedPoint != null && groupedPoint != null)
			ungroupedPoint.y = (ungroupedPoint.y * (kEndFrame - getCurrentFrame()) + groupedPoint.y * getCurrentFrame()) / kEndFrame;
		
		return ungroupedPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = groupingVariable.noOfCategories();
		return Math.min(kMaxVertJitter,
							(getSize().height - getViewBorder().top - getViewBorder().bottom) / noOfGroups / 2);
	}
	
	public void drawBackground(Graphics g) {
		int currentFrame = getCurrentFrame();
		Point p = null;
		if (currentFrame > 0) {
			Color shadeColor = dimColor(kBandColor, 1 - currentFrame / (double)kEndFrame);
			int noOfCats = groupingVariable.noOfCategories();
			g.setColor(shadeColor);
			for (int i=1 ; i<noOfCats ; i+=2) {
				int catPosBelow = groupAxis.catValToPosition(i - 1);
				int catPos = groupAxis.catValToPosition(i);
				
				int bandBottom = (catPosBelow + catPos) / 2;
				int bandTop;
				if (i < noOfCats - 1) {
					int catPosAbove = groupAxis.catValToPosition(i + 1);
					bandTop = (catPosAbove + catPos) / 2;
				}
				else
					bandTop = getSize().height;
				
				p = translateToScreen(0, bandTop, p);
				g.fillRect(0, p.y, getSize().width, bandTop - bandBottom);
			}
			g.setColor(getForeground());
		}
	}
	
	public void paintView(Graphics g) {
		if (groupingVariable == null) {
			groupingVariable = getCatVariable();
			setJitter(1.0);
		}
		groupAxis.show(getCurrentFrame() == kEndFrame);
		
		drawBackground(g);
		
		super.paintView(g);
	}
	
	public void doGroupingAnimation(XSlider controller) {
		animateFrames(1, kEndFrame - 1, 20, controller);
	}
}