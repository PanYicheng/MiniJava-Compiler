package piglet.visitor;

import piglet.syntaxtree.Temp;

public class CountTempVisitor extends DepthFirstVisitor
{
	private int nMaxTemp = -1;//��ʼʱ���TEMPΪ-1
	public int getNextTemp() { return nMaxTemp + 1; }//�����¸�����TEMP
	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public void visit(Temp n)
	{
		try
		{
			String szInt = n.f1.f0.toString();//��ȡ���
			int nTemp = Integer.parseInt(szInt);
			
			if (nTemp > nMaxTemp) nMaxTemp = nTemp;//��Ϊ���ֵ
		}
		catch (Exception e) { }
	}
}
