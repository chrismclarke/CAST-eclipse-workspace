package sport;

import java.awt.*;

import dataView.*;


abstract public class CoreLeagueView extends DataView {
	static final private String kMaxPointsString = "99";
	
	static final protected Color kDarkGreen = new Color(0x006600);
	
	static final public int kMinRowGap = 5;
	static final public int kTableLeftRightBorder = 7;
	static final public int kTableTopBottomBorder = 4;
	
	protected String resultsKey;
	
	private boolean initialised = false;
	protected int maxNameWidth;
	protected int minWidth, minHeight, minHeaderHeight, minFooterHeight;
	protected int maxPointsValueWidth;
	protected int ascent, descent;
	
	public CoreLeagueView(DataSet theData, XApplet applet, String resultsKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.resultsKey = resultsKey;
		setFont(applet.getSmallFont());
		LeagueTableImages.loadLeageTable(applet);
	}
	
	protected LabelValue[] getTeamNames() {
		LeagueResultsVariable resultsVar = (LeagueResultsVariable)getVariable(resultsKey);
		return resultsVar.getTeamNames();
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			
			LabelValue teamName[] = getTeamNames();
			
			boolean allUpperCase = true;
			for (int i=0 ; i<teamName.length ; i++) {
				String nameString = teamName[i].toString();
				if (!nameString.toUpperCase().equals(nameString)) {
					allUpperCase = false;
					break;
				}
			}
			descent = allUpperCase ? 0 : fm.getDescent();
			
			maxNameWidth = 0;
			for (int i=0 ; i<teamName.length ; i++) {
				int newWidth = teamName[i].stringWidth(g);
				if (newWidth > maxNameWidth)
					maxNameWidth = newWidth; 
			}
			
			maxPointsValueWidth = fm.stringWidth(kMaxPointsString);
			
			minHeaderHeight = Math.max(ascent + descent + LeagueTableImages.kAwayTeamHeight,
												LeagueTableImages.kPointsHeight) + kTableTopBottomBorder;
			minFooterHeight = kTableTopBottomBorder + ascent + descent;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected void drawTeamNameColumn(Graphics g, LabelValue[] teamName, int[] indexFromRank,
													int tableTop, int tableLeft, int valueExtraHeight) {
		int noOfTeams = teamName.length;
		TableRowEnumeration rows = new TableRowEnumeration(tableTop, ascent, descent,
																				valueExtraHeight, noOfTeams);
		for (int i=0 ; i<noOfTeams ; i++) {
			int rowBaseline = rows.nextRowBaseline();
			LabelValue name = (indexFromRank == null) ? teamName[i] : teamName[indexFromRank[i]];
			name.drawLeft(g, tableLeft - kTableLeftRightBorder, rowBaseline);
		}
	}
	
	protected void drawDash(Graphics g, int columnCenter, int baseline) {
		g.drawLine(columnCenter - 3, baseline - 4, columnCenter + 2, baseline - 4);
	}
	
	protected int findExtraHeight(int noOfTeams) {
		int extraHeight = getSize().height - minHeight;
		return (extraHeight >= 0) ? (extraHeight / noOfTeams)
																	: ((extraHeight + 1) / noOfTeams - 1);
	}
	
	protected int findInteriorHeight(int noOfTeams, int valueExtraHeight) {
		return 2 * kTableTopBottomBorder + 2 + noOfTeams * (ascent + descent + valueExtraHeight)
														+ (noOfTeams - 1) * kMinRowGap;
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(minWidth, minHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(resultsKey))
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
	
