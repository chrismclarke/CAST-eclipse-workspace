package exercise2;

import java.util.*;


public class StringArray {
	private String[] array;
	
	public StringArray(String valueString) {
		StringTokenizer st = new StringTokenizer(valueString);
		array = new String[st.countTokens()];
		for (int i=0 ; i<array.length ; i++)
			array[i] = st.nextToken().replace('_', ' ');
	}
	
	public int getNoOfStrings() {
		return array.length;
	}
	
	public String getValue(int i) {
		return array[i];
	}
	
	public String toString() {
		return "";
	}
	
	public String[] getStrings() {
		return array;
	}
}