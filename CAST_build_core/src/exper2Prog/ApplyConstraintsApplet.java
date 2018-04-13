package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import formula.*;
import imageUtils.*;

import regn.*;
import exper2.*;


public class ApplyConstraintsApplet extends DragFactorMeansApplet {
	static final private String CONSTRAINT_PARAM = "constraint";
	static final private String CONSTRAINT_GROUP_PARAM = "constraintGroup";
	static final private String PARAM_DECIMALS_PARAM = "paramDecimals";
	static final private String N_COLUMNS_PARAM = "nColumns";
	static final private String PARAM_NAMES_PARAM = "paramNames";
	
	static final private Color kRssBackgroundColor = new Color(0xFFEEBB);
	static final private Color kConstraintLabelColor = new Color(0x990000);
	static final private Color kCheckBackground = new Color(0xDDDDEE);
	
	private XCheckbox constraintCheck[];
	private int constraintGroup[][];
	private int constraintParent[];
	private double[][] constraints;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
		
			CatVariable xCatVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			xCatVar.readLabels(getParameter(X_LABELS_PARAM));
			xCatVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("xCat", xCatVar);
		
			int paramDecimals = Integer.parseInt(getParameter(PARAM_DECIMALS_PARAM));
			String xKey[] = getXKeys(data);
			
			MultipleRegnModel lsModel = new MultipleRegnModel(translate("Least squares"), data, xKey);
			int nParams = lsModel.noOfParameters();
			int[] bDecs = new int[nParams];
			for (int i=0 ; i<nParams ; i++)
				bDecs[i] = paramDecimals;
			lsModel.setLSParams("y", null, bDecs, 9);
		data.addVariable("ls", lsModel);
		
			ResidValueVariable residVar = new ResidValueVariable(translate("Residual"), data, xKey, "y",
																																			"ls", 9);
		data.addVariable("resid", residVar);
		
