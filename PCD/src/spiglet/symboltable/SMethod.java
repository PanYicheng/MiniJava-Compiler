package spiglet.symboltable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Vector;

import spiglet.syntaxtree.ErrorStmt;
import spiglet.syntaxtree.JumpStmt;
import spiglet.syntaxtree.Node;
import spiglet.syntaxtree.Temp;
import spiglet.visitor.ReachingDefVisitor;

public class SMethod
{	
	private Hashtable<String, String> mapS2KLabel = new Hashtable<String, String>();
	private Hashtable<String, SBlock> mapEntryLabel = new Hashtable<String, SBlock>();//入口标签映射
	
	private Vector<SBlock> lstBlock = new Vector<SBlock>();//块列表
	
	private SBlock currentBlock, entryBlock, exitBlock;
	private int nArgs, nMaxCallArgs;//参数个数，被其调用的过程最多参数数
	private int nBlockSize, nStmtSize;//基本块数，语句数
	
	private Hashtable<Integer, STemp> mapArg = new Hashtable<Integer, STemp>();
	private Hashtable<Integer, STemp> mapTemp = new Hashtable<Integer, STemp>();
	
	private Hashtable<Integer, Integer> mapDef = new Hashtable<Integer, Integer>();
	private Hashtable<Integer, SBlock> mapDefBlock = new Hashtable<Integer, SBlock>();
	
	private String szName;//过程名
	
	public SMethod(String szName, int nArgs)
	{
		this.szName = szName;//设置过程名
		this.nArgs = nArgs;//设置参数个数
		newBlock();//初始加入一个块
	}
	
	public Hashtable<Integer, Integer> getDefs()
	{
		return mapDef;
	}
	
	public int addDef(int nDef, SBlock block)
	{
		int nRet = mapDef.size();
		mapDef.put(nRet, nDef);
		mapDefBlock.put(nRet, block);
		return nRet;
	}
	
	public String getName()
	{
		return szName;
	}
	
	public int getArgs()
	{
		return nArgs;
	}
	
	public int getMaxCallArgs()
	{
		return nMaxCallArgs;
	}
	
	public void addStmt(SStatement stmt)
	{
		stmt.setMethod(this);
		currentBlock.addStmt(stmt);
	}
	
	public void newBlock()//新建一个块
	{
		currentBlock = new SBlock(lstBlock.size(), this);//创建一个块
		lstBlock.add(currentBlock);//加入当前块
	}
	
	public void setMaxCallArgs(int nCallArgs)
	{
		if (nMaxCallArgs < nCallArgs) nMaxCallArgs = nCallArgs;//设置被调用的过程最大参数数
	}
	
	public void setEntryLabel(String szLabel)//设置入口标签
	{
		currentBlock.setEntryLabel(szLabel);
		mapEntryLabel.put(szLabel, currentBlock);//建立映射
	}
	
	public void setExitLabel(String szLabel)
	{
		currentBlock.setExitLabel(szLabel);
	}
	
	public int reassignLabel(int nCount)//重新分配为全局标签
	{
		for (Entry<String, SBlock> entry : mapEntryLabel.entrySet())
		{
			String szLabel = entry.getKey();//标签文本
			mapS2KLabel.put(szLabel, "L" + nCount++);//建立映射
		}
		return nCount;//返回已分配的全局标签数
	}
	
	public void createFlowGraph()//建立流图
	{
		clearEmptyBlocks();
		
		int nLength = lstBlock.size();//块个数
		
		entryBlock = lstBlock.firstElement();//入口块
		exitBlock = new SBlock(nLength, this);//出口块，空块
		
		for (int i = 0 ; i < nLength; ++i)
		{
			SBlock block = lstBlock.get(i);//获取当前块
			Node jumpStmt = block.getStmts().lastElement().getNode();//获取最后一条指令
			
			if (jumpStmt instanceof ErrorStmt) continue;//如果报错指令，无下一条指令
			
			if (!(jumpStmt instanceof JumpStmt) && i < nLength - 1)//非无条件跳转指令，且非最后一个块
			{
				SBlock nextBlock = lstBlock.get(i + 1);
				
				block.getNext().add(nextBlock);//加入后继
				nextBlock.getPrev().add(block);//加入前驱
			}
			
			if (block.getExitLabel() != null)//有出口标签
			{
				SBlock nextBlock = mapEntryLabel.get(block.getExitLabel());
				
				block.getNext().add(nextBlock);//加入后继
				nextBlock.getPrev().add(block);//加入前驱
			}
		}
				
		clearUnreferredBlocks();//清除未访问块		
		clearLabels();//清除无用标签
		
		reachingDefinitions();//到达定义 
		
		analyzeLiveness();//活性分析
		
		for (SBlock block : lstBlock) block.removeNoop();
	}
	
