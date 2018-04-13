package percentile;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import formula.*;


public class PropnWorkingPanel extends XPanel implements PropnConstants {
	
	static final protected String kZeroOneAxis = "0 1 0.0 0.2";
	
	private DataSet data, refData;
	
	private SimplePropnFormulaPanel formula;
	private HorizAxis horizAxis;
	private CumDotBoxPlotView cumView;
	
	public PropnWorkingPanel(DataSet data, DataSet refData, XApplet applet,
															String horizAxisInfo, String variableName, NumValue maxY) {
		this.data = data;
		this.refData = refData;
		setLayout(new BorderLayout(0, 0));
		add("North", topPanel(data, applet));
		add("Center", dataDisplayPanel(data, refData, applet, horizAxisInfo, variableName));
		add("South", propnCalcPanel(data, refData, applet, maxY));
	}
	
	public void setReferenceValue(NumValue ref) {
		NumVariable refVar = cumView.getReferenceVariable();
		refVar.setValueAt(new NumValue(ref), 0);
		data.variableChanged("y");
		refData.valueChanged(0);
	}
	
	public void changeData(String axisInfo, String yVarName, NumValue maxCutoff) {
		horizAxis.readNumLabels(axisInfo);
		horizAxis.setAxisName(yVarName);
		horizAxis.invalidate();
		cumView.invalidate();
		formula.changeDetails(maxCutoff);
	}
	
	protected XPanel topPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			XLabel cumLabel = new XLabel(applet.translate("Cumulative proportion"), XLabel.LEFT, applet);
			cumLabel.setFont(applet.getStandardFont());
			cumLabel.setForeground(Color.red);
		thePanel.add(cumLabel);
		return thePanel;
	}
	
	protected XPanel dataDisplayPanel(DataSet data, DataSet referenceData, XApplet applet,
																										String horizAxisInfo, String variableName) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			horizAxis = new HorizAxis(applet);
			horizAxis.readNumLabels(horizAxisInfo);
			horizAxis.setAxisName(variableName);
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = new VertAxis(applet);
			vertAxis.readNumLabels(kZeroOneAxis);
			vertAxis.setForeground(Color.red);
		thePanel.add("Left", vertAxis);
		
			cumView = new CumFunctDotPlotView(data, applet, horizAxis, referenceData, "ref", LESS_EQUAL, vertAxis);
		thePanel.add("Center", cumView);
		return thePanel;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet referenceData, XApplet applet, NumValue maxY) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			FormulaContext boldContext = new FormulaContext(Color.black, applet.getStandardBoldFont(), applet);
			formula = new PropnFormulaPanel(data, "y", "ref", referenceData, maxY, LESS_EQUAL, boldContext);
		thePanel.add(formula);
		return thePanel;
	}
}
