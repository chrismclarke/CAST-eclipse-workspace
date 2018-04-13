package structureProg;

import java.awt.*;

import dataView.*;
import valueList.*;

import structure.*;


public class BodyDimensionApplet extends XApplet {
	static final private String COLOUR_PARAM = "colour";
	
	static final private String VALUES_PARAMS[] = {"fatValues", "ageValues", "htValues",
											"chestValues", "waistValues", "thighValues", "kneeValues"};
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	
	private String kVarNames[] = {"Fat", "Age", "Height", "Chest", "Waist", "Thigh", "Knee"};
																			
	static final private String kVarKeys[] = {"fat", "age", "ht", "chest", "waist",
																								"thigh", "knee"};
	
	public void setupApplet() {
		kVarNames = new String[7];
		kVarNames[0] = translate("Fat");
		kVarNames[1] = translate("Age");
		kVarNames[2] = translate("Height");
		kVarNames[3] = translate("Chest");
		kVarNames[4] = translate("Waist");
		kVarNames[5] = translate("Thigh");
		kVarNames[6] = translate("Knee");
		
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 10));
		
			ScrollValueList theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
			for (int i=0 ; i<kVarKeys.length ; i++)
				theList.addVariableToList(kVarKeys[i], ScrollValueList.RAW_VALUE);
			
			theList.setRetainLastSelection(true);
		add("Center", theList);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout());
				String colourString = getParameter(COLOUR_PARAM);
				int colour = (colourString == null || colourString.equals("white")) ? PeopleView.WHITE
																										: PeopleView.BLACK;
			topPanel.add("East", new BodyDimensionsView(data, this, theList, colour));
			
				long randomSeed = Long.parseLong(getParameter(RANDOM_SEED_PARAM));
				int noOfValues = ((NumVariable)data.getVariable(kVarKeys[0])).noOfValues();
				PeopleView people = new PeopleView(data, this, randomSeed, noOfValues, colour);
				people.lockBackground(Color.white);
				people.setRetainLastSelection(true);
			topPanel.add("Center", people);
		add("North", topPanel);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		for (int i=0 ; i<kVarNames.length ; i++)
			data.addNumVariable(kVarKeys[i], kVarNames[i], getParameter(VALUES_PARAMS[i]));
		
		return data;
	}
}