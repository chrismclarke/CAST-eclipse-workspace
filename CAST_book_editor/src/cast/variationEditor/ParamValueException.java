package cast.variationEditor;

public class ParamValueException extends Exception {
	String paramName;

	public ParamValueException(String paramName) {
		super("bad parameter value");
		this.paramName = paramName;
	}

	public String getName() {
		return paramName;
	}
}
