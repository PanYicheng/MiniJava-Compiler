package spiglet.symboltable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import spiglet.syntaxtree.CJumpStmt;
import spiglet.syntaxtree.ErrorStmt;
import spiglet.syntaxtree.JumpStmt;
import spiglet.syntaxtree.NoOpStmt;
import spiglet.syntaxtree.Node;
import spiglet.syntaxtree.special.MoveTempStmt;
import spiglet.syntaxtree.special.ReturnStmt;
import spiglet.visitor.LivenessAnalyzerVisitor;

public class SBlock implements Comparable<Object>
{
	private int nIndex;//次序
	private String szEntryLabel, szExitLabel;//入出口标签
	private Vector<SStatement> lstStmt = new Vector<SStatement>();//语句列表	
	
	private HashSet<SBlock> lstPrev = new HashSet<SBlock>();//前驱块
	private HashSet<SBlock> lstNext = new HashSet<SBlock>();//后继块
	
	private HashSet<Integer> lstLiveDef = new HashSet<Integer>();//定义变量
	private HashSet<Integer> lstLiveUse = new HashSet<Integer>();//使用变量
	
	private HashSet<Integer> lstLiveIn = new HashSet<Integer>();
	private HashSet<Integer> lstLiveOut = new HashSet<Integer>();
	
	private HashSet<Integer> lstAddedIn = new HashSet<Integer>();
	private HashSet<Integer> lstRemovedOut = new HashSet<Integer>();
	private boolean bHasBeenScanned;
	
	private HashSet<Integer> lstGen = new HashSet<Integer>();
	private HashSet<Integer> lstKill = new HashSet<Integer>();
	
	private HashSet<Integer> lstReachIn = new HashSet<Integer>();
	private HashSet<Integer> lstReachOut = new HashSet<Integer>();
	
	private Hashtable<Integer, HashSet<Integer>> mapPhi = new Hashtable<Integer, HashSet<Integer>>();
	private HashSet<Integer> lstNewVersion= new HashSet<Integer>();
	
	private SMethod method;
	
	public SBlock(int nIndex, SMethod method)
	{
		this.method = method;
		this.nIndex = nIndex;//设置次序
	}
	
	@Override
	public int compareTo(Object o)//按照次序比较排序
	{
		SBlock other = (SBlock) o;
		return nIndex - other.nIndex;
	}
	
	protected void addStmt(SStatement stmt)//加入语句
	{
		stmt.setBlock(this);
		lstStmt.add(stmt);
	}
		
	protected void removeNoop()//删除所有NOOP
	{
		if (lstStmt.size() == 1) return;//基本块只有一个NOOP，不删除
		
		Vector<SStatement> lstNewStmt = new Vector<SStatement>();
		for (SStatement stmt : lstStmt) if (!(stmt.getNode() instanceof NoOpStmt)) lstNewStmt.add(stmt);
		if (lstNewStmt.size() > 0) lstStmt = lstNewStmt;//加入所有非NOOP语句
		else
		{
			SStatement firstStmt = lstStmt.firstElement();
			lstStmt.clear();
			lstStmt.add(firstStmt);
		}
		
	}
	
	protected void initLiveness()//初始化活性分析
	{
		lstLiveDef.clear();
		lstLiveUse.clear();
		lstLiveIn.clear();//清除各集合
		
		LivenessAnalyzerVisitor vl = new LivenessAnalyzerVisitor();
		
		for (SStatement stmt : lstStmt)
		{
			stmt.getNode().accept(vl, stmt);//遍历该点
			for (int x : stmt.getLiveUse()) if (!lstLiveDef.contains(x)) lstLiveUse.add(x);//如果之前没出现过定值，加入值的使用
			int nDef = stmt.getLiveDef();
			if (nDef >= 0 && !lstLiveUse.contains(nDef)) lstLiveDef.add(nDef);//如果之前没有出现过值的使用，加入定值
		}
	}
	
	protected boolean analyzeLivenessByBlock()//按块分析
	{
		lstLiveOut.clear();
		for (SBlock nextBlock : lstNext) lstLiveOut.addAll(nextBlock.lstLiveIn);
		
		HashSet<Integer> lstOldLiveIn = new HashSet<Integer>();
		lstOldLiveIn.addAll(lstLiveIn);//加入全部元素
		
		lstLiveIn.clear();
		
		lstLiveIn.addAll(lstLiveOut);
		lstLiveIn.removeAll(lstLiveDef);
		lstLiveIn.addAll(lstLiveUse);
				
		return lstLiveIn.size() != lstOldLiveIn.size() || !lstLiveIn.containsAll(lstOldLiveIn);//返回是否改变
	}
	
