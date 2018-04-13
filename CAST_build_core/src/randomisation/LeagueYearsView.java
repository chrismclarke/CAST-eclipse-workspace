package randomisation;

import java.awt.*;

import dataView.*;


public class LeagueYearsView extends DataView implements RandomisationInterface {
//	static final public String LEAGUE_YEARS = "leagueYears";
	
	static final public int kEndFrame = 40;
	
	static final private int kNameTableGap = 6;
	static final private int kHeaderTableGap = 3;
	
	
	static final private Color kLightGrey = new Color(0xDDDDDD);
	static final private Color kDarkGreen = new Color(0x006600);
//	static final private Color kDarkRed = new Color(0x990000);
//	static final private Color kDarkBlue = new Color(0x0000CC);
	static final private Color kDarkRed = Color.red;
	static final private Color kDarkBlue = Color.blue;
	
	private String pts2001Key, pts2002Key, pts2001Key2, pts2002Key2, namesKey, namesKey2;
	private Value title2001, title2002;
	private int initFixedEntries, endFixedEntries;
	
	private int minColWidth, maxNameWidth;
	
	private boolean initialised = false;
	protected int minWidth, minHeight;
	protected int ascent, descent;
	
	private int oldInvMap[];
	private Color shade[];
	
	public LeagueYearsView(DataSet theData, XApplet applet,
							String pts2001Key, String pts2002Key, String pts2001Key2, String pts2002Key2,
							String namesKey, String namesKey2, int initFixedEntries) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.pts2001Key = pts2001Key;
		this.pts2002Key = pts2002Key;
		this.pts2001Key2 = pts2001Key2;
		this.pts2002Key2 = pts2002Key2;
		this.namesKey = namesKey;
		this.namesKey2 = namesKey2;
		title2001 = new LabelValue(getData().getVariable(pts2001Key).name);
		title2002 = new LabelValue(getData().getVariable(pts2002Key).name);
		this.initFixedEntries = initFixedEntries;
		Variable teams2 = (Variable)theData.getVariable(namesKey2);
		endFixedEntries = teams2.noOfValues() - initFixedEntries;
		setFont(applet.getSmallFont());
		fixOldInfo();
	}
	
	public void fixOldInfo() {
		RandomisedNumVariable yVar = (RandomisedNumVariable)getVariable(pts2002Key);
		int map[] = yVar.getMap();
		if (oldInvMap == null || oldInvMap.length != map.length)
			oldInvMap = new int[map.length];
		for (int i=0 ; i<map.length ; i++)
			oldInvMap[map[i]] = i;
		if (initialised)
			setFrame(0);
		else
			setInitialFrame(0);
	}
	
	public void doAnimation() {
		animateFrames(1, kEndFrame - 1, 40, null);
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			fixOldInfo();
			
			FontMetrics fm = g.getFontMetrics();
			ascent = fm.getAscent();
			descent = fm.getDescent();
			
			minColWidth = Math.max(title2001.stringWidth(g), title2002.stringWidth(g));
			
			LabelVariable teamNames1 = (LabelVariable)getVariable(namesKey);
			LabelVariable teamNames2 = (LabelVariable)getVariable(namesKey2);
			maxNameWidth = Math.max(teamNames1.getMaxWidth(g), teamNames2.getMaxWidth(g));
			
			int noOfTeams = teamNames1.noOfValues() + teamNames2.noOfValues();
			
			minWidth = maxNameWidth + kNameTableGap + 2 * minColWidth + 2;
			
			minHeight = (ascent + descent) + kHeaderTableGap + noOfTeams * (ascent + descent) + 2;
			
			shade = new Color[teamNames1.noOfValues()];
			int red0 = kDarkRed.getRed();
			int green0 = kDarkRed.getGreen();
			int blue0 = kDarkRed.getBlue();
			int red1 = kDarkBlue.getRed();
			int green1 = kDarkBlue.getGreen();
			int blue1 = kDarkBlue.getBlue();
			for (int i=0 ; i<shade.length ; i++)  {
				int red = (i * red1 + (shade.length - i) * red0) / shade.length;
				int green = (i * green1 + (shade.length - i) * green0) / shade.length;
				int blue = (i * blue1 + (shade.length - i) * blue0) / shade.length;
				shade[i] = new Color(red, green, blue);
			}
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		LabelVariable teamNames = (LabelVariable)getVariable(namesKey);
		LabelVariable teamNamesx = (LabelVariable)getVariable(namesKey2);
		NumVariable pts2001 = (NumVariable)getVariable(pts2001Key);
		RandomisedNumVariable pts2002 = (RandomisedNumVariable)getVariable(pts2002Key);
		NumVariable pts2001x = (NumVariable)getVariable(pts2001Key2);
		NumVariable pts2002x = (NumVariable)getVariable(pts2002Key2);
		
		int noOfTeams = teamNames.noOfValues() + initFixedEntries + endFixedEntries;
		
		int halfColGap = (getSize().width - minWidth) / 4;
//		int halfRowGap = (getSize().height - minHeight) / (noOfTeams * 2);
		
		int tableLeft = maxNameWidth + kNameTableGap;
		int tableTop = (ascent + descent) + kHeaderTableGap;
		
		int interiorWidth = 2 + 4 * halfColGap + 2 * minColWidth;
		int interiorHeight = getSize().height - tableTop;
		
		int y2001Centre = tableLeft + 1 + halfColGap + minColWidth / 2;
		int y2002Centre = y2001Centre + 2 * halfColGap + minColWidth;
		
		g.setColor(Color.white);
		g.fillRect(tableLeft, tableTop, interiorWidth, interiorHeight);
		
		g.setColor(kLightGrey);
		g.fillRect(tableLeft, tableTop, interiorWidth, initFixedEntries * interiorHeight / noOfTeams);
			int endHt = endFixedEntries * interiorHeight / noOfTeams;
		g.fillRect(tableLeft, tableTop + interiorHeight - endHt, interiorWidth, endHt);
		
		g.setColor(Color.black);
		g.drawRect(tableLeft, tableTop, interiorWidth - 1, interiorHeight - 1);
		
		int baseline = ascent;
		g.setColor(kDarkGreen);
		title2001.drawCentred(g, y2001Centre, baseline);
		g.setColor(kDarkRed);
		title2002.drawCentred(g, y2002Centre, baseline);
		
		Value name, y2001Pts, y2002Pts;
		int map[] = pts2002.getMap();
		double p = getCurrentFrame() / (double)kEndFrame;
		int nRandomTeams = teamNames.noOfValues();
		
		for (int i=0 ; i<noOfTeams ; i++) {
			baseline = tableTop + 1 + (2 * i + 1) * interiorHeight / (2 * noOfTeams) + (ascent - descent) / 2;
			int randBaseline = baseline;
			int colorIndex;
			
			if (i < initFixedEntries) {
				name = teamNamesx.valueAt(i);
				y2001Pts = pts2001x.valueAt(i);
				y2002Pts = pts2002x.valueAt(i);
				colorIndex = 0;
			}
			else {
				int i2 = i - initFixedEntries;
				if (i2 < nRandomTeams) {
					name = teamNames.valueAt(i2);
					y2001Pts = pts2001.valueAt(i2);
					y2002Pts = pts2002.valueAt(i2);
					int oldI = initFixedEntries + oldInvMap[map[i2]];
					int oldBaseline = tableTop + 1 + (2 * oldI + 1) * interiorHeight / (2 * noOfTeams) + (ascent - descent) / 2;
					randBaseline = (int)Math.round(p * baseline + (1 - p) * oldBaseline);
					colorIndex = map[i2];
				}
				else {
					int i3 = i - nRandomTeams;
					name = teamNamesx.valueAt(i3);
					y2001Pts = pts2001x.valueAt(i3);
					y2002Pts = pts2002x.valueAt(i3);
					colorIndex = nRandomTeams - 1;
				}
			}
			
			g.setColor(getForeground());
			name.drawLeft(g, tableLeft - kNameTableGap, baseline);
			
			g.setColor(kDarkGreen);
			y2001Pts.drawCentred(g, y2001Centre, baseline);
			
			g.setColor(shade[colorIndex]);
			y2002Pts.drawCentred(g, y2002Centre, randBaseline);
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
		if (key.equals(pts2002Key))
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
	
