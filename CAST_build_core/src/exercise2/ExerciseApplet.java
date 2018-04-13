package exercise2;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import formula.*;


abstract public class ExerciseApplet extends XApplet implements ExerciseConstants {
	static final protected String N_QUESTION_PARAM = "nQuestions";
	static final protected String QUESTION_PARAM = "question";
	static final protected String QN_PARAM_PARAM = "qnParam";
	static final protected String QUESTION_EXTRA_PARAM = "questionExtra";
	static final private String OPTIONS_PARAM = "options";
	
	static final private String FIXED_SEED_PARAM = "fixedSeed";
	static final private String MARK_PARAM = "mark";
	static final private String ATTEMPT_STATUS_PARAM = "attemptStatus";
	
	static final public Color kAnswerBackground = new Color(0xEEEEDD);
	static final public Color kWorkingBackground = new Color(0xDDDDEE);
	static final public Color kTypeChoiceBackground = new Color(0xFFEEBB);
	
	static final protected Color kWrongAnswerBackground = new Color(0xFFDDDD);
	static final protected Color kCorrectAnswerBackground = new Color(0xDDFFDD);
	
	static final protected int QUESTION_MENU = 0;
	static final protected int QUESTION_EXTRA_MENU = 1;
	static final protected int OTHER_MENU = 2;
	
	static final private int PRACTICE = 0;
	static final private int TEST = 1;
	static final private int ANSWER = 2;
	
	static final private long kSeedOffset = 0x1308927502834745l;
//	static final private int kMarkSeedFactor = 87654321;
	
	static final private String kInfinityChar = "\u221E";
	static final private String kInfinityCode = "\\u221E";
//	static final private String kInfinityCode = "#infinity#";
	
	protected String[] question;
	protected String[] qnParam;
	protected String[][] qnExtra;
	
	private AlmostRandomInteger qnGenerator, qnExtraGenerator;
	protected XChoice qnChoice, qnExtraChoice, otherChoice;
																							//	first item must be "Mixed questions"
	
	protected int questionVersion;
	protected int questionExtraVersion;
	protected QuestionPanel questionPanel;
	
	private Random seedGenerator = new Random();
	
	private MarkingPanel markingPanel;
	protected ExerciseMessagePanel message;
	
	private boolean displayInitialised = false;
	
	protected DataSet data = null;
	
	public boolean hasHints = false;
	public int result = ANS_UNCHECKED;
	private int qnMode = PRACTICE;
	private int recordedMark = -1;
	
//================================================
	
	protected class QuestionPanel extends MessagePanel {
		public QuestionPanel(ExerciseApplet exerciseApplet) {
			this(translate("Question"), exerciseApplet);
		}
		
		public QuestionPanel(String title, ExerciseApplet exerciseApplet) {
			super(title, exerciseApplet, NO_SCROLL);
			changeContent();
		}
		
		protected void setQuestionText(String qn) {
			insertText(qn);
		}
		
		protected void fillContent() {
			String param = qnParam[questionVersion];
			String qnCore = question[questionVersion];
			String extra = (qnExtra[questionVersion] == null || hasQuestionParts()) ? null
																		: qnExtra[questionVersion][questionExtraVersion];
			
//			System.out.println("questionVersion = " + questionVersion + ", questionExtraVersion = " + questionExtraVersion);
//			System.out.println("param = " + param);
//			System.out.println("qnCore = " + qnCore);
//			System.out.println("extra = " + extra);
			String qn = expandQuestion(param, qnCore, extra);
			setQuestionText(qn);
			
			setDataForQuestion();
			if (displayInitialised)
				setDisplayForQuestion();
		}
	}
	
//================================================
	
	protected class ExerciseMessagePanel extends MessagePanel {
		public ExerciseMessagePanel(ExerciseApplet exerciseApplet) {
			super(translate("Message"), exerciseApplet, MessagePanel.CAN_SCROLL);
			changeContent();
		}
		
		protected void fillContent() {
			insertMessageContent(this);
		}
	
		protected boolean hasBiggestContent() {
			return false;
		}
	}
	
//================================================
	
	protected boolean hasQuestionParts() {
		return false;
	}
	
	private boolean firstPaint = true;
	
