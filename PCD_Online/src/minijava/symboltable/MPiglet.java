package minijava.symboltable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MPiglet extends MIdentifier//Piglet�����������
{
	private static Pattern patternTemp = Pattern.compile("^TEMP \\d+$");//������ƥ��TEMP��
	private static Pattern patternDigit = Pattern.compile("^\\d+$");//������ƥ��������
	private static Pattern patternLabel = Pattern.compile("^L\\d+");//������ƥ���ǩ
	
	private String szCode;//�м����
	private int nTemp = -1, nDigit = -1;//TEMP��ţ����������ݣ�-1������
	
	public MPiglet(String szName, String szType, String szCode)
	{
		super(szName, szType, null);
		this.szCode = szCode;
		check();//����Ƿ�ΪTEMP��������
	}
	
	private void check()
	{
		if (szCode == null) return;
		
		Matcher matcher = patternTemp.matcher(szCode);//������ƥ��TEMP��
		
		if (matcher.find())//���ƥ��
		{
			try
			{
				String szNumber = szCode.substring(5);
				nTemp = Integer.parseInt(szNumber);
			}
			catch(Exception e)//��������
			{
				nTemp = -1;
			}
			
			return;
		}
		
		matcher = patternDigit.matcher(szCode);//������ƥ��������
		
		if (matcher.find())//���ƥ��
		{
			try
			{
				nDigit = Integer.parseInt(szCode);
			}
			catch(Exception e)//��������
			{
				nDigit = -1;
			}
		}
	}
	
	public int getTemp()//����TEMP���
	{
		return nTemp;
	}
	
	public boolean isTemp()//�Ƿ���TEMP
	{
		return nTemp >= 0;
	}
	
	public boolean isDigit()//�Ƿ���������
	{
		return nDigit >= 0;
	}
	
	public String toString()//�������
	{
		return szCode;
	}
	
	public void append(String szNewCode)//׷�Ӵ���
	{
		if (szNewCode == null) return;
		
		if (szCode == null)
		{
			szCode = szNewCode;
			check();
		}
		else szCode += szNewCode;
	}
	
	public void append(MPiglet newPiglet)//׷�Ӵ���
	{
		if (newPiglet != null)
		{
			if (szCode == null)//ֱ�Ӹ���
			{
				szCode = newPiglet.szCode;
				nTemp = newPiglet.nTemp;
				nDigit = newPiglet.nDigit;
			}
			else append(newPiglet.toString());
		}
	}
	
	public void setType(String szType)//��������
	{
		this.szType = szType;
	}
	
	public void format()//��ʽ������������
	{
		if (szCode == null) return;
		
		String[] lines = szCode.split("\\n");
		szCode = "";
		
		int nTab = 0, i;//��������
		
		for (String szLine : lines)
		{
			if (szLine.indexOf("END") == 0) --nTab;//��END��������
			
			Matcher matcher = patternLabel.matcher(szLine);//������ƥ���ǩ
			
			if (matcher.find())
			{
				--nTab;
				for (i = 0; i < nTab; ++i) szCode += "\t";
				++nTab;
			}
			else for (i = 0; i < nTab; ++i) szCode += "\t";
			
			szCode += szLine + "\n";
			
			if (szLine.indexOf("BEGIN") == 0 || szLine.indexOf("MAIN") == 0) ++nTab;//��BEGIN/MAIN��������
		}
	}
}
