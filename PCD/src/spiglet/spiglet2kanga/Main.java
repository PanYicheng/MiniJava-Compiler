package spiglet.spiglet2kanga;

import java.util.Vector;

import spiglet.ParseException;
import spiglet.SpigletParser;
import spiglet.TokenMgrError;
import spiglet.symboltable.SMethod;
import spiglet.syntaxtree.Node;
import spiglet.visitor.CreateFlowGraphVisitor;
import spiglet.visitor.GJDepthFirst;
import spiglet.visitor.RegisterAllocatorVisitor;

public class Main { 
 
    public static void main(String[] args) {
    	try {
    		Node root = new SpigletParser(System.in).Goal();
    		/*
    		 * TODO: Implement your own Visitors and other classes.
    		 * 
    		 */
    		CreateFlowGraphVisitor vd = new CreateFlowGraphVisitor();
    		//Traverse the Abstract Grammar Tree
    		root.accept(vd, null);
    		
    		Vector<SMethod> lstMethods = vd.getMethods();
    		for (SMethod method : lstMethods)
    		{
    			RegisterAllocatorVisitor vr = new RegisterAllocatorVisitor(method);    			
    			System.out.println(vr.genCode());
    		}
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