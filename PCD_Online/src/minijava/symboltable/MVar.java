package minijava.symboltable;

public class MVar extends MIdentifier//������ʶ��
{	
	public MVar(String szName, String szType, MIdentifier parent)
	{
		super(szName, szType, parent);
	}
	
	public String toString()
	{
		return szType + " " + szName;//��ӡ�����������-����-�����Ĳ�νṹ 		
	}
}
