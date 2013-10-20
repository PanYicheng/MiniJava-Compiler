package spiglet.syntaxtree.special;

import spiglet.syntaxtree.Node;
import spiglet.syntaxtree.NodeToken;
import spiglet.visitor.GJNoArguVisitor;
import spiglet.visitor.GJVisitor;
import spiglet.visitor.GJVoidVisitor;
import spiglet.visitor.Visitor;

public class Reg implements Node
{
	private static final long serialVersionUID = -2103128912160048021L;
	public NodeToken f0;
	
	private int nIndex;//±¾ÉíµÄ±àºÅ
		
	public Reg(int nIndex, NodeToken n0)
	{
		this.nIndex = nIndex; 
		f0 = n0;
	}
	
	public Reg(int nIndex, String szName)
	{
		this.nIndex = nIndex;
		f0 = new NodeToken(szName);
	}
	
	public String toString()
	{
		return f0.tokenImage;
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof Reg)) return false;
		
		Reg other = (Reg) o;
		return f0.tokenImage.equals(other.f0.tokenImage);
	}
	
	public int getIndex()
	{
		return nIndex;
	}
	
	public void accept(Visitor v) { }
	public <R,A> R accept(GJVisitor<R,A> v, A argu) { return null; }
	public <R> R accept(GJNoArguVisitor<R> v) { return null; }
	public <A> void accept(GJVoidVisitor<A> v, A argu) { }
}
