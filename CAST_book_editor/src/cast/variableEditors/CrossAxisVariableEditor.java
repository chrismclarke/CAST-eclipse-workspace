package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class CrossAxisVariableEditor extends CoreVariableEditor {
	
	private class CrossAxisValueEditor extends CoreValueEditor {
		private JTextField minEditor, maxEditor, classWidthEditor, firstLabelColEditor,
																													ticksPerLabelEditor, labelDecimalsEditor;
		
		public CrossAxisValueEditor(JPanel valuePanel, GridBagConstraints minConstraint,
																GridBagConstraints maxConstraint, GridBagConstraints classWidthConstraint,
																GridBagConstraints firstLabelColConstraint, GridBagConstraints ticksPerLabelConstraint,
																GridBagConstraints labelDecimalsConstraint) {
			minEditor = variation.createMonitoredTextField("", 5);
			maxEditor = variation.createMonitoredTextField("", 5);
			classWidthEditor = variation.createMonitoredTextField("", 5);
			firstLabelColEditor = variation.createMonitoredTextField("", 5);
			ticksPerLabelEditor = variation.createMonitoredTextField("", 5);
			labelDecimalsEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(minEditor, minConstraint);
			valuePanel.add(maxEditor, maxConstraint);
			valuePanel.add(classWidthEditor, classWidthConstraint);
			valuePanel.add(firstLabelColEditor, firstLabelColConstraint);
			valuePanel.add(ticksPerLabelEditor, ticksPerLabelConstraint);
			valuePanel.add(labelDecimalsEditor, labelDecimalsConstraint);
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString);
			minEditor.setText(st.nextToken());
			maxEditor.setText(st.nextToken());
			classWidthEditor.setText(st.nextToken());
			firstLabelColEditor.setText(st.nextToken());
			ticksPerLabelEditor.setText(st.nextToken());
			labelDecimalsEditor.setText(st.nextToken());
		}
		
		public String getValue() {
			return minEditor.getText() + " " + maxEditor.getText() + " " + classWidthEditor.getText()
																	+ " " + firstLabelColEditor.getText() + " " + ticksPerLabelEditor.getText()
																	+ " " + labelDecimalsEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(minEditor);
			valuePanel.remove(maxEditor);
			valuePanel.remove(classWidthEditor);
			valuePanel.remove(firstLabelColEditor);
			valuePanel.remove(ticksPerLabelEditor);
			valuePanel.remove(labelDecimalsEditor);
			minEditor = null;
			maxEditor = null;
			classWidthEditor = null;
			firstLabelColEditor = null;
			ticksPerLabelEditor = null;
			labelDecimalsEditor = null;
		}
		
		public boolean isValidValue() {
			try {
				double minValue = Double.parseDouble(minEditor.getText());
				double maxValue = Double.parseDouble(maxEditor.getText());
				double classWidth = Double.parseDouble(classWidthEditor.getText());
				@SuppressWarnings("unused")
				int firstLabelCol = Integer.parseInt(firstLabelColEditor.getText());
				@SuppressWarnings("unused")
				int ticksPerLabel = Integer.parseInt(ticksPerLabelEditor.getText());
				@SuppressWarnings("unused")
				int labelDecimals = Integer.parseInt(labelDecimalsEditor.getText());
				return minValue < maxValue && classWidth < (maxValue - minValue);
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
	
	private GridBagConstraints classWidthConstraint = new GridBagConstraints(
																										2, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints firstLabelColConstraint = new GridBagConstraints(
																										3, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints ticksPerLabelConstraint = new GridBagConstraints(
																										4, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints labelDecimalsConstraint = new GridBagConstraints(
																										5, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	
	public CrossAxisVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
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
		
		classWidthConstraint.gridy = 0;
		JLabel classWidth = new JLabel("Class width", JLabel.CENTER);
		valuePanel.add(classWidth, classWidthConstraint);
		classWidthConstraint.gridy ++;
		
		firstLabelColConstraint.gridy = 0;
		JLabel firstLabelColLabel = new JLabel("Index of 1st labeled boundary", JLabel.CENTER);
		valuePanel.add(firstLabelColLabel, firstLabelColConstraint);
		firstLabelColConstraint.gridy ++;
		
		ticksPerLabelConstraint.gridy = 0;
		JLabel ticksPerLabelLabel = new JLabel("Classes per label", JLabel.CENTER);
		valuePanel.add(ticksPerLabelLabel, ticksPerLabelConstraint);
		ticksPerLabelConstraint.gridy ++;
		
		labelDecimalsConstraint.gridy = 0;
		JLabel labelDecimalsLabel = new JLabel("Decimals for labels", JLabel.CENTER);
		valuePanel.add(labelDecimalsLabel, labelDecimalsConstraint);
		labelDecimalsConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new CrossAxisValueEditor(valuePanel, minConstraint, maxConstraint,
																				classWidthConstraint, firstLabelColConstraint, ticksPerLabelConstraint,
																				labelDecimalsConstraint);
		minConstraint.gridy ++;
		maxConstraint.gridy ++;
		classWidthConstraint.gridy ++;
		firstLabelColConstraint.gridy ++;
		ticksPerLabelConstraint.gridy ++;
		labelDecimalsConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		minConstraint.gridy --;
		maxConstraint.gridy --;
		classWidthConstraint.gridy --;
		firstLabelColConstraint.gridy --;
		ticksPerLabelConstraint.gridy --;
		labelDecimalsConstraint.gridy --;
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
