package minijava.symboltable;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

public class MMethod extends MIdentifier//方法标识符
{	
	private int nExtraParamSize;//第19个参数起各参数占内存空间
	private String szPigletName;//Piglet下名称
	
	private Hashtable<String, MVar> varList = new Hashtable<String, MVar>();//变量列表
	private Vector<MVar> paramList = new Vector<MVar>();//参数列表
	
	public MMethod(String szName, String szType, MClass parent)//父标识符必然是类
	{
		super(szName, szType, parent);
	}
	
	public boolean insertParam(MVar newParam)//插入参数					
	{
		paramList.add(newParam);//直接加入参数列表
		
		if (paramList.size() < 19)//前18个参数直接设置TEMP
		{
			newParam.setTemp(paramList.size());
		}
		else
		{
			newParam.setTemp(19);//第19个参数起设置相同TEMP，不同偏移
			newParam.setOffset(nExtraParamSize);
			nExtraParamSize += 4;
		}
		
		return insertVar(newParam);//判重
	}
	
	public boolean insertVar(MVar newVar)//插入变量
	{
		String szNewVar = newVar.getName();
		if (varList.containsKey(szNewVar)) return false;//如果已有则不插入
		
		varList.put(szNewVar, newVar);//插入
		return true;
	}
	
	public MVar getVar(String szName)//获取变量
	{
		MVar newVar = varList.get(szName);
		
		if (newVar != null) return newVar;//在自身内部找不到
		else return parent.getVar(szName);//去父标识符找
	}
	
	public String toString()//打印出来
	{
		String szTemp = "\t" + szType + " " + szName + "(";//方法名
		
		for (MVar _var : paramList) szTemp += _var + ",";//各参数
		if (!paramList.isEmpty()) szTemp = szTemp.substring(0, szTemp.length() - 1);
		
		szTemp += ")" + "\n\t{\n";
		
		for (MVar _var : varList.values())//其他变量
		{
			if (!paramList.contains(_var)) szTemp += "\t\t" + _var + ";\n";
		}
		
		szTemp += "\t}\n";
		return szTemp;
	}
	
	public boolean equals(Object o)//是否等价，判断是继承还是重载
	{
		MMethod other = (MMethod) o;
		
		if (!classList.classEqualsOrDerives(other.szType, szType)) return false;//返回类型必须兼容
		
		int nParam = paramList.size(); 
		if (nParam != other.paramList.size()) return false;//参数个数一致
		
		for (int i = 0; i < nParam; ++i)//参数类型一致
		{
			if (!paramList.get(i).getType().equals(other.paramList.get(i).getType())) return false;
		}
		
		return true;
	}
	
	public int checkParam(MActualParamList p)//检查实参
	{
		int nLength = paramList.size();
		if (nLength != p.paramList.size()) return 1;//参数个数一致
		
		for (int i = 0; i < nLength; ++i)
		{
			if (p.paramList.get(i) == null) return 2;//有未定义变量，直接挂
			if (!classList.classEqualsOrDerives(paramList.get(i).szType, p.paramList.get(i))) return 2;//参数类型一致或实参继承自形参
		}
		
		return 0;
	}
	
	public int getPigletParamCount()//返回Piglet下参数
	{
		int ret = paramList.size();
		return (ret > 19) ? 19 : ret; 
	}
	
	public int getExtraParamSize()//返回额外参数内存大小
	{
		return nExtraParamSize;
	}
	
	protected void setPigletName(String szPigletName) { this.szPigletName = szPigletName; }
	public String getPigletName() { return szPigletName; }//设置和获取Piglet下名称
	
	protected int alloc(int nTemp, HashSet<String> methodPigletNames)//所有局部变量分配TEMP编号，分配piglet名称
	{
		if (this.szPigletName != null) return nTemp;//已分配过
		
		int i = 1;
		String szPigletName = parent.getName() + "_" + szName;
		
		while (methodPigletNames.contains(szPigletName))//如果已包含名字，避免如A类下的B_C函数与A_B类下的C函数重名
		{
			szPigletName = parent.getName() + "_" + szName + "_" + (++i);
		}
		
		methodPigletNames.add(szPigletName);//设置名称
		this.szPigletName = szPigletName;
				
		for (MVar _var : varList.values())//检查所有变量
		{
			if (paramList.contains(_var)) continue;//不是参数
			_var.setTemp(nTemp++);//设置TEMP,编号加一
		}
		
		return nTemp;
	}
}
