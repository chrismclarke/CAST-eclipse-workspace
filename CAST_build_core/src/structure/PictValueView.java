package structure;

import java.awt.*;

import dataView.*;
import images.*;


public class PictValueView extends DataView {
	static final private int kTopBottomBorder = 5;
	static final private int kLeftRightBorder = 10;
	static final private int kPictValueGap = 3;
	static final private int kMinHorizGap = 16;
	static final private int kMinVertGap = 7;
	
	static final private int kMaxWait = 30000;		//		30 seconds
	
	private Image itemPict;
	private int pictWidth, pictHeight;
	private String yKey;
	private int nCols, nRows;
	
	private int ascent;
	private int valueWidth, itemWidth, itemHeight;
	private int minWidth, minHeight;
	
	private boolean initialised = false;
	
	public PictValueView(DataSet theData, XApplet applet, String pictFileName,
																int pictWidth, int pictHeight, String yKey, int nCols) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.pictWidth = pictWidth;
		this.pictHeight = pictHeight;
		this.yKey = yKey;
		this.nCols = nCols;
		
		MediaTracker tracker = new MediaTracker(applet);
			itemPict = CoreImageReader.getImage(pictFileName);
		tracker.addImage(itemPict, 0);
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		initialised = true;
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		
		Variable yVar = (Variable)getVariable(yKey);
		valueWidth = yVar.getMaxWidth(g);
		itemWidth = Math.max(pictWidth, valueWidth);
		itemHeight = pictHeight + ascent + kPictValueGap;
		
		minWidth = 2 * kLeftRightBorder + nCols * itemWidth + (nCols - 1) * kMinHorizGap;
		nRows = (yVar.noOfValues() - 1) / nCols + 1;
		minHeight = 2 * kTopBottomBorder + nRows * itemHeight + (nRows - 1) * kMinVertGap;
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int horizExtra = getSize().width - minWidth;
		int vertExtra = getSize().height - minHeight;
		
		int leftBorder = kLeftRightBorder + horizExtra / (2 * nCols);
		int horizGap = kMinHorizGap + horizExtra / nCols;
		
		int topBorder = kTopBottomBorder + vertExtra / (2 * nRows);
		int vertGap = kMinVertGap + vertExtra / nRows;
		
		int pictHorizOffset = (itemWidth - pictWidth) / 2;
		int centreHorizOffset = itemWidth / 2;
		
		Variable yVar = (Variable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		int col = 0;
		int rowTop = topBorder;
		int colLeft = leftBorder;
		while (ye.hasMoreValues()) {
			g.drawImage(itemPict, colLeft + pictHorizOffset, rowTop, pictWidth, pictHeight, this);
			ye.nextValue().drawCentred(g, colLeft + centreHorizOffset,
																														rowTop + itemHeight);
			
			if (col == nCols - 1) {
				col = 0;
				colLeft = leftBorder;
				rowTop += (itemHeight + vertGap);
			}
			else {
				col ++;
				colLeft += (itemWidth + horizGap);
			}
		}
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (yKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(minWidth, minHeight);
	}
}