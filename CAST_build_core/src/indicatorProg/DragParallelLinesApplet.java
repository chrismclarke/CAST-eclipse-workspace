package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import multiRegn.*;
import indicator.*;


public class DragParallelLinesApplet extends CoreLinesApplet {
	static final private String MAX_COEFFS_PARAM = "maxCoeffs";
	
	protected MultiLinearEqnView eqn;
	
	protected void addBaselineHandles(Variable xVar, NumVariable yVar, CatVariable zVar,
												MultipleRegnModel model) {
//			int nLines = zVar.noOfCategories();
			Value[] x = new Value[getXDataKeys().length];
			Value y = null;
			
			double xMin = xAxis.minOnAxis;
			double xMax = xAxis.maxOnAxis;
		
			fillXArray(x, new NumValue(xMin), zVar.getLabel(0), xVar, zVar);
			y = new NumValue(model.evaluateMean(x));
		
		xVar.addValue(x[0]);
		yVar.addValue(y);
		zVar.addValue(x[1]);
		
			fillXArray(x, new NumValue(xMax), zVar.getLabel(0), xVar, zVar);
			y = new NumValue(model.evaluateMean(x));
		
		xVar.addValue(x[0]);
		yVar.addValue(y);
		zVar.addValue(x[1]);
	}
	
	protected void addGroupHandles(Variable xVar, NumVariable yVar, CatVariable zVar,
												MultipleRegnModel model) {
		int nLines = zVar.noOfCategories();
		Value[] x = new Value[getXDataKeys().length];
		double xMin = xAxis.minOnAxis;
		double xMax = xAxis.maxOnAxis;
		
		for (int i=1 ; i<nLines ; i++) {
			fillXArray(x, new NumValue(xMin + (i + 1) * (xMax - xMin) / (nLines + 2)),
																												zVar.getLabel(i), xVar, zVar);
			NumValue y = new NumValue(model.evaluateMean(x));
			
			xVar.addValue(x[0]);
			yVar.addValue(y);
			zVar.addValue(x[1]);
		}
	}
	
	protected DragParallelLinesView getLinesView(DataSet data) {
		return new DragParallelLinesView(data, this, xAxis, yAxis, getXDataKeys(), kYDataKey,
																				getXHandleKeys(), kYHandleKey, "model", paramDecimals);
	}
	
	protected XPanel equationPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			InsetPanel insetPanel = new InsetPanel(10, 4, 10, 3);
			insetPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(SHORT_NAMES_PARAM));
			int nExplan = st.countTokens() - 1;
			String yName = st.nextToken();
			String xName[] = new String[nExplan];
			for (int i=0 ; i<nExplan ; i++)
				xName[i] = st.nextToken();
			
			st = new StringTokenizer(getParameter(MAX_COEFFS_PARAM));
			NumValue maxCoeff[] = new NumValue[st.countTokens()];
			for (int i=0 ; i<maxCoeff.length ; i++)
				maxCoeff[i] = new NumValue(st.nextToken());
			
				eqn = new MultiLinearEqnView(data, this, "model", yName, xName, maxCoeff, maxCoeff);
				eqn.setFont(getBigFont());
			
			insetPanel.add(eqn);
			
			insetPanel.lockBackground(kEqnBackgroundColor);
		thePanel.add(insetPanel);
		
		return thePanel;
	}
}