package sport;

import java.awt.*;

import imageGroups.AudioVisual;


public class LeagueTableImages extends AudioVisual {
	static public Image homeTeam, awayTeam, homePoints, awayPoints, totalPoints;
	
	static final public int kHomeTeamWidth = 19;
	static final public int kHomeTeamHeight = 108;
	static final public int kAwayTeamWidth = 110;
	static final public int kAwayTeamHeight = 17;
	static final public int kPointsHeight = 29;
	static final public int kPointsWidth = 37;
	
	synchronized static public void loadLeageTable(Component theComponent) {
		if (homeTeam != null)
			return;
		
		MediaTracker tracker = new MediaTracker(theComponent);
		
		homeTeam = loadImage("sport/homeTeam.gif", tracker, homeTeam, theComponent);
		awayTeam = loadImage("sport/awayTeam.gif", tracker, awayTeam, theComponent);
		homePoints = loadImage("sport/homePoints.gif", tracker, homePoints, theComponent);
		awayPoints = loadImage("sport/awayPoints.gif", tracker, awayPoints, theComponent);
		totalPoints = loadImage("sport/totalPoints.gif", tracker, totalPoints, theComponent);
		
		waitForLoad(tracker);
	}
}