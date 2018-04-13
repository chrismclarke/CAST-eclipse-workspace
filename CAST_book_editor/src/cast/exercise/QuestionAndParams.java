package cast.exercise;

import java.util.*;


public class QuestionAndParams {
	private String question;
	private Hashtable params, anonVariables;
	
	public QuestionAndParams(String question, Hashtable params, Hashtable anonVariables) {
		this.question = question;
		this.params = params;
		this.anonVariables = anonVariables;
	}
	
	public QuestionAndParams(String question, Hashtable params) {
		this(question, params, null);
	}
	
	public String getQuestion() { return question; }
	public Hashtable getParams() { return params; }
	public Hashtable getAnonVariables() { return anonVariables; }
	
	public void setQuestion(String question) { this.question = question; }
	
	public boolean hasIndexVariable() {
		return params.containsKey("index");
	}
	
	public void clearParamsFromStartOfQuestion() {		//	for question ending
		String processedQuestion = "";
		boolean pastParams = false;
		StringTokenizer st = new StringTokenizer(question, "#");
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (pastParams) {
				processedQuestion += "#" + s;
			}
			else if (!params.containsKey(s)) {
				pastParams = true;
				processedQuestion += s;
			}
		}
		question = processedQuestion;
	}
}