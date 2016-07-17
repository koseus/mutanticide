
import java.io.*;
import java.lang.invoke.VolatileCallSite;

import org.objectweb.asm.*;

public class Main {

    public static void main(String[] args) throws IOException {
		String className = args[0];

    	InputStream in = Main.class.getResourceAsStream(className + ".class");
        ClassReader classReader = new ClassReader(in);

        // Traverse the class and its methods
        ClassTraverser cv = new ClassTraverser(className);
        classReader.accept(cv, 0);
        in.close();

        for(int i = 0; i < cv.syncBlocks.size(); i++){
	        	EXCRMutation excr = new EXCRMutation(cv.syncBlocks.get(i).className, cv.syncBlocks.get(i).methodName, cv.syncBlocks.get(i).lineNumber);
	        	try{
	    	   		excr.EXCR();
        		}catch (Exception e){}
	       
	        SKCRMutation skcr = new SKCRMutation(cv.syncBlocks.get(i).className, cv.syncBlocks.get(i).methodName, cv.syncBlocks.get(i).lineNumber);
	        try{
	        		skcr.SKCR();
	        }catch (Exception e){}
	        	
	        	SHCRMutation shcr = new SHCRMutation(cv.syncBlocks.get(i).className, cv.syncBlocks.get(i).methodName, cv.syncBlocks.get(i).lineNumber);
	        	try{
	        		shcr.SHCR();
	        	}catch (Exception e){}
	        	
	        	SPCRMutation spcr = new SPCRMutation(cv.syncBlocks.get(i).className, cv.syncBlocks.get(i).methodName, cv.syncBlocks.get(i).lineNumber);
	        	try{
	        		spcr.SPCR();
	        	}catch (Exception e){}
	        	
	        	RSBMutation rsb = new RSBMutation(cv.syncBlocks.get(i).className, cv.syncBlocks.get(i).methodName, cv.syncBlocks.get(i).lineNumber);
	        	try{
	        		rsb.RSB();
	        	}catch (Exception e){}
        }
        
        for(int i = 0; i < cv.methodSleep.size(); i++){        	
	        	RTXCMutation rtxc = new RTXCMutation(className, cv.methodSleep.get(i).methodName, cv.methodSleep.get(i).lineNumber);
	        	try{
	        		rtxc.RTXC();
	        	} catch(Exception e) {}
	        	
	        	MXTMutation mxt = new MXTMutation(className, cv.methodSleep.get(i).methodName, cv.methodSleep.get(i).lineNumber);
	        	try{
	        		mxt.MXT();
	        	} catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.methodWait.size(); i++){
	        	RTXCMutation rtxc = new RTXCMutation(className, cv.methodWait.get(i).methodName, cv.methodWait.get(i).lineNumber);
	        	try {
				rtxc.RTXC();
			} catch (Exception e) {}
	        	
	        	MXTMutation mxt = new MXTMutation(className, cv.methodWait.get(i).methodName, cv.methodWait.get(i).lineNumber);
	        try{
	        		mxt.MXT();
	        } catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.methodJoin.size(); i++){
	        	RTXCMutation rtxc = new RTXCMutation(className, cv.methodJoin.get(i).methodName, cv.methodJoin.get(i).lineNumber);
	        	try{
	        		rtxc.RTXC();
	        	} catch(Exception e) {}
	        	
	        	MXTMutation mxt = new MXTMutation(className, cv.methodJoin.get(i).methodName, cv.methodJoin.get(i).lineNumber);
	        	try{
	        		mxt.MXT();
	        	} catch(Exception e) {}
	        	
	        	RJSMutation rjs = new RJSMutation(className, cv.methodJoin.get(i).methodName, cv.methodJoin.get(i).lineNumber);
	        	try{
	        		rjs.RJS();
	        	} catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.methodYield.size(); i++){
	        	RTXCMutation rtxc = new RTXCMutation(className, cv.methodYield.get(i).methodName, cv.methodYield.get(i).lineNumber);
	        	try{
	        		rtxc.RTXC();
	        	} catch(Exception e) {}
        }
        
//        for(int i = 0; i < cv.methodNotify.size(); i++){
//	       
//        }
        
//        for(int i = 0; i < cv.methodNotifyAll.size(); i++){
//	        	
//        }
        
        for(int i = 0; i < cv.synchronizedMethods.size(); i++){
	        	RSKMutation rsk = new RSKMutation(cv.synchronizedMethods.get(i).className, cv.synchronizedMethods.get(i).methodName);
	        	try{
	        		rsk.RSK();
	        	} catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.nonSynchronizedMethods.size(); i++){        	
	        	ASKMutation ask = new ASKMutation(cv.nonSynchronizedMethods.get(i).className, cv.nonSynchronizedMethods.get(i).methodName);
	        	try{
	        		ask.ASK();
	        	} catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.nonStaticMethods.size(); i++){        	
	        	ASTKMutation astk = new ASTKMutation(cv.nonStaticMethods.get(i).className, cv.nonStaticMethods.get(i).methodName);
	        try{
	        		astk.ASTK();
	        } catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.staticMethods.size(); i++){        	
	        	RSTKMutation rstk = new RSTKMutation(cv.staticMethods.get(i).className, cv.staticMethods.get(i).methodName);
	        	try{
	        		rstk.RSTK();
	        	} catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.volatileFields.size(); i++){
	        	RVKMutation rvk = new RVKMutation(cv.volatileFields.get(i).className, cv.volatileFields.get(i).fieldName);
	        try{
	        		rvk.RVK();
	        } catch(Exception e) {}
        }
        
        for(int i = 0; i < cv.methodNotifyAll.size(); i++){
        		RNANMutation rnan = new RNANMutation(cv.methodNotifyAll.get(i).className, cv.methodNotifyAll.get(i).methodName, cv.methodNotifyAll.get(i).lineNumber);
        		try{
        			rnan.RNAN();
        		} catch(Exception e) {}
        		
        		RTXCMutation rtxc = new RTXCMutation(cv.methodNotifyAll.get(i).className, cv.methodNotifyAll.get(i).methodName, cv.methodNotifyAll.get(i).lineNumber);
	        	try{
	        		rtxc.RTXC();
	        	} catch(Exception e) {}
    }
        
        for(int i = 0; i < cv.methodNotify.size(); i++){
	        	RNNAMutation rnna = new RNNAMutation(cv.methodNotify.get(i).className, cv.methodNotify.get(i).methodName, cv.methodNotify.get(i).lineNumber);
	        	try{
	        		rnna.RNNA();
	        	}catch (Exception e){
//	        		System.out.println(e.getMessage());
	        	}
	        	
	         RTXCMutation rtxc = new RTXCMutation(className, cv.methodNotify.get(i).methodName, cv.methodNotify.get(i).lineNumber);
	        	try{
	        		rtxc.RTXC();
	        	} catch(Exception e) {}
        }
    }
}