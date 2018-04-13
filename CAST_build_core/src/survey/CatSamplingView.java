package survey;

import java.awt.*;
import java.util.*;

import dataView.*;


public class CatSamplingView extends DataView {
//	static final public String SAMPLING_PLOT = "catSampling";
	
	static final private int kCrossSize = 5;
	static final private int kCrossGap = 2;
	static final private int kHeadingGap = 3;
//	static final private int kFramesPerSec = 1;
	
	static final private Color kPink = new Color(0xFFCAD3);
	static final private Color kPaleBlue = new Color(0xC8D0FF);
	
	private LabelValue kPopulationName, kSampleName;
	
	private Random generator;
	private Flags sampleFlags;
	private boolean[] inSample;
	private int sampleSize, rows, cols;
	private boolean sampleSelected = false;
	
	private boolean popNotSamp = true;
	
//	private WaitThread theWaitThread = null;
	
	public CatSamplingView(DataSet theData, XApplet applet,
						int sampleSize, long randomSeed, int rows, int cols) {
		super(theData, applet, new Insets(0, 0, 0, 0));
	
		kPopulationName = new LabelValue(applet.translate("Population"));
		kSampleName = new LabelValue(applet.translate("Sample"));
		
		this.sampleSize = sampleSize;
		generator = new Random(randomSeed);
		sampleFlags = new Flags(rows * cols);
		inSample = new boolean[rows * cols];
		this.rows = rows;
		this.cols = cols;
	}
	
	public boolean canShowSample() {
		return sampleSelected;
	}
	
	public void takeSample() {
		int sampleLeft = sampleSize;
		int popnLeft = rows * cols;
		for (int i=0 ; i<rows * cols ; i++) {
			inSample[i] = (sampleLeft > 0 && sampleLeft >= popnLeft * generator.nextDouble());
			if (inSample[i])
				sampleLeft --;
			popnLeft --;
		}
		sampleFlags.setFlags(inSample);
		
		popNotSamp = false;
		getData().setSelection(sampleFlags);
	}
	
	public void showPopNotSamp(boolean popNotSamp) {
		this.popNotSamp = popNotSamp;
		repaint();
	}
	
	protected void drawSymbol(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																								boolean isSucceess) {
		if (isSucceess) {
			g.setColor(dimmed ? kPaleBlue : Color.blue);
			g.drawLine(x - 2, y - 2, x - 1, y - 2);
			g.drawLine(x + 1, y - 2, x + 2, y - 2);
			g.drawLine(x - 2, y - 1, x + 2, y - 1);
			g.drawLine(x - 1, y, x + 1, y);
			g.drawLine(x - 2, y + 1, x + 2, y + 1);
			g.drawLine(x - 2, y + 2, x - 1, y + 2);
			g.drawLine(x + 1, y + 2, x + 2, y + 2);
		}
		else {
			g.setColor(dimmed ? kPink : Color.red);
			g.drawLine(x - 1, y - 2, x + 1, y - 2);
			g.drawLine(x - 2, y - 1, x + 2, y - 1);
			g.drawLine(x - 2, y, x - 1, y);
			g.drawLine(x + 1, y, x + 2, y);
			g.drawLine(x - 2, y + 1, x + 2, y + 1);
			g.drawLine(x - 1, y + 2, x + 1, y + 2);
		}
	}
	
	protected Point getTableBorder() {
		return new Point(kCrossGap + kCrossSize / 2, kCrossGap + kCrossSize / 2);
	}
	
	protected Point getOffsetBetweenRows() {
		return new Point(0, kCrossGap + kCrossSize);
	}
	
	protected Point getOffsetWithinRow() {
		return new Point(kCrossGap + kCrossSize, 0);
	}
	
	private Dimension getTableSize(Point border, Point offsetBetweenRows, Point offsetWithinRow) {
		int tableWidth = 2 * border.x + (cols - 1) * Math.abs(offsetWithinRow.x)
																+ (rows - 1) * Math.abs(offsetBetweenRows.x);
		int tableHeight = 2 * border.y + (cols - 1) * Math.abs(offsetWithinRow.y)
																+ (rows - 1) * Math.abs(offsetBetweenRows.y);
		return new Dimension(tableWidth, tableHeight);
	}
	
	public void paintView(Graphics g) {
//		if (theWaitThread != null && theWaitThread.isAlive())
//			theWaitThread.stop();
//		theWaitThread = new WaitThread(this);
//		Thread.yield();
		
		FontMetrics fm = g.getFontMetrics();
		LabelValue heading = popNotSamp ? kPopulationName : kSampleName ;
		heading.drawCentred(g, getSize().width / 2, kHeadingGap + fm.getAscent());
		
		Point border = getTableBorder();
		Point offsetBetweenRows = getOffsetBetweenRows();
		Point offsetWithinRow = getOffsetWithinRow();
		
		Dimension tableSize = getTableSize(border, offsetBetweenRows, offsetWithinRow);
		
		int tableLeft = (getSize().width - tableSize.width) / 2;
		int tableTop = 2 * kHeadingGap + fm.getAscent() + fm.getDescent();
		
		g.setColor(Color.white);
		g.fillRect(tableLeft, tableTop, tableSize.width, tableSize.height);
		
		CatVariable v = getCatVariable();
		Value successVal = v.getLabel(0);
		FlagEnumeration fe = getSelection().getEnumeration();
		
		int valueIndex = 0;
		int rowStartHoriz = border.x;
		if (offsetBetweenRows.x < 0)
			rowStartHoriz -= offsetBetweenRows.x * (rows - 1);
		int rowStartVert = tableTop + border.y;
		for (int rowIndex=0 ; rowIndex<rows ; rowIndex++) {
			int itemHoriz = rowStartHoriz;
			int itemVert = rowStartVert;
			for (int colIndex=0 ; colIndex<cols ; colIndex++) {
				boolean selected = fe.nextFlag();
//				if (popNotSamp || selected)
					drawSymbol(g, itemHoriz, itemVert, valueIndex, !selected, v.valueAt(valueIndex) == successVal);
				valueIndex ++;
				itemHoriz += offsetWithinRow.x;
				itemVert += offsetWithinRow.y;
			}
			rowStartHoriz += offsetBetweenRows.x;
			rowStartVert += offsetBetweenRows.y;
//			Thread.yield();		//		to allow WaitThread to print "Wait..."
		}
//		theWaitThread.stop();
	}
	
	public Dimension getMinimumSize() {
		Point border = getTableBorder();
		Point offsetBetweenRows = getOffsetBetweenRows();
		Point offsetWithinRow = getOffsetWithinRow();
		
		Dimension tableSize = getTableSize(border, offsetBetweenRows, offsetWithinRow);
		
		FontMetrics fm = getGraphics().getFontMetrics();
		return new Dimension(tableSize.width,
								2 * kHeadingGap + fm.getAscent() + fm.getDescent() + tableSize.height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
