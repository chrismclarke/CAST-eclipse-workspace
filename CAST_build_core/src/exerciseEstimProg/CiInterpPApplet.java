package exerciseEstimProg;


public class CiInterpPApplet extends CiInterpApplet {
	//	basically identical but a new class is needed to appear under a different topic in the tests
	//																																(propn instead of mean)
	
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("success", "string");
	}
	
	protected void createDisplay() {
		meanNotPropn = false;
		super.createDisplay();
	}
	
	protected String getSuccess() {
		return getStringParam("success");
	}
	
	protected String getUnits() {
		return null;
	}
	
	protected String getNewValue() {
		return null;
	}
	
	protected String getSampleStat() {
		return null;
	}
	
	protected String getPopnMean() {
		return null;
	}
}