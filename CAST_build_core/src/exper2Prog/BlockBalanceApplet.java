package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import exper2.*;

public class BlockBalanceApplet extends XApplet {
//	static final private String Y_NAME_PARAM = "yVarName";
	static final private String Y_MODEL_PARAM = "yModel";
	static final private String BLOCK_NAME_PARAM = "blockVarName";
	static final private String BLOCK_VALUES_PARAM = "blockValues";
	static final private String BLOCK_LABELS_PARAM = "blockLabels";
	static final private String TREAT_NAME_PARAM = "treatVarName";
	static final private String TREAT_VALUES_PARAM = "treatValues";
	static final private String TREAT_LABELS_PARAM = "treatLabels";
	
	static final private String DATA_NAMES_PARAM = "dataNames";
	static final private String MAX_SE_PARAM = "maxSe";
	
	static final private String[] kXKeys = {"block", "treat"};
	
	static final protected Color kSeColor = new Color(0x000099);
	static final protected Color kCountColor = new Color(0x990000);
	
	protected DataSet data;
	
	private XChoice dataChoice;
	private int currentDataIndex = 0;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 30));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 0));
			
			String dataNamesString = getParameter(DATA_NAMES_PARAM);
			if (dataNamesString != null) {
				dataChoice = new XChoice(this);
				StringTokenizer st = new StringTokenizer(dataNamesString, "#");
				while (st.hasMoreTokens())
					dataChoice.addItem(st.nextToken());
				
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				choicePanel.add(dataChoice);
				topPanel.add("North", choicePanel);
			}
			
			topPanel.add("Center", designPanel(data));
			
		add("North", topPanel);
		
		add("Center", displayPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		data.addCatVariable("block", getParameter(BLOCK_NAME_PARAM),
											getParameter(BLOCK_VALUES_PARAM), getParameter(BLOCK_LABELS_PARAM));
											
		data.addCatVariable("treat", getParameter(TREAT_NAME_PARAM),
											getParameter(TREAT_VALUES_PARAM), getParameter(TREAT_LABELS_PARAM));
		
			MultipleRegnModel model = new MultipleRegnModel("Model", data, kXKeys,
																															getParameter(Y_MODEL_PARAM));
		data.addVariable("model", model);
		
		return data;
	}
	
	private XPanel designPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			IncompleteBlockView theView = new IncompleteBlockView(data, this, null, "block", "treat");
			theView.setFont(getBigFont());
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new BorderLayout(0, 0));
			
				NumValue maxSe = new NumValue(getParameter(MAX_SE_PARAM));
				DiffTreatSeView theView = new DiffTreatSeView(data, this, "block", "treat",
																										"model", maxSe, kSeColor, kCountColor);
				theView.setFont(getBigFont());
			
			innerPanel.add("Center", theView);
			
				XPanel countTitlePanel = new XPanel();
				countTitlePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
					XLabel countLabel = new XLabel(translate("No of times in same block"), XLabel.LEFT, this);
					countLabel.setFont(getBigBoldFont());
					countLabel.setForeground(kCountColor);
				countTitlePanel.add(countLabel);
			
			innerPanel.add("North", countTitlePanel);
			
				XPanel seTitlePanel = new XPanel();
				seTitlePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
					XLabel seLabel = new XLabel(translate("SE of difference between levels"), XLabel.LEFT, this);
					seLabel.setFont(getBigBoldFont());
					seLabel.setForeground(kSeColor);
				seTitlePanel.add(seLabel);
			
			innerPanel.add("South", seTitlePanel);
			
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dataChoice) {
			int newChoice = dataChoice.getSelectedIndex();
			if (newChoice != currentDataIndex) {
				currentDataIndex = newChoice;
				CatVariable treatVar = (CatVariable)data.getVariable("treat");
				String valueParam = TREAT_VALUES_PARAM;
				if (newChoice > 0)
					valueParam += (newChoice + 1);
				treatVar.readValues(getParameter(valueParam));
				
				CatVariable blockVar = (CatVariable)data.getVariable("block");
				valueParam = BLOCK_VALUES_PARAM;
				if (newChoice > 0)
					valueParam += (newChoice + 1);
				blockVar.readValues(getParameter(valueParam));
				
				data.variableChanged("treat");
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