	public void paint(Graphics g) {
		if (firstPaint && markingPanel != null) {									//	some exercises must be laid out before correct feedback can be given since slops may depend on pixel dimensions
			markingPanel.doShowAttempt();		//	does nothing if ATTEMPT_STATUS_PARAM does not exist
			firstPaint = false;
		}
		super.paint(g);
	}
	
	public void init() {
		String fixedSeed = getParameter(FIXED_SEED_PARAM);
		if (fixedSeed != null) {
			int seed = Integer.parseInt(fixedSeed);		//	must be integer not long because PHP does not have long type
			seedGenerator.setSeed(seed);
			if (verifyAnswerMode())
				qnMode = ANSWER;
			else
				qnMode = TEST;
		}
		super.init();
	}
	
//	protected boolean badTestPin() {
//		return false;
//	}
	
	public void setupApplet() {
		registerParameter("index", "int");
		registerParameterTypes();
		readQuestions();
		data = getData();
		
		createDisplay();
		
		displayInitialised = true;
		setDisplayForQuestion();
		
//		markingPanel.doShowAttempt();		//	this should not be called until applet has been laid out since slops may depend on pixel dimensions
	}
	
	public void setFixedQuestionSeed(long seed) {
		seedGenerator.setSeed(seed);
		qnGenerator.setSeed(nextSeed());
		if (qnExtraGenerator != null)
			qnExtraGenerator.setSeed(nextSeed());
		
		parameter = new Object[10];			//		forget history of previous parameters
		
		if (data != null) {
			Enumeration ye = data.getVariableEnumeration();
			while (ye.hasMoreElements()) {
				CoreVariable y = (CoreVariable)ye.nextElement();
				if (y instanceof SampleInterface)
					((SampleInterface)y).setNextSeed(nextSeed());
			}
		}
	}
	
	private boolean verifyAnswerMode() {
		String mark = getParameter(MARK_PARAM);
		if (mark == null)
			return false;
		
		recordedMark = Integer.parseInt(mark);
		return (recordedMark >= 0) && (recordedMark <= 10);
	}
	
	public int getRecordedMark() {
		return recordedMark;					//	between 0 and 10
	}
	
	public boolean isPracticeMode() {
		return qnMode == PRACTICE;
	}
	
	public boolean isTestMode() {
		return qnMode == TEST;
	}
	
	public boolean isAnswerMode() {
		return qnMode == ANSWER;
	}
	
	abstract protected void createDisplay();
	abstract protected XPanel getWorkingPanels(DataSet data);
	
	abstract protected void setDataForQuestion();
	abstract protected void setDisplayForQuestion();
	
	abstract protected DataSet getData();
	
//-------------------------------------------------------------------------
	
	private String[] statusName = new String[10];
	private StatusInterface[] statusItem = new StatusInterface[10];
	private int noOfStatusItems = 0;
	
	protected void registerStatusItem(String name, StatusInterface item) {
		if (noOfStatusItems >= statusName.length) {
			int newLength = statusName.length * 2;
			String[] oldName = statusName;
			statusName = new String[newLength];
			System.arraycopy(oldName, 0, statusName, 0, oldName.length);
			
			StatusInterface[] oldItem = statusItem;
			statusItem = new StatusInterface[newLength];
			System.arraycopy(oldItem, 0, statusItem, 0, oldItem.length);
		}
		statusName[noOfStatusItems] = name;
		statusItem[noOfStatusItems ++] = item;
	}
	
	public String getStatus() {
		String statusString = "";
		for (int i=0 ; i<noOfStatusItems ; i++) {
			String s = statusItem[i].getStatus();
			s = encode(s, kInfinityChar, kInfinityCode);
			statusString += statusName[i] + "(" + s + ") ";
		}
		return statusString;
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status, "()", true);		//	returns ( and ) as tokens
		while (st.hasMoreTokens()) {
			String key = st.nextToken();
			while (key.length() > 0 && key.charAt(0) == ' ')
				key = key.substring(1);
			if (key.length() == 0 || !st.hasMoreTokens())
				break;
			
			@SuppressWarnings("unused")
			String delim = st.nextToken();		//	must be "("
			String value = "";
			int bracketCount = 1;
			while (bracketCount > 0 && st.hasMoreTokens()) {
				String s = st.nextToken();
				if (s.equals(")"))
					bracketCount --;
				else if (s.equals("("))
					bracketCount ++;
				
				if (bracketCount > 0)
					value += s;
			}
			value = encode(value, kInfinityCode, kInfinityChar);
			for (int i=0 ; i<noOfStatusItems ; i++)
				if (statusName[i].equals(key)) {
					statusItem[i].setStatus(value);
					break;
				}
		}
	}
	
	private String encode(String s, String fromCode, String toCode) {
		String result = "";
		int start = 0;
		while (true) {
			int charIndex = s.indexOf(fromCode, start);
			if (charIndex < 0)
				return result + s.substring(start);
			result += s.substring(start, charIndex) + toCode;
			start = charIndex + fromCode.length();
		}
	}
	
	public String getStartStatus() {
		return getParameter(ATTEMPT_STATUS_PARAM);
	}
	
	