	protected void analyzeLivenessByStmt()//每个语句活性分析
	{
		lstStmt.lastElement().anaylzeLiveness(lstLiveOut);
		for (int i = lstStmt.size() - 2; i >= 0; --i) lstStmt.get(i).anaylzeLiveness(lstStmt.get(i + 1).getLiveIn());
	}
	
	protected void setEntryLabel(String szEntryLabel)
	{
		this.szEntryLabel = szEntryLabel;
	}
	
	protected void setExitLabel(String szExitLabel)
	{
		this.szExitLabel = szExitLabel;
	}
	
	protected String getEntryLabel()
	{
		return szEntryLabel;
	}
	
	protected String getExitLabel()
	{
		return szExitLabel;
	}
	
	protected HashSet<SBlock> getPrev()
	{
		return lstPrev;
	}
	
	protected HashSet<SBlock> getNext()
	{
		return lstNext;
	}
	
	protected Vector<SStatement> getStmts()
	{
		return lstStmt;
	}
	
	protected void setIndex(int nIndex)
	{
		this.nIndex = nIndex;
	}
	
	protected int getIndex()
	{
		return nIndex;
	}
	
	protected boolean removeDeadCode()//删除死代码，返回是否改变了Live-in
	{
		lstLiveOut.clear();
		for (SBlock nextBlock : lstNext) lstLiveOut.addAll(nextBlock.lstLiveIn);
		
		HashSet<Integer> lstNextIn = lstLiveOut;
		for (int i = lstStmt.size() - 1; i >= 0; --i)//逆序枚举每一语句
		{
			SStatement stmt = lstStmt.get(i);
			
			HashSet<Integer> lstLiveOut = stmt.getLiveOut();
			lstLiveOut.clear();
			lstLiveOut.addAll(lstNextIn);//重置Live-out
			
			if (stmt.isDeadCode()) stmt.erase();//死代码变成NOOP,USE和DEF全部清空
			
			HashSet<Integer> lstLiveIn = stmt.getLiveIn();
			lstLiveIn.clear();
			
			lstLiveIn.addAll(lstLiveOut);//重新计算Live-in
			lstLiveIn.remove(stmt.getLiveDef());
			lstLiveIn.addAll(stmt.getLiveUse());
			
			lstNextIn = lstLiveIn;
		}
		
		HashSet<Integer> lstOldLiveIn = new HashSet<Integer>();
		lstOldLiveIn.addAll(lstLiveIn);//加入全部元素
				
		lstLiveIn.clear();
		lstLiveIn.addAll(lstNextIn);
		
		return lstLiveIn.size() != lstOldLiveIn.size() || !lstLiveIn.containsAll(lstOldLiveIn);//返回是否改变
	}

	protected void getGenKill()
	{
		for (SStatement stmt : lstStmt)
		{
			int nGen = stmt.getGen();
			if (nGen == -1) continue;
			
			lstGen.add(nGen);
			lstKill.remove(nGen);
			
			Hashtable<Integer, Integer> mapDef = method.getDefs();
			
			for (int x : mapDef.keySet())
			{
				if (x == nGen) continue;
				if (mapDef.get(nGen).equals(mapDef.get(x)))
				{
					stmt.addKill(x);
					if (lstGen.contains(x)) lstGen.remove(x);
					else lstKill.add(x);
				}
			}
		}
		return;
	}
	
	protected HashSet<Integer> getReachIn()
	{
		return lstReachIn;
	}
	
	protected HashSet<Integer> getReachOut()
	{
		return lstReachOut;
	}
	
	protected HashSet<Integer> getGen()
	{
		return lstGen;
	}
	
	protected HashSet<Integer> getKill()
	{
		return lstKill;
	}

	protected void addPhi(int nVersion, HashSet<Integer> lstVersion)
	{
		mapPhi.put(nVersion, lstVersion);
	}
	
	protected Hashtable<Integer, HashSet<Integer>> getPhi()
	{
		return mapPhi;
	}
	
	protected void addLastStmt(SStatement stmt)
	{
		int nPos = lstStmt.size();
		Node node = lstStmt.lastElement().getNode();
		
		if (node instanceof JumpStmt || node instanceof CJumpStmt || node instanceof ErrorStmt || node instanceof ReturnStmt)
		{
			--nPos;
		}
		
		stmt.setBlock(this);
		lstStmt.add(nPos, stmt);
	}
	
	protected void addNewVersion(int x, int y)
	{
		if (lstNewVersion.contains(x)) return;
		lstNewVersion.add(x);
		
		SStatement newStmt = new SStatement(new MoveTempStmt(x, y));
		newStmt.setMethod(method);
		addLastStmt(newStmt);
	}
}
