package sport;

import java.awt.*;

import dataView.*;


public class TennisPointsView extends DataView {
//	static final public String TENNIS_POINTS = "tennisPoints";
	
	static final private LabelValue kA = new LabelValue("A");
	static final private LabelValue kB = new LabelValue("B");
	
	static final private int kPlayerGap = 12;
	static final private int kComponentGap = 24;
	static final private int kDataRowGap = 5;
	static final private int kTableLeftRightBorder = 7;
	static final private int kTableTopBottomBorder = 4;
	static final private int kMaxExtraRowGap = 5;
	
	static final private LabelValue kPointLabel[] = {new LabelValue("0"), new LabelValue("15"),
													new LabelValue("30"), new LabelValue("40"), new LabelValue("Adv")};
	
	static final private LabelValue kIntegerLabel[] = {new LabelValue("0"), new LabelValue("1"),
													new LabelValue("2"), new LabelValue("3"), new LabelValue("4"),
													new LabelValue("5"), new LabelValue("6"), new LabelValue("7")};
	
	private LabelValue kSets, kGames, kPoints, kServer, kWinner;
	private String kWonGame, kWonSet, kWonMatch;
	
	private String matchKey;
	private Font boldFont;
	
	private boolean initialised = false;
	private int ascent, boldAscent, boldDescent, boldLeading;
	private int setWidth, gameWidth, pointWidth, serverWidth, winnerWidth;
	
	private int minWidth, minHeight;
	
