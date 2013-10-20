package piglet.piglet2spiglet;


import piglet.ParseException;
import piglet.PigletParser;
import piglet.TokenMgrError;
import piglet.syntaxtree.Node;
import piglet.visitor.Convert2SPigletVisitor;
import piglet.visitor.CountTempVisitor;


public class Main { 
 
    public static void main(String[] args) {
    	try {
    		Node root = new PigletParser(System.in).Goal();
    		/*
    		 * TODO: Implement your own Visitors and other classes.
    		 * 
    		 */
    		CountTempVisitor vc = new CountTempVisitor();
    		//Traverse the Abstract Grammar Tree
    		root.accept(vc);//计算Temp个数

    		Convert2SPigletVisitor va = new Convert2SPigletVisitor(vc.getNextTemp());
    		root.accept(va, null);
    		
    		System.out.print(va.getCode());//输出SPiglet代码
    	}
    	catch(TokenMgrError e){
    		//Handle Lexical Errors
    		e.printStackTrace();
    	}
    	catch (ParseException e){
    		//Handle Grammar Errors
    		e.printStackTrace();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
}