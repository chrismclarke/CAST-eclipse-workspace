package cast.exercise;

import org.w3c.dom.*;

import cast.utils.*;

public class VariableType {
	private String type;
	private String name;
	private String comment;
	private boolean extra;		// for variable that appears in ending only
	private boolean forceToParam;		// for variable that must appear in param tag, even if used in question tag
	
	public VariableType(Element varElement) {
		name = varElement.getAttribute("name");
		type = varElement.getAttribute("type");
		comment = XmlHelper.getTagInterior(varElement);
//		comment = varElement.getAttribute("comment");
		if (comment != null && comment.length() == 0)
			comment = null;
		String extraString = varElement.getAttribute("extra");
		extra = extraString != null && extraString.equals("true");
		String forceToParamString = varElement.getAttribute("forceToParam");
		forceToParam = forceToParamString != null && forceToParamString.equals("true");
	}
	
	public String getType() { return type; }
	public String getName() { return name; }
	public String getComment() { return comment; }
	public boolean isExtra() { return extra; }
	public boolean forcedToParamTag() { return forceToParam; }
}