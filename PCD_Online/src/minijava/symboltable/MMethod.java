package minijava.symboltable;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

public class MMethod extends MIdentifier//������ʶ��
{	
	private int nExtraParamSize;//��19�������������ռ�ڴ�ռ�
	private String szPigletName;//Piglet������
	
	private Hashtable<String, MVar> varList = new Hashtable<String, MVar>();//�����б�
	private Vector<MVar> paramList = new Vector<MVar>();//�����б�
	
	public MMethod(String szName, String szType, MClass parent)//����ʶ����Ȼ����
	{
		super(szName, szType, parent);
	}
	
	public boolean insertParam(MVar newParam)//�������					
	{
		paramList.add(newParam);//ֱ�Ӽ�������б�
		
		if (paramList.size() < 19)//ǰ18������ֱ������TEMP
		{
			newParam.setTemp(paramList.size());
		}
		else
		{
			newParam.setTemp(19);//��19��������������ͬTEMP����ͬƫ��
			newParam.setOffset(nExtraParamSize);
			nExtraParamSize += 4;
		}
		
		return insertVar(newParam);//����
	}
	
	public boolean insertVar(MVar newVar)//�������
	{
		String szNewVar = newVar.getName();
		if (varList.containsKey(szNewVar)) return false;//��������򲻲���
		
		varList.put(szNewVar, newVar);//����
		return true;
	}
	
	public MVar getVar(String szName)//��ȡ����
	{
		MVar newVar = varList.get(szName);
		
		if (newVar != null) return newVar;//�������ڲ��Ҳ���
		else return parent.getVar(szName);//ȥ����ʶ����
	}
	
	public String toString()//��ӡ����
	{
		String szTemp = "\t" + szType + " " + szName + "(";//������
		
		for (MVar _var : paramList) szTemp += _var + ",";//������
		if (!paramList.isEmpty()) szTemp = szTemp.substring(0, szTemp.length() - 1);
		
		szTemp += ")" + "\n\t{\n";
		
		for (MVar _var : varList.values())//��������
		{
			if (!paramList.contains(_var)) szTemp += "\t\t" + _var + ";\n";
		}
		
		szTemp += "\t}\n";
		return szTemp;
	}
	
	public boolean equals(Object o)//�Ƿ�ȼۣ��ж��Ǽ̳л�������
	{
		MMethod other = (MMethod) o;
		
		if (!classList.classEqualsOrDerives(other.szType, szType)) return false;//�������ͱ������
		
		int nParam = paramList.size(); 
		if (nParam != other.paramList.size()) return false;//��������һ��
		
		for (int i = 0; i < nParam; ++i)//��������һ��
		{
			if (!paramList.get(i).getType().equals(other.paramList.get(i).getType())) return false;
		}
		
		return true;
	}
	
	public int checkParam(MActualParamList p)//���ʵ��
	{
		int nLength = paramList.size();
		if (nLength != p.paramList.size()) return 1;//��������һ��
		
		for (int i = 0; i < nLength; ++i)
		{
			if (p.paramList.get(i) == null) return 2;//��δ���������ֱ�ӹ�
			if (!classList.classEqualsOrDerives(paramList.get(i).szType, p.paramList.get(i))) return 2;//��������һ�»�ʵ�μ̳����β�
		}
		
		return 0;
	}
	
	public int getPigletParamCount()//����Piglet�²���
	{
		int ret = paramList.size();
		return (ret > 19) ? 19 : ret; 
	}
	
	public int getExtraParamSize()//���ض�������ڴ��С
	{
		return nExtraParamSize;
	}
	
	protected void setPigletName(String szPigletName) { this.szPigletName = szPigletName; }
	public String getPigletName() { return szPigletName; }//���úͻ�ȡPiglet������
	
	protected int alloc(int nTemp, HashSet<String> methodPigletNames)//���оֲ���������TEMP��ţ�����piglet����
	{
		if (this.szPigletName != null) return nTemp;//�ѷ����
		
		int i = 1;
		String szPigletName = parent.getName() + "_" + szName;
		
		while (methodPigletNames.contains(szPigletName))//����Ѱ������֣�������A���µ�B_C������A_B���µ�C��������
		{
			szPigletName = parent.getName() + "_" + szName + "_" + (++i);
		}
		
		methodPigletNames.add(szPigletName);//��������
		this.szPigletName = szPigletName;
				
		for (MVar _var : varList.values())//������б���
		{
			if (paramList.contains(_var)) continue;//���ǲ���
			_var.setTemp(nTemp++);//����TEMP,��ż�һ
		}
		
		return nTemp;
	}
}
