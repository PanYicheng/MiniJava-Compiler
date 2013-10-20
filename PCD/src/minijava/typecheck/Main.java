package minijava.typecheck;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import minijava.MiniJavaParser;
import minijava.ParseException;
import minijava.TokenMgrError;
import minijava.symboltable.MClassList;
import minijava.symboltable.MIdentifier;
import minijava.syntaxtree.Node;
import minijava.visitor.BuildSymbolTableVisitor;
import minijava.visitor.TypeCheckVisitor;


public class Main { 
 
    public static void main(String[] args) {
    	try
    	{
    		//FileInputStream fis = new FileInputStream("Test.java");
    		Node root = new MiniJavaParser(System.in).Goal();
    		//初始化符号表中最大的类
    		//fis.close();
			
    		MClassList my_classes = new MClassList();
    		MIdentifier.classList =  my_classes;//设置静态变量，类型表
    		
    		//Traverse the Abstract Grammar Tree
    		root.accept(new BuildSymbolTableVisitor(), my_classes);//建符号表
    		root.accept(new TypeCheckVisitor(), my_classes);//其他类型检查
    		
    		//FileOutputStream fos = new FileOutputStream("output.txt");
    		//PrintStream ps = new PrintStream(fos);
    		
    		my_classes.printErrors(System.err);//输出所有错误
    		
    		//ps.close();
    		//fos.close();
    		
    		//System.out.println(my_classes);
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