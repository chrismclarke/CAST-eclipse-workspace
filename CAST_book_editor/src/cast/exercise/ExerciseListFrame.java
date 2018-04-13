package cast.exercise;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.utils.*;
import cast.core.*;


public class ExerciseListFrame extends JFrame {
	
	private JMenuBar mainMenuBar = new JMenuBar();
	protected JMenu buildMenu;

	protected Action buildExercisesAction, buildTestsAction, buildTestJarAction;
	
	private DomCustomVariations customVariations = null;
	private File defaultVariationsFile;
	
	private DomTopic[] topics;
	private OneTopic[] topicPanels;
	private int selectedTopic = -1;
	
	private JPanel variationList;
	private JButton newButton;
	
	public ExerciseListFrame(String[] topicName, File exerciseXmlDir, final JButton callingButton,
																																		final AdvancedWindow callingFrame) {
		super("Exercises");
		
		topics = new DomTopic[topicName.length];
		for (int i=0 ; i<topicName.length ; i++)
			topics[i] = new DomTopic(topicName[i], exerciseXmlDir);
	
		createActions(exerciseXmlDir, topics);
		addMenus();
		
		defaultVariationsFile = new File(exerciseXmlDir, "custom_variations.xml");
		setLayout(new BorderLayout(0, 5));
		
		add("North", customVariationsPanel());
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout(0, 5));
		
			mainPanel.add("North", headingPanel());
			mainPanel.add("Center", topicExerciseVariationPanel());
			
		add("Center", mainPanel);
		
		addWindowListener( new WindowAdapter() {
									public void windowClosing(WindowEvent e) {
										boolean changes = false;
										for (int i=0 ; i<topics.length ; i++)
											changes = changes || topics[i].domHasChanged();
										
										if (changes) {
											Object[] options = {"Quit", "Cancel"};
											int result = JOptionPane.showOptionDialog(ExerciseListFrame.this,
																	"Are you sure that you want to quit without saving the exercises variation that you have edited?", "Quit?",
																	JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

											switch (result) {
												case JOptionPane.YES_OPTION:
													callingFrame.reenable(callingButton);
													dispose();
													break;
												case JOptionPane.NO_OPTION:
												default:
													break;
											}
										}
										else {
											callingFrame.reenable(callingButton);
											dispose();
										}
									}
								} );
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	public DomExercise findExercise(String topicName, String exerciseName) {
		DomTopic topic = null;
		for (int i=0 ; i<topics.length ; i++)
			if (topicName.equals(topics[i].getTopicName()))
				topic = topics[i];
				
		if (topic == null)
			return null;
		
		return topic.getExercise(exerciseName);
	}
	
	private JPanel customVariationsPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		thePanel.setBackground(Color.white);
		
		Border bottomLineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black);
		Border insetBorder = BorderFactory.createMatteBorder(7, 8, 7, 8, Color.white);
		thePanel.setBorder(BorderFactory.createCompoundBorder(bottomLineBorder, insetBorder));
		
		final JLabel fileNameLabel = new JLabel("", JLabel.LEFT);
		thePanel.add("Center", fileNameLabel);
		
