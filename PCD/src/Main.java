import java.io.ByteArrayInputStream;
import java.util.Vector;

import kanga.KangaParser;
import kanga.visitor.ConvertToMipsVisitor;

import piglet.PigletParser;
import piglet.visitor.Convert2SPigletVisitor;
import piglet.visitor.CountTempVisitor;
import spiglet.SpigletParser;
import spiglet.symboltable.SMethod;
import spiglet.visitor.CreateFlowGraphVisitor;
import spiglet.visitor.RegisterAllocatorVisitor;
import minijava.MiniJavaParser;
import minijava.symboltable.MClassList;
import minijava.symboltable.MIdentifier;
import minijava.visitor.BuildSymbolTableVisitor;
import minijava.visitor.Convert2PigletVisitor;
import minijava.visitor.TypeCheckVisitor;

public class Main {

	public static void main(String[] args)
	{
		try 
		{
			minijava.syntaxtree.Node mjRoot = new MiniJavaParser(System.in).Goal();//读取输入流
			
			MClassList my_classes = new MClassList();//符号表
			MIdentifier.classList =  my_classes;//设置静态变量，类型表
			
			mjRoot.accept(new BuildSymbolTableVisitor(), my_classes);//建符号表
			mjRoot.accept(new TypeCheckVisitor(), my_classes);//其他类型检查
			
			if (my_classes.hasTypeErrors())//如果有类型错误
			{
				my_classes.printErrors(System.err);//输出错误
				return;
			}
			
			String szPg = mjRoot.accept(new Convert2PigletVisitor(), my_classes).toString();//转换为中间代码			
			
			ByteArrayInputStream pgStream = new ByteArrayInputStream(szPg.getBytes());//转换为输入流
			piglet.syntaxtree.Node pgRoot = new PigletParser(pgStream).Goal();//读取输入流
			pgStream.close();
			
			CountTempVisitor vc = new CountTempVisitor();
			pgRoot.accept(vc);//计算Temp个数

			Convert2SPigletVisitor va = new Convert2SPigletVisitor(vc.getNextTemp());
			pgRoot.accept(va, null);
			
			String szSpg = va.getCode();//获取中间代码
			
			ByteArrayInputStream spgStream = new ByteArrayInputStream(szSpg.getBytes());//转换为输入流
			spiglet.syntaxtree.Node spgRoot = new SpigletParser(spgStream).Goal();//读取输入流
			spgStream.close();
			
			String szKg = "";
			
			CreateFlowGraphVisitor vd = new CreateFlowGraphVisitor();
			spgRoot.accept(vd, null);
			
			Vector<SMethod> lstMethods = vd.getMethods();
			for (SMethod method : lstMethods)
			{
				RegisterAllocatorVisitor vr = new RegisterAllocatorVisitor(method);				
				szKg += vr.genCode() + "\n";//获取中间代码
			}
			
			ByteArrayInputStream kgStream = new ByteArrayInputStream(szKg.getBytes());//转换为输入流
			kanga.syntaxtree.Node kgRoot = new KangaParser(kgStream).Goal();			
			kgStream.close();
			
			ConvertToMipsVisitor v = new ConvertToMipsVisitor();
			
			kgRoot.accept(v);
			System.out.println(v);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}