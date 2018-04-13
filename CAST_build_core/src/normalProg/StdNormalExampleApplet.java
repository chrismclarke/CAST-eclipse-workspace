package normalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;

import normal.*;


public class StdNormalExampleApplet extends XApplet {
	static final private String N_MODEL_PARAM = "nModels";
	static final private String MODEL_NAME_PARAM = "modelName";
	static final private String MODEL_DESC_PARAM = "modelDescription";
	static final private String MODEL_PARAM_PARAM = "modelParams";
	
//	static final private String kStdNormalAxis = "-3.5 3.5 -3 1";
	static final private String kStdNormalDistn = "0 1";
	
	private String[] modelName;
	private String[] modelDesc;
	private NumValue mean[];
	private NumValue sd[];
	
	private DataSet data;
	
	private XChoice modelChoice;
	private int currentModel = 0;
	private XTextArea modelDescArea;
	
	private StdNormalView normView;
	private StdNormalAxis modelAxis;
	
	public void setupApplet() {
		readModels();
		data = getData();
		
		setLayout(new BorderLayout(0, 20));
		add("Center", displayPanel(data));
		add("North", controlPanel(data));
	}
	
	private void readModels() {
		int nModels = Integer.parseInt(getParameter(N_MODEL_PARAM));
		modelName = new String[nModels];
		modelDesc = new String[nModels];
		mean = new NumValue[nModels];
		sd = new NumValue[nModels];
		
		for (int i=0 ; i<nModels ; i++) {
			modelName[i] = getParameter(MODEL_NAME_PARAM + i);
			modelDesc[i] = getParameter(MODEL_DESC_PARAM + i);
			StringTokenizer st = new StringTokenizer(getParameter(MODEL_PARAM_PARAM + i));
			mean[i] = new NumValue(st.nextToken());
			sd[i] = new NumValue(st.nextToken());
		}
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		NormalDistnVariable z = new NormalDistnVariable("z");
		z.setParams(kStdNormalDistn);
		z.setMinSelection(0.0);
		z.setMaxSelection(0.0);
		data.addVariable("z", z);
		
		return data;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
				modelChoice = new XChoice(this);
				for (int i=0 ; i<modelName.length ; i++)
					modelChoice.addItem(modelName[i]);
			choicePanel.add(modelChoice);
			
		thePanel.add("North", choicePanel);
		
			modelDescArea = new XTextArea(modelDesc, 0, 450, this);
			modelDescArea.lockBackground(Color.white);
		thePanel.add("Center", modelDescArea);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			modelAxis = new StdNormalAxis(this);
			modelAxis.setupStdAxis(mean[0], sd[0]);
		thePanel.add("Bottom", modelAxis);
		
			StdNormalAxis theHorizAxis = new StdNormalAxis(this);
			theHorizAxis.setupStdAxis();
		thePanel.add("Bottom", theHorizAxis);
		
			normView = new StdNormalView(data, this, theHorizAxis, "z");
			normView.setZValue(5.0);
			normView.setSD(sd[0]);
			normView.setFont(getBigFont());
			normView.lockBackground(Color.white);
		thePanel.add("Center", normView);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int newModel = modelChoice.getSelectedIndex();
			if (newModel != currentModel) {
				currentModel = newModel;
				
				modelAxis.setupStdAxis(mean[newModel], sd[newModel]);
				modelAxis.repaint();
				
				modelDescArea.setText(newModel);
				
				normView.setSD(sd[newModel]);
				normView.repaint();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}