		return data;
	}
	
	protected String[] getXKeys(DataSet data) {
		String[] xKey = {"xCat"};
		return xKey;
	}
	
	protected CoreOneFactorView getMeansView(DataSet data, HorizAxis xAxis, VertAxis yAxis) {
		String paramNameString = getParameter(PARAM_NAMES_PARAM);
		String paramNames[] = null;
		if (paramNameString != null) {
			StringTokenizer st = new StringTokenizer(paramNameString);
			paramNames = new String[st.countTokens()];
			for (int i=0 ; i<paramNames.length ; i++)
				paramNames[i] = MText.expandText(st.nextToken());
		}
		CoreOneFactorView view = new OneFactorLabelView(data, this, xAxis, yAxis, "xCat", "y", "ls", paramNames);
		view.setFont(getBigFont());
		return view;
	}
	
	protected XPanel rssPanel(DataSet data) {
		int nConstraints = 0;
		while (true) {
			if (getParameter(CONSTRAINT_PARAM + (nConstraints + 1)) != null)
				nConstraints ++;
			else
				break;
		}
		
		constraints = new double[nConstraints][];
		for (int i=0 ; i<nConstraints ; i++) {
			StringTokenizer st = new StringTokenizer(getParameter(CONSTRAINT_PARAM + (i + 1)));
			double c[] = new double[st.countTokens()];
			for (int j=0 ; j<c.length ; j++)
				c[j] = Double.parseDouble(st.nextToken());
			constraints[i] = c;
		}
		
		int nConstraintGroups = 0;
		while (true) {
			if (getParameter(CONSTRAINT_GROUP_PARAM + (nConstraintGroups + 1)) != null)
				nConstraintGroups ++;
			else
				break;
		}
		
		constraintCheck = new XCheckbox[nConstraintGroups];
		constraintGroup = new int[nConstraintGroups][];
		constraintParent = new int[nConstraintGroups];
		XPanel constraintPanel[] = new XPanel[nConstraintGroups];
		
		for (int i=0 ; i<nConstraintGroups ; i++) {
			StringTokenizer st = new StringTokenizer(getParameter(CONSTRAINT_GROUP_PARAM + (i + 1)), "*");
			
			StringTokenizer st2 = new StringTokenizer(st.nextToken());
			int c[] = new int[st2.countTokens()];
			for (int j=0 ; j<c.length ; j++)
				c[j] = Integer.parseInt(st2.nextToken()) - 1;
			constraintGroup[i] = c;
			
			constraintParent[i] = Integer.parseInt(st.nextToken()) - 1;
			
			constraintCheck[i] = new XCheckbox(MText.expandText(st.nextToken()), this);
			constraintCheck[i].setFont(getBigBoldFont());
			
			XLabel constraintLabel = new XLabel(st.nextToken(), XLabel.CENTER, this);
			constraintLabel.setFont(getStandardBoldFont());
			constraintLabel.setForeground(kConstraintLabelColor);
			XLabel constraintLabel2 = null;
			if (st.hasMoreTokens()) {
				constraintLabel2 = new XLabel(st.nextToken(), XLabel.CENTER, this);
				constraintLabel2.setFont(getStandardBoldFont());
				constraintLabel2.setForeground(kConstraintLabelColor);
			}
			
			constraintPanel[i] = new XPanel();
			constraintPanel[i].setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 1));
			
			if (constraintLabel2 == null)
				constraintPanel[i].add(constraintLabel);
			else {
				XPanel labelPanel = new XPanel();
				labelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, -2));
				labelPanel.add(constraintLabel);
				labelPanel.add(constraintLabel2);
				
				constraintPanel[i].add(labelPanel);
			}
			constraintPanel[i].add(constraintCheck[i]);
		}
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(-20, 5);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			
			int nColumns = Integer.parseInt(getParameter(N_COLUMNS_PARAM));
			if (nConstraintGroups <= nColumns) {
				for (int i=0 ; i<nConstraintGroups ; i++)
					innerPanel.add(constraintPanel[i]);
			}
			else {
				int nRows = (nConstraintGroups + nColumns - 1) / nColumns;
				for (int i=0 ; i<nColumns ; i++) {
					XPanel colPanel = new XPanel();
					colPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 12));
					for (int j=nRows*i ; j<nConstraintGroups&&j<nRows*(i+1) ; j++)
						colPanel.add(constraintPanel[j]);
					
					innerPanel.add(colPanel);
				}
			}
		innerPanel.lockBackground(kCheckBackground);
		thePanel.add(innerPanel);
		
		checkConstaintEnabling();
		
		return thePanel;
	}
	
	private void checkConstaintEnabling() {
		for (int i=0 ; i<constraintCheck.length ; i++) {
			boolean parentOK = (constraintParent[i] < 0) || constraintCheck[constraintParent[i]].getState();
												//		e.g. can only enable main effect if interaction constraint is set
			boolean childrenOK = true;
			for (int j=0 ; j<constraintCheck.length ; j++)
				if (constraintParent[j] == i && constraintCheck[j].getState())
					childrenOK = false;
			
			constraintCheck[i].setEnabled(parentOK && childrenOK);
		}
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(20, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				XPanel rssPanel = new InsetPanel(10, 5, 10, 5);
				rssPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
					
				rssPanel.add(new ImageCanvas("xEquals/residualSsq.png", this));
					
					NumValue maxRss = new NumValue(getParameter(MAX_RSS_PARAM));
					ResidSsq2View rssValue = new ResidSsq2View(data, this, "resid", maxRss, null, 0);
			
					rssValue.setForeground(Color.blue);
				rssPanel.add(rssValue);
				rssPanel.lockBackground(kRssBackgroundColor);
			
			thePanel.add(rssPanel);
		
		return thePanel;
	}
	
	private void fitModel() {
		MultipleRegnModel factorModel = (MultipleRegnModel)data.getVariable("ls");
		
		int nConstraints = 0;
		for (int i=0 ; i<constraintCheck.length ; i++)
			if (constraintCheck[i].getState())
				nConstraints += constraintGroup[i].length;
		
		double c[][] = new double[nConstraints][];
		int index = 0;
		for (int i=0 ; i<constraintCheck.length ; i++)
			if (constraintCheck[i].getState()) {
				for (int j=0 ; j<constraintGroup[i].length ; j++)
					c[index++] = constraints[constraintGroup[i][j]];
			}
		
		factorModel.updateLSParams("y", c);
		data.variableChanged("ls");
	}

	
	private boolean localAction(Object target) {
		for (int i=0 ; i<constraintCheck.length ; i++)
			if (target == constraintCheck[i]) {
				fitModel();
				checkConstaintEnabling();
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