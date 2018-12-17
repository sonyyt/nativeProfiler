package seLab.maven4JDT;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jboss.forge.roaster.Problem;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;


public class codeGenerator{
	public String tempFile = "/data/nativeProfiler/temp/MainActivity.java.template";
	public JavaClassSource MainActivityClass;
	private String projectPackage;
	private ArrayList<nativeInterface> interfaceArrayList = new ArrayList<nativeInterface>();
	private ArrayList<String> classArrayList = new ArrayList<String>();
	private String templateClassDir;
	private String templateClassContent;
	public String nativeDeclarationClass;
	private String outputDir;
	
	public codeGenerator() {
		templateClassDir = tempFile;
		try {
			templateClassContent = new String(Files.readAllBytes(Paths.get(templateClassDir)));
			nativeDeclarationClass = new String("package #packageNeedle#; public class #classnameNeedle# {"
					+ " static { #loadLibraryNeedle# }   "
					+ " public	#classnameNeedle#(){}"
					+ " #nativeInterfaceDeclarationNeedle#"
					+ "}");
			//System.out.println(templateClassContent);
		} catch (Exception e) {
			//System.out.println("error loading template"+e.toString());
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getNIAmount() {
		return this.interfaceArrayList.size();
	}
	
	/*
	 * parameter: 
	 * String templateClass: contains loadLibraryNeedle;
	 * String[] libraries: native-lib (.so not included in the string)
	 * 
	 * need to handle exception: what if needle is not found? 
	 */
	public void addNativeLibraries(String[] libraries) {
		String libraryLoadRequest = "";
		for(int i=0; i<libraries.length;i++) {
			String tmp = libraries[i];
			if(tmp.startsWith("lib")) {
				tmp = tmp.substring(3);
			}
			libraryLoadRequest += "System.loadLibrary(\""+tmp+"\");\n";
		}
		//templateClassContent=templateClassContent.replace("#loadLibraryNeedle#", libraryLoadRequest);
		nativeDeclarationClass = nativeDeclarationClass.replace("#loadLibraryNeedle#", libraryLoadRequest);
	}

	
	
	public void setOutputDir(String outputDir) {
		if(!outputDir.endsWith("/")) {
			outputDir = outputDir + "/";
		}
		this.outputDir = outputDir;
	}
	/*
	 * read the Template file of the code generator; 
	 * 
	 */
	public void loadTemplateClass() {
//		List<Problem> problem = Roaster.validateSnippet(templateClassContent);
//		if(problem.size()!=0) {
//			System.out.println("Error Loading Class"+problem.toString());
//			System.exit(-1);
//		}	
		MainActivityClass = Roaster.parse(JavaClassSource.class, templateClassContent);			
	}
	
	public void startProcessPackage(String packageName) {
		this.projectPackage = packageName;
		templateClassContent=templateClassContent.replace("#packageNeedle#", packageName);
		this.loadTemplateClass();
		//MainActivityClass.setPackage(packageName);
	}
	
	public void registerInterface(nativeInterface ni) {
		interfaceArrayList.add(ni);
		classArrayList.add(ni.classNameWithPackage);
		//System.out.print(ni.getPackageName());
	}
	
	public boolean hasValidInterfaces() {
		if(this.interfaceArrayList.size()!=0) {
			return true;
		}
		return false;
	}
	
	/*
	 * todo: example whether the generated code snippet is valid; 
	 */
	
	public boolean isValidate() {
//		List<Problem> problem = Roaster.validateSnippet(MainActivityClass.getInternal().toString());
//		if(problem.size()!=0) {
//			System.out.println("Error Loading Class"+problem.toString());
//			//System.exit(-1);
//			return true;
//		}	
		return true;
	}
	
	//normally, how should we setup this?
	// ignore this for now
	public void setInterfaceHappenBeforeRules() {}
	
	//output to outputDir. each class as a file. 
	public String[] generateDeclaration() {
		//step 1: generate String of declaration; 
		String[] declaration = new String[interfaceArrayList.size()];
		int i = 0;
		for(nativeInterface ni:interfaceArrayList) {
			declaration[i] = ni.getDeclaration()+"\n";
			i++;
		}
		return declaration;
		//step 2: insert declaration into main activity;
		
	}
	
	//output to outputDir. each class as a file. 
	public void generateDeclarationFiles() {
		classArrayList  = new ArrayList<String>(new HashSet<String>(classArrayList));
		
		for(String classWithPackage:classArrayList) {
			//System.out.println(classWithPackage);
			//1. generate dir and file. 
			String[] tmp = classWithPackage.split("\\.");
			String packagePath = outputDir;
			for(int i =0; i<tmp.length-1; i++) {
				packagePath += tmp[i] + "/";
			}
			new File(packagePath).mkdirs();
			
			String classStr = nativeDeclarationClass;
			classStr =  classStr.replace("#classnameNeedle#",tmp[tmp.length-1]);
			classStr =  classStr.replace("#packageNeedle#", classWithPackage.replace("."+tmp[tmp.length-1], ""));
			
			String jniDeclaration = "";
			for(nativeInterface ni:interfaceArrayList) {
				if(ni.classNameWithPackage.equals(classWithPackage)) {
					jniDeclaration += ni.getDeclaration()+"\n";
				}
			}
			
			classStr =  Roaster.format(classStr.replace("#nativeInterfaceDeclarationNeedle#", jniDeclaration));
			
			String generatedClassPath = packagePath+tmp[tmp.length-1]+".java";
			
			//System.out.println("Generating class file:"+generatedClassPath);
	        try (PrintStream out = new PrintStream(new FileOutputStream(generatedClassPath))) {
	            out.print(classStr);
	        } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("File not generated, stop execution" + classWithPackage);
				//System.exit(-1);
			}
		}
	}
	
	//need to consider how to initialize each native class. 
	
	public String generateInvocations() {
		String invocation = "";
		
		for(String classWithPackage : classArrayList) {
			//System.out.println(classWithPackage);
			//System.out.println(MainActivityClass.getInternal());
			MainActivityClass.addImport(classWithPackage);
			String[] tmp = classWithPackage.split("\\.");
			String className = tmp[tmp.length-1];
			invocation += className+" "+className+"Handler"+" = new "+className+"();\n";
		}
		
		for(nativeInterface ni:interfaceArrayList) {
			nativeInvocationGenerator nig = new nativeInvocationGenerator(ni);
			nig.fillDefaultValuesByType();
			invocation += nig.prefixDeclaration;
			String[][] testcases = nig.generateTestCases();
			for(int i=0;i<testcases.length;i++) {
				invocation += ni.getInvocation(testcases[i])+"\n";	
			}
		}
		return invocation;
		
	}
	
	public void autoGenerateTestAtDir(String outputdir) {
		//String[] declaration = this.generateDeclaration();
		
		this.setOutputDir(outputdir);
		
		this.generateDeclarationFiles();
		
		String invocation = this.generateInvocations();
		
		//System.out.println(invocation);
//    	for(int i=0; i<declaration.length;i++) {
//    		MainActivityClass.addMethod(declaration[i]);
//    	}
    	
    	MainActivityClass.getMethod("runTestCases").setBody(invocation);
    	
    	String mainActivityClassContent = Roaster.format(MainActivityClass.getInternal().toString());
    	

    	
		String packagePath = this.outputDir + this.projectPackage.replace(".", "/") + "/";

		new File(packagePath).mkdirs();		
        try (PrintStream out = new PrintStream(new FileOutputStream(packagePath+"MainActivity.java"))) {
            out.print(mainActivityClassContent);
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("MainActivity.java not generated, stop execution");
			//System.exit(-1);
		}
	}

	
}