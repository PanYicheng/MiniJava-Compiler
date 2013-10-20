package piglet.piglet2spiglet;

public class PExp//���ʽ��
{
	public static final int TEMP = 3, SIMPLE = 1;//��ʱ������־���򵥱��ʽ��־
	
	private boolean bTemp, bSimple;//�Ƿ���ʱ�������Ƿ�򵥱��ʽ
	private String szExp;//���ʽ
	
	public PExp(String szExp)//�����ʽ
	{
		this.szExp = szExp;
	}
	
	public PExp(String szExp, int nFlag)//�����ʽ���־
	{
		this.szExp = szExp;
		this.bTemp = (nFlag & 2) == 2;
		this.bSimple = (nFlag & 1) == 1;		
	}
	
	public String toString()//������ʽ
	{
		return szExp;
	}
	
	public boolean isTemp()//�Ƿ���ʱ����
	{
		return bTemp;
	}
	
	public boolean isSimple()//�Ƿ�򵥱��ʽ
	{
		return bSimple;
	}
	
	public void append(String szNewExp)//����һ���ַ���
	{
		if (szNewExp == null) return;
		if (szExp == null) szExp = szNewExp;
		else szExp += szNewExp;
	}
	
	public void append(PExp newExp)//����һ�����ʽ
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
