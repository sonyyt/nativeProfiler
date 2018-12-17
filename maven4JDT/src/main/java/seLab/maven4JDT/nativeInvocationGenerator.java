package seLab.maven4JDT;

import java.util.ArrayList;
import java.util.Iterator;


/*
 * 1. mapping from input type to java type. nativeInterface ! 
 * 2. set Potential value by type (type -> potential value array).
 * 3. testing. 
 * 
 */
public class nativeInvocationGenerator{
	private nativeInterface ni;
	public String prefixDeclaration;
	private ArrayList<paramPotentialValues> paramVs = new ArrayList<paramPotentialValues>();

	public nativeInvocationGenerator(nativeInterface interf) {
		this.ni = interf;
		prefixDeclaration = "";
	}

	/*
	 * todo: add error check;
	 */
	public boolean fillDefaultValuesByType() {
		for(int i=0; i<ni.inputIDs.length;i++) {
			String[] potentialValues = getTypePotentialValues(ni.inputTypes[i]);
			//System.out.println("inputType"+ni.inputTypes[i]+String.join(",",potentialValues));
			setParamValue(ni.inputIDs[i], potentialValues);
		}
		return true;
	}
	

	/*
	 * current solution: for each paramvs, generate how many times one item of it will repeat. we know how many strings will there be overall. for each parameter from 
	 * front to end, add new parameter to the String?
	 * 
	 * can we operate on string array? instead of multiple dimentional, operate on two dimonsion: 3*4*5 amount of String[] 
	 */
	public String[][] generateTestCases() {
		int i = 1;
		Iterator<paramPotentialValues> itr = paramVs.iterator();
		while (itr.hasNext()) {
			i *= itr.next().potentialValues.length;
		}
		
		// for each parameter (from front to end)
		String[][] ret = new String[i][ni.inputIDs.length];
		for(int paramOrder =0; paramOrder <ni.inputIDs.length; paramOrder++) {
			String input = ni.inputIDs[paramOrder];
			String[] inputValues = this.getParamPotentialValue(input);
			//we assume that the inputValues are not null!
			for(int inputSet = 0; inputSet<i; inputSet++) {
				ret[inputSet][paramOrder] = inputValues[inputSet % inputValues.length];
			}
		}
		
//		String[] retArr = new String[i];
//		for(int inputSet = 0; inputSet<i; inputSet++) {
//			retArr[i] = String.join(",",ret[i]);
//		}
		return ret;
	}

	/*
	 * not sure if we need this func now. 
	 */
	public boolean checkParamsAvailable() {
		for(int i=0; i<ni.inputIDs.length;i++) {
			if(checkParamValueAvailable(ni.inputIDs[i])==false) {return false;}
		}
		return true;
	}
	
	public String[] getParamPotentialValue(String paramName) {
		Iterator<paramPotentialValues> itr = paramVs.iterator();
		while (itr.hasNext()) {
			paramPotentialValues pv = itr.next();
			if(paramName.equals(pv.paramName)) {return pv.potentialValues;}
		}
		return null;
	}

