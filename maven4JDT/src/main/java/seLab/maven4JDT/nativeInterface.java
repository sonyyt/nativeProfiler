package seLab.maven4JDT;

import java.util.Arrays;

/*
	 * nativeInterface: inner class for JNI native interfaces; 
	 * 
	 * 1. to specify a nativeInterface
	 * 2. to generate the declaration of a native Interface; 
	 * 3. to generate the call of a native interface?
	 */


/*
 * potential input types: (obtain from native layer, we will consider return types later)
 * acceptable patterns: 
 * 1) J, I, D, B, Z, F, S, C
 * 2) Ljava/lang/+String, Integer, Long,  (Object, Class, cannot process )
 * 3) [, [[, [[[ + 1) or 2)
 * 
 * with certain meaning and can be processed later: 
 *  1) java/util/Map, java/util/List, java/util/ArrayList, java/util/concurrent/atomic/AtomicInteger, java/util/HashMap, java/util/set
 *  2) java/nio/ByteBuffer, java/io/FileDescriptor; java/io/InputStream
 *  3) android/content/Context, android/content/res/AssetManager
 *  4) android/graphics/Bitmap, android/graphics/PointF, android/graphics/Canvas, android/graphics/RectF, android/view/Surface, android/os/IBinder
 *  5) com/viber/jni/
 *  
 *  basic type mapping rules:
 *  Z: boolean; 
 *  B: Byte;
 *  C: Char;
 *  S: Short;
 *  I: int;
 *  J: long; 
 *  F: float; 
 *  D: double; 
 *  V: void
 * 
 * 
 */
public class nativeInterface{
	public String classNameWithPackage;
	public String objectHandler;
	public String funcName;
	public String[] inputTypes;
	public String[] inputIDs;
	public String returnType;

	/*
	 * nativeInterface: need a mapping from parameterTypes to actual types.
	 * parameterIDs: sequentially add parameters;  
	 */
	public boolean set(String cname, String interfaceName, String[] parameterTypes, String[] parameterIDs, String rtType){
		classNameWithPackage = cname;
		String[] tmp = classNameWithPackage.split("\\.");
		String className = tmp[tmp.length-1];
		objectHandler = className+"Handler";
		
		funcName = interfaceName;
		inputTypes = new String[parameterTypes.length];
				
		for(int i=0; i<parameterTypes.length;i++) {
			String type = typeMapping(parameterTypes[i]);
			if(type==null) {
				System.out.println("type cannot be processed:"+parameterTypes[i]);
				return false;
			}
			inputTypes[i] = type;
		}
		
		returnType = typeMapping(rtType);
		if(returnType==null) {
			System.out.println("return type cannot be processed:"+returnType);
			return false;
		}
		inputIDs = parameterIDs;
		// error checking: 1. input type = input id; 2. 
		if(inputTypes.length!=inputIDs.length) {
			System.out.println("input not correct: input types and input id have different amount");
			return false;
		}
		return true;
	}
	
	public String getClassName() {
		String[] tmp = this.classNameWithPackage.split("\\.");
		return tmp[tmp.length-1];
		
	}
	
	public String getPackageName() {
		String[] tmp = this.classNameWithPackage.split("\\.");
		String packageName = "";
		for(int i =0; i<tmp.length-1; i++) {
			packageName += "."+tmp[i];
		}
		return packageName.substring(1);
		
	}


	/*
	 * getDeclaration: based on the interfaceInvocation, generate interface declaration; 
	 * param: null;
	 * return: declaration string (public native String xxxx(string);
	 * 
	 * Question: input parameter name related to native code?
	 * 
	 */
	public String getDeclaration() {
		String[] params = new String[inputTypes.length];
		for(int i=0; i<inputTypes.length; i++) {
			params[i] = inputTypes[i]+" "+inputIDs[i];
		}

		return "public native "+returnType+" "+funcName+"("+String.join(",", params)+");";
	}

	/*
	 * getInvocation: get invocation string by filling in parameters; 
	 * 
	 * Question: how?
	 * 
	 * potential solution 1: type cast from bytes[] to target type ( int, boolean, String, object?)
	 * potential solution 2: just add reference to an object?
	 * potential solution 3: setParameter one by one?
	 *  
	 * final solution: it is just generating string!
	 */
	public String getInvocation(String[] inputs){
		return objectHandler+"."+funcName+"("+String.join(",", inputs)+");";

	}

	/*
	 * getInvocationWithReturn: generate invocation with the execution result saved to output;
	 */


	public String getInvocationWithReturn(String[] inputs, String output) {
		//todo: check return type != void
		return returnType+" "+output + " = " +  objectHandler+"."+funcName+"("+String.join(",", inputs)+");";
	}

	/*
	 * typeMapping: static, convert an smali type (or say, a possibly non-java type) to a java type 
	 * I guess that it should be moved to a utility class. 
	 * step 1: remove []
	 * step 2: how many [ does it contain:
	 * step 3:  * 1) J, I, D, B, Z, F, S, C; 2) Ljava/lang/+String, Integer, Long,
	 */

	public String typeMapping(String input){
		//String type = input.trim().substring(1,input.length()-1);
		//System.out.println(input);
		if(input.startsWith("L")) {
			input = input.substring(1);
		}
		if(input.endsWith(";")) {
			input = input.substring(0, input.length()-1);
		}
		String type = input;
		String typeWOArr = type.replace("[", "");
		int arrDimension = type.length() - typeWOArr.length();
		String returnType = null;
		
		switch(typeWOArr) {
			case "Z": returnType = "boolean"; break;
			case "B": returnType = "byte";   break;//need to consider this.
			case "C": returnType = "char"; break;
			case "S": returnType = "short"; break; 
			case "I": returnType = "int"; break;
			case "V": returnType = "void"; break;
			case "J": returnType = "long";break;
			case "F": returnType = "float"; break;
			case "D": returnType = "double"; break;
			case "java/lang/String": returnType = "String"; break;
			case "java/lang/Integer": returnType = "int"; break;
			case "java/lang/Long": returnType = "long"; break;
			case "android/graphics/Bitmap": returnType="bitmap"; break;
			default: return null;
		}
		
		
		if(arrDimension!=0) {
			/*
			 * Temporary solution: now we only accept one dimension array;
			 * todos: extend nativeInvocationGenerator getTypePotentialValues;
			 */
			if(arrDimension>1) {
				return null;
			}
			for(int i=0; i<arrDimension; i++) {
				returnType += "[]";
			}
		}
		
		return returnType;

	}
}