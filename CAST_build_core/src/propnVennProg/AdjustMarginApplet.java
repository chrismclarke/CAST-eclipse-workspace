package propnVennProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import propnVenn.*;


public class AdjustMarginApplet extends AreaContin2Applet {
	static final private String kBlankProbAxisInfo = "0 1 2 1";
	
	private ParameterSlider pxSlider;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
		
		thePanel.add(ProportionLayout.LEFT, propnVennPanel(data, false));
		thePanel.add(ProportionLayout.RIGHT, propnVennPanel(data, true));
		
		return thePanel;
	}
	
	protected AreaContinCoreView getPropnVenn(DataSet data, VertAxis vertAxis,
															HorizAxis horizAxis, boolean yMarginal) {
		if (yMarginal)
			return new AreaContin00View(data, this, vertAxis, horizAxis, "y", "x",
																									AreaContin2View.CANNOT_SELECT, yMarginal);
		else
			return new AreaContin2View(data, this, vertAxis, horizAxis, "y", "x",
																									AreaContin2View.CANNOT_SELECT, yMarginal);
	}
	
	protected void setupAxis(NumCatAxis theAxis) {
		theAxis.readNumLabels(kBlankProbAxisInfo);
		theAxis.setShowUnlabelledAxis(false);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			
			String xProbString = getParameter(X_MARGIN_PARAM);
			StringTokenizer st = new StringTokenizer(xProbString);
			NumValue startProb = new NumValue(st.nextToken());
			
			CatVariableInterface xVar = (CatVariableInterface)data.getVariable("x");
			String catX0 = xVar.getLabel(0).toString();
			
			pxSlider = new ParameterSlider(new NumValue(0.01, 2), new NumValue(0.99, 2),
																	startProb, "Marginal prob of " + catX0, this);
			pxSlider.setFont(getBigBoldFont());
			
		thePanel.add(ProportionLayout.LEFT, pxSlider);
		
			XPanel probPanel = new XPanel();
			probPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																										VerticalLayout.VERT_CENTER, 2));
				
				CatVariableInterface yVar = (CatVariableInterface)data.getVariable("y");
				String catY0 = yVar.getLabel(0).toString();
				XLabel probLabel = new XLabel("Condit prob of " + catX0 + " given " + catY0, XLabel.CENTER, this);
				probLabel.setFont(getBigBoldFont());
			
			probPanel.add(probLabel);
				ConditionalProbView prob = new ConditionalProbView(data, this, "x", "y");
				prob.setFont(getBigBoldFont());
			probPanel.add(prob);
			
			probPanel.lockBackground(Color.yellow);
		thePanel.add(ProportionLayout.RIGHT, probPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == pxSlider) {
			CatDistnVariable xVar = (CatDistnVariable)data.getVariable("x");
			
			double newP0 = pxSlider.getParameter().toDouble();
			double newRemainder = 1.0 - newP0;
			
			double[] probs = xVar.getProbs();
			probs[0] = newP0;
			
			double oldRemainder = 0.0;
			for (int i=1 ; i<probs.length ; i++)
				oldRemainder += probs[i];
			if (oldRemainder <= 0.0)
				for (int i=1 ; i<probs.length ; i++)
					probs[i] = newRemainder / (probs.length - 1);
			else
				for (int i=1 ; i<probs.length ; i++)
					probs[i] *= newRemainder / oldRemainder;
			
			data.variableChanged("x");
			return true;
		}
		else
			return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}