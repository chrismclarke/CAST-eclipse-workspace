package distributionProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;

import distribution.*;


abstract public class CoreContinDistnApplet extends XApplet {
	static final protected String X_AXIS_PARAM = "xAxis";
	static final protected String X_AXIS_NAME_PARAM = "xAxisName";
	static final protected String DENSITY_AXIS_PARAM = "densityAxis";
	
	protected DataSet data;
	
	private XCheckbox densityAxisCheck;
	
	protected NumCatAxis densityAxis;
	protected XLabel densityLabel;
	protected ContinuousProbView pdfView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", topPanel());
		
		add("South", sliderPanel());
		
		add("Center", pdfPanel(data, this));
		
		setParamsFromSliders();
	}
	
	abstract protected ContinDistnVariable getDistn();
	
	abstract protected void setParamsFromSliders();
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addVariable("distn", getDistn());
		data.setSelection("distn", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		return data;
	}
	
	private XPanel topPanel() {
		XPanel thePanel = new XPanel();
		GridBagLayout gbl = new GridBagLayout();
		thePanel.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor=GridBagConstraints.SOUTHWEST; gbc.fill=GridBagConstraints.NONE; gbc.gridheight=1;
		gbc.gridwidth=1; gbc.gridx=GridBagConstraints.RELATIVE; gbc.gridy=GridBagConstraints.RELATIVE;
		gbc.insets=new Insets(0,0,0,0); gbc.ipadx=0; gbc.ipady=0;
		gbc.weightx=0.0; gbc.weighty=0.0;		//	ensures that the label is bottom-justified
		
		densityLabel = new XLabel(translate("Probability density") + ", f(x)", XLabel.LEFT, this);
		thePanel.add(densityLabel);
		gbl.setConstraints(densityLabel, gbc);
		
		gbc.anchor=GridBagConstraints.CENTER; gbc.weightx=1.0;
		
		densityAxisCheck = new XCheckbox(translate("Show density axis"), this);
		densityAxisCheck.setState(true);
		thePanel.add(densityAxisCheck);
		gbl.setConstraints(densityAxisCheck, gbc);
		
		return thePanel;
	}
	
	protected XPanel pdfPanel(DataSet data, XApplet applet) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		NumCatAxis xAxis = new HorizAxis(applet);
		xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
		xAxis.setAxisName(getParameter(X_AXIS_NAME_PARAM));
		thePanel.add("Bottom", xAxis);
		
		densityAxis = new VertAxis(applet);
		densityAxis.readNumLabels(getParameter(DENSITY_AXIS_PARAM));
		thePanel.add("Left", densityAxis);
		
		pdfView = new ContinuousProbView(data, applet, "distn", xAxis, densityAxis);
		setDistnSupport(pdfView);
		pdfView.lockBackground(Color.white);
		thePanel.add("Center", pdfView);
		
		return thePanel;
	}
	
	abstract protected void setDistnSupport(ContinuousProbView pdfView);
	
	abstract protected XPanel sliderPanel();
	
	private boolean localAction(Object target) {
		if (target == densityAxisCheck) {
			boolean showAxis = densityAxisCheck.getState();
			densityAxis.show(showAxis);
			densityLabel.show(showAxis);
			
			pdfView.setIgnoreDensityAxis(!showAxis);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}