package exerciseRegn;

import java.awt.*;

import dataView.*;
import valueList.*;
import exercise2.*;
import models.*;


public class ParameterSummaryPanel extends XPanel {
	static final private LabelValue	estimateLabel = new LabelValue("Estimate");
	static final private LabelValue	seLabel = new LabelValue("Std error");
	
	static final private NumValue kZeroValue = new NumValue(0, 0);
	
	private DataSet data;
	private String lsKey;
	private FixedValueView paramName[], paramEst[], paramSe[];
	
	public ParameterSummaryPanel(DataSet data, String lsKey, ExerciseApplet applet) {
		this.data = data;
		this.lsKey = lsKey;
		paramName = new FixedValueView[2];
		paramEst = new FixedValueView[2];
		paramSe = new FixedValueView[2];
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
    GridBagConstraints nameCons = new GridBagConstraints();
		nameCons.anchor = GridBagConstraints.EAST;
		nameCons.fill = GridBagConstraints.NONE;
		nameCons.gridheight = nameCons.gridwidth = 1;
		nameCons.insets = new Insets(3,0,3,0);
		nameCons.ipadx = 10;
		nameCons.ipady = 0;
		nameCons.weightx = nameCons.weighty = 1.0;
		nameCons.gridx = nameCons.gridy = 0;
		
    GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.CENTER;
		cons.fill = GridBagConstraints.NONE;
		cons.gridheight = cons.gridwidth = 1;
		cons.insets = new Insets(3,6,3,6);
		cons.ipadx = 5;
		cons.ipady = 0;
		cons.weightx = cons.weighty = 1.0;
		cons.gridy = 0;
		
		cons.gridx = 1;
		FixedValueView estHeading = new FixedValueView(null, estimateLabel, estimateLabel, applet);
		estHeading.unboxValue();
		estHeading.setFont(applet.getBigFont());
		add(estHeading);
		gbl.setConstraints(estHeading, cons);
		
		cons.gridx ++;
		FixedValueView seHeading = new FixedValueView(null, seLabel, seLabel, applet);
		seHeading.unboxValue();
		seHeading.setFont(applet.getBigFont());
		add(seHeading);
		gbl.setConstraints(seHeading, cons);
		
		nameCons.gridy ++;
		cons.gridy ++;
		cons.ipady = 4;
			
		for (int paramIndex=0 ; paramIndex<2 ; paramIndex++) {
			paramName[paramIndex] = new FixedValueView(null, kZeroValue, kZeroValue, applet);
			paramName[paramIndex].unboxValue();
			paramName[paramIndex].setFont(applet.getBigFont());
			add(paramName[paramIndex]);
			gbl.setConstraints(paramName[paramIndex], nameCons);
			
			cons.gridx = 1;
			paramEst[paramIndex] = new FixedValueView(null, kZeroValue, kZeroValue, applet);
			paramEst[paramIndex].setFont(applet.getBigFont());
			add(paramEst[paramIndex]);
			gbl.setConstraints(paramEst[paramIndex], cons);
			
			cons.gridx ++;
			paramSe[paramIndex] = new FixedValueView(null, kZeroValue, kZeroValue, applet);
			paramSe[paramIndex].setFont(applet.getBigFont());
			add(paramSe[paramIndex]);
			gbl.setConstraints(paramSe[paramIndex], cons);
			
			nameCons.gridy ++;
			cons.gridy ++;
		}
	}
	
	public void updateForNewData(Value interceptName, Value slopeName, NumValue maxParam, NumValue maxSe) {
		paramName[0].setValue(interceptName);
		paramName[0].setMaxValue(interceptName);
		paramName[1].setValue(slopeName);
		paramName[1].setMaxValue(slopeName);
		
		LinearModel ls = (LinearModel)data.getVariable(lsKey);
		paramEst[0].setValue(ls.getIntercept());
		paramEst[0].setMaxValue(maxParam);		//	does revalidate()
		
		paramEst[1].setValue(ls.getSlope());
		paramEst[1].setMaxValue(maxParam);		//	does revalidate()
		
		paramSe[0].setValue(new NumValue(ls.getSeIntercept(), maxSe.decimals));
		paramSe[0].setMaxValue(maxSe);		//	does revalidate()
		
		paramSe[1].setValue(new NumValue(ls.getSeSlope(), maxSe.decimals));
		paramSe[1].setMaxValue(maxSe);		//	does revalidate()
	}
	
	public void setValues(Value interceptName, Value slopeName, NumValue interceptParam, NumValue interceptSe,
																													NumValue slopeParam, NumValue slopeSe) {
		paramName[0].setValue(interceptName);
		paramName[0].setMaxValue(interceptName);
		paramName[1].setValue(slopeName);
		paramName[1].setMaxValue(slopeName);
		
		paramEst[0].setValue(interceptParam);
		paramEst[0].setMaxValue(interceptParam);		//	does revalidate()
		
		paramEst[1].setValue(slopeParam);
		paramEst[1].setMaxValue(slopeParam);		//	does revalidate()
		
		paramSe[0].setValue(interceptSe);
		paramSe[0].setMaxValue(interceptSe);		//	does revalidate()
		
		paramSe[1].setValue(slopeSe);
		paramSe[1].setMaxValue(slopeSe);		//	does revalidate()
	}
	
}