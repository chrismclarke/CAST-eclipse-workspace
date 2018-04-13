package factorial;

import java.awt.*;

public class Term {
	static final private Color kAliasColor[] = {Color.red, new Color(0x009900), Color.blue, new Color(0x660066), Color.gray};
	static final private Color kArrowColor = new Color(0xFFFF99);
	static final private Color kOtherTermColor = new Color(0x999999);
	
	static final public int headingHeight(Graphics g, int nAlias, int rowSpacing) {
		int nRows = 1 << nAlias;		//	2 to power nAlias;
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		return (nRows + 1) * rowSpacing + nRows * ascent;
	}
	
	private String[] varName;
	private boolean[] completeTerm;		//		defines the interactions between the main variables
																		//		for this column
	private int nComplete;
	
	private boolean[][] aliasTerm;
	
	private int[] x = new int[8];
	private int[] y = new int[8];
	
	private boolean termInvolvesComplete;
	
	public Term(String[] varName, boolean[] completeTerm, int nComplete) {
		this.varName = varName;
		this.nComplete = nComplete;
		this.completeTerm = (boolean[])completeTerm.clone();
	}
	
	public void setDesign(boolean[] design, int aliasIndex) {
		int nAlias = varName.length - nComplete;
		if (aliasTerm == null || aliasTerm.length != nAlias)
			aliasTerm = new boolean[nAlias][varName.length];
		
		for (int i=0 ; i<varName.length ; i++)
			aliasTerm[aliasIndex][i] = completeTerm[i] != design[i];
	}
	
	private void drawTermBackground(Graphics g, int centre, int baseline, int spacing,
																																					String term) {
		g.setColor(kArrowColor);
		FontMetrics fm = g.getFontMetrics();
		int charWidth = fm.stringWidth(term);
		int ascent = fm.getAscent();
		y[0] = y[6] = y[7] = baseline - ascent - spacing;
		y[1] = y[2] = y[4] = y[5] = baseline;
		y[3] = baseline + spacing;
		
		x[0] = x[1] = x[7] = centre - charWidth;
		x[2] = centre - charWidth - spacing;
		x[3] = centre;
		x[4] = centre + charWidth + spacing;
		x[5] = x[6] = centre + charWidth;
		
		g.fillPolygon(x, y, 8);
		
		g.setColor(Color.red);
		g.drawPolygon(x, y, 8);
	}
	
	public boolean isMainAliasEffect(int index) {
		for (int i=0 ; i<aliasTerm[index].length ; i++)
			if ((i != nComplete + index) == aliasTerm[index][i])
				return false;
		return true;
	}
	
	private int recursiveDrawTerms(Graphics g, int centre, int lineHt, boolean[] term,
																						int fromIndex, int nActiveAlias, int baseline) {
		if (fromIndex == aliasTerm.length) {
			if (nActiveAlias > 1) {
				termInvolvesComplete = false;			//	is the term one that only involves alias variables?
				for (int i=0 ; i<nComplete ; i++)
					if (term[i])
						termInvolvesComplete = true;
				
				String aliasName = translateToName(term);
				int aliasNameWidth = g.getFontMetrics().stringWidth(aliasName);
				g.drawString(aliasName, centre - aliasNameWidth / 2, baseline);
				baseline += lineHt;
			}
		}
		else {
			baseline = recursiveDrawTerms(g, centre, lineHt, term, fromIndex + 1,
																																		nActiveAlias, baseline);
			for (int i=0 ; i<term.length ; i++)
				term[i] = (term[i] != aliasTerm[fromIndex][i]) != completeTerm[i];
			baseline = recursiveDrawTerms(g, centre, lineHt, term, fromIndex + 1,
																																	nActiveAlias + 1, baseline);
			for (int i=0 ; i<term.length ; i++)
				term[i] = (term[i] != aliasTerm[fromIndex][i]) != completeTerm[i];
		}
		return baseline;
	}
	
	public void drawHeading(Graphics g, int centre, int rowSpacing, boolean showBlocks) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		Color oldColor = g.getColor();
		
		int baseline = rowSpacing + ascent;
		
		boolean term[] = (boolean[])completeTerm.clone();
		g.setColor(kOtherTermColor);
		termInvolvesComplete = true;
		baseline = recursiveDrawTerms(g, centre, rowSpacing + ascent, term, 0, 0, baseline);
		
		for (int i=0 ; i<aliasTerm.length ; i++) {
			String aliasName = translateToName(aliasTerm[i]);
			int aliasNameWidth = fm.stringWidth(aliasName);
			if (isMainAliasEffect(i))
				drawTermBackground(g, centre, baseline, rowSpacing * 2 / 3, aliasName);
			g.setColor(kAliasColor[i]);
			g.drawString(aliasName, centre - aliasNameWidth / 2, baseline);
			
			baseline += rowSpacing + ascent;
		}
		
		String completeName = translateToName(completeTerm);
		int completeNameWidth = fm.stringWidth(completeName);
		
		if (showBlocks)
			for (int i=0 ; i<aliasTerm.length ; i++)
				if (isMainAliasEffect(i) || !termInvolvesComplete) {
					g.setColor(Color.white);
					g.fillRect(centre - completeNameWidth / 2 - 3, baseline - ascent - 1, completeNameWidth + 6, ascent + 4);
					g.setColor(Color.red);
					g.drawRect(centre - completeNameWidth / 2 - 3, baseline - ascent - 1, completeNameWidth + 6, ascent + 4);
				}
		
		g.setColor(oldColor);
		g.drawString(completeName, centre - completeNameWidth / 2, baseline);
	}
	
	private String translateToName(boolean[] active) {
		StringBuffer sb = new StringBuffer(varName.length);
		for (int i=0 ; i<varName.length ; i++)
			if (active[i])
				sb.append(varName[i]);
		return sb.toString();
	}
	
	public int getOrder() {
		int order = 0;
		for (int i=0 ; i<completeTerm.length ; i++)
			if (completeTerm[i])
				order ++;
		return order;
	}
	
	public boolean getValue(int index) {
		boolean val = true;
		for (int i=nComplete-1 ; i>=0 ; i--) {
			if (completeTerm[i] && index % 2 == 0)
				val = !val;
			index /= 2;
		}
		return val;
	}
	
	public int getAliasDefnHit(int y, int headingGap, int ascent) {
		int nAlias = completeTerm.length - nComplete;
		
		int nExtraAlias = 1 << nAlias;		//	2 to power nAlias;
		nExtraAlias -= (nAlias + 1);
		
		y -= headingGap / 2 + nExtraAlias * (ascent + headingGap);
		if (y < 0)
			return -1;
		int itemHit = y / (ascent + headingGap);
		if (itemHit < nAlias)
			return itemHit;
		else
			return -1;
	}
	
	public boolean[] aliasedDesign(int aliasIndex) {
		boolean design[] = (boolean[])completeTerm.clone();
		design[nComplete + aliasIndex] = true;
		return design;
	}
	
	public Color getValueColor() {
		for (int i=0 ; i<varName.length-nComplete ; i++)
			if (isMainAliasEffect(i))
				return kAliasColor[i];
		return Color.black;
	}
}