	public TennisPointsView(DataSet theData, XApplet applet, String matchKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		kSets = new LabelValue(applet.translate("Sets"));
		kGames = new LabelValue(applet.translate("Games (Current Set)"));
		kPoints = new LabelValue(applet.translate("Points (Current Game)"));
		kServer = new LabelValue(applet.translate("Server"));
		kWinner = new LabelValue(applet.translate("Winner"));
		
		kWonGame = " " + applet.translate("won game");
		kWonSet = " " + applet.translate("won set");
		kWonMatch = " " + applet.translate("won match");
		
		this.matchKey = matchKey;
		boldFont = applet.getStandardBoldFont();
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			
			int maxPointsWidth = Math.max(2 * fm.stringWidth("40+") + kPlayerGap,
																	fm.stringWidth("A" + kWonGame));
			
			g.setFont(boldFont);
			fm = g.getFontMetrics();
			boldAscent = fm.getAscent();
			boldDescent = fm.getDescent();
			boldLeading = fm.getLeading();
			
			int playersWidth = 2 * fm.stringWidth("A") + kPlayerGap;
			setWidth = Math.max(kSets.stringWidth(g), playersWidth);
			gameWidth = Math.max(kGames.stringWidth(g), playersWidth);
			pointWidth = Math.max(Math.max(kPoints.stringWidth(g), playersWidth), maxPointsWidth);
			serverWidth = kServer.stringWidth(g);
			winnerWidth = kWinner.stringWidth(g);
			
			minWidth = 2 * kTableLeftRightBorder + setWidth + gameWidth + pointWidth
														+ serverWidth + winnerWidth + 4 * kComponentGap + 2;
			
			TennisMatchVariable match = (TennisMatchVariable)getVariable(matchKey);
			int noOfRows = match.getNoOfRecords();
			
			minHeight = 2 * (boldAscent + boldDescent + boldLeading) + 2 * kTableTopBottomBorder
									+ 2 + noOfRows * ascent + (noOfRows - 1) * kDataRowGap;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		TennisMatchVariable match = (TennisMatchVariable)getVariable(matchKey);
		int noOfRows = match.getNoOfRecords();
//		MatchStatus currentStatus = match.getCurrentStatus();
		
		int extraColGap = (getSize().width - minWidth) / 6;
		int extraRowGap = Math.min(kMaxExtraRowGap,
										(getSize().height - minHeight) / (match.getNoOfRecords() + 1));
		if (getSize().height < minHeight)
			extraRowGap --;
		
		Font standardFont = g.getFont();
		g.setFont(boldFont);
		int baseline1 = boldAscent;
		int baseline2 = 2 * boldAscent + boldDescent + boldLeading;
		
		int serverCentre = kTableLeftRightBorder + 1 + extraColGap + serverWidth / 2;
		kServer.drawCentred(g, serverCentre, (baseline1 + baseline2) / 2);
		
		int winnerCentre = kTableLeftRightBorder + 1 + 2 * extraColGap + kComponentGap
																	+ serverWidth + winnerWidth / 2;
		kWinner.drawCentred(g, winnerCentre, (baseline1 + baseline2) / 2);
		
		int setCentre = kTableLeftRightBorder + 1 + 3 * extraColGap + 2 * kComponentGap
											+ serverWidth + winnerWidth + setWidth / 2;
		kSets.drawCentred(g, setCentre, baseline1);
		kA.drawLeft(g, setCentre - kPlayerGap / 2, baseline2);
		kB.drawRight(g, setCentre + kPlayerGap / 2, baseline2);
		
		int gameCentre = kTableLeftRightBorder + 1 + 4 * extraColGap + 3 * kComponentGap
										+ serverWidth + winnerWidth + setWidth + gameWidth / 2;
		kGames.drawCentred(g, gameCentre, baseline1);
		kA.drawLeft(g, gameCentre - kPlayerGap / 2, baseline2);
		kB.drawRight(g, gameCentre + kPlayerGap / 2, baseline2);
		
		int pointCentre = kTableLeftRightBorder + 1 + 5 * extraColGap + 4 * kComponentGap
								+ serverWidth + winnerWidth + setWidth + gameWidth + pointWidth / 2;
		kPoints.drawCentred(g, pointCentre, baseline1);
		kA.drawLeft(g, pointCentre - kPlayerGap / 2, baseline2);
		kB.drawRight(g, pointCentre + kPlayerGap / 2, baseline2);
		
		g.setColor(Color.white);
		g.setFont(standardFont);
		int tableTop = 2 * (boldAscent + boldDescent + boldLeading);
		g.fillRect(0, tableTop, getSize().width, getSize().height - tableTop);
		g.setColor(Color.black);
		g.drawRect(0, tableTop, getSize().width - 1, getSize().height - tableTop - 1);
		
		int baseline = tableTop + 1 + kTableTopBottomBorder + ascent + extraRowGap;
		for (int i=0 ; i<noOfRows ; i++) {
			MatchStatus status = match.getStatus(i);
			if (status != null) {
				LabelValue serverString = (status.server == 0) ? kA : kB;
				serverString.drawCentred(g, serverCentre, baseline);
				
				LabelValue winnerString = (status.winner == 0) ? kA : kB;
				winnerString.drawCentred(g, winnerCentre, baseline);
				
				LabelValue aSets = kIntegerLabel[status.sets[0]];
				LabelValue bSets = kIntegerLabel[status.sets[1]];
				aSets.drawLeft(g, setCentre - kPlayerGap / 2, baseline);
				bSets.drawRight(g, setCentre + kPlayerGap / 2, baseline);
				g.drawLine(setCentre - 3, baseline - 4, setCentre + 2, baseline - 4);
				
				if (status.matchFinished(match.getNoOfSets())) {
					g.setColor(Color.red);
					g.drawString((status.winner == 0 ? "A" : "B") + kWonMatch, gameCentre - gameWidth / 2, baseline);
					g.setColor(getForeground());
				}
				else if (status.justWonSet()) {
					g.setColor(Color.red);
					g.drawString((status.winner == 0 ? "A" : "B") + kWonSet, gameCentre - gameWidth / 2, baseline);
					g.setColor(getForeground());
				}
				else {
					LabelValue aGamesLabel = kIntegerLabel[status.games[0]];
					LabelValue bGamesLabel = kIntegerLabel[status.games[1]];
					aGamesLabel.drawLeft(g, gameCentre - kPlayerGap / 2, baseline);
					bGamesLabel.drawRight(g, gameCentre + kPlayerGap / 2, baseline);
					g.drawLine(gameCentre - 3, baseline - 4, gameCentre + 2, baseline - 4);
					
					if (status.justWonGame()) {
						g.setColor(Color.red);
						g.drawString((status.winner == 0 ? "A" : "B") + kWonGame, pointCentre - pointWidth / 2, baseline);
						g.setColor(getForeground());
					}
					else {
						boolean tieBreak = (status.games[0] == 6) && (status.games[1] == 6);
						int aPoints = status.points[0];
						int bPoints = status.points[1];
						Value aPointsLabel, bPointsLabel;
						if (tieBreak) {
							g.setColor(Color.blue);
							g.drawString("tb", gameCentre + gameWidth / 2 + 2, baseline);
							aPointsLabel = null;
							if (aPoints < kIntegerLabel.length)
								aPointsLabel = kIntegerLabel[aPoints];
							else
								aPointsLabel = new NumValue(aPoints, 0);
							bPointsLabel = null;
							if (bPoints < kIntegerLabel.length)
								bPointsLabel = kIntegerLabel[bPoints];
							else
								bPointsLabel = new NumValue(bPoints, 0);
						}
						else {
							if (aPoints == bPoints && aPoints > 3)
								aPoints = bPoints = 3;
							else if (aPoints > bPoints && aPoints > 4) {
								bPoints -= (aPoints - 4);
								aPoints = 4;
							}
							else if (bPoints > aPoints && bPoints > 4) {
								aPoints -= (bPoints - 4);
								bPoints = 4;
							}
							
							aPointsLabel = kPointLabel[aPoints];
							bPointsLabel = kPointLabel[bPoints];
						}
						aPointsLabel.drawLeft(g, pointCentre - kPlayerGap / 2, baseline);
						bPointsLabel.drawRight(g, pointCentre + kPlayerGap / 2, baseline);
						g.drawLine(pointCentre - 3, baseline - 4, pointCentre + 2, baseline - 4);
						g.setColor(getForeground());
					}
				}
			}
			baseline += ascent + kDataRowGap + extraRowGap;
		}
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return new Dimension(minWidth, minHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(matchKey))
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
	