//-------------------------------------------------------------------------
	
	private Object parameter[] = new Object[10];
	private Hashtable paramTypeList = new Hashtable(10);
	
	private class ParamType {
		public String type;
		public int index;
		
		public ParamType(String type, int index) {
			this.type = type;
			this.index = index;
		}
	}
	
	abstract protected void registerParameterTypes();
	
	final protected void registerParameter(String name, String type) {
		paramTypeList.put(name, new ParamType(type, paramTypeList.size()));
	}
	
	private void insertParameter(Object token, int paramIndex) {
		if (parameter == null)
			parameter = new Object[paramIndex + 1];
		else if (paramIndex >= parameter.length) {
			Object temp[] = new Object[paramIndex + 1];
			System.arraycopy(parameter, 0, temp, 0, parameter.length);
			parameter = temp;
		}
		parameter[paramIndex] = token;
	}
	
	public Object getObjectParam(String name) {
		int index = ((ParamType)paramTypeList.get(name)).index;
		if (index < parameter.length)
			return parameter[index];
		else
			return null;
	}
	
	public int getIntParam(String name) {
		Object param = getObjectParam(name);
		if (param instanceof Integer)
			return ((Integer)param).intValue();
		else
			return ((IntChoice)param).intValue();
	}
	
	public String getStringParam(String name) {
		return (String)getObjectParam(name);
	}
	
	public StringArray getArrayParam(String name) {
		return (StringArray)getObjectParam(name);
	}
	
	public NumValue getNumValueParam(String name) {
		return (NumValue)getObjectParam(name);
	}
	
	public double getDoubleParam(String name) {
		return getNumValueParam(name).toDouble();
	}
	
	public boolean getBooleanParam(String name) {
		return ((Boolean)getObjectParam(name)).booleanValue();
	}
	
	protected Object[] cloneParameters() {
		if (parameter == null)
			return null;
		else
			return (Object[])parameter.clone();
	}
	
//----------------------------------------------------------------
	
	private String typeArray[] = {"int", "choice", "boolean", "const", "string", "array"};
	private String delimiterArray[] = {",", ",", ",", ",", "*", "*"};			//	delimiter for array type is never used
	
	final protected void addType(String type, String delimiter) {
		String temp[] = typeArray;
		typeArray = new String[temp.length + 1];
		System.arraycopy(temp, 0, typeArray, 0, temp.length);
		typeArray[temp.length] = type;
		
		temp = delimiterArray;
		delimiterArray = new String[temp.length + 1];
		System.arraycopy(temp, 0, delimiterArray, 0, temp.length);
		delimiterArray[temp.length] = delimiter;
	}
	
	private String getArrayDelimiter(String baseType) {
		for (int i=0 ; i<typeArray.length ; i++)
			if (baseType.equals(typeArray[i]))
				return delimiterArray[i];
		return null;
	}
	
	protected void addTypeDelimiters() {
	}
	
