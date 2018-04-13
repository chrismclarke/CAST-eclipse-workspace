package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class StemAxisVariableEditor extends CoreVariableEditor {
	
	private class StemAxisValueEditor extends CoreValueEditor {
		private JTextField minEditor, maxEditor, stemPowerEditor, repeatsEditor;
		
		public StemAxisValueEditor(JPanel valuePanel, GridBagConstraints minConstraint,
																GridBagConstraints maxConstraint, GridBagConstraints stemPowerConstraint,
																GridBagConstraints repeatsConstraint) {
			minEditor = variation.createMonitoredTextField("", 5);
			maxEditor = variation.createMonitoredTextField("", 5);
			stemPowerEditor = variation.createMonitoredTextField("", 5);
			repeatsEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(minEditor, minConstraint);
			valuePanel.add(maxEditor, maxConstraint);
			valuePanel.add(stemPowerEditor, stemPowerConstraint);
			valuePanel.add(repeatsEditor, repeatsConstraint);
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString);
			minEditor.setText(st.nextToken());
			maxEditor.setText(st.nextToken());
			stemPowerEditor.setText(st.nextToken());
			repeatsEditor.setText(st.nextToken());
		}
		
		public String getValue() {
			return minEditor.getText() + " " + maxEditor.getText() + " " + stemPowerEditor.getText()
																							+ " " + repeatsEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(minEditor);
			valuePanel.remove(maxEditor);
			valuePanel.remove(stemPowerEditor);
			valuePanel.remove(repeatsEditor);
			minEditor = null;
			maxEditor = null;
			stemPowerEditor = null;
			repeatsEditor = null;
		}
		
		public boolean isValidValue() {
			try {
				double minValue = Double.parseDouble(minEditor.getText());
				double maxValue = Double.parseDouble(maxEditor.getText());
				@SuppressWarnings("unused")
				int stemPower = Integer.parseInt(stemPowerEditor.getText());
				@SuppressWarnings("unused")
				int repeats = Integer.parseInt(repeatsEditor.getText());
				return minValue < maxValue;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
	
//-----------------------------------------------------------------------------
	
	private GridBagConstraints minConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints maxConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints stemPowerConstraint = new GridBagConstraints(
																										2, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints repeatsConstraint = new GridBagConstraints(
																										3, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	
	public StemAxisVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "stemAxis";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		minConstraint.gridy = 0;
		JLabel minLabel = new JLabel("Min", JLabel.CENTER);
		valuePanel.add(minLabel, minConstraint);
		minConstraint.gridy ++;
		
		maxConstraint.gridy = 0;
		JLabel maxLabel = new JLabel("Max", JLabel.CENTER);
		valuePanel.add(maxLabel, maxConstraint);
		maxConstraint.gridy ++;
		
		stemPowerConstraint.gridy = 0;
		JLabel startLabel = new JLabel("Stem power", JLabel.CENTER);
		valuePanel.add(startLabel, stemPowerConstraint);
		stemPowerConstraint.gridy ++;
		
		repeatsConstraint.gridy = 0;
		JLabel stepLabel = new JLabel("Repeats per stem", JLabel.CENTER);
		valuePanel.add(stepLabel, repeatsConstraint);
		repeatsConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new StemAxisValueEditor(valuePanel, minConstraint, maxConstraint,
																																	stemPowerConstraint, repeatsConstraint);
		minConstraint.gridy ++;
		maxConstraint.gridy ++;
		stemPowerConstraint.gridy ++;
		repeatsConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		minConstraint.gridy --;
		maxConstraint.gridy --;
		stemPowerConstraint.gridy --;
		repeatsConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
	}
	
	public boolean hasRandomValue() {
		return false;
	}
	
	public void initialiseRandomValue(String initialValue) {		//	no random axes are allowed
	}
	
	public String getRandomValue() {
		return null;
	}
	
	protected boolean isValidRandomValue() {
		return false;
	}
	
	public char arrayDelimiter() {
		return '*';
	}
}
