package curveInteract;

import java.awt.*;

import dataView.*;


public class ResidSsqScaleView extends DataView {
//	static final public String RESID_SSQ_SCALE_VIEW = "residSsqScaleView";
	
	static final private Color kMinSsqColor = new Color(0xCC0000);
	static final private Color kActualSsqColor = Color.red;
	static final private Color kMaxSsqColor = new Color(0xCCCCCC);
	static final private Color kBarOutlineColor = new Color(0x666666);
	
	static final private int kBarWidth = 20;
	static final private int kLabelLeftBorder = 2;
	static final private int kLabelBarGap = 9;
	static final private int kTickLength = 6;
	static final private int kRightBorder = 10;
	static final private int kMinHeight = 150;
	
	private String rssKey;
	
	private double minRss, maxRss;
	private NumValue axisLabel[];
	
	private int maxLabelWidth;
	
	private boolean initialised = false;
	
	public ResidSsqScaleView(DataSet theData, XApplet applet, String rssKey,
																		double minRss, double maxRss, NumValue[] axisLabel) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.rssKey = rssKey;
		this.minRss = minRss;
		this.maxRss = maxRss;
		this.axisLabel = axisLabel;
	}
	
	protected void doInitialisation(Graphics g) {
		maxLabelWidth = 0;
		for (int i=0 ; i<axisLabel.length ;i++)
			maxLabelWidth = Math.max(maxLabelWidth, axisLabel[i].stringWidth(g));
	}
	
	protected final boolean initialise(Graphics g) {
		if (initialised)
			return false;
		doInitialisation(g);
		initialised = true;
		return true;
	}
	
	private int findAxisPos(double value, int barTop, int barBottom) {
		return barBottom - (int)Math.round((barBottom - barTop) * value / maxRss);
	}
	
	private void drawLabels(Graphics g, int barTop, int barBottom) {
		int baselineOffset = g.getFontMetrics().getAscent() / 2;
		g.setColor(getForeground());
		for (int i=0 ; i<axisLabel.length ; i++) {
			int vert = findAxisPos(axisLabel[i].toDouble(), barTop, barBottom);
			g.drawLine(kLabelLeftBorder + maxLabelWidth + kLabelBarGap - kTickLength, vert,
																kLabelLeftBorder + maxLabelWidth + kLabelBarGap - 1, vert);
			int baseline = vert + baselineOffset;
			axisLabel[i].drawLeft(g, kLabelLeftBorder + maxLabelWidth, baseline);
		}
		g.setColor(kBarOutlineColor);
		g.drawRect(kLabelLeftBorder + maxLabelWidth + kLabelBarGap, barTop, kBarWidth - 1,
																																			barBottom - barTop - 1);
	}
	
	private void fillBarBetween(double low, double high, Graphics g, int barTop, int barBottom) {
		int left = kLabelLeftBorder + maxLabelWidth + kLabelBarGap + 1;
		int width = kBarWidth - 2;
		int top = findAxisPos(high, barTop, barBottom);
		int bottom = findAxisPos(low, barTop, barBottom);
		g.fillRect(left, top, width, bottom - top);
	}
	
	private void drawArrowsAt(int vert, Graphics g) {
		g.setColor(Color.red);
		
		int[] yCoord = new int[8];
		int[] xCoord = new int[8];
		
		yCoord[0] = yCoord[7] = vert;
		yCoord[1] = vert - 5;
		yCoord[2] = yCoord[3] = vert - 3;
		yCoord[4] = yCoord[5] = vert + 3;
		yCoord[6] = vert + 5;
		
		int horiz = kLabelLeftBorder + maxLabelWidth + kLabelBarGap + kBarWidth;
		xCoord[0] = xCoord[7] = horiz + 1;
		xCoord[1] = xCoord[2] = xCoord[5] = xCoord[6] = horiz + 6;
		xCoord[3] = xCoord[4] = horiz + 9;
		
		g.fillPolygon(xCoord, yCoord, 8);
		g.drawPolygon(xCoord, yCoord, 8);
		
		horiz = kLabelLeftBorder + maxLabelWidth + kLabelBarGap;
		xCoord[0] = xCoord[7] = horiz - 1;
		xCoord[1] = xCoord[2] = xCoord[5] = xCoord[6] = horiz - 6;
		xCoord[3] = xCoord[4] = horiz - 9;
		
		g.fillPolygon(xCoord, yCoord, 8);
		g.drawPolygon(xCoord, yCoord, 8);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int top = ascent / 2 + 1;		//	offset a bit in case top label stretched above top
		int bottom = getSize().height - top;		//	offset a bit for zero label
		
		NumVariable rssVar = (NumVariable)getVariable(rssKey);
		int n = rssVar.noOfValues();
		
		if (n > 0) {
			int selectedIndex = getSelection().findSingleSetFlag();
			if (selectedIndex < 0)
				selectedIndex = n - 1;
			double rss = rssVar.doubleValueAt(selectedIndex);
			
			g.setColor(kMaxSsqColor);
			fillBarBetween(rss, maxRss, g, top, bottom);
			
			g.setColor(kActualSsqColor);
			fillBarBetween(minRss, rss, g, top, bottom);
			
			g.setColor(kMinSsqColor);
			fillBarBetween(0.0, minRss, g, top, bottom);
			
			int rssPos = findAxisPos(rss, top, bottom);
			drawArrowsAt(rssPos, g);
		}
		drawLabels(g, top, bottom);
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
		return new Dimension(kLabelLeftBorder + maxLabelWidth + kLabelBarGap + kBarWidth
																														+ kRightBorder, kMinHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}