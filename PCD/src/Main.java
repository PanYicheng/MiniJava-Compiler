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
			minijava.syntaxtree.Node mjRoot = new MiniJavaParser(System.in).Goal();//��ȡ������
			
			MClassList my_classes = new MClassList();//���ű�
			MIdentifier.classList =  my_classes;//���þ�̬���������ͱ�
			
			mjRoot.accept(new BuildSymbolTableVisitor(), my_classes);//�����ű�
			mjRoot.accept(new TypeCheckVisitor(), my_classes);//�������ͼ��
			
			if (my_classes.hasTypeErrors())//��������ʹ���
			{
				my_classes.printErrors(System.err);//�������
				return;
			}
			
			String szPg = mjRoot.accept(new Convert2PigletVisitor(), my_classes).toString();//ת��Ϊ�м����			
			
			ByteArrayInputStream pgStream = new ByteArrayInputStream(szPg.getBytes());//ת��Ϊ������
			piglet.syntaxtree.Node pgRoot = new PigletParser(pgStream).Goal();//��ȡ������
			pgStream.close();
			
			CountTempVisitor vc = new CountTempVisitor();
			pgRoot.accept(vc);//����Temp����

			Convert2SPigletVisitor va = new Convert2SPigletVisitor(vc.getNextTemp());
			pgRoot.accept(va, null);
			
			String szSpg = va.getCode();//��ȡ�м����
			
			ByteArrayInputStream spgStream = new ByteArrayInputStream(szSpg.getBytes());//ת��Ϊ������
			spiglet.syntaxtree.Node spgRoot = new SpigletParser(spgStream).Goal();//��ȡ������
			spgStream.close();
			
			String szKg = "";
			
			CreateFlowGraphVisitor vd = new CreateFlowGraphVisitor();
			spgRoot.accept(vd, null);
			
			Vector<SMethod> lstMethods = vd.getMethods();
			for (SMethod method : lstMethods)
			{
				RegisterAllocatorVisitor vr = new RegisterAllocatorVisitor(method);				
				szKg += vr.genCode() + "\n";//��ȡ�м����
			}
			
			ByteArrayInputStream kgStream = new ByteArrayInputStream(szKg.getBytes());//ת��Ϊ������
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