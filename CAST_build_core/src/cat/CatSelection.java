package cat;


public class CatSelection	{
	public boolean valueClicked[];
	public int selectedVal = -1;
	
	public CatSelection(int nValues) {
		valueClicked = new boolean[nValues];
		selectedVal = -1;
	}
	
	public void resetList(int nValues) {
		if (valueClicked.length != nValues)
			valueClicked = new boolean[nValues];
		else
			for (int i=0 ; i<nValues ; i++)
				valueClicked[i] = false;
		
		selectedVal = -1;
	}
	
	public void resetList() {
		resetList(valueClicked.length);
	}
	
	public void completeList() {
		for (int i=0 ; i<valueClicked.length ; i++)
			valueClicked[i] = true;
		
		selectedVal = -1;
	}
	
	public int noOfValues() {
		return valueClicked.length;
	}
	
	public int numberCompleted() {
		int n = 0;
		for (int i=0 ; i<valueClicked.length ; i++)
			if (valueClicked[i])
				n ++;
		return n;
	}
	
	public void selectIndex(int index) {
		if (index >= 0)
			valueClicked[index] = true;
		selectedVal = index;
	}
	
	public void selectNext() {
		if (selectedVal >= 0) {
			valueClicked[selectedVal] = true;
			if (selectedVal < valueClicked.length - 1)
				selectedVal ++;
			else
				selectedVal = -1;
		}
	}
	
	public boolean unselect() {
		if (selectedVal != 0) {
			if (selectedVal > 0)
				selectedVal --;
			else
				selectedVal = valueClicked.length - 1;
			valueClicked[selectedVal] = false;
			return true;
		}
		else
			return false;
	}
}
