package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;

import inference.*;


public class KernelDisplayApplet extends XApplet {
	static final private String DATA_NAMES_PARAM = "dataNames";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String KERNEL_WIDTH_PARAM = "kernelWidth";
	static final private String JITTERING_PARAM = "jittering";
	
	private DataSet data;
	private String dataNames[] = null;
	
	private KernelEstimateView kernelView;
	private DotPlotView dotPlotView;
	
	private HorizAxis kernelAxis, dotPlotAxis;
	
	private int kernelWidth, minKernalWidth, maxKernalWidth;
	private XNoValueSlider kernelWidthSlider;
	
	private XChoice dataSetChoice;
	private int currentDataSet = 0;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(0.6, 10, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		dataPanel.add(ProportionLayout.TOP, kernelPlotPanel(data));
		dataPanel.add(ProportionLayout.BOTTOM, dotPlotPanel(data));
		
		add("Center", dataPanel);
		add("South", controlPanel(data));
		
		if (dataNames != null)
			add("North", dataChoicePanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		String dataNameString = getParameter(DATA_NAMES_PARAM);
		if (dataNameString != null) {
			StringTokenizer st = new StringTokenizer(dataNameString, "#");
			dataNames = new String[st.countTokens()];
			for (int i=0 ; i<dataNames.length ; i++)
				dataNames[i] = st.nextToken();
		}
		
		NumVariable y = new NumVariable(getParameter(VAR_NAME_PARAM));
		y.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y0", y);
		
		if (dataNames != null)
			for (int i=1 ; i<dataNames.length ; i++) {
				NumVariable yi = new NumVariable(getParameter(VAR_NAME_PARAM + i));
				yi.readValues(getParameter(VALUES_PARAM + i));
				data.addVariable("y" + i, yi);
			}
		
		return data;
	}
	
	private XPanel dataChoicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			dataSetChoice = new XChoice(translate("Data set") + ":", XChoice.HORIZONTAL, this);
			for (int i=0 ; i<dataNames.length ; i++)
				dataSetChoice.addItem(dataNames[i]);
			
		thePanel.add(dataSetChoice);
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			dotPlotAxis = new HorizAxis(this);
			changeAxis(dotPlotAxis, 0, data, true);
		thePanel.add("Bottom", dotPlotAxis);
		
			double jittering = Double.parseDouble(getParameter(JITTERING_PARAM));
			dotPlotView = new DotPlotView(data, this, dotPlotAxis, jittering);
			dotPlotView.setActiveNumVariable("y0");
			dotPlotView.lockBackground(Color.white);
			dotPlotView.setRetainLastSelection(true);
		thePanel.add("Center", dotPlotView);
		
		return thePanel;
	}
	
	private XPanel kernelPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			kernelAxis = new HorizAxis(this);
			changeAxis(kernelAxis, 0, data, false);
		thePanel.add("Bottom", kernelAxis);
		
			StringTokenizer st = new StringTokenizer(getParameter(KERNEL_WIDTH_PARAM));
			minKernalWidth = Integer.parseInt(st.nextToken());
			kernelWidth = Integer.parseInt(st.nextToken());
			maxKernalWidth = Integer.parseInt(st.nextToken());
		
			kernelView = new KernelEstimateView(data, this, kernelAxis, kernelWidth);
			kernelView.lockBackground(Color.white);
		thePanel.add("Center", kernelView);
		
		return thePanel;
	}
	
	private void changeAxis(HorizAxis axis, int index, DataSet data, boolean addVariableName) {
		String labelInfo = getParameter(AXIS_INFO_PARAM + (index == 0 ? "" : String.valueOf(index)));
		axis.readNumLabels(labelInfo);
		if (addVariableName) {
			CoreVariable v = data.getVariable("y" + index);
			axis.setAxisName(v.name);
		}
		axis.invalidate();
	}
	
	private void changeJittering(int index) {
		String jitterString = getParameter(JITTERING_PARAM + (index == 0 ? "" : String.valueOf(index)));
		double jittering = Double.parseDouble(jitterString);
		dotPlotView.setJitter(jittering);
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new BorderLayout());
		kernelWidthSlider = new XNoValueSlider(translate("Narrow"), translate("Wide"), translate("Kernel width"),
																			minKernalWidth, maxKernalWidth, kernelWidth, this);
		thePanel.add("Center", kernelWidthSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == kernelWidthSlider) {
			kernelView.setKernelWidth(kernelWidthSlider.getValue());
			return true;
		}
		else if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != currentDataSet) {
				currentDataSet = newChoice;
				dotPlotView.setActiveNumVariable("y" + newChoice);
				kernelView.setActiveNumVariable("y" + newChoice);
				changeAxis(dotPlotAxis, newChoice, data, true);
				changeAxis(kernelAxis, newChoice, data, false);
				changeJittering(newChoice);
				kernelWidthSlider.setValue(kernelWidthSlider.getMaxValue() / 4);
				data.variableChanged("y" + newChoice);
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}