package minijava.symboltable;

import java.util.Vector;

public class MActualParamList extends MIdentifier//ʵ���б�
{
	protected Vector<String> paramList = new Vector<String>();//�������в�����������
	protected Vector<MPiglet> pigletList = new Vector<MPiglet>();//�������в����м����
	
	private int nLine;//�б������к�
	
	public MActualParamList(int nLine, MIdentifier parent)
	{
		super(null, null, parent);
		this.nLine = nLine;
	}
	
	public void addParam(String szType)//��Ӳ����������ͣ�
	{
		paramList.add(szType);
	}
	
	public MVar getVar(String szName)//�ڰ����Լ��ı�ʶ���в����Ƿ����������������δ����
	{		
		return parent.getVar(szName);
	}
	
	public int getLine()//��ȡ�к�
	{ 
		return nLine;
	}
	
	public void addPiglet(MPiglet piglet)//�����м����
	{
		pigletList.add(piglet);
	}
	
	public Vector<MPiglet> getPiglets()//��ȡ�����м�����б�
	{
		return pigletList;
	}
}