	public boolean checkParamValueAvailable(String paramName) {
		//todo: replace it with lambda expression.
		Iterator<paramPotentialValues> itr = paramVs.iterator();
		while (itr.hasNext()) {
			if(paramName.equals(itr.next().paramName)) {return true;}
		}
		return false;
	}
	
	
	public String[] getTypePotentialValues(String type) {
		/*
		 * now we only initialize one dimensional array. 
		 */
		//System.out.println("type"+ type);
		String[] returnStrArr = null;
		if(type.equals("int"))returnStrArr = new String[] {"1","0","-1"};
		if(type.equals("String"))returnStrArr = new String[] {"\"test\"","\"1111111111\"","\"file_dir_tobefilled_later\""};
		if(type.equals("boolean"))returnStrArr = new String[] {"true","false"};
		if(type.equals("short"))returnStrArr = new String[] {"1","0","2"};
		if(type.equals("long"))returnStrArr = new String[] {"1","0","-1"};
		if(type.equals("float"))returnStrArr = new String[] {"1","0","-1"};
		if(type.equals("double"))returnStrArr = new String[] {"1.0","0.0","-1.0"};
		if(type.equals("byte"))returnStrArr = new String[] {"(byte)0xba", "(byte)0x0d","(byte)0x45", "(byte)0x25"};
		if(type.equals("char"))returnStrArr = new String[] {"1","0","a"};
			
		if(type.equals("int[]"))returnStrArr = new String[] {"new int[]{\"1\",\"0\",\"-1\"}","new int[]{\"-1\",\"0\",\"1\"}","new int[]{\"255\",\"461\",\"-123\"}"};
		if(type.equals("String[]")) {
			//System.out.println("string array detected"+type);
			returnStrArr = new String[] {
				"new String[]{\"test\",\"1111111111\",\"file_dir_tobefilled_later\"}",
				"new String[]{\"test\",\"1111111111\",\"file_dir_tobefilled_later\"}",
				"new String[]{\"test\",\"1111111111\",\"file_dir_tobefilled_later\"}"
				};
		}
		if(type.equals("boolean[]"))returnStrArr = new String[] {"new boolean[]{\"true\",\"true\"}","new boolean[]{\"false\",\"true\"}","new boolean[]{\"false\",\"false\"}"};
		if(type.equals("short[]"))returnStrArr = new String[] {"new short[]{1,0,-1}","new short[]{-1,0,1}","new short[]{255,461,-123}"};
		if(type.equals("long[]"))returnStrArr = new String[] {"new long[]{1,0,-1}","new long[]{-1,0,1}","new long[]{255,461,-123}"};
		if(type.equals("float[]"))returnStrArr = new String[] {"new float[]{\"1\",\"0\",\"-1\"}","new float[]{\"-1\",\"0\",\"1\"}","new float[]{\"255\",\"461\",\"-123\"}"};
		if(type.equals("double[]"))returnStrArr = new String[] {"new double[]{\"1.0\",\"0.0\",\"-1.0\"}","new double[]{\"-1.0\",\"0.0\",\"1\"}","new double[]{\"255.0\",\"461.0\",\"-123.0\"}"};
		if(type.equals("byte[]"))returnStrArr = new String[] {"\"Any String you want\".getBytes()","new byte[]{ (byte)0x80, 0x53, 0x1c," + 
				" (byte)0x87, (byte)0xa0, 0x42, 0x69, 0x10, (byte)0xa2, (byte)0xea, 0x08," + 
				" 0x00, 0x2b, 0x30, 0x30, (byte)0x9d }"};
		if(type.equals("char[]"))returnStrArr = new String[] {"\"abcdefg\".toCharArray()","\"test char array\".toCharArray()"};
		if(type.equals("bitmap")) {
			returnStrArr = new String[] {"bitmap1","bitmap2"};
			// need to setup path 1. 
			//need to move bitmap file to resource folder!!!!
			prefixDeclaration += "BitmapFactory.Options options = new BitmapFactory.Options();\n" + 
					"options.inPreferredConfig = Bitmap.Config.ARGB_8888;\n" + 
					"Bitmap bitmap1 = BitmapFactory.decodeFile(\"path1\", options);\n" + 
					"Bitmap bitmap2 = BitmapFactory.decodeFile(\"path2\", options);\n";
		}
		return returnStrArr;
	}
	

	/*
	 * set all possible values to String[]
	 * 
	 * however, one problem here is that, how to indicate the possibleValue is the execution result of a previous native invocation?
	 */
	public void setParamValue(String paramName, String[] possibleValues) {
		paramPotentialValues paramV = new paramPotentialValues(paramName, possibleValues);
		paramVs.add(paramV);
	}


	public class paramPotentialValues{
		public String paramName;
		public String paramType; // will extend this later. 
		public String[] potentialValues; 

		//direct copy... will it cause memory issue?
		public paramPotentialValues(String name, String[] values) {
			this.paramName = name;
			this.potentialValues = values;
		}
	}

}