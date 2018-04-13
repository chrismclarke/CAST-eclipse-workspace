package cast.variationEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.exercise.*;

public class AnonListPanel extends JPanel {
	static final private Color kAnonBackground = new Color(0xFFEEEE);
	static final private Color kLineColor = new Color(0x999999);
	
	private GridBagConstraints nameConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTHWEST,		//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 5, 0, 10),				//	insets
																										0, 0);													//	ipadx & ipady
	private GridBagConstraints valueConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										1, 0,														//	weightx & weighty,
																										GridBagConstraints.CENTER,			//	anchor
																										GridBagConstraints.HORIZONTAL,	//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										0, 0);													//	ipadx & ipady												//	ipadx & ipady
	private GridBagConstraints lineConstraint = new GridBagConstraints(
																										0, 1,														//	gridx & gridy
																										2, 1,														//	gridwidth & gridheight
																										1, 0,														//	weightx & weighty,
																										GridBagConstraints.CENTER,			//	anchor
																										GridBagConstraints.HORIZONTAL,	//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										0, 0);													//	ipadx & ipady
	private GridBagConstraints deleteConstraint = new GridBagConstraints(
																										2, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.CENTER,			//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										0, 0);													//	ipadx & ipady
																										
	private Hashtable anonEditors = new Hashtable();
	
	public AnonListPanel(Hashtable anonVariables, VariableType[] validParams, DomVariation variation,
																						VariationEditor editorFrame, boolean hasIndexVariable) {
		setLayout(new GridBagLayout());
		setBackground(kAnonBackground);
		
			Border topLineBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, kLineColor);
			Border insetBorder = BorderFactory.createMatteBorder(4, 0, 0, 0, kAnonBackground);
		setBorder(BorderFactory.createCompoundBorder(topLineBorder, insetBorder));
			
			nameConstraint.gridy = 0;
			valueConstraint.gridy = 0;
			deleteConstraint.gridy = 0;
			lineConstraint.gridy = 1;
			lineConstraint.gridwidth = 3;
			
		int nVariables = anonVariables.size();
		Enumeration e = anonVariables.keys();
		for (int i=0 ; i<nVariables ; i++) {
			String name = (String)e.nextElement();
			String typeAndValue = (String)anonVariables.get(name);		//	includes type and () round value
			
			boolean notLastVariable = i < (nVariables - 1);
			addVariableToPanel(name, typeAndValue, validParams, variation, anonVariables, editorFrame, hasIndexVariable, notLastVariable);
		}
	}
	
	private void addVariableToPanel(final String name, String typeAndValue, VariableType[] validParams, final DomVariation variation,
																		final Hashtable anonVariables, final VariationEditor editorFrame,
																		boolean hasIndexVariable, boolean notLastVariable) {
			StringTokenizer st = new StringTokenizer(typeAndValue, "[]()");
			String type = st.nextToken();
			String value = typeAndValue.substring(type.length());		//	remove type
			
			JPanel namePanel = CoreVariableEditor.createNamePanel(name, type, null);
			
			CoreVariableEditor editor = CoreVariableEditor.createEditor(name, type, variation, validParams, hasIndexVariable);
			editor.setInitialValue(value);
		
		add(namePanel, nameConstraint);
		add(editor, valueConstraint);
		
			JButton deleteButton = new JButton("Remove");
			deleteButton.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				anonEditors.remove(name);
																				anonVariables.remove(name);
																				editorFrame.resetAnonPanel();
																				variation.setDomChanged();
																			}
													});
		add(deleteButton, deleteConstraint);
		
		if (notLastVariable)
			add(new LinePanel(), lineConstraint);
		
		nameConstraint.gridy += 2;
		valueConstraint.gridy += 2;
		deleteConstraint.gridy += 2;
		lineConstraint.gridy += 2;
		
		anonEditors.put(name, editor);
	}
	
	public void updateAnonVariables(Hashtable anonVariables) throws ParamValueException {
						//		entries in anonEditors and anonVariables should match
		Enumeration ve = anonVariables.keys();
		while (ve.hasMoreElements()) {
			String varName = (String)ve.nextElement();
			CoreVariableEditor varEditor = (CoreVariableEditor)anonEditors.get(varName);
			
			String varValue = varEditor.getTextValue();
			String type = varEditor.getType();
			anonVariables.put(varName, type + varValue);
		}
	}
	
	
//-------------------------------------------------------
	
	private class LinePanel extends JPanel {
		final private Dimension kPrefSize = new Dimension(0, 5);
		
		public LinePanel() {
			setOpaque(false);
		}
		
		public Dimension getPreferredSize() {
			return kPrefSize;
		}
		
		public void paintComponent(Graphics g) {
			g.setColor(kLineColor);
			g.drawLine(0, getSize().height / 2, getSize().width, getSize().height / 2);
		}
	}
}