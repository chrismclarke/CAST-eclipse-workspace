package boxPlotProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.RandomNormal;


class GroupInfo {
	int sampleSize;
	LabelValue groupName;
	RandomNormal generator;
	
	public GroupInfo(String paramString) {
		LabelEnumeration tokens = new LabelEnumeration(paramString);
		sampleSize = Integer.parseInt((String)(tokens.nextElement()));
		groupName = new LabelValue((String)tokens.nextElement());
		double mean = Double.parseDouble((String)(tokens.nextElement()));
		double sd = Double.parseDouble((String)(tokens.nextElement()));
		double trunc = tokens.hasMoreElements()
										? Double.parseDouble((String)(tokens.nextElement())) : 5.0;
		generator = new RandomNormal(sampleSize, mean, sd, trunc);
		if (tokens.hasMoreElements()) {
			String seedString = (String)tokens.nextElement();
			generator.setSeed((Long.valueOf(seedString)).longValue());
		}
	}
}

public class RandomGroupedBoxApplet extends GroupedBoxApplet {
	static final private String GROUP_INFO_PARAM = "group";
	
	private XButton takeSampleButton;
	
	int totalCount;
	protected Vector group = new Vector(5);
	
	protected void setupData() {
		data = new DataSet();
		synchronized (data) {
			CatVariable groupVar = new CatVariable(getParameter(CAT_NAME_PARAM));
			data.addVariable("group", groupVar);
			NumVariable variable = new NumVariable(getParameter(VAR_NAME_PARAM));
			data.addVariable("y", variable);
			
			totalCount = 0;
			int index = 0;
			while (true) {
				String groupParam = getParameter(GROUP_INFO_PARAM + index);
				if (groupParam == null)
					break;
				GroupInfo groupInfo = new GroupInfo(groupParam);
				group.addElement(groupInfo);
				totalCount += groupInfo.sampleSize;
				index++;
			}
			
			LabelValue groupName[] = new LabelValue[group.size()];
			for (int i=0 ; i<groupName.length ; i++)
				groupName[i] = ((GroupInfo)group.elementAt(i)).groupName;
			groupVar.setLabels(groupName);
			
			index = 0;
			int groupIndex[] = new int[totalCount];
			for (int i=0 ; i<group.size() ; i++) {
				GroupInfo groupIInfo = (GroupInfo)(group.elementAt(i));
				for (int j=0 ; j<groupIInfo.sampleSize ; j++)
					groupIndex[index++] = i;
			}
			groupVar.setValues(groupIndex);
			
			generateNumValues();
		}
	}
	
	protected void generateNumValues() {
		double value[] = new double[totalCount];
		int itemIndex = 0;
		for (int i=0 ; i<group.size() ; i++) {
			GroupInfo groupIInfo = (GroupInfo)(group.elementAt(i));
			double groupVal[] = groupIInfo.generator.generate();
			for (int j=0 ; j<groupIInfo.sampleSize ; j++)
				value[itemIndex++] = groupVal[j];
		}
		data.getNumVariable().setValues(value);
	}
	
	protected XButton createSampleButton(String name) {
		takeSampleButton = new XButton(name, this);
		return takeSampleButton;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		thePanel.add(createSampleButton(translate("Sample")));
		thePanel.add(createDotBoxChoice());
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			generateNumValues();
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}