//----------------------------------------------------------------
	
	protected int getQuestionExtraMask() {
		return -1;				//	all 'extra' variations allowed. In specific exercises, the question options
											//	may allow some variations to be disabled by overriding this.
	}
	
	protected void readQuestions() {
		int nQuestions = Integer.parseInt(getParameter(N_QUESTION_PARAM));
		qnParam = new String[nQuestions];
		question = new String[nQuestions];
		qnExtra = new String[nQuestions][];
		
		for (int i=0 ; i<nQuestions ; i++) {
			qnParam[i] = readString(QN_PARAM_PARAM + i);
			if (qnParam[i] == null)
				qnParam[i] = readString(QN_PARAM_PARAM);
			question[i] = readString(QUESTION_PARAM + i);
			if (question[i] == null)
				question[i] = readString(QUESTION_PARAM);
			String allExtras = readString(QUESTION_EXTRA_PARAM + i);
			if (allExtras != null) {
				StringTokenizer st = new StringTokenizer(allExtras, "|");
				int nExtras = st.countTokens();
				qnExtra[i] = new String[nExtras];
				for (int j=0 ; j<nExtras ; j++)
					qnExtra[i][j] = st.nextToken();
			}
		}
			
		qnGenerator = new AlmostRandomInteger(0, question.length - 1, nextSeed());
		questionVersion = qnGenerator.generateOne();
		if (qnExtra[questionVersion] != null) {
			qnExtraGenerator = new AlmostRandomInteger(0, qnExtra[questionVersion].length - 1, nextSeed());
			int mask = getQuestionExtraMask();
			do {
				questionExtraVersion = qnExtraGenerator.generateOne();
			} while (((1<<questionExtraVersion) & mask) == 0);
		}
		else
			questionExtraVersion = 0;
		
		addTypeDelimiters();
	}
	
	protected String expandOneQuestionString(String template, Object[] oldParam) {
//		System.out.println("Expanding template: " + template);
		StringTokenizer st = new StringTokenizer(template, "#");
		String result = "";
		while (st.hasMoreTokens()) {
			String s = translateType(st.nextToken());
			
			StringTokenizer st2 = new StringTokenizer(s, "()");
			String type = st2.nextToken();
//			System.out.println("Found token: " + type);
			
			if ((type.length() > 1 && type.charAt(1) == ':') || (type.length() > 2 && type.charAt(2) == ':')) {
				int paramIndex;
				if (type.charAt(1) == ':') {
					String indexString = type.substring(0,1);
					paramIndex = indexString.equals("?") ? -1 : Integer.parseInt(indexString);
					type = type.substring(2);
				}
				else {
					paramIndex = Integer.parseInt(type.substring(0,2));
					type = type.substring(3);
				}
				String paramString = null;
				if (st2.hasMoreTokens())
					paramString = st2.nextToken();
				
				if (paramIndex >= 0 && paramIndex < parameter.length && parameter[paramIndex] != null && paramString == null) {
//					System.out.println("type = " + type);
					if (type.length() > 2 && type.startsWith("array[") && type.charAt(type.length() - 1) == ']') {
						String arrayIndexParam = type.substring(6, type.length() - 1);
						String indexAndType = translateType(arrayIndexParam);
						int arrayParamIndex = Integer.parseInt(indexAndType.charAt(1) == ':'
																	? indexAndType.substring(0, 1) : indexAndType.substring(0, 2));
						int indexValue = (parameter[arrayParamIndex] instanceof IntChoice)
																			? ((IntChoice)parameter[arrayParamIndex]).intValue()
																			: ((Integer)parameter[arrayParamIndex]).intValue();
						StringArray array = (StringArray)parameter[paramIndex];
						result += array.getValue(indexValue).replaceAll("_", " ");
					}
					else
						result += parameter[paramIndex];						//	copy existing parameter
				}
				else {
					Object old = (oldParam == null || paramIndex < 0 || paramIndex >= oldParam.length) ? null
																													: oldParam[paramIndex];
					Object token = expandQuestionToken(type, paramString, old);
					if (paramIndex >= 0)
						insertParameter(token, paramIndex);
					if (token != null)
						result += token.toString();
				}
			}
			else {
				String unicode = MText.translateUnicode(s);		//	deals with unicode chars
				if (unicode != null)
					result += unicode;
				else
					result += s;
			}
		}
//		for (int i=0 ; i<parameter.length ; i++)
//			System.out.println("param " + i + ": " + parameter[i]);
		return result;
	}
	
	public String expandQuestion(String qnParamTemplate, String qnTemplate,
																															String qnExtraTemplate) {
		Object oldParam[] = cloneParameters();
		if (parameter != null)
			for (int i=0 ; i<parameter.length ; i++)
				parameter[i] = null;
		
		if (qnParamTemplate !=  null)
			expandOneQuestionString(qnParamTemplate, oldParam);
		
		String questionText = expandOneQuestionString(qnTemplate, oldParam);
		if (qnExtraTemplate !=  null) {
			String questionExtra = expandOneQuestionString(qnExtraTemplate, oldParam);
			questionText += questionExtra;
		}
		
		return questionText;
	}
	
	private String translateType(String s) {
		Enumeration e = paramTypeList.keys();
		while (e.hasMoreElements()) {
			String name = (String)e.nextElement();
			if (s.indexOf(name) == 0) {
				ParamType typeRecord = (ParamType)paramTypeList.get(name);
				String type = typeRecord.index + ":" + typeRecord.type;
				return type + s.substring(name.length());
			}
		}
		return s;
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		boolean isEmpty = valueString == null || valueString.length() == 0;
		if (baseType.equals("int"))
			return isEmpty ? null : Integer.valueOf(valueString);
		else if (baseType.equals("choice"))
			return isEmpty ? null : new IntChoice(valueString);
		else if (baseType.equals("const"))
			return isEmpty ? null : new NumValue(valueString);
		else if (baseType.equals("boolean"))
			return Boolean.valueOf(valueString);
		else if (baseType.equals("string"))
			return valueString;
		else if (baseType.equals("array"))
			return new StringArray(valueString);
		else
			return null;
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("int")) {
			StringTokenizer pst = new StringTokenizer(paramString, ":");
			int min = Integer.parseInt(pst.nextToken());
			int max = Integer.parseInt(pst.nextToken());
			int oldInt = (oldParam == null) ? -999 : ((Integer)oldParam).intValue();
			int value;
			do {
				value = new RandomInteger(min, max, 1, nextSeed()).generateOne();
			} while (max - min > 1 && value == oldInt);	
																					//	don't allow same value twice unless only 2 options
			return Integer.valueOf(value);
		}
		else if (baseType.equals("choice")) {
			StringTokenizer pst = new StringTokenizer(paramString, ":");
			String minString = pst.nextToken();
			String maxString = pst.nextToken();
			return new IntChoice(minString, maxString, (IntChoice)oldParam, this);
		}
		else if (baseType.equals("const")) {
			StringTokenizer pst = new StringTokenizer(paramString, ":");
			NumValue low = new NumValue(pst.nextToken());
			NumValue high = new NumValue(pst.nextToken());
			int decimals = pst.hasMoreTokens() ? Integer.parseInt(pst.nextToken())
																						: Math.max(low.decimals, high.decimals);
			for (int i=0 ; i<decimals ; i++) {
				low.setValue(low.toDouble() * 10);
				high.setValue(high.toDouble() * 10);
			}
			for (int i=0 ; i<-decimals ; i++) {
				low.setValue(low.toDouble() / 10);
				high.setValue(high.toDouble() / 10);
			}
			low.decimals = high.decimals = 0;
			
			int min = (int)Math.round(low.toDouble());
			int max = (int)Math.round(high.toDouble());
			int value = new RandomInteger(min, max, 1, nextSeed()).generateOne();
			
			double x = value;
			for (int i=0 ; i<decimals ; i++)
				x /= 10.0;
			for (int i=0 ; i<-decimals ; i++)
				x *= 10.0;
//			System.out.println("random const = " + x);
			return new NumValue(x, Math.max(decimals, 0));
		}
		else if (baseType.equals("boolean")) {
			StringTokenizer pst = new StringTokenizer(paramString, ":");		//	must have : at start or end
			double probTrue = 0.5;
			if (pst.hasMoreTokens())
				probTrue = Double.parseDouble(pst.nextToken());
			boolean isTrue = new Random(nextSeed()).nextDouble() < probTrue;
			return isTrue ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (baseType.equals("string") || baseType.equals("array"))
			return paramString;					//	does not make sense to have random String or array
		else
			return null;
	}
	
	private Object createObject(String baseType, String paramString, Object oldParam) {
		if (paramString == null || paramString.indexOf(":") < 0)
			return createConstObject(baseType, paramString);
		else
			return createRandomObject(baseType, paramString, oldParam);
	}
	
	final private Object expandOneToken(String baseType, String type, String paramString,
																																		Object oldParam) {
//		System.out.println("baseType:" + baseType);
//		System.out.println("type:" + type);
//		System.out.println("paramString:" + paramString);
		if (type.indexOf(baseType) != 0)
			return null;
		
		if (type.equals(baseType))
			return createObject(baseType, paramString, oldParam);
		else if (type.indexOf(baseType) == 0) {
			String delimiter = getArrayDelimiter(baseType);
			if (type.indexOf("[]") == baseType.length()) {
				Object value;
				do {
					StringTokenizer pst = new StringTokenizer(paramString, delimiter);
					int options = pst.countTokens();
					int index = new RandomInteger(0, options - 1, 1, nextSeed()).generateOne();
					for (int i=0 ; i<index ; i++)
						pst.nextToken();
					value = createObject(baseType, pst.nextToken(), oldParam);
					if (options <= 2)
						break;
				} while (value.equals(oldParam));
																		//	don't allow same value twice unless only 2 options
				
				return value;
			}
			else {
				StringTokenizer pst = new StringTokenizer(paramString, delimiter);
				String arrayIndexString = type.substring(baseType.length() + 1, type.length() - 1);
				int indexIndex;
				try {
					indexIndex = Integer.parseInt(arrayIndexString);
				} catch (NumberFormatException e) {
					ParamType pt = (ParamType)paramTypeList.get(arrayIndexString);
					if (pt == null) {
						System.out.println("Cannot find parameter type: " + arrayIndexString);
						System.out.println("(Type: " + type + ", BaseType: " + baseType + ")");
					}
					indexIndex = pt.index;
				}
				int index;
				if (parameter[indexIndex] instanceof Integer)
					index = ((Integer)parameter[indexIndex]).intValue();
				else
					index = ((IntChoice)parameter[indexIndex]).intValue();
				for (int i=0 ; i<index ; i++)
					pst.nextToken();
				return createObject(baseType, pst.nextToken(), oldParam);
			}
		}
		else
			return null;
	}
	
	protected Object expandQuestionToken(String type, String paramString, Object oldParam) {
		for (int i=0 ; i<typeArray.length ; i++) {
			Object result = expandOneToken(typeArray[i], type, paramString, oldParam);
			if (result != null)
				return result;
		}
		return null;
	}
	
