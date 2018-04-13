package cast.exercise;

import java.util.*;

import cast.variationEditor.*;

	
public class DomConverter {
static final private Hashtable codeLookup;
	
	static {															//			Based on formula.MText class in main CAST code
		codeLookup = new Hashtable(4);
		codeLookup.put("#amp#", "&");
		codeLookup.put("#lt#", "<");
		codeLookup.put("#gt#", ">");
		codeLookup.put("#quot#", "\"");
		codeLookup.put("#apos#", "'");
		codeLookup.put("#alpha#", "\u03B1");
		codeLookup.put("#beta#", "\u03B2");
		codeLookup.put("#gamma#", "\u03B3");
		codeLookup.put("#delta#", "\u03B4");
		codeLookup.put("#epsilon#", "\u03B5");
		codeLookup.put("#mu#", "\u03BC");
		codeLookup.put("#sigma#", "\u03C3");
		codeLookup.put("#capitalSigma#", "\u03A3");
		codeLookup.put("#rho#", "\u03C1");
		codeLookup.put("#pi#", "\u03C0");
		codeLookup.put("#lambda#", "\u03BB");
		codeLookup.put("#hat#", "\u0302");
		codeLookup.put("#bar#", "\u0305");
		codeLookup.put("#sub0#", "\u2080");
		codeLookup.put("#sub1#", "\u2081");
		codeLookup.put("#sub2#", "\u2082");
		codeLookup.put("#sub3#", "\u2083");
		codeLookup.put("#sub4#", "\u2084");
		codeLookup.put("#sub5#", "\u2085");
		codeLookup.put("#sub6#", "\u2086");
		codeLookup.put("#sub7#", "\u2087");
		codeLookup.put("#sub8#", "\u2088");
		codeLookup.put("#sub9#", "\u2089");
		codeLookup.put("#sup0#", "\u2070");
		codeLookup.put("#sup1#", "\u00B9");
		codeLookup.put("#sup2#", "\u00B2");
		codeLookup.put("#sup3#", "\u00B3");
		codeLookup.put("#sup4#", "\u2074");
		codeLookup.put("#sup5#", "\u2075");
		codeLookup.put("#sup6#", "\u2076");
		codeLookup.put("#sup7#", "\u2077");
		codeLookup.put("#sup8#", "\u2078");
		codeLookup.put("#sup9#", "\u2079");
		codeLookup.put("#plusMinus#", "\u00B1");
		codeLookup.put("#degrees#", "\u00B0");
		codeLookup.put("#approxEqual#", "\u2248");
		codeLookup.put("#sqrt#", "\u221A");
		codeLookup.put("#infinity#", "\u221E");
		codeLookup.put("#bullet#", "\u2022");
		codeLookup.put("#spade#", "\u2660");
		codeLookup.put("#club#", "\u2663");
		codeLookup.put("#heart#", "\u2665");
		codeLookup.put("#diamond#", "\u2666");
		codeLookup.put("#en#", "\u2013");
		codeLookup.put("#em#", "\u2014");
		codeLookup.put("#times#", "\u00D7");
		codeLookup.put("#quarter#", "\u00BC");
		codeLookup.put("#half#", "\u00BD");
		codeLookup.put("#le#", "\u2264");
		codeLookup.put("#ge#", "\u2265");
		codeLookup.put("#ne#", "\u2260");
	}
	
	static public String decodeSpecialChars(String s) {				//		allows unicode chars to be typed in "#" form
		Enumeration translationKeys = codeLookup.keys();
		while (translationKeys.hasMoreElements()) {
			String key = (String)translationKeys.nextElement();
			String value = (String)codeLookup.get(key);
			s = s.replaceAll(key, value);
		}
		return s;
	}
	
	static public String encodeSpecialChars(String s) {				//		allows unicode chars to be typed in "#" form
		Enumeration translationKeys = codeLookup.keys();
		while (translationKeys.hasMoreElements()) {
			String key = (String)translationKeys.nextElement();
			String value = (String)codeLookup.get(key);
			s = s.replaceAll(value, key);
		}
		return s;
	}
	
