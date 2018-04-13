package multivar;

import java.util.*;
import java.awt.*;

import dataView.*;

public class SymbolKey extends DataView {
//	static public final String SYMBOL_KEY = "symbolKey";
	
	static private final int kItemSpacing = 4;
	static private final int kTitleKeyGap = 6;
	static private final int kValueSymbolGap = 10;
	static private final int kBorder = 3;
	
	private String kLabel1, kLabel2;
	
	private String zKey;
	private NumValue keyValues[];
	private ZSymbolScatterView theView;
	
	private int itemHeight = 0;
	private int lineHt, titleHeight, titleWidth, itemWidth, textVertOffset,
											valueWidth, symbolHorizOffset;
	private Dimension bestDimension;
	
	public SymbolKey(DataSet data, String zKey, String keyValueString, ZSymbolScatterView theView,
																	XApplet applet) {
		super(data, applet, new Insets(0, 0, 0, 0));
		this.zKey = zKey;
		this.theView = theView;
		
		StringTokenizer st = new StringTokenizer(applet.translate("Symbols for*selected values of"), "*");
		kLabel1 = st.nextToken();
		kLabel2 = st.nextToken();
		
		st = new StringTokenizer(keyValueString);
		keyValues = new NumValue[st.countTokens()];
		int i=0;
		while (st.hasMoreTokens()) {
			keyValues[i] = new NumValue(st.nextToken());
			i++;
		}
	}
	
	private void findLayoutInfo(Graphics g) {
		NumVariable z = (NumVariable)getVariable(zKey);
		FontMetrics fm = g.getFontMetrics();
		
		lineHt = fm.getHeight();
		titleHeight = 3 * lineHt + kTitleKeyGap;
		titleWidth = Math.max(Math.max(fm.stringWidth(kLabel1), fm.stringWidth(kLabel2)),
																	fm.stringWidth(z.name));
		
		itemHeight = Math.max(fm.getAscent() + fm.getDescent(), ZSymbolScatterView.maxSymbolSize())
																				+ kItemSpacing;
		valueWidth = 0;
		for (int i=0 ; i<keyValues.length ; i++)
			valueWidth = Math.max(valueWidth, keyValues[i].stringWidth(g));
			
		itemWidth = 2 * kBorder + valueWidth + kValueSymbolGap + ZSymbolScatterView.maxSymbolSize();
		
		textVertOffset = (itemHeight + fm.getAscent() - fm.getDescent()) / 2;
		symbolHorizOffset = (ZSymbolScatterView.maxSymbolSize() + 1) / 2;
		
		bestDimension = new Dimension(Math.max(itemWidth, titleWidth), titleHeight + itemHeight * keyValues.length);
	}
	
	private void drawOneItem(Graphics g, NumValue value, double proportion,
																int keyOffset, int vert) {
		g.setColor(getForeground());
		value.drawLeft(g, keyOffset + kBorder + valueWidth, vert + textVertOffset);
		theView.drawSymbol(g, new Point(keyOffset + kBorder + valueWidth
				+ kValueSymbolGap + symbolHorizOffset, vert + itemHeight / 2), proportion);
	}
	
	public void paintView(Graphics g) {
		if (itemHeight == 0)
			findLayoutInfo(g);
		
		NumVariable z = (NumVariable)getVariable(zKey);
		
		int vert = g.getFontMetrics().getAscent();
		g.drawString(kLabel1, 0, vert);
		vert += lineHt;
		g.drawString(kLabel2, 0, vert);
		vert += lineHt;
		g.drawString(z.name, 0, vert);
		
		int keyOffset = (getSize().width - itemWidth) / 2;
		vert = titleHeight;
		g.setColor(Color.black);
		g.drawRect(keyOffset, vert, itemWidth - 1, bestDimension.height - vert - 1);
		g.setColor(Color.white);
		g.fillRect(keyOffset + 1, vert + 1, itemWidth - 2, bestDimension.height - vert - 2);
		g.setColor(getForeground());
		
		NumValue sortedZ[] = z.getSortedData();
		double zMin = sortedZ[0].toDouble();
		double zMax = sortedZ[z.noOfValues() - 1].toDouble();
		
		for (int i=0 ; i<keyValues.length ; i++) {
			drawOneItem(g, keyValues[i], (keyValues[i].toDouble() - zMin) / (zMax - zMin),
																			keyOffset, vert);
			vert += itemHeight;
		}
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	public Dimension getMinimumSize() {
		findLayoutInfo(getGraphics());
		return bestDimension;
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
