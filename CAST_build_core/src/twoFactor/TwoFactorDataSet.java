package twoFactor;

import dataView.*;
import models.*;

import multiRegn.*;


public class TwoFactorDataSet extends MultiRegnDataSet {
	static final private String TWO_FACTOR_MEANS_PARAM = "twoFactorMeans";
	static final private String ERROR_SD_PARAM = "errorSD";
	
	public TwoFactorDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void addResponseModel(String yKey, XApplet applet) {
		addErrorVariable(applet);
		
			TwoFactorModel yDistn = new TwoFactorModel(getYVarName(), this, xKeys(),
																TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, false, 0.0);
			yDistn.setParameters(applet.getParameter(TWO_FACTOR_MEANS_PARAM));
			double errorSD = Double.parseDouble(applet.getParameter(ERROR_SD_PARAM));
			yDistn.setSD(errorSD);
		addVariable("model", yDistn);
		
			ResponseVariable yData = new ResponseVariable(getYVarName(), this, xKeys(), "error", "model",
																																			getResponseDecimals());
		addVariable(yKey, yData);
	}
	
	protected void addLeastSquaresFit(XApplet applet) {
		TwoFactorModel lsFit = new TwoFactorModel("least squares", this, xKeys(),
																TwoFactorModel.FACTOR, TwoFactorModel.FACTOR, false, 0.0);
		addVariable("ls", lsFit);
	}

}