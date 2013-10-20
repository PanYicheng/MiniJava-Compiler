package spiglet.symboltable;

import java.util.HashSet;
import java.util.Vector;

import spiglet.syntaxtree.NoOpStmt;
import spiglet.syntaxtree.Node;
import spiglet.syntaxtree.Temp;

public class SStatement
{
	private Node node;//语句
	private String szLabel;
	
	private int nLiveDef = -1, nIndex;//定义变量，编号
	private HashSet<Integer> lstLiveUse = new HashSet<Integer>();//使用变量
	
	private HashSet<Integer> lstLiveIn = new HashSet<Integer>();//Live-in变量
	private HashSet<Integer> lstLiveOut = new HashSet<Integer>();//Live-out变量
	
	private SMethod method;
	private SBlock block;
	
	private Temp defTemp;
	private Vector<Temp> lstUseTemp = new Vector<Temp>();
	
	private int nGen = -1;
	private HashSet<Integer> lstKill = new HashSet<Integer>();
	
	private HashSet<Integer> lstReachIn = new HashSet<Integer>();
	private HashSet<Integer> lstReachOut = new HashSet<Integer>();
	
	public void setDefTemp(Temp tmp)
	{
		int nTemp = Integer.parseInt(tmp.f1.f0.tokenImage);
		nGen = method.addDef(nTemp, block);
		defTemp = tmp;
	}
	
	public void addKill(int nKill)
	{
		lstKill.add(nKill);
	}
	
	public HashSet<Integer> getKill()
	{
		return lstKill;
	}
	
	public int getGen()
	{
		return nGen;
	}
	
	public Temp getDefTemp()
	{
		return defTemp;
	}
	
	public void addUseTemp(Temp tmp)
	{
		lstUseTemp.add(tmp);
	}
	
	public Vector<Temp> getUseTemp()
	{
		return lstUseTemp;
	}
	
	public void setMethod(SMethod method)
	{
		this.method = method;
	}
	
	public void setBlock(SBlock block)
	{
		this.block = block;
	}
	
	public SMethod getMethod()
	{
		return method;
	}
	
	public SBlock getBlock()
	{
		return block;
	}
	
	public SStatement(Node node)//设置语句
	{
		setNode(node);
	}
	
	protected void setNode(Node node)//设置结点
	{
		this.node = node;
	}
		
	public Node getNode()//获取包含的结点
	{
		return node;
	}
	
	public void addLiveDef(int nLiveDef)//加入定义的临时单元
	{		
		this.nLiveDef = nLiveDef;
	}
	
	public void addLiveUse(int nLiveUse)//加入使用的临时单元
	{
		lstLiveUse.add(nLiveUse);
	}
	
	public int getLiveDef()//获取值的定义
	{
		return nLiveDef;
	}
	
	public HashSet<Integer> getLiveUse()//获取值的使用
	{
		return lstLiveUse;
	}
	
	protected void anaylzeLiveness(HashSet<Integer> _lstLiveOut)//分析活性
	{
		lstLiveOut.clear();
		lstLiveOut.addAll(_lstLiveOut);//设置Live-out
		
		lstLiveIn.clear();
		
		lstLiveIn.addAll(lstLiveOut);
		lstLiveIn.remove(nLiveDef);
		lstLiveIn.addAll(lstLiveUse);
	}
		
	public HashSet<Integer> getLiveIn()//获取Live-in
	{
		return lstLiveIn;
	}
	
	public HashSet<Integer> getLiveOut()//获取Live-out
	{
		return lstLiveOut;
	}
	
	protected boolean isDeadCode()//是否死代码
	{
		return nLiveDef >= 0 && !lstLiveOut.contains(nLiveDef);
	}
	
	protected void erase()//变为NOOP
	{
		node = new NoOpStmt();
		
		nLiveDef = -1;
		lstLiveUse.clear();
		
		lstLiveIn.clear();
		lstLiveIn.addAll(lstLiveOut);
	}
	
	protected void setIndex(int nIndex)
	{
		this.nIndex = nIndex;
	}
	
	public int getIndex()
	{
		return nIndex;
	}
	
	protected void setLabel(String szLabel)
	{
		this.szLabel = szLabel;
	}
	
	protected HashSet<Integer> getReachIn()
	{
		return lstReachIn;
	}
	
	protected HashSet<Integer> getReachOut()
	{
		return lstReachOut;
	}
	
	public String getLabel()
	{
		return szLabel;
	}
}
