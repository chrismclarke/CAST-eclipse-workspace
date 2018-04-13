package cast.variationEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import cast.utils.*;
import cast.exercise.*;
import cast.variableEditors.*;

abstract public class CoreVariableEditor extends JPanel {
	static final private Color kNameBackground = new Color(0xFFFFCC);
	static final private Color kIndexBackground = new Color(0xFFDDDD);
	static final private Color kQuestionMarkColor = new Color(0xCCCCCC);
	
	static CoreVariableEditor createEditor(String name, String type, DomVariation variation,
																														VariableType[] validParams, boolean hasIndex) {
		CoreVariableEditor theEditor = null;
		int bracketIndex = type.indexOf('(');
		String coreType = (bracketIndex > 0) ? type.substring(0, bracketIndex) : type;
		
		if (coreType.endsWith("_choice")) {
			boolean returnsInt = coreType.startsWith("int");
			theEditor = new ChoiceVariableEditor(variation, validParams, returnsInt, hasIndex);
		}
		else if (coreType.startsWith("string"))
			theEditor = new StringVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("int"))
			theEditor = new IntVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("double"))
			theEditor = new DoubleVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("boolean"))
			theEditor = new BooleanVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("axis"))
			theEditor = new AxisVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("stemAxis"))
			theEditor = new StemAxisVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("crossAxis"))
			theEditor = new CrossAxisVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("histo"))
			theEditor = new HistoVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("meanSd"))
			theEditor = new MeanSdVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("cut_offs"))
			theEditor = new CutoffVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("answer"))
			theEditor = new AnswerVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("predictionType"))
			theEditor = new PredictionVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("model"))
			theEditor = new ModelVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("index"))
			theEditor = new IntVariableEditor(variation, validParams, hasIndex);
		else if (coreType.startsWith("random"))			//	value must be ":"
			theEditor = new RandomVariableEditor(variation, validParams, hasIndex);
		else
			System.out.println("Unknown type: \"" + type + "\"\ncoreType: \"" + coreType + "\"");
		
		if (coreType.endsWith("_array"))
			theEditor.initialiseArray(name, type);
		else
			theEditor.initialise(name, type);
		
		return theEditor;
	}
	
	static JPanel createNamePanel(String name, String type, String comment) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT));
		thePanel.setOpaque(false);
		
		boolean isIndex = name.equals("index") && type.equals("index");
		
			JPanel titlePanel = new JPanel() {
														public Insets getInsets() { return new Insets(3, 6, 3, 6); }
													};
			titlePanel.setLayout(new VerticalLayout());
			titlePanel.setBorder(BorderFactory.createLineBorder(Color.black));
			titlePanel.setBackground(isIndex ? kIndexBackground : kNameBackground);
				JLabel title = new JLabel(name, JLabel.LEFT);
				title.setFont(new Font("SansSerif", Font.BOLD, 12));
			titlePanel.add(title);
			
		thePanel.add(titlePanel);
			
		if (!isIndex) {
			if (type.indexOf("(") > 0)
				type = type.substring(0, type.indexOf("("));
			JLabel typeLabel = new JLabel("  " + type, JLabel.LEFT);
			typeLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
			
			thePanel.add(typeLabel);
		}
		
		if (comment != null) {
			JPanel mainPanel = thePanel;
			thePanel = new JPanel();
			thePanel.setLayout(new BorderLayout(5, 0));
			thePanel.setOpaque(false);
			
			thePanel.add("Center", mainPanel);
			
				JLabel questionLabel = new JLabel("?", JLabel.LEFT);
				questionLabel.setFont(new Font("Serif", Font.BOLD, 36));
				questionLabel.setForeground(kQuestionMarkColor);
			thePanel.add("East", questionLabel);
			
			thePanel.setToolTipText(comment);
			thePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		return thePanel;
	}

	static {
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
	}
	
	static final private int UNINITIALISED_TYPE = -1;
	static final private int ONE_VALUE_TYPE = 0;
	static final private int VALUE_ARRAY_TYPE = 1;
	static final private int RANDOM_VALUE_TYPE = 2;
	