	static public void inputQuestionFromDom(DomVariation variation, VariationEditor editor) {
		String shortName = variation.getShortName();
		String longName = variation.getLongName();
		
		String rawQuestionText = variation.getRawQuestionText();
		String rawParamString = variation.getRawParamString();
		
		VariableType[] mainParamsAllowed = variation.getExercise().getVariableTypes(DomExercise.MAIN_PARAMS_ONLY);		//	excludes params in endings
		
		Hashtable params = new Hashtable();
		if (rawParamString != null)
			processParamString(rawParamString, params, mainParamsAllowed);
		
		Hashtable anonVariables = new Hashtable();
		QuestionAndParams qnAndParams = new QuestionAndParams(rawQuestionText, params, anonVariables);
		
		processQuestion(qnAndParams, mainParamsAllowed, editor);
		
		editor.setShortName(shortName);
		editor.setLongName(longName);
		editor.setQuestionAndParams(qnAndParams);
		
		inputEndingsFromDom(variation, editor);
	}
	
	static public void inputEndingsFromDom(DomVariation variation, VariationEditor editor) {
		int nEndings = variation.getNoOfEndings();
		Vector endings = null;
		if (nEndings > 0) {
			endings = new Vector();
			VariableType[] allParamsAllowed = variation.getExercise().getVariableTypes(DomExercise.ALL_PARAMS);		//	includes params in endings
			for (int i=0 ; i<nEndings ; i++) {
				String rawEndingString = variation.getRawEndingString(i);
				QuestionAndParams endingAndParams = new QuestionAndParams(rawEndingString, new Hashtable(), null);
				processQuestion(endingAndParams, allParamsAllowed, editor);
				endings.add(endingAndParams);
			}
		}
		
		editor.setEndings(endings);
	}
	
	
	static private void processParamString(String rawParamString, Hashtable params, VariableType[] paramsAllowed) {
		StringTokenizer st = new StringTokenizer(rawParamString, "#");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			
			StringTokenizer st2 = new StringTokenizer(s, "()[]");
			String paramName = st2.nextToken();
			
			if (isParameterName(paramName, paramsAllowed))
				params.put(paramName, s.substring(paramName.length()));		//	includes any [] and (value) parts
			else
				System.out.println("Unknown parameter \"" + s + "\"");
		}
	}
	
	static private void processQuestion(QuestionAndParams qnAndParams, VariableType[] paramsAllowed,
																																								VariationEditor editor) {
		String rawQuestionText = qnAndParams.getQuestion();
		Hashtable params = qnAndParams.getParams();
		Hashtable anonVariables = qnAndParams.getAnonVariables();
		
		rawQuestionText = rawQuestionText.replaceAll("\\\\\\\\n", "\n");		//		changes "\\n" to new line char
		
		rawQuestionText = decodeSpecialChars(rawQuestionText);
		
		String questionText = "";
		StringTokenizer st = new StringTokenizer(rawQuestionText, "#");
		while (st.hasMoreTokens()) {
			if (questionText.length() > 0)
				questionText += "#";
			String s = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(s, "()[]");
			String paramName = st2.nextToken();
			
			if (anonVariables != null && paramName.startsWith("?:")) {
				if (questionText.length() == 0)		//	param should have # before it at start of question
					questionText += "#";
				
				String anonParamName = editor.getNextAnonName();
				String typeAndValue = s.substring(2);
				anonVariables.put(anonParamName, typeAndValue);
				questionText += anonParamName;
				
				if (!st.hasMoreTokens())
					questionText += "#";		//	param should have # after it at end of question
			}
			else if (isParameterName(paramName, paramsAllowed) && st2.hasMoreTokens()) {
				if (questionText.length() == 0)		//	param should have # before it at start of question
					questionText += "#";
				
				if (s.indexOf("(") > 0) {
					questionText += paramName;
					params.put(paramName, s.substring(paramName.length()));		//	includes any [] and (value) parts
				}
				else
					questionText += s;
				
				if (!st.hasMoreTokens())
					questionText += "#";		//	param should have # after it at end of question
			}
			else
				questionText += s;
		}
		qnAndParams.setQuestion(questionText);
	}
	
	static private boolean isParameterName(String name, VariableType[] paramsAllowed) {
		if (name.equals("index"))
			return true;
		
		for (int i=0 ; i<paramsAllowed.length ; i++)
			if (paramsAllowed[i].getName().equals(name))
				return true;
		return false;
	}
	
	static private boolean notAllowedInQuestion(String name, VariableType[] paramsAllowed) {
		for (int i=0 ; i<paramsAllowed.length ; i++)
			if (paramsAllowed[i].getName().equals(name) && paramsAllowed[i].forcedToParamTag())
				return true;
		return false;
	}
	
	
	
