package stemLeafProg;

import java.awt.*;

import dataView.*;
import imageGroups.TickCrossImages;

import dragStemLeaf.*;


public class FindStemsApplet extends XApplet {
	static final private String CORRECT_STEM_PARAM = "correctStem";
	
	private FindStemsView stemsView;
	
	public void setupApplet() {
		TickCrossImages.loadCrossAndTick(this);
		
		DataSet data = new DataSet();
		StemLeafVariable slv = new StemLeafVariable(getParameter(VAR_NAME_PARAM));
		slv.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", slv);
		
		setLayout(new BorderLayout());
		String correctStemString = getParameter(CORRECT_STEM_PARAM);
		int correctStem = Integer.parseInt(correctStemString);
		stemsView = new FindStemsView(data, correctStem, this);
		add("Center", stemsView);
		stemsView.lockBackground(Color.white);
	}
	
	public void checkStems() {
		stemsView.checkStems();
	}
}