	public void reachingDefinitions()
	{
		for (int i = 0; i < nArgs; ++i) addDef(i, entryBlock);
		
		for (SBlock block : lstBlock) for (SStatement stmt : block.getStmts()) stmt.getNode().accept(new ReachingDefVisitor(), stmt);
		for (SBlock block : lstBlock) block.getGenKill();
		
		HashSet<Integer> lstEntryReachIn = entryBlock.getReachIn();
		HashSet<Integer> lstEntryReachOut = entryBlock.getReachOut();
		
		for (int i = 0; i < nArgs; ++i) lstEntryReachIn.add(i);
		
		lstEntryReachOut.addAll(lstEntryReachIn);
		lstEntryReachOut.removeAll(entryBlock.getKill());
		lstEntryReachOut.addAll(entryBlock.getGen());
		
		HashSet<SBlock> lstChanged = new HashSet<SBlock>();
		lstChanged.addAll(lstBlock);
		lstChanged.remove(entryBlock);
		
		while (!lstChanged.isEmpty())
		{
			SBlock block = lstChanged.iterator().next();//取一个基本块
			lstChanged.remove(block);
			
			HashSet<Integer> lstReachIn = block.getReachIn();
			HashSet<Integer> lstReachOut = block.getReachOut();
			
			HashSet<Integer> lstOldReachOut = new HashSet<Integer>();
			lstOldReachOut.addAll(lstReachOut);
			
			for (SBlock prevBlock : block.getPrev()) lstReachIn.addAll(prevBlock.getReachOut());
			
			lstReachOut.clear();
			
			lstReachOut.addAll(lstReachIn);
			lstReachOut.removeAll(block.getKill());
			lstReachOut.addAll(block.getGen());
			
			if (lstReachOut.size() != lstOldReachOut.size() || !lstOldReachOut.containsAll(lstReachOut))
			{
				for (SBlock nextBlock : block.getNext()) lstChanged.add(nextBlock);
			}
		}
		
		for (SBlock block : lstBlock)
		{
			HashSet<Integer> lstPrevOut = block.getReachIn();
			
			for (SStatement stmt : block.getStmts())
			{
				HashSet<Integer> lstReachIn = stmt.getReachIn();
				HashSet<Integer> lstReachOut = stmt.getReachOut();
				
				lstReachIn.clear();
				lstReachIn.addAll(lstPrevOut);
				
				for (Temp tmp : stmt.getUseTemp())
				{
					int nTemp = Integer.parseInt(tmp.f1.f0.tokenImage);
					for (int x : lstReachIn) if (nTemp == mapDef.get(x)) tmp.lstVersion.add(x);
					
					int nVersion;
					if (tmp.lstVersion.contains(nTemp)) nVersion = nTemp;
					else nVersion = tmp.lstVersion.iterator().next();
					
					if (tmp.lstVersion.size() > 1) block.addPhi(nVersion, tmp.lstVersion);
					tmp.f1.f0.tokenImage = Integer.toString(nVersion);
				}
				
				lstReachOut.clear();
				lstReachOut.addAll(lstReachIn);
				lstReachOut.removeAll(stmt.getKill());
				
				int nGen = stmt.getGen();
				if (nGen >= 0)
				{
					lstReachOut.add(nGen);
					Temp defTmp = stmt.getDefTemp();
					defTmp.f1.f0.tokenImage = Integer.toString(nGen);
				}
				
				lstPrevOut = lstReachOut;
			}
			
			Hashtable<Integer, HashSet<Integer>> mapPhi = block.getPhi();
			for (int x : mapPhi.keySet())
			{
				for (int y : mapPhi.get(x))
				{
					if (x == y) continue;
					mapDefBlock.get(y).addNewVersion(x, y);
				}
			}
		}
	}
	
	private void analyzeLiveness()//活性分析
	{
		for (SBlock block : lstBlock) block.initLiveness();
		
		HashSet<SBlock> lstChanged = new HashSet<SBlock>();
		lstChanged.addAll(lstBlock);
		
		while (!lstChanged.isEmpty())//未出现不动点
		{
			SBlock block = lstChanged.iterator().next();//取一个基本块
			lstChanged.remove(block);
			
			if (block.analyzeLivenessByBlock())
			{
				for (SBlock prevBlock : block.getPrev()) lstChanged.add(prevBlock);
			}
		}
		
		for (SBlock block : lstBlock) block.analyzeLivenessByStmt();//分析块内语句
		removeDeadCode();
	}
	
	private void removeDeadCode()
	{
		HashSet<SBlock> lstChanged = new HashSet<SBlock>();//待检查的基本块集合
		lstChanged.addAll(lstBlock);//所有基本块都待检查
		
		while (!lstChanged.isEmpty())//未出现不动点
		{
			SBlock block = lstChanged.iterator().next();//取一个基本块
			lstChanged.remove(block);
			
			if (block.removeDeadCode())//如果删除了新的死代码并且改变了Live-in
			{
				for (SBlock prevBlock : block.getPrev()) lstChanged.add(prevBlock);
			}
		}
	}
	
	private void clearEmptyBlocks()//清除空块
	{
		Vector<SBlock> lstNewBlock = new Vector<SBlock>();
		int nLength = lstBlock.size();//块个数
		
		for (int i = 0; i < nLength; ++i)
		{
			SBlock block = lstBlock.get(i);//获取当前块
			if (!block.getStmts().isEmpty()) lstNewBlock.add(block);//如果非空块
		}
		lstBlock = lstNewBlock;//清除非空块
	}
	