		JButton pickFileButton = new JButton("Select custom variations file...");
		pickFileButton.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	File customXmlFile = FileFinder.getCustomVariationsFile(ExerciseListFrame.this, defaultVariationsFile);
																	if (customXmlFile != null) {
																		customVariations = new DomCustomVariations(customXmlFile, ExerciseListFrame.this);
																		
																		fileNameLabel.setText(shortFileName(customXmlFile));
																		updateVariationList();
																	}
																}
													});
		thePanel.add("West", pickFileButton);
		
		return thePanel;
	}
	
	private String shortFileName(File f) {
		String fileName = f.getName();
		String dir = f.getParentFile().getName();
		return ".../" + dir + "/" + fileName;
	}
	
	private JPanel headingPanel() {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new GridLayout(1, 2, 5, 0));
		
			JLabel exerciseLabel = new JLabel("Topics & Exercises", JLabel.CENTER);
			exerciseLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
		titlePanel.add(exerciseLabel);
		
			JLabel variationLabel = new JLabel("Variations", JLabel.CENTER);
			variationLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
		titlePanel.add(variationLabel);
		
		return titlePanel;
	}
	
	private JPanel topicExerciseVariationPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new GridLayout(1, 2, 5, 0));
		
			final JPanel topicList = createTopicList(topics);
			final JScrollPane scrollPane = new JScrollPane(topicList);
			scrollPane.setBorder(null);
		thePanel.add(scrollPane);
		
			JPanel variationPanel = new JPanel();
			variationPanel.setLayout(new BorderLayout(0, 10));
			
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					newButton = new JButton("New variation");
					newButton.addActionListener(new ActionListener() {
																					public void actionPerformed(ActionEvent e) {
																						DomExercise exercise = topicPanels[selectedTopic].getSelectedExercise();
																						customVariations.addVariation(exercise);
																						
																						updateVariationList();
																					}
																			});
					newButton.setEnabled(false);
				buttonPanel.add(newButton);
				
			variationPanel.add("North", buttonPanel);
			
				variationList = new JPanel();
				variationList.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 6));
				JScrollPane variationScrollPane = new JScrollPane(variationList);
				variationScrollPane.setBorder(null);
				
			variationPanel.add("Center", variationScrollPane);
				
		thePanel.add(variationPanel);
		
		return thePanel;
	}
	
	
	public DomCustomVariations getCustomVariations() {
		return customVariations;
	}
	

	public void createActions(final File exerciseXmlDir, final DomTopic[] topics) {
		buildExercisesAction = new AbstractAction("Build exercises...") {
																public void actionPerformed(ActionEvent e) {
																	File castDir = FileFinder.getCastFolder(ExerciseListFrame.this);
																	if (castDir != null) {
																		File coreDir = new File(castDir, "core");
																		ExerciseDefnBuilder theWindow = new ExerciseDefnBuilder(topics, customVariations, exerciseXmlDir, coreDir);
																		theWindow.pack();
																		theWindow.setVisible(true);
																		theWindow.toFront();
																	}
																}
														};
		buildTestsAction = new AbstractAction("Build tests...") {
																public void actionPerformed(ActionEvent e) {
																	File pluginDir = FileFinder.getTestPluginFolder(ExerciseListFrame.this);
																	if (pluginDir != null) {
																		TestDefnBuilder theWindow = new TestDefnBuilder(topics, customVariations, pluginDir);
																		theWindow.pack();
																		theWindow.setVisible(true);
																		theWindow.toFront();
																	}
																}
														};
		buildTestJarAction = new AbstractAction("Build jar file for tests...") {
																public void actionPerformed(ActionEvent e) {
																	File pluginDir = FileFinder.getTestPluginFolder(ExerciseListFrame.this);
																	if (pluginDir != null) {
																		TestJarBuilder theWindow = new TestJarBuilder(topics, pluginDir);
																		theWindow.pack();
																		theWindow.setVisible(true);
																		theWindow.toFront();
																	}
																}
														};
	}
	
	public void addMenus() {
		buildMenu = new JMenu("Build");
		buildMenu.setBackground(AdvancedWindow.kMenuBackground);
		buildMenu.add(new JMenuItem(buildExercisesAction));
		buildMenu.add(new JMenuItem(buildTestsAction));
		if (Options.hasMultipleCollections)
			buildMenu.add(new JMenuItem(buildTestJarAction));
		
		mainMenuBar.add(buildMenu);
		mainMenuBar.setBackground(AdvancedWindow.kMenuBackground);

		setJMenuBar(mainMenuBar);
	}
	
	private JPanel createTopicList(DomTopic[] topics) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
		
		topicPanels = new OneTopic[topics.length];
		for (int i=0 ; i<topics.length ; i++) {
			topicPanels[i] = new OneTopic(topics[i], this);
			thePanel.add(topicPanels[i]);
		}
		
		return thePanel;
	}
	
	public void highlightTopic(OneTopic topic) {
		int topicIndex = -1;
		for (int i=0 ; i<topicPanels.length ; i++)
			if (topicPanels[i] == topic)
				topicIndex = i;
		if (topicIndex == selectedTopic) {
			topicPanels[selectedTopic].setHilite(false);
			selectedTopic = -1;
		}
		else {
			if (selectedTopic >= 0)
				topicPanels[selectedTopic].setHilite(false);
			if (topicIndex >= 0)
				topicPanels[topicIndex].setHilite(true);
			selectedTopic = topicIndex;
		}
		updateVariationList();
	}
	
	public void updateVariationList() {
		variationList.removeAll();
		DomExercise exercise = null;
		if (selectedTopic >= 0) {
			DomTopic topic = topics[selectedTopic];
			OneTopic topicPanel = topicPanels[selectedTopic];
			exercise = topicPanel.getSelectedExercise();
			
			if (exercise != null) {
				int nVariations = exercise.noOfVariations();
				
				for (int i=0 ; i<nVariations ; i++) {
					DomVariation variation = exercise.getVariation(i);
					variationList.add(new OneVariation(topic, exercise, variation, this));
				}
				
				if (customVariations != null) {
					Vector customVector = customVariations.getVariations(topic.getTopicName(), exercise.getName());
					if (customVector != null)
						for (int i=0 ; i<customVector.size() ; i++) {
							DomVariation variation = (DomVariation)customVector.elementAt(i);
							variationList.add(new OneCustomVariation(exercise, variation, customVariations, this));
						}
				}
			}
		}
		variationList.revalidate();
		repaint();
		if (exercise == null)
			newButton.setEnabled(false);
		else {
			boolean canCreateVariation = (exercise != null) && exercise.hasTemplate() && customVariations != null;
			newButton.setEnabled(canCreateVariation);
		}
	}
	
}