//--------------------------------------------------------
	
	private String name;
	protected boolean isArray;
	protected DomVariation variation;
	private VariableType[] validParams;
	private boolean hasIndex;
	protected boolean optionalVariable = false;
	
	private JComboBox valueTypeChoice;
	private int currentValueType = UNINITIALISED_TYPE;
	private JComboBox indexVariableChoice;
	private JLabel indexLabel;
	
	private JButton removeButton, addButton;
	
	private JPanel valuePanel;
	private Vector valueEditors = new Vector();
	
	public CoreVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		this.variation = variation;
		this.validParams = validParams;
		this.hasIndex = hasIndex;
	}
	
	public void initialiseArray(String name, String type) {
		this.name = name;
		isArray = true;
		try {
			String typeDetails = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
			setTypeDetails(typeDetails);
		} catch (IndexOutOfBoundsException e) {
		}
		
		setLayout(new BorderLayout(0, 0));
		setOpaque(false);
		
		add("Center", arrayPanel());
	}
	
	public void initialise(String name, String type) {
		this.name = name;
		isArray = false;
		try {
			String typeDetails = type.substring(type.indexOf("(") + 1, type.lastIndexOf(")"));
			setTypeDetails(typeDetails);
		} catch (IndexOutOfBoundsException e) {
		}
		boolean onlyRandom = type.startsWith("random");
		
		setLayout(new BorderLayout(20, 0));
		setOpaque(false);
		
			JPanel valueTypePanel = new JPanel();
			valueTypePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, 3));
			valueTypePanel.setOpaque(false);
				
				if (onlyRandom) {
					String[] allowedTypes = {"Random"};
					valueTypeChoice = variation.createMonitoredMenu(allowedTypes);
					valueTypeChoice.setEnabled(false);
				}
				else {
					String[] typesWithRandom = {"Constant", "Array", "Random"};
					String[] typesWithoutRandom = {"Constant", "Array"};
					String[] allowedTypes = hasRandomValue() ? typesWithRandom : typesWithoutRandom;
					valueTypeChoice = variation.createMonitoredMenu(allowedTypes);
					valueTypeChoice.addActionListener(new ActionListener() {
																											public void actionPerformed(ActionEvent e) {
																												JComboBox cb = (JComboBox)e.getSource();
																												int newValueType = cb.getSelectedIndex();
																												
																												changeValueType(newValueType);
																											}
																					});
				}
				
			valueTypePanel.add(valueTypeChoice);
			
				JPanel indexPanel = new JPanel();
				indexPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				indexPanel.setOpaque(false);
				
				if (type.startsWith("index")) {
					indexLabel = new JLabel("Random index", JLabel.RIGHT);
					indexPanel.add(indexLabel);
				}
				else {
					indexLabel = new JLabel("Indexed by:", JLabel.RIGHT);
					int nAllowedIndices = 1;				//	first entry is "Random"
					if (hasIndex)
							nAllowedIndices ++;
					for (int i=0 ; i<validParams.length ; i++)
						if (isIndexType(validParams[i].getName(), validParams[i].getType()))
							nAllowedIndices ++;
					
					String[] allowedIndices = new String[nAllowedIndices];
					
					allowedIndices[0] = "Random";
					int index = 1;
					if (hasIndex)
						allowedIndices[index ++] = "index";
					
					for (int i=0 ; i<validParams.length ; i++)
						if (isIndexType(validParams[i].getName(), validParams[i].getType()))
							allowedIndices[index ++] = validParams[i].getName();
					
					indexVariableChoice = variation.createMonitoredMenu(allowedIndices);
					
					indexLabel.setLabelFor(indexVariableChoice);
					indexLabel.setVisible(false);
					indexVariableChoice.setVisible(false);
					
					indexPanel.add(indexLabel);
					indexPanel.add(indexVariableChoice);
				}
				
			valueTypePanel.add(indexPanel);
			
		add("West", valueTypePanel);
		
		add("Center", arrayPanel());
	}
	
	private JPanel arrayPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setOpaque(false);
		
			JPanel innerPanel = new JPanel();
			innerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			innerPanel.setOpaque(false);
				valuePanel = new JPanel();
				valuePanel.setOpaque(false);
			innerPanel.add(valuePanel);
			
		thePanel.add("Center", innerPanel);
		
			JPanel arrayControlPanel = new JPanel();
			arrayControlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
			arrayControlPanel.setOpaque(false);
			
				removeButton = new JButton("-");
				removeButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						removeLastArrayPanel();
																						variation.setDomChanged();
																						revalidate();
																						repaint();
																					}
															});
			removeButton.setVisible(false);
			arrayControlPanel.add(removeButton);
		
				addButton = new JButton("+");
				addButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						addArrayPanel();
																						variation.setDomChanged();
																						revalidate();
																						repaint();
																					}
															});
			arrayControlPanel.add(addButton);
			addButton.setVisible(false);
			
		thePanel.add("South", arrayControlPanel);
		return thePanel;
	}
	
	private boolean isIndexType(String paramName, String paramType) {
		if (paramName.equals(name))
			return false;
		return paramType.equals("int") || paramType.startsWith("int(") || paramType.startsWith("int_choice");
	}
	
	public void setDefaultValueType() {
		changeValueType(ONE_VALUE_TYPE);
	}
	
	private boolean isFixedValueType(int valueType) {
		return valueType == ONE_VALUE_TYPE || valueType == VALUE_ARRAY_TYPE;
	}
	
	private void changeValueType(int newValueType) {
		if (!isFixedValueType(currentValueType) || !isFixedValueType(newValueType)) {
			valuePanel.removeAll();
			valueEditors.clear();
			
			if (isFixedValueType(newValueType)) {
				initialiseValuePanel(valuePanel);
				CoreValueEditor firstValueEditor = addOneValueEditor(valuePanel);
				valueEditors.add(firstValueEditor);
			}
			else if (newValueType == RANDOM_VALUE_TYPE)
				createRandomValuePanel(valuePanel);
		}
		if (!isArray) {
			if (newValueType == VALUE_ARRAY_TYPE) {
				indexLabel.setVisible(true);
				if (indexVariableChoice != null)
					indexVariableChoice.setVisible(true);
			}
			else {
				indexLabel.setVisible(false);
				if (indexVariableChoice != null)
					indexVariableChoice.setVisible(false);
			}
		}
		
		if (isArray || newValueType == VALUE_ARRAY_TYPE) {
//			CoreValueEditor newValueEditor = addOneValueEditor(valuePanel);
//			valueEditors.add(newValueEditor);
			
			removeButton.setVisible(true);
			removeButton.setEnabled(false);
			addButton.setVisible(true);
		}
		else {
			removeButton.setVisible(false);
			addButton.setVisible(false);
			
			if (newValueType == ONE_VALUE_TYPE) {
				while (valueEditors.size() > 1) 
					removeLastArrayPanel();
			}
		}
		
		revalidate();
		repaint();
		
		currentValueType = newValueType;
	}
	
	private void removeLastArrayPanel() {
		CoreValueEditor lastEditor = (CoreValueEditor)valueEditors.lastElement();
		removeLastValueEditor(valuePanel, lastEditor);
		valueEditors.remove(lastEditor);
		
		if (valueEditors.size() <= 1)
			removeButton.setEnabled(false);
	}
	
	private void addArrayPanel() {
		CoreValueEditor newValueEditor = addOneValueEditor(valuePanel);
		valueEditors.add(newValueEditor);
		
		if (valueEditors.size() > 1)
			removeButton.setEnabled(true);
	}
	
	public void setTypeDetails(String typeDetails) {			//	can set range for valid values or default number of chars
		if (typeDetails.equals("optional"))
			optionalVariable = true;
	}
	
	protected boolean isRandomChoice(String initialValue) {
		return false;									//	overridden by ChoiceVariableEditor to change random value from String array into completely random
	}
	
	public void setInitialValue(String initialValue) {
		if (isRandomChoice(initialValue))
			initialValue = "(:)";
		
		if (initialValue.charAt(0) == '[') {
//			changeValueType(VALUE_ARRAY_TYPE);
			if (valueTypeChoice == null)
				System.out.println("Error in " + name + ": initialValue = " + initialValue);
			valueTypeChoice.setSelectedIndex(VALUE_ARRAY_TYPE);		//	fires event that calls changeValueType()
			
			int endIndex = initialValue.indexOf(']');
			String indexName = (endIndex == 1) ? "Random" : initialValue.substring(1, endIndex);
			indexVariableChoice.setSelectedItem(indexName);
				
			String valueList = initialValue.substring(endIndex + 2, initialValue.length() - 1);			//	also deletes () round values
			StringTokenizer st = new StringTokenizer(valueList, String.valueOf(arrayDelimiter()));
			int nValues = st.countTokens();
			int nExistingValues = valueEditors.size();			//		should always be 2
			
			for (int i=nExistingValues ; i<nValues ; i++) {
				CoreValueEditor newEditor = addOneValueEditor(valuePanel);
				valueEditors.add(newEditor);
			}
			
			for (int i=0 ; i<nValues ; i++) {
				CoreValueEditor valueEditor = (CoreValueEditor)valueEditors.elementAt(i);
				valueEditor.setValue(st.nextToken());
			}
			removeButton.setEnabled(nValues > 1);
		}
		else {
			initialValue = initialValue.substring(1, initialValue.length() - 1);			//	to delete () round values
			if (isArray) {
				initialiseValuePanel(valuePanel);
				changeValueType(VALUE_ARRAY_TYPE);
				
				StringTokenizer st = new StringTokenizer(initialValue, " ");
				int nValues = st.countTokens();
				int nExistingValues = valueEditors.size();			//		should always be 2
				
				for (int i=nExistingValues ; i<nValues ; i++) {
					CoreValueEditor newEditor = addOneValueEditor(valuePanel);
					valueEditors.add(newEditor);
				}
				
				for (int i=0 ; i<nValues ; i++) {
					CoreValueEditor valueEditor = (CoreValueEditor)valueEditors.elementAt(i);
					valueEditor.setValue(st.nextToken().replaceAll("_", " "));
				}
				removeButton.setEnabled(nValues > 1);
			}
			else if (hasRandomValue() && initialValue.indexOf(":") >= 0) {
				changeValueType(RANDOM_VALUE_TYPE);
//				valueTypeChoice.setSelectedIndex(RANDOM_VALUE_TYPE);
				valueTypeChoice.setSelectedItem("Random");
				initialiseRandomValue(initialValue);
			}
			else {
				changeValueType(ONE_VALUE_TYPE);
				valueTypeChoice.setSelectedIndex(ONE_VALUE_TYPE);
				CoreValueEditor constantEditor = (CoreValueEditor)valueEditors.lastElement();
				constantEditor.setValue(initialValue);
			}
		}
	}
	
	public String getTextValue() throws ParamValueException {
		if (isArray) {
			String result = "(";
			for (int i=0 ; i<valueEditors.size() ; i++) {
				if (i > 0)
					result += " ";
				CoreValueEditor valueEditor = (CoreValueEditor)valueEditors.elementAt(i);
				if (!valueEditor.isValidValue())
					throw new ParamValueException(name);
				result += valueEditor.getValue().replaceAll(" ", "_");
			}
			return result + ")";
		}
		else
			switch (currentValueType) {
				case VALUE_ARRAY_TYPE:
					String result;
					if (indexVariableChoice == null || indexVariableChoice.getSelectedIndex() == 0)
						result = "[](";
					else
						result = "[" + (String)indexVariableChoice.getSelectedItem() + "](";
					for (int i=0 ; i<valueEditors.size() ; i++) {
						if (i > 0)
							result += arrayDelimiter();
						CoreValueEditor valueEditor = (CoreValueEditor)valueEditors.elementAt(i);
						if (!valueEditor.isValidValue())
							throw new ParamValueException(name);
						result += valueEditor.getValue();
					}
					return result + ")";
					
				case RANDOM_VALUE_TYPE:
					if (!isValidRandomValue())
						throw new ParamValueException(name);
					return getRandomValue();
					
				default:
				case ONE_VALUE_TYPE:
					CoreValueEditor constantEditor = (CoreValueEditor)valueEditors.lastElement();
					if (!constantEditor.isValidValue())
						throw new ParamValueException(name);
					return "(" + constantEditor.getValue() + ")";
			}
	}
	
	public String getType() {
		return getBaseType() + (isArray ? "_array" : "");
	}
	
	abstract protected String getBaseType();
	
	abstract public void initialiseValuePanel(JPanel valuePanel);
	abstract public CoreValueEditor addOneValueEditor(JPanel valuePanel);
	abstract public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor);
	
	abstract public void createRandomValuePanel(JPanel valuePanel);
	abstract public boolean hasRandomValue();
	abstract public void initialiseRandomValue(String initialValue);
	abstract public String getRandomValue();
	abstract protected boolean isValidRandomValue();
	
	abstract public char arrayDelimiter();
	
}