//-------------------------------------------------------------------------
	
	public long nextSeed() {
		return seedGenerator.nextLong() + kSeedOffset;
	}
	
	public double randomDouble() {
		return seedGenerator.nextDouble();
	}
	
	final public int[] createPermutation(int n) {
		Random rand01 = new Random(nextSeed());
		int[] permutation = new int[n];
		permute(permutation,  rand01);
		return permutation;
	}
	
	final public void permute(int[] permutation) {
		permute(permutation, new Random(nextSeed()));
	}
	
	final public void permute(int[] permutation, Random rand01) {
		permute(permutation, permutation.length, rand01);
	}
	
	final public void permute(int[] permutation, int nItems, Random rand01) {
		permutation[0] = 0;				//	does not use previous values in array
		for (int i=1 ; i<nItems ; i++) {
			int j = (int)Math.round((i + 1) * rand01.nextDouble() - 0.5);
			if (i == j)
				permutation[i] = i;
			else {
				permutation[i] = permutation[j];
				permutation[j] = i;
			}
		}
	}
	
	final public void repermute(int[] permutation, int nSelectedItems, Random rand01) {
		for (int i=1 ; i<nSelectedItems ; i++) {		//	permute previously selected items
			int j = (int)Math.round((i + 1) * rand01.nextDouble() - 0.5);
			if (i != j) {
				int temp = permutation[i];
				permutation[i] = permutation[j];
				permutation[j] = temp;
			}
		}
		
		int nTotalItems = permutation.length;
		for (int i=0 ; i<nTotalItems / 2 ; i++) {		//	invert list to move previous selection to end
			int temp = permutation[i];
			permutation[i] = permutation[nTotalItems - i - 1];
			permutation[nTotalItems - i - 1] = temp;
		}
		
		int nCandidates = Math.max(nSelectedItems, nTotalItems - nSelectedItems);
		for (int i=1 ; i<nCandidates ; i++) {				//	permute all candidates for next selection
			int j = (int)Math.round((i + 1) * rand01.nextDouble() - 0.5);
			if (i != j) {
				int temp = permutation[i];
				permutation[i] = permutation[j];
				permutation[j] = temp;
			}
		}
	}
	
