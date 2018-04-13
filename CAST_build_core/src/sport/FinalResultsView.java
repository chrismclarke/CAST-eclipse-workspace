package sport;

import java.awt.*;

import dataView.*;


public class FinalResultsView extends DataView {
//	static final public String LEAGUE_RESULTS = "leagueResults";
	
	private String kSemiFinalString, kFinalString, kBeatString;
	
	static final private int kMinRowGap = 5;
	static final private int kMaxHeadingGap = 8;
	static final private int kMaxRoundGap = 16;
	static final private int kResultOffset = 12;
	
	private String resultsKey;
	
	private Font bigBoldFont;
	
	private boolean initialised = false;
	private int maxNameWidth, beatStringWidth, maxHeadingWidth;
	private int ascent, descent;
	private int bigAscent, bigDescent;
	private int minWidth, minHeight;
	
	public FinalResultsView(DataSet theData, XApplet applet, String resultsKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.resultsKey = resultsKey;
		kSemiFinalString = applet.translate("Semifinals");
		kFinalString = applet.translate("Final");
		kBeatString = " " + applet.translate("beat") + " ";
		bigBoldFont = applet.getBigBoldFont();
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			LeagueResultsVariable resultsVar = (LeagueResultsVariable)getVariable(resultsKey);
			LabelValue teamName[] = resultsVar.getTeamNames();
			maxNameWidth = 0;
			for (int i=0 ; i<teamName.length ; i++) {
				int newWidth = teamName[i].stringWidth(g);
				if (newWidth > maxNameWidth)
					maxNameWidth = newWidth; 
			}
			beatStringWidth = fm.stringWidth(kBeatString);
			
			Font standardFont = g.getFont();
			g.setFont(bigBoldFont);
			fm = g.getFontMetrics();
			bigAscent = fm.getAscent();
			bigDescent = fm.getDescent();
			maxHeadingWidth = Math.max(fm.stringWidth(kSemiFinalString),
																				fm.stringWidth(kFinalString));
			g.setFont(standardFont);
			
			minWidth = Math.max(maxHeadingWidth, kResultOffset + 2 * maxNameWidth
																						+ beatStringWidth);
			minHeight = 2  * (bigAscent + bigDescent) + 3 * (ascent + descent) + 4 * kMinRowGap;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		int rowGap = kMinRowGap + (getSize().height - minHeight - kMinRowGap) / 3;
		int headingGap = Math.min(rowGap, kMaxHeadingGap);
		int roundGap = Math.min(rowGap, kMaxRoundGap);
		
		Font standardFont = g.getFont();
		g.setFont(bigBoldFont);
		g.drawString(kSemiFinalString, 0, bigAscent);
		g.drawString(kFinalString, 0, 2 * bigAscent + bigDescent + 2 * (ascent + descent)
																		+ headingGap + kMinRowGap + roundGap);
		g.setFont(standardFont);
		
		LeagueResultsVariable resultsVar = (LeagueResultsVariable)getVariable(resultsKey);
		
		if (resultsVar.dataExists()) {
			LabelValue teamName[] = resultsVar.getTeamNames();
			int[] leagueIndex = resultsVar.getRankIndices();
			int[] finalIndex = resultsVar.getFinalsRankIndices();
			
			boolean team0Won = (finalIndex[0] == leagueIndex[0]) || (finalIndex[1] == leagueIndex[0]);
			boolean team1Won = (finalIndex[0] == leagueIndex[1]) || (finalIndex[1] == leagueIndex[1]);
			
			String resultString;
			if (team0Won)
				resultString = teamName[leagueIndex[0]].toString() + kBeatString + teamName[leagueIndex[3]].toString();
			else
				resultString = teamName[leagueIndex[3]].toString() + kBeatString + teamName[leagueIndex[0]].toString();
			g.drawString(resultString, kResultOffset, (bigAscent + bigDescent) + headingGap + ascent);
			
			if (team1Won)
				resultString = teamName[leagueIndex[1]].toString() + kBeatString + teamName[leagueIndex[2]].toString();
			else
				resultString = teamName[leagueIndex[2]].toString() + kBeatString + teamName[leagueIndex[1]].toString();
			g.drawString(resultString, kResultOffset, (bigAscent + bigDescent) + headingGap + 2 * ascent + descent + kMinRowGap);
			
			resultString = teamName[finalIndex[0]].toString() + kBeatString + teamName[finalIndex[1]].toString();
			g.drawString(resultString, kResultOffset, 2 * (bigAscent + bigDescent) + 2 * headingGap + roundGap + 3 * ascent + 2 * descent + kMinRowGap);
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
	
