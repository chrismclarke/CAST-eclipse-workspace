package cast.variationEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;
import cast.exercise.*;


public class VariationEditor extends JFrame {
	static final private int kMinWindowWidth = 800;
	static final private int kMinWindowHeight = 800;
	
	static final private Color kHeadingBackground = new Color(0xEEEEFF);
//	static final private Color kVariableBackground = new Color(0xFFFFEE);
//	static final private Color kAnonBackground = new Color(0xFFEEEE);
//	static final private Color kLineColor = new Color(0x999999);
	
/*
	static private File createScriptFileReference() {
		return new File(".", "runAppletScript.sh");
	}
*/
	
	static private File createHtmlFileReference() {
		return new File(".", "applet.html");
	}
	
	
		
	private GridBagConstraints labelConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTHEAST,		//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 5, 0, 10),				//	insets
																										0, 0);													//	ipadx & ipady
	private GridBagConstraints textEditConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										1, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTHWEST,		//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 10),				//	insets
																										0, 0);													//	ipadx & ipady
	private GridBagConstraints heightOverrideConstraint = new GridBagConstraints(
																										2, 0,														//	gridx & gridy
																										1, 2,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.CENTER,		//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 10),				//	insets
																										0, 0);													//	ipadx & ipady
		
	private DomVariation variation;
	private VariableType[] validParams;
	
	private String shortName, longName;
	private QuestionAndParams qnAndParams;
	private Vector endings;				//	Vector of QuestionAndParam
		
	private JPanel editorContent;
	private ParameterListPanel namedVarPanel;
	private AnonListPanel anonPanel;
	private EndingsListPanel allEndingsPanel = null;
	
	private JTextField nameField, shortNameField, heightField;
	private QuestionPanel questionField;
	
	private int nextAnonIndex = 1;
	
	public VariationEditor(final DomVariation variation, final OneCoreVariation caller) {
		super("Variation Editor");
		
		this.variation = variation;
		validParams = variation.getExercise().getVariableTypes(DomExercise.MAIN_PARAMS_ONLY);			//	excludes any in question endings
		
		DomConverter.inputQuestionFromDom(variation, this);
		
		setLayout(new BorderLayout(0, 0));
		setMinimumSize(new Dimension(kMinWindowWidth, kMinWindowHeight));
		
		add("North", questionPanel());
		
			JPanel mainParamPanel = new JPanel();
			mainParamPanel.setLayout(new BorderLayout(0, 0));
			
				JPanel buttonPanel = buttonPanel();
				buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
			
			mainParamPanel.add("North", buttonPanel);
		
				editorContent = new JPanel();
				editorContent.setLayout(new BorderLayout(0, 0));
				
					namedVarPanel = new ParameterListPanel(qnAndParams.getParams(), validParams, variation);
				editorContent.add("Center", namedVarPanel);
					boolean hasIndexVariable = qnAndParams.hasIndexVariable();
					anonPanel = new AnonListPanel(qnAndParams.getAnonVariables(), validParams, variation, this, hasIndexVariable);
				editorContent.add("South", anonPanel);
			
				JScrollPane scrollPane = new JScrollPane(editorContent);
				Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
				scrollPane.setBorder(emptyBorder);
				
			mainParamPanel.add("Center", scrollPane);
			
		if (variation.getNoOfEndings() == 0)
			add("Center", mainParamPanel);
		else {
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridLayout(1, 2));
			
			bottomPanel.add(mainParamPanel);
				allEndingsPanel = new EndingsListPanel(variation, endings, this);
			bottomPanel.add(allEndingsPanel);
			
			add("Center", bottomPanel);
		}
		
		addWindowListener( new WindowAdapter() {
									public void windowClosed(WindowEvent e) {
										if (!variation.neverSaved())
											caller.finishedEdit(variation);
									}
									public void windowClosing(WindowEvent e) {
										if (variation.domHasChanged() || variation.neverSaved()) {
											Object[] options = {"Save", "Don't save", "Cancel"};
											int result = JOptionPane.showOptionDialog(VariationEditor.this,
																	"This variation has been changed. Do you want to save the changes?", "Quit?",
																	JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);

											switch (result) {
												case JOptionPane.YES_OPTION:
														String vName = shortNameField.getText();
														if (variation.getExercise().variationExists(vName, variation, caller.getCustomVariations())) {
															JOptionPane.showMessageDialog(VariationEditor.this,
																	"Error! A core variation with the same name already exists.", "Error!",
																	JOptionPane.ERROR_MESSAGE);
															break;
														}
														try {
															namedVarPanel.updateParams(qnAndParams.getParams());
															anonPanel.updateAnonVariables(qnAndParams.getAnonVariables());
															qnAndParams.setQuestion(questionField.getText());
															DomConverter converter = new DomConverter(qnAndParams, validParams);
															
															String[] endingParams = (allEndingsPanel == null) ? null : allEndingsPanel.getEndingStrings();
															
															int height;
															try {
																height = Integer.parseInt(heightField.getText());
															} catch (NumberFormatException ne) {
																height = 0;
															}
															converter.updateDom(nameField.getText(), shortNameField.getText(), height,
																																									variation, endingParams);
															if (variation.isCoreVariation())
																variation.getExercise().getTopic().saveDom();
															else {
																OneCustomVariation variationPanel = (OneCustomVariation)caller;
																variationPanel.saveDom();
															}
															
															dispose();
														} catch (ParamValueException ex) {
															alertBadParamValue(ex);
														}
													break;
												case JOptionPane.NO_OPTION:
													if (variation.isCoreVariation() || !variation.neverSaved())
														variation.clearDomChanged();
													else {
														OneCustomVariation variationPanel = (OneCustomVariation)caller;
														variationPanel.deleteVariation();
													}
													dispose();
													break;
												case JOptionPane.CANCEL_OPTION:
												default:
													break;
											}
										}
										else
											dispose();
									}
								} );
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		variation.clearDomChanged();		//	since initialisation of text fields fires event that marks the variation as changed
	}
	
	public void setQuestionAndParams(QuestionAndParams qnAndParams) {
		this.qnAndParams = qnAndParams;
	}
	
	public void setEndings(Vector endings) {
		this.endings = endings;
	}
	
	public Vector getEndings() {
		return endings;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public void setLongName(String longName) {
		this.longName = longName;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getLongName() {
		return longName;
	}
	
	
//-------------------------------------------------------
	
	private JPanel buttonPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setBackground(kHeadingBackground);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			JButton tryAppletButton = new JButton("Try exercise");
			tryAppletButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					tryApplet();
																				}
														});
		thePanel.add(tryAppletButton);
		
			JCheckBox indexCheck = new JCheckBox("Use index variable");
			boolean hasIndexVariable = qnAndParams.hasIndexVariable();
			indexCheck.setSelected(hasIndexVariable);
			indexCheck.addItemListener(new ItemListener() {
																				public void itemStateChanged(ItemEvent e) {
																					Hashtable params = qnAndParams.getParams();
																					try {
																						if (e.getStateChange() == ItemEvent.SELECTED) {
																							namedVarPanel.updateParams(params);
																							params.put("index", "(0:1)");
																							resetNamedVarPanel();
																						}
																						else {
																							namedVarPanel.updateParams(params);
																							params.remove("index");
																							resetNamedVarPanel();
																						}
																					} catch (ParamValueException ex) {
																						alertBadParamValue(ex);
																					}
																				}
														});
		thePanel.add(indexCheck);
		
			JButton newAnonButton = new JButton("Add anonymous variable...");
			newAnonButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					try {
																						String typeAndValue = AnonTypePicker.pickType(VariationEditor.this);
																						if (typeAndValue != null) {
																							Hashtable anonVariables = qnAndParams.getAnonVariables();
																							anonPanel.updateAnonVariables(anonVariables);
																							String newName = getNextAnonName();
																							anonVariables.put(newName, typeAndValue);
																							resetAnonPanel();
																						}
																					} catch (ParamValueException ex) {
																						alertBadParamValue(ex);
																					}
																				}
														});
		thePanel.add(newAnonButton);
		
		return thePanel;
	}
	
	
	private JPanel questionPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new GridBagLayout());
		thePanel.setBackground(kHeadingBackground);
		thePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		
			JLabel nameLabel = new JLabel("Variation name", JLabel.LEFT);
			nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
			nameLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		thePanel.add(nameLabel, labelConstraint);
		
			nameField = variation.createMonitoredTextField(variation.getLongName(), 40);
			nameField.setFont(new Font("SansSerif", Font.PLAIN, 12));
		thePanel.add(nameField, textEditConstraint);
		
			labelConstraint.gridy ++;
			textEditConstraint.gridy ++;
		
			JLabel shortNameLabel = new JLabel("Short name", JLabel.LEFT);
			shortNameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
			shortNameLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		thePanel.add(shortNameLabel, labelConstraint);
		
			shortNameField = variation.createMonitoredTextField(variation.getShortName(), 10);
			shortNameField.setFont(new Font("SansSerif", Font.PLAIN, 12));
		thePanel.add(shortNameField, textEditConstraint);
		
		thePanel.add(heightOverridePanel(), heightOverrideConstraint);
		
			labelConstraint.gridy ++;
			textEditConstraint.gridy ++;
			textEditConstraint.gridwidth = 2;
			textEditConstraint.fill = GridBagConstraints.HORIZONTAL;
		
			JLabel questionLabel = new JLabel("Question", JLabel.LEFT);
			questionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		thePanel.add(questionLabel, labelConstraint);
		
			questionField = variation.createMonitoredQuestionPanel(qnAndParams.getQuestion(), validParams);
			JScrollPane scrollArea2 = new JScrollPane(questionField);
			scrollArea2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollArea2.setPreferredSize(new Dimension(40, 85));			//		sets height of scrollArea
			
		thePanel.add(scrollArea2, textEditConstraint);
		
		return thePanel;
	}
	
	private JPanel heightOverridePanel() {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			final JCheckBox overrideCheck = new JCheckBox("Custom height:");
			overrideCheck.addItemListener(new ItemListener() {
																				public void itemStateChanged(ItemEvent e) {
																					if (overrideCheck.isSelected())
																						heightField.setEnabled(true);
																					else {
																						heightField.setText(variation.getExercise().getAppletHeight());
																						heightField.setEnabled(false);
																					}
																				}
														});
			
		thePanel.add(overrideCheck);
		
			int customHeight = variation.getHeight();
			String displayHeight = (customHeight > 0) ? String.valueOf(customHeight) : variation.getExercise().getAppletHeight();
			
			heightField = variation.createMonitoredTextField(displayHeight, 3);
			heightField.setEnabled(customHeight > 0);
			overrideCheck.setSelected(customHeight > 0);
		
		thePanel.add(heightField);
		
		return thePanel;
	}
	
	public void resetAnonPanel() {
		editorContent.remove(anonPanel);
		
		boolean hasIndexVariable = qnAndParams.hasIndexVariable();
		anonPanel = new AnonListPanel(qnAndParams.getAnonVariables(), validParams, variation, this, hasIndexVariable);
		editorContent.add("South", anonPanel);
		editorContent.revalidate();
		editorContent.repaint();
		
		int height = (int)editorContent.getPreferredSize().getHeight();
		Rectangle rect = new Rectangle(0,height,10,10);
		editorContent.scrollRectToVisible(rect);
	}
	
	private void resetNamedVarPanel() {
		editorContent.remove(namedVarPanel);
		namedVarPanel = new ParameterListPanel(qnAndParams.getParams(), validParams, variation);
		editorContent.add("Center", namedVarPanel);
		editorContent.revalidate();
		editorContent.repaint();
	}
	
	private void tryApplet() {
		File castFolder = FileFinder.getCastFolder(this);
		if (castFolder == null)
			return;
		
		File coreFolder = new File(castFolder, "core");
		File javaFolder = new File(coreFolder, "java");
		String codebasePath = javaFolder.getAbsolutePath();
		
//		File scriptFile = createScriptFileReference();
		File appletFile = createHtmlFileReference();
		
		DomExercise exercise = variation.getExercise();
		
		try {
//			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(appletFile),"UTF8"));
//			PrintWriter appletWriter = new PrintWriter(bw);
			PrintWriter appletWriter = FileFinder.createUTF8Writer(appletFile);
			
			appletWriter.println("<html><body>");
			
				int appletHeight;
				try {
					appletHeight = Integer.parseInt(heightField.getText());
				} catch (NumberFormatException e) {
					appletHeight = Integer.parseInt(variation.getExercise().getAppletHeight());
				}
			appletWriter.println("<applet code='" + exercise.getAppletName() + ".class' width='" + exercise.getAppletWidth()
												+ "' height='" + appletHeight + "' codebase='" + codebasePath
												+ "' archive='coreCAST.jar'>");
			
			Hashtable coreParams = exercise.getCoreParams();
			Enumeration coreEnum = coreParams.keys();
			while (coreEnum.hasMoreElements()) {
				String paramName = (String)coreEnum.nextElement();
				String paramValue = (String)coreParams.get(paramName);
				appletWriter.println("<param name=\"" + paramName + "\" value=\"" + paramValue + "\">");
			}
			
			appletWriter.println("<param name=\"nQuestions\" value=\"1\">");
			
			namedVarPanel.updateParams(qnAndParams.getParams());
			anonPanel.updateAnonVariables(qnAndParams.getAnonVariables());
			qnAndParams.setQuestion(questionField.getText());
			DomConverter converter = new DomConverter(qnAndParams, validParams);
			
			String rawQuestionText = converter.getQuestionText();
			rawQuestionText = rawQuestionText.replaceAll("\\\\\\\\n", "\\\\n");		//	to replace \\n by \n in question
			String paramString = converter.getQuestionParams();
			appletWriter.println("<param name=\"question0\" value=\"" + rawQuestionText + "\">");
			appletWriter.println("<param name=\"qnParam0\" value=\"" + paramString + "\">");
			
			if (allEndingsPanel != null) {
				String endingTag = allEndingsPanel.getAppletTag();
				appletWriter.println(endingTag);
			}
			
			appletWriter.println("</applet>");
			appletWriter.println("</html></body>");

			appletWriter.flush();
			appletWriter.close();
			
			String command = "appletviewer " + appletFile.getName() + " -encoding UTF8";
			Runtime.getRuntime().exec(command);
			
		} catch (ParamValueException e) {
			alertBadParamValue(e);
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
	
	private void alertBadParamValue(ParamValueException e) {
		JOptionPane.showMessageDialog(this, "Error! Invalid value for parameter \"" + e.getName() + "\"",
				                       "Error!", JOptionPane.ERROR_MESSAGE);
	}
	
	public String getNextAnonName() {
		return "anon_" + (nextAnonIndex ++);
	}
}
