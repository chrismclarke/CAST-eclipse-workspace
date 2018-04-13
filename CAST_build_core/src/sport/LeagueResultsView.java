package sport;

import java.awt.*;

import dataView.*;


public class LeagueResultsView extends CoreLeagueView {
//	static final public String LEAGUE_RESULTS = "leagueResults";
	
	static final public int kMinColGap = 10;
	static final private String kMaxResultString = "9";
	
	private int minResultWidth, maxResultValueWidth;
	
	public LeagueResultsView(DataSet theData, XApplet applet, String resultsKey) {
		super(theData, applet, resultsKey);
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			FontMetrics fm = g.getFontMetrics();
			
			maxResultValueWidth = fm.stringWidth(kMaxResultString);
			minResultWidth = Math.max(maxResultValueWidth, maxNameWidth);
		
			int noOfTeams = getTeamNames().length;
			
			minWidth = LeagueTableImages.kHomeTeamWidth + 4 * kTableLeftRightBorder
							+ maxNameWidth + maxPointsValueWidth + (noOfTeams + 1) * minResultWidth
							+ (noOfTeams - 1) * kMinColGap + 2;
				
			minHeight = minHeaderHeight + minFooterHeight + 3 * kTableTopBottomBorder
								+ noOfTeams * (ascent + descent) + (noOfTeams - 1) * kMinRowGap + 2;
			
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		LeagueResultsVariable resultsVar = (LeagueResultsVariable)getVariable(resultsKey);
		LabelValue[] teamName =  resultsVar.getTeamNames();
		int[][][] results = resultsVar.getResults();
		int noOfTeams = teamName.length;
		
		int tableLeft = maxNameWidth + kTableLeftRightBorder + LeagueTableImages.kHomeTeamWidth;
		int tableTop = minHeaderHeight;
		
		int valueExtraWidth = (getSize().width - minWidth) / noOfTeams;
		int valueExtraHeight = findExtraHeight(noOfTeams);
		
		int interiorWidth = 2 * kTableLeftRightBorder + 2
														+ noOfTeams * (minResultWidth + valueExtraWidth)
														+ (noOfTeams - 1) * kMinColGap;
		int interiorHeight = findInteriorHeight(noOfTeams, valueExtraHeight);
		
		g.drawImage(LeagueTableImages.awayTeam,
					tableLeft + (interiorWidth - LeagueTableImages.kAwayTeamWidth) / 2, 0,
					LeagueTableImages.kAwayTeamWidth, LeagueTableImages.kAwayTeamHeight, this);
		int topHeadingBaseline = LeagueTableImages.kAwayTeamHeight + ascent;
	
		TableColumnEnumeration columns = new TableColumnEnumeration(tableLeft, minResultWidth,
																				valueExtraWidth, noOfTeams);
		for (int i=0 ; i<noOfTeams ; i++) {
			int columnCenter = columns.nextColumnCentre();
			teamName[i].drawCentred(g, columnCenter, topHeadingBaseline);
		}
	
		g.drawImage(LeagueTableImages.homeTeam, 0,
					tableTop + (interiorHeight - LeagueTableImages.kHomeTeamHeight) / 2,
					LeagueTableImages.kHomeTeamWidth, LeagueTableImages.kHomeTeamHeight, this);
		
		drawTeamNameColumn(g, teamName, null, tableTop, tableLeft, valueExtraHeight);
		
		g.setColor(Color.white);
		g.fillRect(tableLeft, tableTop, interiorWidth, interiorHeight);
		g.setColor(Color.black);
		g.drawRect(tableLeft, tableTop, interiorWidth - 1, interiorHeight - 1);
		
		NumValue tempVal = new NumValue(0, 0);
		int rowTotal[] = resultsVar.getHomePoints();
		int colTotal[] = resultsVar.getAwayPoints();
		
		TableRowEnumeration rows = new TableRowEnumeration(tableTop, ascent, descent, valueExtraHeight, noOfTeams);
		int totalColumnRight = tableLeft + interiorWidth + kTableLeftRightBorder
																								+ maxPointsValueWidth;
		for (int i=0 ; i<noOfTeams ; i++) {
			g.setColor(getForeground());
			int rowBaseline = rows.nextRowBaseline();
			columns = new TableColumnEnumeration(tableLeft, minResultWidth,
																				valueExtraWidth, noOfTeams);
			for (int j=0 ; j<noOfTeams ; j++) {
				int columnCenter = columns.nextColumnCentre();
				if (i == j || results[0][i][j] < 0)
					drawDash(g, columnCenter, rowBaseline);
				else if (results.length == 1) {
					int res = results[0][i][j];
					if (res == RandomResult.DRAW_SCORE) {
						g.drawLine(columnCenter - 3, rowBaseline, columnCenter + 3, rowBaseline - 6);
						g.drawLine(columnCenter - 3, rowBaseline - 6, columnCenter + 3, rowBaseline);
					}
					else {
						LabelValue winner = teamName[(res == RandomResult.LOSE_SCORE) ? j : i];
						winner.drawCentred(g, columnCenter, rowBaseline);
					}
				}
				else {
					int homeScore = 0;
					for (int k=0 ; k<results.length ; k++)
						homeScore += results[k][i][j];
					tempVal.setValue(homeScore);
					tempVal.drawCentred(g, columnCenter, rowBaseline);
				}
			}
			tempVal.setValue(rowTotal[i]);
			g.setColor(kDarkGreen);
			tempVal.drawLeft(g, totalColumnRight, rowBaseline);
		}
		
		g.setColor(Color.blue);
		int totalRowBaseline = tableTop + interiorHeight + kTableTopBottomBorder + 1 + ascent;
		columns = new TableColumnEnumeration(tableLeft, minResultWidth,
																				valueExtraWidth, noOfTeams);
		for (int i=0 ; i<noOfTeams ; i++) {
			int columnCenter = columns.nextColumnCentre();
			tempVal.setValue(colTotal[i]);
			tempVal.drawCentred(g, columnCenter, totalRowBaseline);
		}
	}
}
	