//-------------------------------------------------------------------------
	
	abstract protected void insertMessageContent(MessagePanel messagePanel);
	abstract protected int getMessageHeight();
	
	protected XPanel createQuestionTypeMenu(String typeLabel, String[] menuItem, int qnType) {
		XPanel thePanel = new InsetPanel(20, 4);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XChoice theChoice = new XChoice(typeLabel, XChoice.HORIZONTAL, this);
			for (int i=0 ; i<menuItem.length ; i++)
				theChoice.addItem(menuItem[i]);
			if (qnType == QUESTION_MENU)
				qnChoice = theChoice;
			else if (qnType == QUESTION_EXTRA_MENU)
				qnExtraChoice = theChoice;
			else
				otherChoice = theChoice;
			
		thePanel.add(theChoice);
		
		thePanel.lockBackground(kTypeChoiceBackground);
		return thePanel;
	}
	
	public void showExpressionError(String s) {
		result = ANS_BAD_EXPTRESSION;
		message.showError(s);
	}
	
//-------------------------------------------------------------------------
	
	
	protected MarkingPanel createMarkingPanel(int allowHints) {
		markingPanel = new MarkingPanel(this, allowHints);
		return markingPanel;
	}
	
	public boolean noteChangedWorking() {
		if (result == ANS_UNCHECKED)
			return false;
		else {
			markingPanel.clear();
			result = ANS_UNCHECKED;
			message.changeContent();
			return true;
		}
	}
	
	abstract protected int assessAnswer();
	abstract protected void giveFeedback();
	abstract protected void showCorrectWorking();
	abstract protected double getMark();
	
	final public void check() {
		result = assessAnswer();
		giveFeedback();
		message.changeContent();
	}
	
	final public void showAnswer() {
		result = ANS_TOLD;
		showCorrectWorking();
		message.changeContent();
	}
	
	public boolean isIncomplete() {
		return assessAnswer() == ANS_INCOMPLETE;
	}
	
