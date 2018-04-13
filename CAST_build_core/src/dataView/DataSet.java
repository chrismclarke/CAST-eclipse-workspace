package dataView;

import java.util.*;

import axis.NumCatAxis;

@SuppressWarnings("deprecation")
public class DataSet extends Observable {
	static final public boolean LIST_SELECTION = false;
	static final public boolean GROUP_SELECTION = true;
	
	private Hashtable variables = null;
	private Flags selection = null;
	
	public DataSet() {
		variables = new Hashtable(4);
	}
	
	public synchronized void addVariable(String key, CoreVariable theVariable) {
		variables.put(key, theVariable);
	}
	
	public synchronized void addNumVariable(String key, String nameString, String valueString) {
		NumVariable theVariable = new NumVariable(nameString);
		theVariable.readValues(valueString);
		addVariable(key, theVariable);
	}
	
	public synchronized void addNumVariable(String key, String nameString, double values[]) {
		NumVariable theVariable = new NumVariable(nameString);
		theVariable.setValues(values);
		addVariable(key, theVariable);
	}
	
	public synchronized void addCatVariable(String key, String nameString, String valueString,
																									String labelString) {
		CatVariable theVariable = new CatVariable(nameString);
		theVariable.readLabels(labelString);
		theVariable.readValues(valueString);
		addVariable(key, theVariable);
	}
	
	public synchronized void addCatVariable(String key, String nameString, int values[],
																									String labelString) {
		CatVariable theVariable = new CatVariable(nameString);
		theVariable.readLabels(labelString);
		theVariable.setValues(values);
		addVariable(key, theVariable);
	}
	
	public synchronized void addLabelVariable(String key, String nameString, String valueString) {
		LabelVariable theVariable = new LabelVariable(nameString);
		theVariable.readValues(valueString);
		addVariable(key, theVariable);
	}
	
	public Flags getSelection() {
		initialiseSelection();
		return selection;
	}
	
	public Enumeration getVariableEnumeration() {
		return variables.elements();
	}
	
	public CoreVariable getVariable(String key) {
		return (key == null) ? null : (CoreVariable)variables.get(key);
	}
	
