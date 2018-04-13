package graphicsProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;
import coreGraphics.*;

import graphics.*;


public class RegionScatterApplet extends ScatterApplet {
	static final private String CHOICE_NAME_PARAM = "choiceName";
	static final private String NO_OF_REGIONS_PARAM = "noOfRegions";
	static final private String REGION_PARAM = "region";
	
	static final private Color kRgnColor = Color.yellow;
	
	private HighlightRgnView rgnView;
	
	private XTextArea message;
	
	private String[] rgnMenuName;
	private String[] rgnMessage;
	private double[][] rgnX;
	private double[][] rgnY;
	
	private XChoice rgnChoice;
	private int currentRgnIndex = 0;
	
	protected DataSet readData() {
		DataSet data = super.readData();
	
		int noOfRgns = Integer.parseInt(getParameter(NO_OF_REGIONS_PARAM));
		rgnX = new double[noOfRgns][];
		rgnY = new double[noOfRgns][];
		rgnMenuName = new String[noOfRgns];
		
		rgnMessage = new String[noOfRgns + 1];
		rgnMessage[0] = "";
		
		for (int i=0 ; i<noOfRgns ; i++) {
			StringTokenizer st = new StringTokenizer(getParameter(REGION_PARAM + i), "#");
			rgnMenuName[i] = st.nextToken();
			rgnMessage[i + 1] = st.nextToken();
			
			st = new StringTokenizer(st.nextToken(), ", ");
			int nPoints = st.countTokens() / 2;
			rgnX[i] = new double[nPoints];
			rgnY[i] = new double[nPoints];
			for (int j=0 ; j<nPoints ; j++) {
				rgnX[i][j] = Double.parseDouble(st.nextToken());
				rgnY[i][j] = Double.parseDouble(st.nextToken());
			}
		}
		
		return data;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 50));
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		if (labelName != null) {
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			labelPanel.add(new XLabel(labelName, XLabel.LEFT, this));
			
				OneValueView labelView = new OneValueView(data, "label", this);
				labelView.setNameDraw(false);
			labelPanel.add(labelView);
			
			thePanel.add(labelPanel);
		}
		
		message = new XTextArea(rgnMessage, 0, 100, this);
		message.lockBackground(Color.white);
		message.setForeground(Color.red);
		thePanel.add(message);
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		rgnView = new HighlightRgnView(data, this, theHorizAxis, theVertAxis, "x", "y");
		rgnView.setRetainLastSelection(true);
		return rgnView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
		
			XLabel choiceLabel = new XLabel(getParameter(CHOICE_NAME_PARAM), XLabel.LEFT, this);
			choiceLabel.setFont(getStandardBoldFont());
		thePanel.add(choiceLabel);
			
			rgnChoice = new XChoice(this);
			rgnChoice.addItem("---");
			for (int i=0 ; i<rgnMenuName.length ; i++)
				rgnChoice.addItem(rgnMenuName[i]);
				
		thePanel.add(rgnChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == rgnChoice) {
			int newChoice = rgnChoice.getSelectedIndex();
			if (newChoice != currentRgnIndex) {
				currentRgnIndex = newChoice;
				
				if (newChoice == 0)
					rgnView.setHighlightRgn(null, null, kRgnColor);
				else
					rgnView.setHighlightRgn(rgnX[newChoice - 1], rgnY[newChoice - 1], kRgnColor);
				rgnView.repaint();
				
				message.setText(newChoice);
				
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}