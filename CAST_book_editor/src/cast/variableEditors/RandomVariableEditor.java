package cast.variableEditors;

import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class RandomVariableEditor extends CoreVariableEditor {
	static final private int GENERAL = 0;
	static final private int MIN_MAX_PROB = 1;
	
	private int randomType = GENERAL;
	
	private JTextField lowRandomValue, highRandomValue;
	
	public RandomVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "random";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		return null;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
	}
	
	public void setTypeDetails(String typeDetails) {
		if (typeDetails != null && typeDetails.equals("minMaxProb"))
			randomType = MIN_MAX_PROB;
	}
	
	public boolean hasRandomValue() {
		return true;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
		if (randomType == MIN_MAX_PROB) {
				lowRandomValue = variation.createMonitoredTextField("", 6);
				JLabel lowValueLabel = new JLabel("for probability between", JLabel.LEFT);
				lowValueLabel.setLabelFor(lowRandomValue);
			valuePanel.add(lowValueLabel);
			valuePanel.add(lowRandomValue);
			
				highRandomValue = variation.createMonitoredTextField("", 6);
				JLabel highValueLabel = new JLabel("and", JLabel.LEFT);
				highValueLabel.setLabelFor(highRandomValue);
			valuePanel.add(highValueLabel);
			valuePanel.add(highRandomValue);
		}
	}
	
	public void initialiseRandomValue(String initialValue) {
		switch (randomType) {
			case MIN_MAX_PROB:
				StringTokenizer st = new StringTokenizer(initialValue, ":");
				lowRandomValue.setText(st.nextToken());
				highRandomValue.setText(st.nextToken());
		}
	}
	
	public String getRandomValue() {
		switch (randomType) {
			case GENERAL:
				return "(:)";
			case MIN_MAX_PROB:
				return "(" + lowRandomValue.getText() + ":" + highRandomValue.getText() + ")";
		}
		return null;
	}
	
	protected boolean isValidRandomValue() {
		switch (randomType) {
			case GENERAL:
				return true;
			case MIN_MAX_PROB:
				try {
					double lowValue = Double.parseDouble(lowRandomValue.getText());
					double highValue = Double.parseDouble(highRandomValue.getText());
					return highValue >= lowValue && lowValue >= 0 && highValue <= 1;
				} catch (NumberFormatException e) {
					return false;
				}
		}
		return false;
	}
	
	public char arrayDelimiter() {
		return ',';									//	not used
	}
	
}
