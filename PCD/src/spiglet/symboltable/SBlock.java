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
	private int nIndex;//����
	private String szEntryLabel, szExitLabel;//����ڱ�ǩ
	private Vector<SStatement> lstStmt = new Vector<SStatement>();//����б�	
	
	private HashSet<SBlock> lstPrev = new HashSet<SBlock>();//ǰ����
	private HashSet<SBlock> lstNext = new HashSet<SBlock>();//��̿�
	
	private HashSet<Integer> lstLiveDef = new HashSet<Integer>();//�������
	private HashSet<Integer> lstLiveUse = new HashSet<Integer>();//ʹ�ñ���
	
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
		this.nIndex = nIndex;//���ô���
	}
	
	@Override
	public int compareTo(Object o)//���մ���Ƚ�����
	{
		SBlock other = (SBlock) o;
		return nIndex - other.nIndex;
	}
	
	protected void addStmt(SStatement stmt)//�������
	{
		stmt.setBlock(this);
		lstStmt.add(stmt);
	}
		
	protected void removeNoop()//ɾ������NOOP
	{
		if (lstStmt.size() == 1) return;//������ֻ��һ��NOOP����ɾ��
		
		Vector<SStatement> lstNewStmt = new Vector<SStatement>();
		for (SStatement stmt : lstStmt) if (!(stmt.getNode() instanceof NoOpStmt)) lstNewStmt.add(stmt);
		if (lstNewStmt.size() > 0) lstStmt = lstNewStmt;//�������з�NOOP���
		else
		{
			SStatement firstStmt = lstStmt.firstElement();
			lstStmt.clear();
			lstStmt.add(firstStmt);
		}
		
	}
	
	protected void initLiveness()//��ʼ�����Է���
	{
		lstLiveDef.clear();
		lstLiveUse.clear();
		lstLiveIn.clear();//���������
		
		LivenessAnalyzerVisitor vl = new LivenessAnalyzerVisitor();
		
		for (SStatement stmt : lstStmt)
		{
			stmt.getNode().accept(vl, stmt);//�����õ�
			for (int x : stmt.getLiveUse()) if (!lstLiveDef.contains(x)) lstLiveUse.add(x);//���֮ǰû���ֹ���ֵ������ֵ��ʹ��
			int nDef = stmt.getLiveDef();
			if (nDef >= 0 && !lstLiveUse.contains(nDef)) lstLiveDef.add(nDef);//���֮ǰû�г��ֹ�ֵ��ʹ�ã����붨ֵ
		}
	}
	
	protected boolean analyzeLivenessByBlock()//�������
	{
		lstLiveOut.clear();
		for (SBlock nextBlock : lstNext) lstLiveOut.addAll(nextBlock.lstLiveIn);
		
		HashSet<Integer> lstOldLiveIn = new HashSet<Integer>();
		lstOldLiveIn.addAll(lstLiveIn);//����ȫ��Ԫ��
		
		lstLiveIn.clear();
		
		lstLiveIn.addAll(lstLiveOut);
		lstLiveIn.removeAll(lstLiveDef);
		lstLiveIn.addAll(lstLiveUse);
				
		return lstLiveIn.size() != lstOldLiveIn.size() || !lstLiveIn.containsAll(lstOldLiveIn);//�����Ƿ�ı�
	}
	
	protected void analyzeLivenessByStmt()//ÿ�������Է���
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
	
	protected boolean removeDeadCode()//ɾ�������룬�����Ƿ�ı���Live-in
	{
		lstLiveOut.clear();
		for (SBlock nextBlock : lstNext) lstLiveOut.addAll(nextBlock.lstLiveIn);
		
		HashSet<Integer> lstNextIn = lstLiveOut;
		for (int i = lstStmt.size() - 1; i >= 0; --i)//����ö��ÿһ���
		{
			SStatement stmt = lstStmt.get(i);
			
			HashSet<Integer> lstLiveOut = stmt.getLiveOut();
			lstLiveOut.clear();
			lstLiveOut.addAll(lstNextIn);//����Live-out
			
			if (stmt.isDeadCode()) stmt.erase();//��������NOOP,USE��DEFȫ�����
			
			HashSet<Integer> lstLiveIn = stmt.getLiveIn();
			lstLiveIn.clear();
			
			lstLiveIn.addAll(lstLiveOut);//���¼���Live-in
			lstLiveIn.remove(stmt.getLiveDef());
			lstLiveIn.addAll(stmt.getLiveUse());
			
			lstNextIn = lstLiveIn;
		}
		
		HashSet<Integer> lstOldLiveIn = new HashSet<Integer>();
		lstOldLiveIn.addAll(lstLiveIn);//����ȫ��Ԫ��
				
		lstLiveIn.clear();
		lstLiveIn.addAll(lstNextIn);
		
		return lstLiveIn.size() != lstOldLiveIn.size() || !lstLiveIn.containsAll(lstOldLiveIn);//�����Ƿ�ı�
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
