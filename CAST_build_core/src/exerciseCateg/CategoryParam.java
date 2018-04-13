package exerciseCateg;

import java.util.*;

import dataView.*;
import random.*;
import exercise2.*;


public class CategoryParam {
	private int index;
	private Value[] categories;
	private boolean isOrdinalNotPareto, isLess;
	
	static public Object createConstObject(String valueString, Value[] categories, boolean isOrdinalNotPareto,
																																											boolean isLess) {
		int index = Integer.parseInt(valueString);
		return new CategoryParam(index, categories, isOrdinalNotPareto, isLess);
	}
	
	static public Object createRandomObject(String paramString, Value[] categories, boolean isOrdinalNotPareto,
																																					boolean isLess, ExerciseApplet applet) {
		StringTokenizer pst = new StringTokenizer(paramString, ":");
		int min = Integer.parseInt(pst.nextToken());
		int max = Integer.parseInt(pst.nextToken());
		return new CategoryParam(min, max, categories, isOrdinalNotPareto, isLess, applet);
	}
	
	private CategoryParam(int index, Value[] categories, boolean isOrdinalNotPareto, boolean isLess) {
		this.index = index;
		this.categories = categories;
		this.isOrdinalNotPareto = isOrdinalNotPareto;
		this.isLess = isLess;
	}
	
	private CategoryParam(int min, int max, Value[] categories, boolean isOrdinalNotPareto, boolean isLess, ExerciseApplet applet) {
		RandomInteger rand = new RandomInteger(min, max, 1, applet.nextSeed());
		index = rand.generateOne();
		this.categories = categories;
		this.isOrdinalNotPareto = isOrdinalNotPareto;
		this.isLess = isLess;
	}
	
	public int intValue() {
		return index;
//		if (isOrdinalNotPareto)
//			return index;
//		else if (isLess)
//			return index + 1;
//		else
//			return categories.length - index;
	}
	
	public String toString() {
		if (isOrdinalNotPareto)
			return categories[index].toString();
		else if (isLess)
			return String.valueOf(index + 1);			// since it is displayed in qn as "least common x categories"
		else
			return String.valueOf(categories.length - index);			// since it is displayed in qn as "most common x categories"
	}
}
