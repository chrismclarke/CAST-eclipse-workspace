package dataView;


public class LabelVariable extends Variable {
	public LabelVariable(String theName) {
		super(theName);
	}
	
	public void readValues(String valueString) {
		clearData();
		LabelEnumeration theValues = new LabelEnumeration(valueString);
		while (theValues.hasMoreElements())
			addValue(new LabelValue((String)theValues.nextElement()));
	}
	
	public void setValues(String[] label) {
		clearData();
		for (int i=0 ; i<label.length ; i++)
			addValue(new LabelValue(label[i]));
	}
}
