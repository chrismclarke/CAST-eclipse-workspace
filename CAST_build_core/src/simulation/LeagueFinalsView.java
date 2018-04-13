package simulation;

import java.awt.*;
import java.util.*;

import dataView.*;
import sport.*;


public class LeagueFinalsView extends DataView {
//	static final public String LEAGUE_FINALS_VIEW = "leagueFinalsView";
	
	public LabelValue kAFinalPos, kALeaguePos1, kALeaguePos2, kTop, kNotTop;
	
	static final private int kHeadingVertGap = 4;
	static final private int kHeadingHorizGap = 10;
//	static final private int kTableCellHeight = 24;
	static final private int kCellVertMargin = 12;
	static final private int kCellHorizMargin = 8;
	
	private String leagueRankKey, finalsRankKey;
	
	private Font boldFont, standardFont, headingFont;
	
	private boolean initialised = false;
	private int ascent, descent;

//	private int leagueHeadingWidth;
	private int finalHeadingWidth, headingHt, cellHt, headingWidth, notTopWidth, cellWidth;
	private int minWidth, minHeight;
	
	private int[][] count = {{-1,-1}, {-1,-1}};
	private NumValue tempCount = new NumValue(0, 0);
	
	public LeagueFinalsView(DataSet theData, XApplet applet, String leagueRankKey, String finalsRankKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		standardFont = applet.getBigFont();
		boldFont = applet.getBigBoldFont();
		headingFont = applet.getStandardBoldFont();
		setFont(headingFont);
		
		this.leagueRankKey = leagueRankKey;
		this.finalsRankKey = finalsRankKey;
	
		kAFinalPos = new LabelValue(applet.translate("A's position after finals"));
		StringTokenizer st = new StringTokenizer(applet.translate("A's position*in league"), "*");
		kALeaguePos1 = new LabelValue(st.nextToken());
		kALeaguePos2 = new LabelValue(st.nextToken());
		kTop = new LabelValue(applet.translate("Top"));
		kNotTop = new LabelValue(applet.translate("Not top"));
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
//			leagueHeadingWidth = kAFinalPos.stringWidth(g);
			finalHeadingWidth = Math.max(kALeaguePos1.stringWidth(g), kALeaguePos2.stringWidth(g));
			notTopWidth = kNotTop.stringWidth(g);
			
			headingHt =  2  * (ascent + descent) + 2 * kHeadingVertGap;
			cellHt = ascent + descent + 2 * kCellVertMargin;
			
			headingWidth = finalHeadingWidth + notTopWidth + 2 * kHeadingHorizGap;
			cellWidth = notTopWidth + 2 * kCellHorizMargin;
			
			minWidth = headingWidth + 2 * cellWidth + 2;
			minHeight = headingHt + 2 * cellHt + 2;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public boolean updateCounts() {
		TeamARankVariable leagueRankVar = (TeamARankVariable)getVariable(leagueRankKey);
		TeamARankVariable finalsRankVar = (TeamARankVariable)getVariable(finalsRankKey);
		
		int n00 = 0;
		int n01 = 0;
		int n10 = 0;
		int n11 = 0;
		ValueEnumeration le = leagueRankVar.values();
		ValueEnumeration fe = finalsRankVar.values();
		while (le.hasMoreValues() && le.hasMoreValues()) {
			double nextLeagueRank = le.nextDouble();
			double nextFinalsRank = fe.nextDouble();
			if (nextLeagueRank < 1.5)
				if (nextFinalsRank < 1.5)
					n00 ++;
				else
					n01++;
			else
				if (nextFinalsRank < 1.5)
					n10 ++;
				else
					n11++;
		}
		if (count[0][0] == n00 && count[0][1] == n01 && count[1][0] == n10 && count[1][1] == n11)
			return false;
		else {
			count[0][0] = n00;
			count[0][1] = n01;
			count[1][0] = n10;
			count[1][1] = n11;
			return true;
		}
	}
	
	private boolean lastLeagueWasTop(String rankKey) {
		TeamARankVariable rankVar = (TeamARankVariable)getVariable(rankKey);
		if (rankVar.noOfValues() == 0)
			return false;
		else
			return rankVar.doubleValueAt(rankVar.noOfValues() - 1) < 1.5;
	}
	
	public int[][] getCounts() {
		return count;
	}
	
	private void drawCell(int count, boolean highlight, Color background, Color foreground,
							boolean bold, Graphics g, int left, int top, int width, int height) {
		g.setColor(background);
		g.fillRect(left, top, width, height);
		if (highlight) {
			g.setColor(Color.yellow);
			g.drawRect(left, top, width - 1, height - 1);
			g.drawRect(left + 1, top + 1, width - 3, height - 3);
		}
		g.setColor(foreground);
		g.setFont(bold ? boldFont : standardFont);
		
		tempCount.setValue(count);
		tempCount.drawCentred(g, left + width / 2, top + (height + ascent - descent) / 2);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		updateCounts();
		
		TeamARankVariable rankVar = (TeamARankVariable)getVariable(leagueRankKey);
		boolean hasDoneSimulations = (rankVar.noOfValues() > 0);
		boolean topInLeague = lastLeagueWasTop(leagueRankKey);
		boolean topInFinals = lastLeagueWasTop(finalsRankKey);
		
		int horizOffset = (getSize().width - minWidth) / 2;
		int vertOffset = (getSize().height - minHeight) / 2;
		
		g.setColor(Color.blue);
		kAFinalPos.drawCentred(g, horizOffset + headingWidth + 1 + cellWidth, vertOffset + ascent);
		g.setColor(getForeground());
		kTop.drawCentred(g, horizOffset + headingWidth + 1 + cellWidth / 2,
													vertOffset + 2 * ascent + descent + kHeadingVertGap);
		kNotTop.drawCentred(g, horizOffset + headingWidth + 1 + cellWidth + cellWidth / 2,
													vertOffset + 2 * ascent + descent + kHeadingVertGap);
		
		g.setColor(Color.blue);
		kALeaguePos1.drawRight(g, horizOffset, vertOffset + headingHt + cellHt - descent - 1);
		kALeaguePos2.drawRight(g, horizOffset, vertOffset + headingHt + cellHt + ascent + 1);
		
		g.setColor(getForeground());
		kTop.drawLeft(g, horizOffset + headingWidth - kHeadingHorizGap,
										vertOffset + headingHt + 1 + (cellHt + ascent - descent) / 2);
		kNotTop.drawLeft(g, horizOffset + headingWidth - kHeadingHorizGap,
							vertOffset + headingHt + 1 + cellHt + (cellHt + ascent - descent) / 2);
		
		drawCell(count[0][0], hasDoneSimulations && topInLeague && topInFinals, Color.lightGray,
									Color.black, false, g, horizOffset + headingWidth + 1,
									vertOffset + headingHt + 1, cellWidth, cellHt);
		
		drawCell(count[0][1], hasDoneSimulations && topInLeague && !topInFinals, Color.white,
									Color.blue, true, g, horizOffset + headingWidth + 1 + cellWidth,
									vertOffset + headingHt + 1, cellWidth, cellHt);
		
		drawCell(count[1][0], hasDoneSimulations && !topInLeague && topInFinals, Color.white,
									Color.blue, true, g, horizOffset + headingWidth + 1,
									vertOffset + headingHt + 1 + cellHt, cellWidth, cellHt);
		
		drawCell(count[1][1], hasDoneSimulations && !topInLeague && !topInFinals, Color.lightGray,
									Color.black, false, g, horizOffset + headingWidth + 1 + cellWidth,
									vertOffset + headingHt + 1 + cellHt, cellWidth, cellHt);
		
		g.drawRect(horizOffset + headingWidth, vertOffset + headingHt, 2 * cellWidth + 1,
																									2 * cellHt + 1);
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(minWidth, minHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(leagueRankKey) || key.equals(finalsRankKey))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