	public NumVariable getNumVariable() {
		Enumeration e = variables.elements();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof NumVariable)
				return (NumVariable)v;
		}
		return null;
	}
	
	public CatVariable getCatVariable() {
		Enumeration e = variables.elements();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof CatVariable)
				return (CatVariable)v;
		}
		return null;
	}
	
	public DistnVariable getDistnVariable() {
		Enumeration e = variables.elements();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof DistnVariable)
				return (DistnVariable)v;
		}
		return null;
	}
	
	public LabelVariable getLabelVariable() {
		Enumeration e = variables.elements();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof LabelVariable)
				return (LabelVariable)v;
		}
		return null;
	}
	
	private int countRows() {
		try {
			Enumeration e = variables.elements();
			CoreVariable v = (CoreVariable)e.nextElement();
			while (!(v instanceof Variable))
				v = (CoreVariable)e.nextElement();
			
			return ((Variable)v).noOfValues();
		} catch (NoSuchElementException e) {
			return 0;
		}
	}
	
	private boolean[] allFalseArray() {
		int noOfRows = countRows();
		return (noOfRows == 0) ? null : new boolean[noOfRows];
	}
	
	private void initialiseSelection() {
		if (selection == null)
			selection = new Flags(countRows());
	}
	
	public synchronized boolean clearSelection() {
		initialiseSelection();
		
		boolean selectionChanged = selection.clearFlags();
		if (!selectionChanged)
			return false;
		
		setChanged();
		notifyObservers(new DataChangeMessage(DataChangeMessage.CHANGE_SELECTION, null));
		return true;
	}
	
	public synchronized boolean setSelection(boolean newSelection[]) {
		initialiseSelection();
		
		boolean selectionChanged = selection.setFlags(newSelection);
		if (!selectionChanged)
			return false;
		
		setChanged();
		notifyObservers(new DataChangeMessage(DataChangeMessage.CHANGE_SELECTION, null));
		return true;
	}
	
	public synchronized boolean setSelection(int index) {
		initialiseSelection();
		
		boolean selectionChanged = selection.setFlag(index);
		if (!selectionChanged)
			return false;
		
		setChanged();
		notifyObservers(new DataChangeMessage(DataChangeMessage.CHANGE_SELECTION, null));
		return true;
	}
	
	public synchronized boolean setSelection(String key, double min, double max) {
		boolean selectionChanged = false;
		CoreVariable v = getVariable(key);
		if (v instanceof DistnVariable) {
				DistnVariable dv = (DistnVariable)v;
				if (dv.getMinSelection() != min || dv.getMaxSelection() != max) {
					selectionChanged = true;
					dv.setMinSelection(min);
					dv.setMaxSelection(max);
				}
				if (selectionChanged) {
					setChanged();
					notifyObservers(new DataChangeMessage(DataChangeMessage.CHANGE_SELECTION, null));
				}
			}
		else if (v instanceof NumVariable) {
			NumVariable nv = (NumVariable)v;
			boolean selected[] = allFalseArray();
			if (selected != null) {
				ValueEnumeration e = nv.values();
				int index = 0;
				while (e.hasMoreValues()) {
					double nextVal = e.nextDouble();
					selected[index] = (nextVal >= min) && (nextVal <= max);
					index++;
				};
				selectionChanged = setSelection(selected);
			}
		}
		return selectionChanged;
	}
	
	public synchronized boolean setSelection(Flags newSelection) {
		initialiseSelection();
		if (selection.equals(newSelection))
			return false;
		else {
			selection.setFlags(newSelection.counts);
			setChanged();
			notifyObservers(new DataChangeMessage(DataChangeMessage.CHANGE_SELECTION, null));
			return true;
		}
	}
	
	public synchronized void valueChanged(int theIndex) {
		Enumeration e = variables.elements();
		while (e.hasMoreElements()) {
			CoreVariable v = (CoreVariable)e.nextElement();
			if (v instanceof FunctionInterface)
				((FunctionInterface)v).noteValueChange(theIndex);
		}
		setChanged();
		notifyObservers(new DataChangeMessage(DataChangeMessage.CHANGE_VALUE, theIndex));
	}
	
	public synchronized void variableChanged(String key) {
		variableChanged(key, -1);
	}
	
	public synchronized void variableChanged(String key, int newSelectedIndex) {
		CoreVariable changedVar = getVariable(key);
		if (changedVar instanceof Variable) {
			if (newSelectedIndex < 0) {
				initialiseSelection();
				selection.checkSize(((Variable)changedVar).noOfValues());
			}
			else
				getSelection().setFlag(newSelectedIndex);
		}
		else if (changedVar instanceof BiSampleVariable)
			((BiSampleVariable)changedVar).transmitVariableChanged(this);
											//	for when new sample is selected by SummaryDataSet.takeSample()
		
		Enumeration e = variables.keys();
		while (e.hasMoreElements()) {
			String varKey = (String)e.nextElement();
			CoreVariable v = (CoreVariable)variables.get(varKey);
			if (v instanceof FunctionInterface) {
				boolean changed = ((FunctionInterface)v).noteVariableChange(key);
				if (changed)
					variableChanged(varKey, newSelectedIndex);
			}
		}
		
		setChanged();
		notifyObservers(new DataChangeMessage(DataChangeMessage.CHANGE_VARIABLE, key));
	}
	
	public synchronized void valuesAdded(int noOfValues) {
		getSelection().checkSize(noOfValues);
		setChanged();
		notifyObservers(new DataChangeMessage(DataChangeMessage.ADDED_VALUES, noOfValues));
	}
	
	public synchronized void transformedAxis(NumCatAxis theAxis) {
		setChanged();
		notifyObservers(new DataChangeMessage(DataChangeMessage.TRANSFORMED_AXIS, theAxis));
	}
	
	public synchronized String getKey(CoreVariable v) {
		Enumeration e = variables.keys();
		while (e.hasMoreElements()) {
			String varKey = (String)e.nextElement();
			CoreVariable keyedVar = (CoreVariable)variables.get(varKey);
			if (keyedVar == v)
				return varKey;
		}
		return null;
	}
	
	public String getDefaultNumVariableKey() {
		NumVariable y = getNumVariable();
		if (y == null)
			return null;
		else
			return getKey(y);
	}
	
	public String getDefaultCatVariableKey() {
		CatVariable y = getCatVariable();
		if (y == null)
			return null;
		else
			return getKey(y);
	}
}
