package randomisation;

import java.awt.*;

import dataView.*;


public class LeagueWinsView extends DataView {
//	static final public String LEAGUE_WINS = "leagueWins";
	
	static final private LabelValue kWinString = new LabelValue("Wins");
	static final private LabelValue kDrawString = new LabelValue("Draws");
	static final private LabelValue kPointString = new LabelValue("Pts");
	
	static final private int kNameTableGap = 6;
	static final private int kHeaderTableGap = 3;
	
	
	static final private Color kDarkRed = new Color(0x990000);
	static final private Color kDarkGreen = new Color(0x006600);
	
	private String pointsKey, namesKey;
	
	private int minColWidth, maxNameWidth;
	
	private boolean initialised = false;
	protected int minWidth, minHeight;
	protected int ascent, descent;
	
	public LeagueWinsView(DataSet theData, XApplet applet, String pointsKey, String namesKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.pointsKey = pointsKey;
		this.namesKey = namesKey;
		setFont(applet.getSmallFont());
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			minColWidth = Math.max(kWinString.stringWidth(g), Math.max(kDrawString.stringWidth(g),
																													kPointString.stringWidth(g)));
			
			LabelVariable teamNames = (LabelVariable)getVariable(namesKey);
			maxNameWidth = teamNames.getMaxWidth(g);
			
			int noOfTeams = teamNames.noOfValues();
			
			minWidth = maxNameWidth + kNameTableGap + 3 * minColWidth + 2;
			
			minHeight = (ascent + descent) + kHeaderTableGap + noOfTeams * (ascent + descent) + 2;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		LabelVariable teamNames = (LabelVariable)getVariable(namesKey);
		RandomLeagueVariable results = (RandomLeagueVariable)getVariable(pointsKey);
		
		int noOfTeams = teamNames.noOfValues();
		
		NumValue points[] = results.getSortedData();
		int sortedIndex[] = results.getSortedIndex();
		
		int wins[] = results.getWins();
		int draws[] = results.getDraws();
		
		int halfColGap = (getSize().width - minWidth) / 6;
//		int halfRowGap = (getSize().height - minHeight) / (noOfTeams * 2);
		
		int tableLeft = maxNameWidth + kNameTableGap;
		int tableTop = (ascent + descent) + kHeaderTableGap;
		
		int interiorWidth = 2 + 6 * halfColGap + 3 * minColWidth;
		int interiorHeight = getSize().height - tableTop;
		
		int winsCentre = tableLeft + 1 + halfColGap + minColWidth / 2;
		int drawsCentre = winsCentre + 2 * halfColGap + minColWidth;
		int ptsCentre = drawsCentre + 2 * halfColGap + minColWidth;
		
		g.setColor(Color.white);
		g.fillRect(tableLeft, tableTop, interiorWidth, interiorHeight);
		g.setColor(Color.black);
		g.drawRect(tableLeft, tableTop, interiorWidth - 1, interiorHeight - 1);
		
		int baseline = ascent;
		g.setColor(kDarkGreen);
		kWinString.drawCentred(g, winsCentre, baseline);
		g.setColor(Color.blue);
		kDrawString.drawCentred(g, drawsCentre, baseline);
		g.setColor(kDarkRed);
		kPointString.drawCentred(g, ptsCentre, baseline);
		
		NumValue nWins = new NumValue(0.0, 0);
		NumValue nDraws = new NumValue(0.0, 0);
		
		for (int i=noOfTeams-1 ; i>= 0 ; i--) {
			baseline = tableTop + 1 + (2 * (noOfTeams - i) - 1) * interiorHeight / (2 * noOfTeams) + (ascent - descent) / 2;
			
			Value name = teamNames.valueAt(sortedIndex[i]);
			Value pts = points[i];
			nWins.setValue(wins[sortedIndex[i]]);
			nDraws.setValue(draws[sortedIndex[i]]);
			
			g.setColor(getForeground());
			name.drawLeft(g, tableLeft - kNameTableGap, baseline);
			
			g.setColor(kDarkGreen);
			nWins.drawCentred(g, winsCentre, baseline);
			
			g.setColor(Color.blue);
			nDraws.drawCentred(g, drawsCentre, baseline);
			
			g.setColor(kDarkRed);
			pts.drawCentred(g, ptsCentre, baseline);
		}
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		return super.getMinimumSize();
	}
	
	public Dimension getPreferredSize() {
		initialise(getGraphics());
		return new Dimension(minWidth, minHeight);
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(pointsKey))
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
	
