package exerciseBivar;

import java.awt.*;

import dataView.*;
import exercise2.*;


public class MultipleScatterView extends CoreDragItemsView {
//	static public final String MULTIPLE_SCATTER = "multipleScatter";
	
	static final public int VARIABLE_NAMES = TEXT_LABELS;
	static final public int SCATTER_PLOTS = TEXT_LABELS + 1;
	
	static final private int kScatterBorder = 3;
	
	static final private Color kAxisNameColor = new Color(0xAAAAAA);
	
	private String xKey[];
	private String yKey[];
	
	public MultipleScatterView(DataSet theData, XApplet applet, String[] xKey, String[] yKey,
														int[] order, int displayType) {
		super(theData, applet, order, displayType, new Insets(0,0,0,0));
		
		this.xKey = xKey;
		this.yKey = yKey;
		
		if (displayType == VARIABLE_NAMES) {
			Font stdFont = applet.getStandardFont();
			setFont(new Font(stdFont.getName(), Font.BOLD, stdFont.getSize() * 2));
		}
	}
	
	public void setXYKeys(String[] xKey, String[] yKey) {
		this.xKey = xKey;
		this.yKey = yKey;
	}

//----------------------------------------------------------------
	
	protected int noOfItems() {
		return yKey.length;
	}
	
	protected void drawBackground(Graphics g) {
		int noOfDistns = noOfItems();
		g.setColor(getApplet().getBackground());
		for (int i=0 ; i<noOfDistns ; i++) {
			int scatterTop = getSize().height * i / noOfDistns;
			g.fillRect(0, scatterTop, getSize().width, kScatterBorder);
		}
	}
	
	protected String getItemName(int index) {
		String yName = getVariable(yKey[index]).name;
		String xName = getVariable(xKey[index]).name;
		return yName + " vs " + xName;
	}
	
	protected void drawOneItem(Graphics g, int index, int baseline, int height) {
									//	displayType == SCATTER_PLOTS
			drawOneScatterplot(g, xKey[index], yKey[index], baseline, height);
	}

//----------------------------------------------------------------
	
	private void drawOneScatterplot(Graphics g, String xKey, String yKey, int baseline, int height) {
		int bottom = baseline;
		height -= kScatterBorder;
		int left = 0;
		int width = getSize().width;
		
		Point p = translateToScreen(left, bottom, null);
		g.drawLine(p.x, p.y, p.x + width, p.y);
		g.drawLine(p.x, p.y, p.x, p.y - height);
		
		g.setColor(kAxisNameColor);
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		g.drawString("f(Y)", p.x + 3, p.y - height + ascent + 2);
		g.drawString("g(X)", p.x + width - 3 - fm.stringWidth("g(X)"), p.y - 3);
		
		g.setColor(getForeground());
		
		int innerBorder = getCrossSize() + 1;
		bottom += innerBorder;
		height -= 2 * innerBorder;
		left += innerBorder;
		width -= 2 * innerBorder;
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumVariable xVar = (NumVariable)getVariable(xKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			double x = xe.nextDouble();
			xMin = Math.min(xMin, x);
			xMax = Math.max(xMax, x);
			yMin = Math.min(yMin, y);
			yMax = Math.max(yMax, y);
		}
		
		ye = yVar.values();
		xe = xVar.values();
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			double x = xe.nextDouble();
			double yp = (y - yMin) / (yMax - yMin);
			double xp = (x - xMin) / (xMax - xMin);
			
			int yPos = bottom + (int)Math.round(yp * height);
			int xPos = left + (int)Math.round(xp * width);
			p = translateToScreen(xPos, yPos, p);
			drawCross(g, p);
		}
	}
}