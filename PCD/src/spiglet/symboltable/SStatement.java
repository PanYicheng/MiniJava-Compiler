package spiglet.symboltable;

import java.util.HashSet;
import java.util.Vector;

import spiglet.syntaxtree.NoOpStmt;
import spiglet.syntaxtree.Node;
import spiglet.syntaxtree.Temp;

public class SStatement
{
	private Node node;//���
	private String szLabel;
	
	private int nLiveDef = -1, nIndex;//������������
	private HashSet<Integer> lstLiveUse = new HashSet<Integer>();//ʹ�ñ���
	
	private HashSet<Integer> lstLiveIn = new HashSet<Integer>();//Live-in����
	private HashSet<Integer> lstLiveOut = new HashSet<Integer>();//Live-out����
	
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
	
	public SStatement(Node node)//�������
	{
		setNode(node);
	}
	
	protected void setNode(Node node)//���ý��
	{
		this.node = node;
	}
		
	public Node getNode()//��ȡ�����Ľ��
	{
		return node;
	}
	
	public void addLiveDef(int nLiveDef)//���붨�����ʱ��Ԫ
	{		
		this.nLiveDef = nLiveDef;
	}
	
	public void addLiveUse(int nLiveUse)//����ʹ�õ���ʱ��Ԫ
	{
		lstLiveUse.add(nLiveUse);
	}
	
	public int getLiveDef()//��ȡֵ�Ķ���
	{
		return nLiveDef;
	}
	
	public HashSet<Integer> getLiveUse()//��ȡֵ��ʹ��
	{
		return lstLiveUse;
	}
	
	protected void anaylzeLiveness(HashSet<Integer> _lstLiveOut)//��������
	{
		lstLiveOut.clear();
		lstLiveOut.addAll(_lstLiveOut);//����Live-out
		
		lstLiveIn.clear();
		
		lstLiveIn.addAll(lstLiveOut);
		lstLiveIn.remove(nLiveDef);
		lstLiveIn.addAll(lstLiveUse);
	}
		
	public HashSet<Integer> getLiveIn()//��ȡLive-in
	{
		return lstLiveIn;
	}
	
	public HashSet<Integer> getLiveOut()//��ȡLive-out
	{
		return lstLiveOut;
	}
	
	protected boolean isDeadCode()//�Ƿ�������
	{
		return nLiveDef >= 0 && !lstLiveOut.contains(nLiveDef);
	}
	
	protected void erase()//��ΪNOOP
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
