package piglet.piglet2spiglet;

public class PExp//表达式类
{
	public static final int TEMP = 3, SIMPLE = 1;//临时变量标志，简单表达式标志
	
	private boolean bTemp, bSimple;//是否临时变量，是否简单表达式
	private String szExp;//表达式
	
	public PExp(String szExp)//仅表达式
	{
		this.szExp = szExp;
	}
	
	public PExp(String szExp, int nFlag)//含表达式与标志
	{
		this.szExp = szExp;
		this.bTemp = (nFlag & 2) == 2;
		this.bSimple = (nFlag & 1) == 1;		
	}
	
	public String toString()//输出表达式
	{
		return szExp;
	}
	
	public boolean isTemp()//是否临时变量
	{
		return bTemp;
	}
	
	public boolean isSimple()//是否简单表达式
	{
		return bSimple;
	}
	
	public void append(String szNewExp)//附加一个字符串
	{
		if (szNewExp == null) return;
		if (szExp == null) szExp = szNewExp;
		else szExp += szNewExp;
	}
	
	public void append(PExp newExp)//附加一个表达式
	{
		if (newExp == null) return;
		if (szExp == null)
		{
			szExp = newExp.szExp;
			bTemp = newExp.bTemp;
			bSimple = newExp.bSimple;
		}
		else append(newExp.szExp);
	}
}
