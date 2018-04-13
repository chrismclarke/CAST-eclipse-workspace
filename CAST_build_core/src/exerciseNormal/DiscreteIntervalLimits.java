package exerciseNormal;


public class DiscreteIntervalLimits {
	static final public int UNSPECIFIED = 0;
	static final public int INCLUDED = 1;
	static final public int EXCLUDED = 2;
	
	public int start, end, total;
	public int startType, endType;
	
	public DiscreteIntervalLimits(int start, int end, int startType, int endType, int total) {
		this.start = start;
		this.end = end;
		this.startType = startType;
		this.endType = endType;
		this.total = total;
	}
	
	public String toString() {
		switch (startType) {
			case UNSPECIFIED:
				switch (endType) {
					case UNSPECIFIED:
						return "any value";
					case INCLUDED:
						return "less than or equal to " + end;
					case EXCLUDED: 
						return "less than " + end;
					
				}
			case INCLUDED:
				switch (endType) {
					case UNSPECIFIED:
						return "greater than or equal to " + start;
					case INCLUDED:
						return "between " + start + " and " + end + " (inclusive)";
					case EXCLUDED: 
						return "greater than or equal to " + start + " but less than " + end;
				}
			case EXCLUDED:
				switch (endType) {
					case UNSPECIFIED:
						return "greater than " + start;
					case INCLUDED:
						return "greater than " + start + " but less than or equal to " + end;
					case EXCLUDED: 
						return "between " + start + " and " + end + " (exclusive)";
				}
		}
		return null;
	}
	
	public String probAnswerString() {
		int first = getFirst();
		int last = getLast();
		
		if (first == last)
			return "the probability of exactly " + first;
		else
			return "the sum of the probabilities from " + first + " to " + last;
	}
	
	public int getFirst() {
		return (startType == UNSPECIFIED) ? 0 : (startType == INCLUDED) ? start : (start + 1);
	}
	
	public int getLast() {
		return (endType == UNSPECIFIED) ? total : (endType == INCLUDED) ? end : (end - 1);
	}
}