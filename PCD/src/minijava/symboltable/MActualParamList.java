package minijava.symboltable;

import java.util.Vector;

public class MActualParamList extends MIdentifier//实参列表
{
	protected Vector<String> paramList = new Vector<String>();//保留所有参数数据类型
	protected Vector<MPiglet> pigletList = new Vector<MPiglet>();//保存所有参数中间代码
	
	private int nLine;//列表所在行号
	
	public MActualParamList(int nLine, MIdentifier parent)
	{
		super(null, null, parent);
		this.nLine = nLine;
	}
	
	public void addParam(String szType)//添加参数（仅类型）
	{
		paramList.add(szType);
	}
	
	public MVar getVar(String szName)//在包含自己的标识符中查找是否有这个变量，以免未定义
	{		
		return parent.getVar(szName);
	}
	
	public int getLine()//获取行号
	{ 
		return nLine;
	}
	
	public void addPiglet(MPiglet piglet)//加入中间代码
	{
		pigletList.add(piglet);
	}
	
	public Vector<MPiglet> getPiglets()//获取所有中间代码列表
	{
		return pigletList;
	}
}
