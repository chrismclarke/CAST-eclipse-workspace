package ebook;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import utils.*;
import dataView.*;
import exercise2.*;
import ebookStructure.*;
import pageStructure.*;


public class TestFrame extends JFrame {
	static final public String kMoodlePathToModule = "/mod/testcast";
	
	static final private Font kMessageFont = new Font("Arial", Font.PLAIN, 24);
	
	static final private int kMarkFontSize = 20;
	static final private Font kMarkFont = new Font("Arial", Font.BOLD, kMarkFontSize);
	static final private Color kMarkColor = new Color(0x990000);
	
	static final private int kTimerFontSize = 28;
	static final private Font kTimerFont = new Font("Arial", Font.BOLD, kTimerFontSize);
	static final private Color kTimerColor = new Color(0x000000);
	static final private Color kDimTimerColor = new Color(0xFFFFFF);
	
	static final private int kQuestionNumberFontSize = 28;
	static final private Font kQuestionNumberFont = new Font("Arial", Font.BOLD, kQuestionNumberFontSize);
	static final private Color kQuestionNumberColor = new Color(0x666666);
	
	static final private Color kControlPanelBackground = new Color(0xAAAAAA);
	
	static final private int START_STAGE = 0;
	static final private int TEST_STAGE = 1;
	static final private int REVIEW_STAGE = 2;
	
	static final public int TEST_MODE = 0;
	static final public int PRACTICE_MODE = 1;
	static final public int REVIEW_MODE = 2;
	
	private CastEbook theEbook;
	private BookFrame theBookWindow;
	private File testXmlFile;
	private int mode;
	private String token;
	private int activityId;
	private String moodleServer;
	private int testRandomSeed;
	private int fileHash;
	private String testName;
	private boolean manualToken;
	
	private int currentQuestion = 0;
	private int nQuestions;
	private NodeList questionNodes;
	private JPanel questionPanel;
	private JPanel buttonPanel;
	private JLabel markLabel;
	private JLabel questionNumber;
	private JLabel timeRemaining;
	private javax.swing.Timer testTimer;
	private int stage = START_STAGE;
	private boolean alreadySaved = false;
	
	private long startTime;			// in millisecs
	private long maxTestTime;		// in millisecs
	
	private int randomSeed[];
	private JPanel questionApplet[];
	private String questionStatus[];
	private int questionMark[];
	
	private UiImage backImage, nextImage;
	
