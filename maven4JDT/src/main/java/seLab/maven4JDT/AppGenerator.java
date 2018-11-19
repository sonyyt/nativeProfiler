package seLab.maven4JDT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.roaster.Problem;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MemberSource;
import org.jboss.forge.roaster.model.source.MethodSource;

/**
 * Hello world!
 *
 */
public class AppGenerator
{
	public static String appStoragePath = "/data/apks/gplay/";
	public static String tmpPath = "/data/nativeProfiler/temp/";
	public static String workDirectory = "/data/nativeProfiler/apk_process/";
	public static String generatedAPPDirectory = "/data/nativeProfiler/apk_generated/";
	public static String smaliDirectory = "/data/smali_process/";
	public static String venvActive = "/data/nativeProfiler/venv-androguard/bin/activate";
	public static String templateAPP = "/data/nativeProfiler/shellAPP";
	

	//round 2: add logger in the build procedure. 
    public static void main( String[] args )
    {    
    
    	boolean testMode  = false;
    	if(args.length==0) {
    		System.out.println("Usage: java xxx apk.name");
    		System.exit(-1);
    	}
    	
    	 // ----------------------Step 1: unzip --------------------------
    	 
    	String apkName = args[0];
    	String apkFile = appStoragePath + apkName;
    	String apkTmpPath = tmpPath + apkName;
    	String apkSmaliDir = workDirectory + apkName + "/";
    	String generatedAPPDir = generatedAPPDirectory + apkName + "/";
    	String findResult = "";
    	//unzip, check if it has .so file 
    	
    	Path apkPath = Paths.get(apkFile);
    	
    	if(!Files.exists(apkPath)) {
    		System.out.println("Usage: apk not exist");
    		System.exit(-1);
    	}else {
    		System.out.println("Processing:"+apkName+"\n");
    	}
    	
    	String[] cmd = { "/bin/sh", "-c", "unzip -oq "+apkFile+" -d "+ apkSmaliDir + "; find "+apkSmaliDir+" -name '*.so'" };
    	Runtime run = Runtime.getRuntime();
    	Process pr;
		try {
			
			pr = run.exec(cmd);
	    	pr.waitFor();
	    	BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	    	String line = "";
	    	while ((line=buf.readLine())!=null) {
	    		findResult += line+"\n";
	    		//System.out.println(line);
	    	}
	    	//System.out.println(executionResult);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		 // need to make decision: 
		 // 1) should we try both x86 and arm?
		 // 2) which one first? more apps run at ARM v.s. x86 has better performance. ! ARM then. !

		
		//System.out.println(findResult);
		
		
		String[] libDirStr = findResult.split("\n");
    	List<String> nativeLibs = new ArrayList<String>();
    	List<String> libDir = new ArrayList<String>();
		for(int i=0; i<libDirStr.length; i++) {
			String[] tmp = libDirStr[i].split("/");
			for(int j=0; j<tmp.length; j++) {
				if(tmp[j].contains(".so")) {
					nativeLibs.add(tmp[j].replace(".so", ""));
				}
			}
		}
		// remove duplicate libs. 
		nativeLibs  = new ArrayList<String>(new HashSet<String>(nativeLibs));
		
		System.out.println(nativeLibs.size()+" native library detected\n");
		
		if(nativeLibs.size()==0) {
			System.exit(0);
		}
				
		//System.out.println(nativeLibs);
		//System.exit(1);
		//seq: arm64-v8a, armeabi-v7a, x86, x86_64, we only need x86 now
		for(String lib:nativeLibs){
			for(String str:libDirStr) {
				//System.out.println(str);
				if(str.contains("/x86/"+lib+".so")){
//					System.out.println(str);
//					System.out.println(lib);
					libDir.add(str); 
//					if(str.substring(0,3).equals("lib")) {
//						libDir.add(str)
//						libDir.add(str.substring(3)); // can cause errors. but anyway, I will just leave it for now. 
//						break;
//					}
				}
			}
		}

		if(nativeLibs.size()!=libDir.size()) {
			System.out.println("x86 .so files are not included in the package!");
			System.exit(-1);
		}
		
	
		//step 2: get JNI call info;
		// the apk file has a x86 .so library:
		
		//update: class name + function name + parameters. 
		// now to do: 
		// 1) when generate code, generate separate classes in a folder. 
		// 2) copy the generated code back. 
		String androguardResult = "";
		String packageName = "";
		List<String> nativeInterfaceStrArr = new ArrayList<String>();
		List<String> permissionStrArr = new ArrayList<String>();
		
		if(testMode){
			 try {
				androguardResult = new String(Files.readAllBytes(Paths.get("/data/JDT/maven/maven4JDT/src/main/java/seLab/maven4JDT/com.ANIMPANOO.beutwatvh.apk.log")));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else {
			String[] cmd_androguard = { "/bin/bash", "-c", "source "+venvActive+" && apkAnalyser.py " + apkFile + "&&  deactivate;" };
			try {
				pr = run.exec(cmd_androguard);
			    pr.waitFor();
			    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			    String line = "";
			    while ((line=buf.readLine())!=null) {
			    	///System.out.println(line);
			    	androguardResult += line + "\n";
			    }
			} catch (Exception e1) {
					// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
			
		//System.out.println(executionResult);
			
		String[] tmp = androguardResult.split("\n");
		for(String aResult : tmp) {
			//System.out.println(aResult);
			if(aResult.contains("package:")) {
				packageName = aResult.replace("package:","");
			}else if(aResult.contains("nativeInterface:")) {
				nativeInterfaceStrArr.add(aResult.replace("nativeInterface:","").replace("\n","").trim());
			}else if(aResult.contains("permission:")) {
				permissionStrArr.add(aResult.replace("permission:","").replace("\n","").trim());
			}else {
				System.out.println("Androguard analysis error output @ AppGenerator Line 180");
			}
		}
		
		//generate MainActivity.java
		// todo: process .R
		
    	codeGenerator cg = new codeGenerator();
    	
    	cg.addNativeLibraries(nativeLibs.toArray(new String[0]));
    		
    	cg.loadTemplateClass();
			
		cg.startProcessPackage(packageName);
		
		System.out.println(nativeInterfaceStrArr.size()+" native interfaces detected\n");
		
		for(String nativeInterfaceStr: nativeInterfaceStrArr) {
			//System.out.println("nativeInterfaceStr:"+nativeInterfaceStr);
			nativeInterfaceStr = nativeInterfaceStr.trim();
			String[] method = nativeInterfaceStr.split("\\|");
			String className = method[0].replace("/", ".");
			if(className.startsWith("L")){
				className = className.substring(1);}
			if(className.endsWith(";")){
				className = className.substring(0,className.length()-1);}
			
			//System.out.println(className);
			
			String methodName = method[1];
			String methodParams = method[2];
			
			String[] paramArr = methodParams.replace("(","").split("\\)");
			String[] paramTypes = null;
			String[] paramNames = null;

			
			//System.out.println("paramTypeStr:"+paramTypeStr);
			if(paramArr[0].isEmpty()) {
				paramTypes = new String[] {};
				paramNames = new String[] {};
			}else if(paramArr[0].contains(" ")) {
				paramTypes=paramArr[0].split(" ");
				paramNames = new String[paramTypes.length];
				for(int i=0;i<paramTypes.length;i++) {
					paramNames[i] = "param"+i;
				}
			}else {
				paramTypes = new String[] {paramArr[0]};
				paramNames = new String[] {"param1"};
				
			}
			String returnTypes = paramArr[1];

			
			nativeInterface tmpNI = new nativeInterface();
			//haven't processed return!!!
			if(tmpNI.set(className, methodName, paramTypes, paramNames, returnTypes)) {
				cg.registerInterface(tmpNI);
			}
			
			
		}
		
		//todo: 1) need to process empty parameter!!!!!!!!!!!! 2) put the generate fake input to the outside layer: 
		
		System.out.println(cg.getNIAmount()+" native interfaces invoked\n");

		if(cg.hasValidInterfaces() && cg.isValidate()) {
			cg.autoGenerateTestAtDir(apkTmpPath);    
	        
			//System.out.println(output);
		}else {
			System.out.println("Code Generation Error, stop execution");
			System.exit(-1);
		}
    	
		
		String javaFolder = generatedAPPDir + "app/src/main/java/";
		String permissionFile = apkTmpPath + "AndroidManifest.xml.bak";
		
		packagePermissionProcessor pms = new packagePermissionProcessor(packageName);
		
		pms.requestPermissions(nativeInterfaceStrArr.toArray(new String[0]));
		
		pms.export(permissionFile);
		
		
//		System.out.println(javaFolder);
//		System.out.println(nativeInterfaceStr);
//		System.exit(1);
		//step 3: auto-modify trigger app 
		// what to do with existing triggering app: 1) remove everything in app/src/main/java/; 2) clean lib/; 3) 
		
		String generateAPK = "mkdir -p "+generatedAPPDir+ ";\n "+
				  " cp -R "+templateAPP+"/* "+ generatedAPPDir + ";\n "+ //create new source code dir 
				  //"find "+generatedAPPDir+" -type f -exec sed -i 's/selab.vt.shellapp/"+packageName+"\\/g';"+ // replace packageName
				  "find "+generatedAPPDir+" -type f -exec sed -i 's/selab.vt.shellapp/"+packageName+"/' {} \\;\n "+
				  "find "+generatedAPPDir+" -type f -exec sed -i 's/selab.vt.nqueenandroid/"+packageName+"/' {} \\;\n " + 
				  "cp -R " + apkTmpPath + "/* " + javaFolder + ";\n" +
				  "cp " + permissionFile + "/* " + generatedAPPDir + "app/src/main/AndroidManifest.xml;\n";
		
		//System.out.println(generateAPK);
		for(String lib:libDir) {
			generateAPK += " cp "+lib+" "+generatedAPPDir+"app/imported-lib/x86/;\n ";
		}
		
		generateAPK += "cd "+generatedAPPDir+";\n"+generatedAPPDir + "gradlew assembleDebug";
		//3.1 copy library to target direction; 
		//3.2 generate MainActivity.java. 
		//3.3 compile?
		
		//System.out.println(generateAPK);
    	String[] cmd_genAPP = { "/bin/sh", "-c", generateAPK};
    	String androidGenerationResult = "";
		try {
			pr = run.exec(cmd_genAPP);
		    pr.waitFor();
		    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		    String line = "";
		    while ((line=buf.readLine())!=null) {
		    	//System.out.println(line);
		    	androidGenerationResult += line + "\n";
		    }
		    if(androidGenerationResult.contains("BUILD SUCCESSFUL")) {
		    	System.out.println("APP Built Successfully");
		    }
		} catch (Exception e1) {
				// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
}
