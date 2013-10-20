package minijava.symboltable;

public class MVar extends MIdentifier//变量标识符
{	
	public MVar(String szName, String szType, MIdentifier parent)
	{
		super(szName, szType, parent);
	}
	
	public String toString()
	{
		return szType + " " + szName;//打印，便于输出类-方法-变量的层次结构 		
	}
}
