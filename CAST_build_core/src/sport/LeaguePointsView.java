package sport;

import java.awt.*;

import dataView.*;


public class LeaguePointsView extends CoreLeagueView {
//	static final public String LEAGUE_POINTS = "leaguePoints";
	
	static final private int kArrowWidth = 4;
	static final private int kMinColGap = 4;
	
	private boolean keepFooter = false;
	
	static final private Color kDarkRed = new Color(0x990000);
	static final private Color kPaleGrey = new Color(0xCCCCCC);
	
	private int xCoord[] = new int[4];
	private int yCoord[] = new int[4];
	
	private int minPointsWidth;
	private boolean showTeamA = false;
	
	public LeaguePointsView(DataSet theData, XApplet applet, String resultsKey) {
		super(theData, applet, resultsKey);
	}
	
	public void setKeepFooter(boolean keepFooter) {
		this.keepFooter = keepFooter;
	}
	
	public void setShowTeamA(boolean showTeamA) {
		this.showTeamA = showTeamA;
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			minPointsWidth = Math.max(maxPointsValueWidth, LeagueTableImages.kPointsWidth);
		
			int noOfTeams = getTeamNames().length;
			
			minWidth = 3 * kTableLeftRightBorder + maxNameWidth + 3 * minPointsWidth
																						+ 2 * kMinColGap + 2;
			
			minHeight = minHeaderHeight + 2 * kTableTopBottomBorder
								+ noOfTeams * (ascent + descent) + (noOfTeams - 1) * kMinRowGap + 2;
			if (keepFooter)
				minHeight += minFooterHeight;
			
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		LeagueResultsVariable resultsVar = (LeagueResultsVariable)getVariable(resultsKey);
		LabelValue[] teamName =  resultsVar.getTeamNames();
//		int[][][] results = resultsVar.getResults();
		int noOfTeams = teamName.length;
		
		int tableLeft = maxNameWidth + kTableLeftRightBorder;
		int tableTop = minHeaderHeight;
		
		int valueExtraWidth = (getSize().width - minWidth) / 3;
		int valueExtraHeight = findExtraHeight(noOfTeams);
		
		int interiorWidth = 2 * kTableLeftRightBorder + 2
											+ 3 * (minPointsWidth + valueExtraWidth) + 2 * kMinColGap;
		int interiorHeight = findInteriorHeight(noOfTeams, valueExtraHeight);
		
		NumValue tempVal = new NumValue(0, 0);
		int rowTotal[] = resultsVar.getHomePoints();
		int colTotal[] = resultsVar.getAwayPoints();
		
		boolean dataExists = resultsVar.dataExists();
		
		double total[] = resultsVar.getFinalPoints();
		int indexFromRank[] = dataExists ? resultsVar.getRankIndices() : null;
		
		drawTeamNameColumn(g, teamName, indexFromRank, tableTop, tableLeft, valueExtraHeight);
		
		int homeCol = tableLeft + kTableLeftRightBorder + 2 + (minPointsWidth + valueExtraWidth) / 2;
		int awayCol = homeCol + minPointsWidth + valueExtraWidth + kMinColGap;
		int totalCol = awayCol + minPointsWidth + valueExtraWidth + kMinColGap;
		
		g.drawImage(LeagueTableImages.homePoints,
						homeCol - LeagueTableImages.kPointsWidth / 2, 0,
						LeagueTableImages.kPointsWidth, LeagueTableImages.kPointsHeight, this);
		g.drawImage(LeagueTableImages.awayPoints,
						awayCol - LeagueTableImages.kPointsWidth / 2, 0,
						LeagueTableImages.kPointsWidth, LeagueTableImages.kPointsHeight, this);
		g.drawImage(LeagueTableImages.totalPoints,
						totalCol - LeagueTableImages.kPointsWidth / 2, 0,
						LeagueTableImages.kPointsWidth, LeagueTableImages.kPointsHeight, this);
		homeCol += maxPointsValueWidth / 2;
		awayCol += maxPointsValueWidth / 2;
		totalCol += maxPointsValueWidth / 2;
		
		g.setColor(kPaleGrey);
		g.fillRect(tableLeft, tableTop, interiorWidth, interiorHeight);
		g.setColor(Color.black);
		g.drawRect(tableLeft, tableTop, interiorWidth - 1, interiorHeight - 1);
		
		g.setColor(getForeground());
		
		TableRowEnumeration rows = new TableRowEnumeration(tableTop, ascent, descent, valueExtraHeight, noOfTeams);
		for (int i=0 ; i<noOfTeams ; i++) {
			int rowBaseline = rows.nextRowBaseline();
			g.setColor(kDarkGreen);
			if (indexFromRank == null)
				drawDash(g, homeCol, rowBaseline);
			else {
				tempVal.setValue(rowTotal[indexFromRank[i]]);
				tempVal.drawLeft(g, homeCol, rowBaseline);
			}
			
			g.setColor(Color.blue);
			if (indexFromRank == null)
				drawDash(g, awayCol, rowBaseline);
			else {
				tempVal.setValue(colTotal[indexFromRank[i]]);
				tempVal.drawLeft(g, awayCol, rowBaseline);
			}
			
			g.setColor(kDarkRed);
			if (indexFromRank == null)
				drawDash(g, totalCol, rowBaseline);
			else {
				tempVal.setValue(total[indexFromRank[i]]);
				tempVal.drawLeft(g, totalCol, rowBaseline);
			}
			
			if (showTeamA && indexFromRank != null && indexFromRank[i] == 0) {
				xCoord[3] = xCoord[0] = totalCol + 3;
				yCoord[3] = yCoord[0] = rowBaseline - ascent / 2;
				xCoord[2] = xCoord[1] = xCoord[0] + kArrowWidth;
				yCoord[1] = yCoord[0] - kArrowWidth;
				yCoord[2] = yCoord[0] + kArrowWidth;
				g.fillPolygon(xCoord, yCoord, 4);
				g.drawPolygon(xCoord, yCoord, 4);
				
				xCoord[0] -= (maxPointsValueWidth + 6);
				xCoord[3] = xCoord[0];
				xCoord[1] -= (maxPointsValueWidth + 6 + kArrowWidth * 2);
				xCoord[2] = xCoord[1];
				g.fillPolygon(xCoord, yCoord, 4);
				g.drawPolygon(xCoord, yCoord, 4);
			}
		}
	}
}
	
