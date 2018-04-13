package randomisationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import coreSummaries.*;
import imageGroups.*;

import sampling.*;
import randomisation.*;


public class LeagueSDApplet extends CoreRandomisationApplet {
	static final private String MAX_SD_PARAM = "maxSD";
	static final private String SD_NAME_PARAM = "sdName";
	static final private String DRAW_PROB_PARAM = "drawProb";
	static final private String WINS_PARAM = "wins";
	static final private String DRAWS_PARAM = "draws";
	
	private NumValue maxSD;
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			LabelVariable teamVar = new LabelVariable(getParameter(LABEL_NAME_PARAM));
			teamVar.readValues(getParameter(LABELS_PARAM));
		data.addVariable("team", teamVar);
		
			int noOfTeams = teamVar.noOfValues();
			double drawProb = Double.parseDouble(getParameter(DRAW_PROB_PARAM));
			RandomLeagueVariable points = new RandomLeagueVariable(getParameter(VAR_NAME_PARAM),
																																	noOfTeams, drawProb);
			int wins[] = new int[noOfTeams];
			int draws[] = new int[noOfTeams];
			StringTokenizer winTok = new StringTokenizer(getParameter(WINS_PARAM));
			StringTokenizer drawTok = new StringTokenizer(getParameter(DRAWS_PARAM));
			for (int i=0 ; i<noOfTeams ; i++) {
				wins[i] = Integer.parseInt(winTok.nextToken());
				draws[i] = Integer.parseInt(drawTok.nextToken());
			}
			points.setResults(wins, draws);
			
		data.addVariable("actualPts", points);
			
			RandomLeagueVariable randPoints = new RandomLeagueVariable(getParameter(VAR_NAME_PARAM),
																																	noOfTeams, drawProb);
			int wins2[] = new int[noOfTeams];
			int draws2[] = new int[noOfTeams];
			for (int i=0 ; i<noOfTeams ; i++) {
				wins2[i] = wins[i];
				draws2[i] = draws[i];
			}
			randPoints.setResults(wins2, draws2);
		data.addVariable("randomPts", randPoints);
			
		return data;
	}

	
	protected SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "randomPts");
		
			maxSD = new NumValue(getParameter(MAX_SD_PARAM));
			
			SDVariable sdVar = new SDVariable(getParameter(SD_NAME_PARAM), "randomPts",
																																	maxSD.decimals);
			
		summaryData.addVariable("stat", sdVar);
		
		return summaryData;
	}
	
	protected double getActualSimPropn() {
		return 0.5;
	}
	
	protected double getMaxAbsDiff() {
		NumVariable actualVar = (NumVariable)data.getVariable("actualPts");
		ValueEnumeration e = actualVar.values();
		double sx = 0.0;
		double sxx = 0.0;
		int n = 0;
		while (e.hasMoreValues()) {
			double x = e.nextDouble();
			if (!Double.isNaN(x)) {
				sx += x;
				sxx += x * x;
				n ++;
			}
		}
		double actualSD = Math.sqrt((sxx - sx * sx / n) / (n-1));
		return actualSD;
	}
	
	
	protected XPanel dataPlotPanel(DataSet data, int actualOrRandomised) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			String pointsKey = (actualOrRandomised == ACTUAL) ? "actualPts" : "randomPts";
				
		thePanel.add("Center", new LeagueWinsView(data, this, pointsKey, "team"));
		
		thePanel.add("South", createStatisticPanel(data, null, actualOrRandomised));
		
		return thePanel;
	}
	
	protected RandomisationInterface createAndAddView(XPanel targetPanel, DataSet data,
																								int actualOrRandomised, VertAxis numAxis) {
		return null;			//		Not used
	}
	
	protected XPanel createStatisticPanel(DataSet data, DataView dataView,
																																		int actualOrRandomised) {
		MeanSDImages.loadMeanSD(this);
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			String dataKey = (actualOrRandomised == ACTUAL) ? "actualPts" : "randomPts";
		valuePanel.add(new SummaryView(data, this, dataKey, null, SummaryView.SD, maxSD.decimals,
																																								SummaryView.SAMPLE));
		return valuePanel;
	}
	
	protected boolean randomiseNotSimulate() {
		return false;
	}
}