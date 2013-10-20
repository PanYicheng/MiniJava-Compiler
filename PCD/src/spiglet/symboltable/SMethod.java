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
	private Hashtable<String, SBlock> mapEntryLabel = new Hashtable<String, SBlock>();//��ڱ�ǩӳ��
	
	private Vector<SBlock> lstBlock = new Vector<SBlock>();//���б�
	
	private SBlock currentBlock, entryBlock, exitBlock;
	private int nArgs, nMaxCallArgs;//����������������õĹ�����������
	private int nBlockSize, nStmtSize;//���������������
	
	private Hashtable<Integer, STemp> mapArg = new Hashtable<Integer, STemp>();
	private Hashtable<Integer, STemp> mapTemp = new Hashtable<Integer, STemp>();
	
	private Hashtable<Integer, Integer> mapDef = new Hashtable<Integer, Integer>();
	private Hashtable<Integer, SBlock> mapDefBlock = new Hashtable<Integer, SBlock>();
	
	private String szName;//������
	
	public SMethod(String szName, int nArgs)
	{
		this.szName = szName;//���ù�����
		this.nArgs = nArgs;//���ò�������
		newBlock();//��ʼ����һ����
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
	
	public void newBlock()//�½�һ����
	{
		currentBlock = new SBlock(lstBlock.size(), this);//����һ����
		lstBlock.add(currentBlock);//���뵱ǰ��
	}
	
	public void setMaxCallArgs(int nCallArgs)
	{
		if (nMaxCallArgs < nCallArgs) nMaxCallArgs = nCallArgs;//���ñ����õĹ�����������
	}
	
	public void setEntryLabel(String szLabel)//������ڱ�ǩ
	{
		currentBlock.setEntryLabel(szLabel);
		mapEntryLabel.put(szLabel, currentBlock);//����ӳ��
	}
	
	public void setExitLabel(String szLabel)
	{
		currentBlock.setExitLabel(szLabel);
	}
	
	public int reassignLabel(int nCount)//���·���Ϊȫ�ֱ�ǩ
	{
		for (Entry<String, SBlock> entry : mapEntryLabel.entrySet())
		{
			String szLabel = entry.getKey();//��ǩ�ı�
			mapS2KLabel.put(szLabel, "L" + nCount++);//����ӳ��
		}
		return nCount;//�����ѷ����ȫ�ֱ�ǩ��
	}
	
	public void createFlowGraph()//������ͼ
	{
		clearEmptyBlocks();
		
		int nLength = lstBlock.size();//�����
		
		entryBlock = lstBlock.firstElement();//��ڿ�
		exitBlock = new SBlock(nLength, this);//���ڿ飬�տ�
		
		for (int i = 0 ; i < nLength; ++i)
		{
			SBlock block = lstBlock.get(i);//��ȡ��ǰ��
			Node jumpStmt = block.getStmts().lastElement().getNode();//��ȡ���һ��ָ��
			
			if (jumpStmt instanceof ErrorStmt) continue;//�������ָ�����һ��ָ��
			
			if (!(jumpStmt instanceof JumpStmt) && i < nLength - 1)//����������תָ��ҷ����һ����
			{
				SBlock nextBlock = lstBlock.get(i + 1);
				
				block.getNext().add(nextBlock);//������
				nextBlock.getPrev().add(block);//����ǰ��
			}
			
			if (block.getExitLabel() != null)//�г��ڱ�ǩ
			{
				SBlock nextBlock = mapEntryLabel.get(block.getExitLabel());
				
				block.getNext().add(nextBlock);//������
				nextBlock.getPrev().add(block);//����ǰ��
			}
		}
				
		clearUnreferredBlocks();//���δ���ʿ�		
		clearLabels();//������ñ�ǩ
		
		reachingDefinitions();//���ﶨ�� 
		
		analyzeLiveness();//���Է���
		
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
			SBlock block = lstChanged.iterator().next();//ȡһ��������
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
	
	private void analyzeLiveness()//���Է���
	{
		for (SBlock block : lstBlock) block.initLiveness();
		
		HashSet<SBlock> lstChanged = new HashSet<SBlock>();
		lstChanged.addAll(lstBlock);
		
		while (!lstChanged.isEmpty())//δ���ֲ�����
		{
			SBlock block = lstChanged.iterator().next();//ȡһ��������
			lstChanged.remove(block);
			
			if (block.analyzeLivenessByBlock())
			{
				for (SBlock prevBlock : block.getPrev()) lstChanged.add(prevBlock);
			}
		}
		
		for (SBlock block : lstBlock) block.analyzeLivenessByStmt();//�����������
		removeDeadCode();
	}
	
	private void removeDeadCode()
	{
		HashSet<SBlock> lstChanged = new HashSet<SBlock>();//�����Ļ����鼯��
		lstChanged.addAll(lstBlock);//���л����鶼�����
		
		while (!lstChanged.isEmpty())//δ���ֲ�����
		{
			SBlock block = lstChanged.iterator().next();//ȡһ��������
			lstChanged.remove(block);
			
			if (block.removeDeadCode())//���ɾ�����µ������벢�Ҹı���Live-in
			{
				for (SBlock prevBlock : block.getPrev()) lstChanged.add(prevBlock);
			}
		}
	}
	
	private void clearEmptyBlocks()//����տ�
	{
		Vector<SBlock> lstNewBlock = new Vector<SBlock>();
		int nLength = lstBlock.size();//�����
		
		for (int i = 0; i < nLength; ++i)
		{
			SBlock block = lstBlock.get(i);//��ȡ��ǰ��
			if (!block.getStmts().isEmpty()) lstNewBlock.add(block);//����ǿտ�
		}
		lstBlock = lstNewBlock;//����ǿտ�
	}
	
	private void clearUnreferredBlocks()//���δ���ʿ�
	{
		Vector<SBlock> lstNewBlock = new Vector<SBlock>();
		DFS(lstNewBlock, entryBlock);//��������
		lstBlock = lstNewBlock;
		
		Collections.sort(lstBlock);//����
		
		for (SBlock block : lstBlock)//ɾ����Ч����
		{
			HashSet<SBlock> lstNewPrev = new HashSet<SBlock>();
			for (SBlock prevBlock : block.getPrev()) if (lstBlock.contains(prevBlock)) lstNewPrev.add(prevBlock);
			
			block.getPrev().clear();
			block.getPrev().addAll(lstNewPrev);
		}
	}
	
	private void DFS(Vector<SBlock> lstNewBlock, SBlock block)//�����������
	{
		lstNewBlock.add(block);//����˿�
				
		if (block.getNext().isEmpty())
		{
			block.getNext().add(exitBlock);//�������
			exitBlock.getPrev().add(block);//�������
			return;
		}
				
		for (SBlock nextBlock : block.getNext()) if (!lstNewBlock.contains(nextBlock)) DFS(lstNewBlock, nextBlock);//����δ���ʵĺ�̿�
	}
	
	private void clearLabels()//������ñ�ǩ
	{
		Hashtable<String, SBlock> mapNewEntryLabel = new Hashtable<String, SBlock>();
		for (String szLabel : mapEntryLabel.keySet())
		{
			SBlock block = mapEntryLabel.get(szLabel);
			if (block.getPrev().size() > 1) mapNewEntryLabel.put(szLabel, block);//��ȴ���1
			else
			{
				SBlock prevBlock = block.getPrev().iterator().next();//��ȡǰ��
				if (prevBlock.getExitLabel() == szLabel) mapNewEntryLabel.put(szLabel, block);//����ת����
				else block.setEntryLabel(null);//����ɾ����ǩ
			}
		}
		mapEntryLabel = mapNewEntryLabel;
	}
	
	public SStatement[] getStmts()//���·�����
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
		
		return lstStmt.toArray(new SStatement[0]);//����һ������
	}
	
	public Hashtable<Integer, STemp> getTemp()
	{
		return mapTemp;
	}
	
	public Hashtable<Integer, STemp> getTempArgs()
	{
		return mapArg;
	}
	
	public SBlock getCurrentBlock()//��ȡ��ǰ��
	{
		return currentBlock;
	}
	
	public String getLabelKangaName(String szLabel)
	{
		return mapS2KLabel.get(szLabel);
	}
}