	private void clearUnreferredBlocks()//清除未访问块
	{
		Vector<SBlock> lstNewBlock = new Vector<SBlock>();
		DFS(lstNewBlock, entryBlock);//遍历各块
		lstBlock = lstNewBlock;
		
		Collections.sort(lstBlock);//排序
		
		for (SBlock block : lstBlock)//删除无效连接
		{
			HashSet<SBlock> lstNewPrev = new HashSet<SBlock>();
			for (SBlock prevBlock : block.getPrev()) if (lstBlock.contains(prevBlock)) lstNewPrev.add(prevBlock);
			
			block.getPrev().clear();
			block.getPrev().addAll(lstNewPrev);
		}
	}
	
	private void DFS(Vector<SBlock> lstNewBlock, SBlock block)//深度优先搜索
	{
		lstNewBlock.add(block);//加入此块
				
		if (block.getNext().isEmpty())
		{
			block.getNext().add(exitBlock);//加入出口
			exitBlock.getPrev().add(block);//加入入口
			return;
		}
				
		for (SBlock nextBlock : block.getNext()) if (!lstNewBlock.contains(nextBlock)) DFS(lstNewBlock, nextBlock);//遍历未访问的后继块
	}
	
	private void clearLabels()//清除无用标签
	{
		Hashtable<String, SBlock> mapNewEntryLabel = new Hashtable<String, SBlock>();
		for (String szLabel : mapEntryLabel.keySet())
		{
			SBlock block = mapEntryLabel.get(szLabel);
			if (block.getPrev().size() > 1) mapNewEntryLabel.put(szLabel, block);//入度大于1
			else
			{
				SBlock prevBlock = block.getPrev().iterator().next();//获取前驱
				if (prevBlock.getExitLabel() == szLabel) mapNewEntryLabel.put(szLabel, block);//经跳转而来
				else block.setEntryLabel(null);//否则删除标签
			}
		}
		mapEntryLabel = mapNewEntryLabel;
	}
	
	public SStatement[] getStmts()//重新分配编号
	{
		Vector<SStatement> lstStmt = new Vector<SStatement>();
		
		nBlockSize = nStmtSize = 0;
		for (SBlock block : lstBlock)
		{
			block.setIndex(nBlockSize++);
			block.getStmts().firstElement().setLabel(block.getEntryLabel());
			
			for (SStatement stmt : block.getStmts())
			{
				stmt.setIndex(nStmtSize++);
				lstStmt.add(stmt.getIndex(), stmt);
				
				for (int x : stmt.getLiveUse()) if (!mapTemp.containsKey(x)) mapTemp.put(x, new STemp(x));
				int x = stmt.getLiveDef(); 
				if (x >= 0 && !mapTemp.containsKey(x)) mapTemp.put(x, new STemp(x));
			}
		}
		
		for (int x : lstStmt.firstElement().getLiveIn())
		{
			if (mapTemp.containsKey(x))	mapArg.put(x, mapTemp.get(x));
		}
				
		Hashtable<Integer, STemp> mapNoLive = new Hashtable<Integer, STemp>();
		Hashtable<Integer, STemp> mapNoDead = new Hashtable<Integer, STemp>();
		
		mapNoLive.putAll(mapTemp);
		mapNoDead.putAll(mapTemp);
		
		for (int i = 0; i < nStmtSize; ++i)
		{
			for (int x : lstStmt.get(i).getLiveIn())
			{
				if (mapNoLive.containsKey(x))
				{
					STemp tmp = mapTemp.get(x);
					tmp.setStart(i);
					mapNoLive.remove(x);
				}
			}
			
			for (int x : lstStmt.get(i).getLiveOut())
			{
				if (mapNoLive.containsKey(x))
				{
					STemp tmp = mapTemp.get(x);
					tmp.setStart(i);
					mapNoLive.remove(x);
				}
			}
		}
		
		for (int i = nStmtSize - 1; i >= 0; --i)
		{
			for (int x : lstStmt.get(i).getLiveIn())
			{
				if (mapNoDead.containsKey(x))
				{
					STemp tmp = mapTemp.get(x);
					tmp.setEnd(i);
					mapNoDead.remove(x);
				}
			}
			
			for (int x : lstStmt.get(i).getLiveOut())
			{
				if (mapNoDead.containsKey(x))
				{
					STemp tmp = mapTemp.get(x);
					tmp.setEnd(i);
					mapNoDead.remove(x);
				}
			}
		}
		
		return lstStmt.toArray(new SStatement[0]);//返回一个数组
	}
	
	public Hashtable<Integer, STemp> getTemp()
	{
		return mapTemp;
	}
	
	public Hashtable<Integer, STemp> getTempArgs()
	{
		return mapArg;
	}
	
	public SBlock getCurrentBlock()//获取当前块
	{
		return currentBlock;
	}
	
	public String getLabelKangaName(String szLabel)
	{
		return mapS2KLabel.get(szLabel);
	}
}
