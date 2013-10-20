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
    		//��ʼ�����ű���������
    		//fis.close();
			
    		MClassList my_classes = new MClassList();
    		MIdentifier.classList =  my_classes;//���þ�̬���������ͱ�
    		
    		//Traverse the Abstract Grammar Tree
    		root.accept(new BuildSymbolTableVisitor(), my_classes);//�����ű�
    		root.accept(new TypeCheckVisitor(), my_classes);//�������ͼ��
    		
    		//FileOutputStream fos = new FileOutputStream("output.txt");
    		//PrintStream ps = new PrintStream(fos);
    		
    		my_classes.printErrors(System.err);//������д���
    		
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