//-----------------------------------------------------------------------------------------
	
	
	
	public String processedQuestion, paramString;
	
	public DomConverter(QuestionAndParams qnAndParams, VariableType[] paramsAllowed) {
		String questionText = qnAndParams.getQuestion();
		Hashtable params = qnAndParams.getParams();
		Hashtable anonVariables = qnAndParams.getAnonVariables();
		
		Hashtable paramsCopy = new Hashtable(params);
		
		String codedQuestionText = questionText.replaceAll("\\n", "\\\\\\\\n");		//		changes new line char to "\\n"
		
		codedQuestionText = decodeSpecialChars(codedQuestionText);
		
		processedQuestion = "";
		paramString = "";
		StringTokenizer st = new StringTokenizer(codedQuestionText, "#");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (paramsCopy.containsKey(s)) {
				String varValue = (String)paramsCopy.get(s);
				if (notAllowedInQuestion(s, paramsAllowed)) {
					processedQuestion += "#" + s + "#";
					addOneParam(paramsCopy, s);
				}
				else
					processedQuestion += "#" + s + varValue + "#";
				
				paramsCopy.remove(s);
			}
			else if (anonVariables != null && anonVariables.containsKey(s)) {
				String varTypePlusValue = (String)anonVariables.get(s);
				processedQuestion += "#?:" + varTypePlusValue + "#";
			}
			else if (isParameterName(s, paramsAllowed)) {
				processedQuestion += "#" + s + "#";			//	variable defined previously in question
			}
			else {
				int arrayStart = s.indexOf('[');
				if (arrayStart > 0 && s.charAt(s.length() - 1) == ']') {
					String arrayName = s.substring(0, arrayStart);
					String indexName = s.substring(arrayStart + 1, s.length() - 1);
					if (isParameterName(arrayName, paramsAllowed) && (indexName.equals("index") || isParameterName(indexName, paramsAllowed)))
						processedQuestion += "#" + s + "#";
					else
						processedQuestion += s;
				}
				else
					processedQuestion += s;
			}
			
												//	the # messes up the question when it is parsed if it occurs inside a string param
//			processedQuestion = processedQuestion.replaceAll("<", "#lt#");
//			processedQuestion = processedQuestion.replaceAll(">", "#gt#");
//			processedQuestion = processedQuestion.replaceAll("&", "#amp#");
//			processedQuestion = processedQuestion.replaceAll("\"", "#quot#");
//			processedQuestion = processedQuestion.replaceAll("'", "#apos#");
		}
		
		addOneParam(paramsCopy, "index");			//	always make index first since other params may refer to it
		
		int nParams = paramsAllowed.length;
		for (int i=0 ; i<nParams ; i++) {					//		keeps to order in exercise definition
			String varName = paramsAllowed[i].getName();
			if (!varName.equals("index"))
				addOneParam(paramsCopy, varName);			//	only writes params that were not used in question text
		}
	}
	
	private void addOneParam(Hashtable paramsCopy, String varName) {
		String varValue = (String)paramsCopy.get(varName);
		if (varValue != null) {
			if (paramString.length() > 0)
				paramString += "#";
			paramString += varName + varValue;
		}
	}
	
	public String getQuestionText() {
		return encodeSpecialChars(processedQuestion);
	}
	
	public String getQuestionParams() {
		return encodeSpecialChars(paramString);
	}
	
	public void updateDom(String longName, String shortName, int height, DomVariation variation, String[] endingParams) {
		variation.updateDom(longName, shortName, height, processedQuestion, paramString, endingParams);
	}
}