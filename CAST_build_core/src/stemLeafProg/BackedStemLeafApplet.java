package stemLeafProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;

import stemLeaf.*;


public class BackedStemLeafApplet extends XApplet {
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
			BackedStemAndLeafView theStemAndLeaf = new BackedStemAndLeafView(data, this, getParameter(STEM_AXIS_PARAM));
			theStemAndLeaf.lockBackground(Color.white);
			theStemAndLeaf.setRetainLastSelection(true);
		add("South", theStemAndLeaf);
		
		if (data.getVariable("label") != null)
			add("Center", labelPanel(data));
		else
			add("Center", new XPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
									getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		return data;
	}
	
	protected XPanel labelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		thePanel.add(new OneValueView(data, "label", this));
		return thePanel;
	}
}