/*
	public String getResultString() {
		String fixedSeed = getParameter(FIXED_SEED_PARAM);
		int seed = Integer.parseInt(fixedSeed);
		
		int mark = (int)Math.round(getMark() * 10) + 1;			//		1 to 11
		mark *= kMarkSeedFactor;
		
		return String.valueOf(~seed ^ mark);
	}
*/
	
	public int getResult() {
		return (int)Math.round(getMark() * 10);
	}
	
	public void showHints(boolean hasHints) {
		this.hasHints = hasHints;
		noteChangedWorking();		//		then show hints in all CalculationPanels; perhaps message.changeContent();
	}
	
//-------------------------------------------------------------------------
	
	final public void anotherQuestion() {
		if (qnChoice != null && qnChoice.getSelectedIndex() > 0)
			questionVersion = qnChoice.getSelectedIndex() - 1;
		else
			questionVersion = qnGenerator.generateOne();
		
		if (qnExtra[questionVersion] != null) {
			int nExtras = qnExtra[questionVersion].length;
			if (qnExtraChoice != null && qnExtraChoice.getSelectedIndex() > 0 && qnExtraChoice.getSelectedIndex() <= nExtras)
				questionExtraVersion = qnExtraChoice.getSelectedIndex() - 1;
			else {
				if (qnExtraGenerator == null || qnExtraGenerator.getMax() != nExtras - 1)
					qnExtraGenerator = new AlmostRandomInteger(0, qnExtra[questionVersion].length - 1, nextSeed());
				
				int mask = getQuestionExtraMask();
				do {
					questionExtraVersion = qnExtraGenerator.generateOne();
				} while (((1<<questionExtraVersion) & mask) == 0);
			}
		}
		else
			questionExtraVersion = 0;
			
		questionPanel.changeContent();
		questionPanel.invalidate();
		result = ANS_UNCHECKED;
		message.changeContent();
		message.invalidate();
		
		validate();
	}
	
	
	protected String readString(String param) {
		String s = getParameter(param);		//	translates \n in parameter into new line
		String result = "";
		if (s == null)
			return null;
		else {
			int startIndex = 0;
			while (true) {
				int endIndex = s.indexOf("\\n", startIndex);
				if (endIndex >= 0) {
					result += s.substring(startIndex, endIndex) + "\n";
					startIndex = endIndex + 2;
				}
				else {
					result += s.substring(startIndex, s.length());
					break;
				}
			}
		}
		return result;
	}
	
	public boolean hasOption(String optionName) {
		String paramString = getParameter(OPTIONS_PARAM);
		if (paramString == null)
			return false;
		else
			return paramString.indexOf(optionName) >= 0;
	}

	
	private boolean localAction(Object target) {
		if (target == qnChoice || target == qnExtraChoice || target == otherChoice) {
			noteChangedWorking();
			anotherQuestion();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}