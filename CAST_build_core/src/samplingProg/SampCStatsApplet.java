package samplingProg;

import dataView.*;


public class SampCStatsApplet extends PopSampCStatsApplet {
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		v.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("samp", v);
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		return new XPanel();
	}
}