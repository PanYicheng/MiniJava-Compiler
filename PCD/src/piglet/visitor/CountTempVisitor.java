package piglet.visitor;

import piglet.syntaxtree.Temp;

public class CountTempVisitor extends DepthFirstVisitor
{
	private int nMaxTemp = -1;//初始时最大TEMP为-1
	public int getNextTemp() { return nMaxTemp + 1; }//返回下个可用TEMP
	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public void visit(Temp n)
	{
		try
		{
			String szInt = n.f1.f0.toString();//获取序号
			int nTemp = Integer.parseInt(szInt);
			
			if (nTemp > nMaxTemp) nMaxTemp = nTemp;//改为最大值
		}
		catch (Exception e) { }
	}
}
