package samplingProg;

import dataView.*;


public class SampStatsApplet extends PopSampStatsApplet {
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected XPanel dotPlotPanel(DataSet data, String freqKey, boolean popNotSamp) {
		return super.dotPlotPanel(data, null, popNotSamp);
	}
	
	protected XPanel summaryPanel(DataSet data, String freqKey, boolean popNotSamp) {
		return super.summaryPanel(data, null, popNotSamp);
	}
	
	protected XPanel controlPanel(DataSet data) {
		return new XPanel();
	}
}