	public TestFrame(CastEbook theEbook, File testXmlFile, BookFrame bookWindowParam, TestInformation testInfo) {
		this.theEbook = theEbook;
		this.theBookWindow = bookWindowParam;
		this.testXmlFile = testXmlFile;
		this.token = testInfo.token;
		mode = testInfo.mode;
		activityId = testInfo.activityId;
		testRandomSeed = testInfo.randomSeed;
		fileHash = testInfo.hash;
		manualToken = testInfo.manualToken;
		
		Element testDom = readTestDom(testXmlFile).getDocumentElement();
		moodleServer = testDom.getAttribute("moodleServer");
		questionNodes = testDom.getElementsByTagName("question");
		nQuestions = questionNodes.getLength();
		
		randomSeed = createRandomSeeds((mode == PRACTICE_MODE) ? new Random().nextInt() : testRandomSeed,
																																										nQuestions);
		
		int minutesLeft = Integer.parseInt(testDom.getAttribute("minutes"));
		if (mode == TEST_MODE && testInfo.reducedMinutes >= 0)
			minutesLeft = Math.min(minutesLeft, testInfo.reducedMinutes);
		maxTestTime = minutesLeft * 1000 * 60;
		
		testName = testDom.getAttribute("name");
		String windowTitle = testName;
		if (mode == PRACTICE_MODE)
			windowTitle += " (Practice mode)";
		setTitle(windowTitle);
		setLayout(new BorderLayout(0, 20));
		getContentPane().setBackground(Color.white);
			
		add("North", testControlPanel(testName));
		
			questionPanel = createQuestionPanel(questionNodes);
		add("Center", questionPanel);
			showQuestion(0);
		
		pack();
		setResizable(false);
		
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (stage == TEST_STAGE) {
					Object[] options = {"Quit Test", "Cancel"};
					int result = JOptionPane.showOptionDialog(TestFrame.this, "Are you sure that you want to stop the test?"
																						+ "\nYour attempt will not be saved and your mark will be zero.",
																						"Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
																						null, options, options[1]);
					if (result == 0) {
						if (testTimer != null)
							testTimer.stop();
						theBookWindow.setVisible(true);
						setVisible(false);
					}
				}
				else if (stage == REVIEW_STAGE && (mode == TEST_MODE) && !alreadySaved) {
					Object[] options = {"Quit", "Cancel"};
					int result = JOptionPane.showOptionDialog(TestFrame.this, "Are you sure that you want to quit without saving the attempt?"
																						+ "\nYou will not be able to review the attempt later.",
																						"Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
																						null, options, options[1]);
					if (result == 0) {
						theBookWindow.setVisible(true);
						setVisible(false);
					}
				}
				else {
					theBookWindow.setVisible(true);
					setVisible(false);
				}

      }
    });
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}
	
	public TestFrame(CastEbook theEbook, File attemptXmlFile, BookFrame bookWindowParam) {
		this.theEbook = theEbook;
		this.theBookWindow = bookWindowParam;
		
		mode = REVIEW_MODE;
		stage = REVIEW_STAGE;
		
		Element testDom = readTestDom(attemptXmlFile).getDocumentElement();
		String tokenString = testDom.getAttribute("token");
		Token token = new Token(tokenString);
		
		int testRandomSeed = token.randomSeed;
		
		questionNodes = testDom.getElementsByTagName("question");
		nQuestions = questionNodes.getLength();
		
		randomSeed = createRandomSeeds(testRandomSeed, nQuestions);
		
		questionStatus = new String[nQuestions];
		questionMark = new int[nQuestions];
		for (int i=0 ; i<nQuestions ; i++) {
			Element question = (Element)questionNodes.item(i);
//			String hashMarkAndStatus = question.getTextContent();
			Node qnText = (Node)question.getFirstChild();
			String hashMarkAndStatus = qnText.getNodeValue();
			int hashIndex = hashMarkAndStatus.indexOf("#");
			int markStatusHash = Integer.parseInt(hashMarkAndStatus.substring(0, hashIndex));
			String markAndStatus = hashMarkAndStatus.substring(hashIndex + 1);
			if (markStatusHash != markAndStatus.hashCode()) {
				JOptionPane.showMessageDialog(this, "The attempt file has been modified.", "Error!",
																																			JOptionPane.ERROR_MESSAGE);
				throw new RuntimeException("Error! Modified attempt file.");
			}
			hashIndex = markAndStatus.indexOf("#");
			questionMark[i] = Integer.parseInt(markAndStatus.substring(0, hashIndex));
			questionStatus[i] = markAndStatus.substring(hashIndex + 1);
		}
		
		String testName = testDom.getAttribute("name");
		setTitle(testName + " (Review) ");
		
		setLayout(new BorderLayout(0, 20));
		getContentPane().setBackground(Color.white);
			
		add("North", testControlPanel(testName));
		
			questionPanel = createReviewPanel(questionNodes);
		add("Center", questionPanel);
			showQuestion(1);
		
		pack();
		setResizable(false);
	}
	
	private int[] createRandomSeeds(int startSeed, int nSeeds) {
		int[] seeds = new int[nSeeds];
		seeds[0] = startSeed;
		Random randomGenerator = new Random();
		for (int i=1 ; i<nSeeds ; i++) {
			int nextSeed = seeds[i-1] * 10 + 5;
			randomGenerator.setSeed(nextSeed);
			seeds[i] = randomGenerator.nextInt();
		}
		return seeds;
	}
	
	
	private Document readTestDom(File testXmlFile) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(new ErrorHandler() {
													public void warning(SAXParseException exception) throws SAXException {
													}
													public void error(SAXParseException exception) throws SAXException {
													}
													public void fatalError(SAXParseException exception) throws SAXException {
													}
												} );
			
			Document testDomDocument = db.parse(testXmlFile);
			
			return testDomDocument;
		} catch(Exception e) {
			System.err.println("Error opening test\n" + e);
		}
		return null;
	}
	
	private JPanel createQuestionPanel(NodeList questionNodes) {
		JPanel thePanel = new JPanel();
		CardLayout qnCardLayout = new CardLayout();
		thePanel.setLayout(qnCardLayout);
		thePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		thePanel.setOpaque(false);
		
		thePanel.add(createStartPanel(), "Q0");
		questionApplet = new JPanel[nQuestions];
		for (int i=0 ; i<nQuestions ; i++) {
			questionApplet[i] = createQuestion((Element)questionNodes.item(i), randomSeed[i], null, 0);
			thePanel.add(questionApplet[i], "Q" + (i+1));
		}
		
		return thePanel;
	}
	
	private JPanel createReviewPanel(NodeList questionNodes) {
		JPanel thePanel = new JPanel();
		CardLayout qnCardLayout = new CardLayout();
		thePanel.setLayout(qnCardLayout);
		thePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		thePanel.setOpaque(false);
		
		thePanel.add(createStartPanel(), "Q0");
		questionApplet = new JPanel[nQuestions];
		for (int i=0 ; i<nQuestions ; i++) {
			questionApplet[i] = createQuestion((Element)questionNodes.item(i), randomSeed[i],
																																	questionStatus[i], questionMark[i]);
			thePanel.add(questionApplet[i], "Q" + (i+1));
		}
		
		return thePanel;
	}
	
	private JPanel createStartPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		thePanel.setOpaque(false);
			
			JLabel messageLabel = new JLabel("Click Start to begin test", JLabel.CENTER);
			messageLabel.setFont(kMessageFont);
		thePanel.add(messageLabel);
		return thePanel;
	}
	
	private JPanel createQuestion(Element questionElement, long randomSeed, String status, int mark) {
		String topic = questionElement.getAttribute("topic");
		String applet = questionElement.getAttribute("applet");
		String variations = questionElement.getAttribute("variations");
		if (variations != null && variations.length() == 0)
			variations = null;
		String options = questionElement.getAttribute("options");
		if (options != null && options.length() == 0)
			options = null;
		
		DomExercise theQuestion = new DomExercise(theEbook, topic, applet, variations, options);
			String extraTestParams = "<param name=\"fixedSeed\" value=\"" + randomSeed + "\">";
			if (status != null) {
				extraTestParams += "<param name=\"attemptStatus\" value=\"" + status + "\">";
				extraTestParams += "<param name=\"mark\" value=\"" + mark + "\">";
			}
		String theAppletString = theQuestion.getAppletString(extraTestParams);
//		String theAppletString = theQuestion.getAppletString();
		theAppletString = theAppletString.replaceAll("<div .*>\\s*", "");
		theAppletString = theAppletString.replaceAll("\\s*</div>", "");
		
		AppletDrawer theDrawer = new AppletDrawer(theAppletString, CoreDrawer.getStyleSheet(), null);
			
		return theDrawer.createPanel();
	}
	
	private JPanel testControlPanel(String testName) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		thePanel.setBackground(kControlPanelBackground);
			
			buttonPanel = new JPanel();
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
			buttonPanel.setOpaque(false);
			
		if (mode == REVIEW_MODE) {
			buttonPanel.setLayout(new BorderLayout(0, 0));
			buttonPanel.add("Center", endButtonPanel());
			
			int totalMark = 0;
			for (int i=0 ; i<nQuestions ; i++)
				totalMark += questionMark[i];
			NumValue markValue = new NumValue(totalMark * 0.1, 1);
			markLabel.setText(markValue.toString() + " / " + nQuestions);
		}
		else {
			CardLayout cl = new CardLayout();
			buttonPanel.setLayout(cl);
			
			buttonPanel.add(startButtonPanel(testName), "start");
			buttonPanel.add(runButtonPanel(), "run");
			buttonPanel.add(endButtonPanel(), "end");
			cl.show(buttonPanel, "start");
		}
			
		thePanel.add("West", buttonPanel);
		
			JPanel rightPanel = new JPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			rightPanel.setOpaque(false);
			
				JPanel arrowPanel = new JPanel();
				arrowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				arrowPanel.setOpaque(false);
					
					File structureDir = theEbook.getStructureDir();
					File backStdFile = new File(structureDir, "images/backArrow_std.png");
					File backDimFile = new File(structureDir, "images/backArrow_dim.png");
					File backBoldFile = new File(structureDir, "images/backArrow_bold.png");
					backImage = new UiImage(backStdFile, backDimFile, backBoldFile, true) {
																					protected void doClickAction() {
																						showQuestion(currentQuestion - 1);
																					}
														};
				arrowPanel.add(backImage);
				
					questionNumber = new JLabel("0", JLabel.CENTER);
					questionNumber.setFont(kQuestionNumberFont);
					questionNumber.setForeground(kControlPanelBackground);
				arrowPanel.add(questionNumber);
					
					File nextStdFile = new File(structureDir, "images/nextArrow_std.png");
					File nextDimFile = new File(structureDir, "images/nextArrow_dim.png");
					File nextBoldFile = new File(structureDir, "images/nextArrow_bold.png");
					nextImage = new UiImage(nextStdFile, nextDimFile, nextBoldFile, true) {
																					protected void doClickAction() {
																						showQuestion(currentQuestion + 1);
																					}
														};
				arrowPanel.add(nextImage);
				
			rightPanel.add(arrowPanel);
		
		thePanel.add("Center", rightPanel);
		return thePanel;
	}
	
	private JPanel startButtonPanel(String testName) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
		thePanel.setOpaque(false);
		thePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		
			JLabel testNameLabel = new JLabel(testName, JLabel.LEFT);
			testNameLabel.setFont(kMessageFont);
		thePanel.add(testNameLabel);
		
			JButton startButton = new JButton("Start Test");
			startButton.addActionListener(new ActionListener() {
																	public void actionPerformed(ActionEvent e) {
																		startTest();
																	}
																});
		thePanel.add(startButton);
		return thePanel;
	}
	
	private JPanel runButtonPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		thePanel.setOpaque(false);
		thePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		
			JPanel timerPanel = new JPanel();
			timerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			timerPanel.setOpaque(false);
				JLabel timerLabel = new JLabel("Remaining", JLabel.LEFT);
				timerLabel.setFont(kTimerFont);
				timerLabel.setForeground(kTimerColor);
			timerPanel.add(timerLabel);
				
				timeRemaining = new JLabel("????", JLabel.LEFT);
				timeRemaining.setFont(kTimerFont);
				timeRemaining.setForeground(kTimerColor);
			timerPanel.add(timeRemaining);
		
		thePanel.add(timerPanel);
		
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
			buttonPanel.setOpaque(false);
				JButton endButton = new JButton("Finish Test");
				endButton.addActionListener(new ActionListener() {
																		public void actionPerformed(ActionEvent e) {
																			endTest();
																		}
																	});
			buttonPanel.add(endButton);
		thePanel.add(buttonPanel);
			
		return thePanel;
	}
	
	private JPanel endButtonPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		thePanel.setOpaque(false);
		thePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		
			JPanel markPanel = new JPanel();
			markPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			markPanel.setOpaque(false);
				
				JLabel markTitle = new JLabel("Mark", JLabel.LEFT);
				markTitle.setFont(kMarkFont);
				markTitle.setForeground(kMarkColor);
			markPanel.add(markTitle);
			
				markLabel = new JLabel("", JLabel.LEFT);
				markLabel.setFont(kMarkFont);
				markLabel.setForeground(kMarkColor);
			markPanel.add(markLabel);
			
		thePanel.add(markPanel);
		
		if (mode == TEST_MODE) {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
			buttonPanel.setOpaque(false);
				final JButton saveButton = new JButton("Save attempt");
				saveButton.addActionListener(new ActionListener() {
																		public void actionPerformed(ActionEvent e) {
																			if (saveTest())
																				saveButton.setEnabled(false);
																		}
																	});
			buttonPanel.add(saveButton);
			
			thePanel.add(buttonPanel);
		}
		
		return thePanel;
	}
	
	private void startTest() {
		stage = TEST_STAGE;
		showQuestion(1);
		questionNumber.setForeground(kQuestionNumberColor);
		startTime = System.currentTimeMillis();
		testTimer = new javax.swing.Timer(100, new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				long timeLeft = maxTestTime - System.currentTimeMillis() + startTime;
																				int secondsLeft = (int)(timeLeft / 1000);
																				if (secondsLeft < 0)
																					endTest();
																				else {
																					int minutesLeft = secondsLeft / 60;
																					timeRemaining.setText(String.valueOf(minutesLeft));
																					if (minutesLeft >= 1)
																						timeRemaining.setForeground((secondsLeft % 2 == 0) ? kTimerColor : kDimTimerColor);
																					else {
																						int quarterSecsLeft = (int)(timeLeft / 250);
																						timeRemaining.setForeground((quarterSecsLeft % 2 == 0) ? kTimerColor : kDimTimerColor);
																					}
																				}
																			}
																		});
		testTimer.start();
		
		CardLayout cl = (CardLayout)(buttonPanel.getLayout());
		cl.show(buttonPanel, "run");
	
	}
	
	private void endTest() {
		stage = REVIEW_STAGE;
		showAnswers();
		testTimer.stop();
		testTimer = null;
		theBookWindow.setVisible(true);
		toFront();
		
		if (mode == TEST_MODE) {
			if (manualToken) {
				int codedMark = getEncodedMark();
				JPanel messagePanel = new JPanel();
				messagePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 30));
			
					JPanel commentPanel = new JPanel();
					commentPanel.setOpaque(false);
					commentPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
					
					commentPanel.add(new JLabel("You must now copy the encoded test mark below", JLabel.LEFT));
					commentPanel.add(new JLabel("then paste it into the Moodle page for the test.", JLabel.LEFT));
					commentPanel.add(new JLabel(" ", JLabel.LEFT));
					commentPanel.add(new JLabel("Note that this must be done fairly quickly", JLabel.LEFT));
					commentPanel.add(new JLabel("- Moodle only give you a few minutes to do so.", JLabel.LEFT));
				messagePanel.add(commentPanel);
				
					JTextField encodedMark = new JTextField("Encoded mark: " + codedMark);
					encodedMark.setEditable(false);
					encodedMark.setBackground(null);
					encodedMark.setFont(kMarkFont);
					encodedMark.setForeground(kMarkColor);
					encodedMark.setBorder(null);
				messagePanel.add(encodedMark);
				
				JOptionPane.showMessageDialog(this, messagePanel, "Encoded test results", JOptionPane.INFORMATION_MESSAGE);
			}
			else
				sendMarkToMoodle();
		}
	}
	
	private int getEncodedMark() {
		int totalMark = 0;
		for (int i=0 ; i<nQuestions ; i++)
			totalMark += questionMark[i];
		return (totalMark << 8) ^ (fileHash >>> 2) ^ testRandomSeed;
	}
	

	private void sendMarkToMoodle() {
		int codedMark = getEncodedMark();
		
		String url = moodleServer + kMoodlePathToModule + "/record_result.php?testid=" + activityId + "&response=" + codedMark;
		String heading = "Recording mark...";
		
		BrowserFrame.showBrowserFrame(testName, heading, url, null);
	}

	
