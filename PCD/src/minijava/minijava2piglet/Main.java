package minijava.minijava2piglet;

import minijava.MiniJavaParser;
import minijava.ParseException;
import minijava.TokenMgrError;
import minijava.symboltable.MClassList;
import minijava.symboltable.MIdentifier;
import minijava.syntaxtree.Node;
import minijava.visitor.BuildSymbolTableVisitor;
import minijava.visitor.Convert2PigletVisitor;
import minijava.visitor.TypeCheckVisitor;


public class Main { 
 
    public static void main(String[] args) {
    	try {
    		Node root = new MiniJavaParser(System.in).Goal();
    		/*
    		 * TODO: Implement your own Visitors and other classes.
    		 * 
    		 */
    		MClassList my_classes = new MClassList();
    		MIdentifier.classList =  my_classes;//���þ�̬���������ͱ�
    		
    		//Traverse the Abstract Grammar Tree
    		root.accept(new BuildSymbolTableVisitor(), my_classes);//�����ű�
    		root.accept(new TypeCheckVisitor(), my_classes);//�������ͼ��
    		
    		if (my_classes.hasTypeErrors())//��������ʹ���
    		{
    			my_classes.printErrors(System.err);//�������
    			return;
    		}
    		
    		String szPiglet = root.accept(new Convert2PigletVisitor(), my_classes).toString();//ת��Ϊ�м����    		
    		System.out.println(szPiglet);
		
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