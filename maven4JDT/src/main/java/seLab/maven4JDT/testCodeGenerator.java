package seLab.maven4JDT;


/**
 * Hello world!
 *
 */
public class testCodeGenerator{
	
    public static void main( String[] args )
    {   
    	codeGenerator cg = new codeGenerator();
    	
    	cg.addNativeLibraries(new String[] {"test", "test1"});
    	
    	cg.startProcessPackage("pkg.com.test");
		
		nativeInterface tmpNI_1 = new nativeInterface();
		
		nativeInterface tmpNI_2 = new nativeInterface();
		
		nativeInterface tmpNI_3 = new nativeInterface();
		
		tmpNI_1.set("pk.com.apitest.cn", "testMethodName", new String[] {"I","D"}, new String[] {"p1","p2"}, "V");
		
		tmpNI_2.set("pk.test.api.classname", "testMethodName", new String[] {"I","D"}, new String[] {"p1","p2"}, "V");
		
		tmpNI_3.set("pk.com.apitest.cn", "testMethodName1", new String[] {"I","D"}, new String[] {"p1","p2"}, "V");
		
		cg.registerInterface(tmpNI_1);
		cg.registerInterface(tmpNI_2);
		cg.registerInterface(tmpNI_3);
		
		
		cg.autoGenerateTestAtDir("/data/nativeProfiler/temp/");
		
			
    }
}