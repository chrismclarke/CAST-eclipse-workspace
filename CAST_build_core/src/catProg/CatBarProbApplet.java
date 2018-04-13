package catProg;

import java.awt.*;

import dataView.*;
import axis.*;

import cat.*;


public class CatBarProbApplet extends CatBarApplet {
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 0));
		add("Center", displayPanel(data));
	}
	
	protected CatBarView getBarView(DataSet data, HorizAxis catAxis, VertAxis countAxis) {
		CatBarView theView = super.getBarView(data, catAxis, countAxis);
		theView.setProbabilityMode(true);
		
		CatVariable catVariable = data.getCatVariable();
		catAxis.setAxisName(catVariable.name);
		return theView;
	}
}