/*
	private void sendMarkToMoodle() {
		int totalMark = 0;
		for (int i=0 ; i<nQuestions ; i++)
			totalMark += questionMark[i];
		int codedMark = totalMark ^ (fileHash * 31) ^ testRandomSeed;
		try {
			String url = moodleServer + kMoodlePathToModule + "/record_result.php?testid=" + activityId + "&response=" + codedMark;
			java.awt.Desktop.getDesktop().browse(new URI(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	
	private boolean saveTest() {
		JFileChooser fileChooser = new JFileChooser() {
											public void approveSelection() {
												File f = getSelectedFile();
												if (f.exists()) {
													int result = JOptionPane.showConfirmDialog(this, "A file with this name already exists. Overwrite it?",
																												"Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
													switch (result) {
														case JOptionPane.YES_OPTION:
															super.approveSelection();
															return;
														case JOptionPane.CANCEL_OPTION:
															cancelSelection();
															return;
														case JOptionPane.NO_OPTION:
														case JOptionPane.CLOSED_OPTION:
														default:
															return;
													}
												}
												String fileName = f.getName();
												if (fileName.indexOf(".xml") != (fileName.length() - 4)) {
													JOptionPane.showMessageDialog(this, "Error!", "The file must have extension \".xml\".",
																																												JOptionPane.ERROR_MESSAGE);
													cancelSelection();
													return;
												}
												super.approveSelection();
											}
										};
		fileChooser.setDialogTitle("Specify an XML file where the attempt will be saved.");
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
		fileChooser.setSelectedFile(new File("attempt.xml"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("xml file","xml"));
		
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			Document testDom = readTestDom(testXmlFile);
			saveAttemptXml(testDom, fileToSave);
			alreadySaved = true;
			return true;
		}
		return false;
	}
	
	public void saveAttemptXml(Document testDom, File xmlFile) {
		try {
				Element testElement = testDom.getDocumentElement();
				testElement.setAttribute("token", token);
				testElement.removeAttribute("moodleServer");
				testElement.removeAttribute("minutes");
				testElement.removeAttribute("course");
				NodeList questions = testElement.getElementsByTagName("question");
				for (int i=0 ; i<questionStatus.length ; i++) {
					Element qn = (Element)questions.item(i);
					String markAndStatus = questionMark[i] + "#" + questionStatus[i];
					int hash = markAndStatus.hashCode();
					qn.appendChild(testDom.createTextNode(hash + "#" + markAndStatus));
//					qn.setTextContent(hash + "#" + markAndStatus);
//					qn.setAttribute("attempt", hash + "#" + markAndStatus);
				}
				
				DOMSource domSource = new DOMSource(testDom);
				StreamResult streamResult = new StreamResult(xmlFile);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer serializer = tf.newTransformer();
				serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
//				serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"../exercises/xml/testAttemptXmlDefn.dtd");
				serializer.setOutputProperty(OutputKeys.INDENT,"yes");
				serializer.transform(domSource, streamResult);
		}
		catch (TransformerFactoryConfigurationError factoryError) {
			System.err.println("Error creating TransformerFactory");
			factoryError.printStackTrace();
		} catch (TransformerException transformerError) {
			System.err.println("Error transforming document");
			transformerError.printStackTrace();
		}
	}
	
	private void showQuestion(int i) {
    currentQuestion = i;
		CardLayout cl = (CardLayout)(questionPanel.getLayout());
		cl.show(questionPanel, "Q" + i);
		backImage.setImage(i <= 1 ? UiImage.DIM_IMAGE : UiImage.STD_IMAGE);
		nextImage.setImage((i == 0 || i == nQuestions) ? UiImage.DIM_IMAGE : UiImage.STD_IMAGE);
		
		questionNumber.setText(String.valueOf(i));
	}
	
	private void showAnswers() {
		CardLayout cl = (CardLayout)(questionPanel.getLayout());
		questionStatus = new String[nQuestions];
		questionMark = new int[nQuestions];
		for (int i=0 ; i<nQuestions ; i++) {
			Component[] components = questionApplet[i].getComponents();
			if (components[0] instanceof ExerciseApplet) {
				ExerciseApplet e = (ExerciseApplet)components[0];
				questionStatus[i] = e.getStatus();
				questionMark[i] = e.getResult();
			}
			cl.removeLayoutComponent(questionApplet[i]);
		}
		for (int i=0 ; i<nQuestions ; i++) {
			questionApplet[i] = createQuestion((Element)questionNodes.item(i), randomSeed[i], questionStatus[i], questionMark[i]);
			questionPanel.add(questionApplet[i], "Q" + (i+1));
		}
		showQuestion(1);
		
			int totalMark = 0;
			for (int i=0 ; i<nQuestions ; i++)
				totalMark += questionMark[i];
			NumValue markValue = new NumValue(totalMark * 0.1, 1);
			markLabel.setText(markValue.toString() + " / " + nQuestions);
		
		CardLayout cl2 = (CardLayout)(buttonPanel.getLayout());
		cl2.show(buttonPanel, "end");
		
		pack();
	}
	
}