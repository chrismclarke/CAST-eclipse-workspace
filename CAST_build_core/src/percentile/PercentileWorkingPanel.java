package percentile;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import formula.*;


public class PercentileWorkingPanel extends XPanel {
	static final private String kPercentageAxis = "0 100 0 20";
	static final private String kSingleValueAxis = "0 100 50 100";
																						//	only shows a single value, initialised later
	static final private String kZeroOneAxis = "0 1 3 1";						//		no values are shown
	
	private DataSet data, refData;
	
	protected PercentileFormulaPanel formula;
	private HorizAxis horizAxis;
	private CumFunctInverseView cumView;
	
	public PercentileWorkingPanel(DataSet data, DataSet refData, XApplet applet,
									String horizAxisInfo, String variableName, NumValue maxY, String units) {
		this.data = data;
		this.refData = refData;
		setLayout(new BorderLayout(0, 0));
		add("North", topPanel(data, applet));
		add("Center", dataDisplayPanel(data, refData, applet, horizAxisInfo, variableName));
		add("South", propnCalcPanel(data, refData, applet, maxY, units));
	}
	
	public void setReferenceValue(double ref) {
		NumVariable refVar = cumView.getReferenceVariable();
		NumValue r = (NumValue)refVar.valueAt(0);
		r.setValue(ref);
		data.variableChanged("y");
		refData.valueChanged(0);
	}
	
	public void changeData(String axisInfo, String yVarName, NumValue maxValue, String units) {
		horizAxis.readNumLabels(axisInfo);
		horizAxis.setAxisName(yVarName);
		horizAxis.invalidate();
		cumView.invalidate();
		formula.changeData(maxValue, units);
		setReferenceValue(50);
	}
	
	protected XPanel topPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
			XLabel cumLabel = new XLabel(applet.translate("Cumulative percentage"), XLabel.LEFT, applet);
			cumLabel.setForeground(Color.red);
		thePanel.add(cumLabel);
		return thePanel;
	}
	
	protected XPanel dataDisplayPanel(DataSet data, DataSet refData, XApplet applet,
																									String horizAxisInfo, String variableName) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			horizAxis = new HorizAxis(applet);
			horizAxis.readNumLabels(horizAxisInfo);
			horizAxis.setAxisName(variableName);
			horizAxis.setForeground(CumFunctInverseView.kPercentArrowColor);
		thePanel.add("Bottom", horizAxis);
		
			DragMultiVertAxis vertAxis = new DragMultiVertAxis(applet, 3);
			vertAxis.readNumLabels(kZeroOneAxis);
			vertAxis.readExtraNumLabels(kPercentageAxis);
			vertAxis.readExtraNumLabels(kSingleValueAxis);
			vertAxis.setStartAlternate(1);
			vertAxis.setForeground(Color.red);
		thePanel.add("Left", vertAxis);
		
			cumView = new CumFunctInverseView(data, applet, horizAxis, refData, "ref", vertAxis);
		thePanel.add("Center", cumView);
		return thePanel;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet refData, XApplet applet,
																														NumValue maxY, String units) {
		XPanel thePanel = new InsetPanel(6, 6);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			FormulaContext boldContext = new FormulaContext(Color.black, applet.getStandardBoldFont(), applet);
			formula = new PercentileFormulaPanel(data, "y", "ref", "propn", refData, maxY, units, boldContext);
			
		thePanel.add(formula);
			
		return thePanel;
	}
}