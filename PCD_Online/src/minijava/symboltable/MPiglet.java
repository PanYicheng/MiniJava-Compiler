package minijava.symboltable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MPiglet extends MIdentifier//Piglet代码虚拟符号
{
	private static Pattern patternTemp = Pattern.compile("^TEMP \\d+$");//正则表达匹配TEMP号
	private static Pattern patternDigit = Pattern.compile("^\\d+$");//正则表达匹配立即数
	private static Pattern patternLabel = Pattern.compile("^L\\d+");//正则表达匹配标签
	
	private String szCode;//中间代码
	private int nTemp = -1, nDigit = -1;//TEMP编号，立即数内容，-1代表不是
	
	public MPiglet(String szName, String szType, String szCode)
	{
		super(szName, szType, null);
		this.szCode = szCode;
		check();//检查是否为TEMP或立即数
	}
	
	private void check()
	{
		if (szCode == null) return;
		
		Matcher matcher = patternTemp.matcher(szCode);//正则表达匹配TEMP号
		
		if (matcher.find())//如果匹配
		{
			try
			{
				String szNumber = szCode.substring(5);
				nTemp = Integer.parseInt(szNumber);
			}
			catch(Exception e)//不是数字
			{
				nTemp = -1;
			}
			
			return;
		}
		
		matcher = patternDigit.matcher(szCode);//正则表达匹配立即数
		
		if (matcher.find())//如果匹配
		{
			try
			{
				nDigit = Integer.parseInt(szCode);
			}
			catch(Exception e)//不是数字
			{
				nDigit = -1;
			}
		}
	}
	
	public int getTemp()//返回TEMP编号
	{
		return nTemp;
	}
	
	public boolean isTemp()//是否是TEMP
	{
		return nTemp >= 0;
	}
	
	public boolean isDigit()//是否是立即数
	{
		return nDigit >= 0;
	}
	
	public String toString()//输出代码
	{
		return szCode;
	}
	
	public void append(String szNewCode)//追加代码
	{
		if (szNewCode == null) return;
		
		if (szCode == null)
		{
			szCode = szNewCode;
			check();
		}
		else szCode += szNewCode;
	}
	
	public void append(MPiglet newPiglet)//追加代码
	{
		if (newPiglet != null)
		{
			if (szCode == null)//直接复制
			{
				szCode = newPiglet.szCode;
				nTemp = newPiglet.nTemp;
				nDigit = newPiglet.nDigit;
			}
			else append(newPiglet.toString());
		}
	}
	
	public void setType(String szType)//设置类型
	{
		this.szType = szType;
	}
	
	public void format()//格式化，调整缩进
	{
		if (szCode == null) return;
		
		String[] lines = szCode.split("\\n");
		szCode = "";
		
		int nTab = 0, i;//缩进个数
		
		for (String szLine : lines)
		{
			if (szLine.indexOf("END") == 0) --nTab;//遇END减少缩进
			
			Matcher matcher = patternLabel.matcher(szLine);//正则表达匹配标签
			
			if (matcher.find())
			{
				--nTab;
				for (i = 0; i < nTab; ++i) szCode += "\t";
				++nTab;
			}
			else for (i = 0; i < nTab; ++i) szCode += "\t";
			
			szCode += szLine + "\n";
			
			if (szLine.indexOf("BEGIN") == 0 || szLine.indexOf("MAIN") == 0) ++nTab;//遇BEGIN/MAIN增加缩进
		}
	}
}
