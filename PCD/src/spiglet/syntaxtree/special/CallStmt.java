package spiglet.syntaxtree.special;

import spiglet.syntaxtree.Node;
import spiglet.syntaxtree.NodeToken;
import spiglet.syntaxtree.SimpleExp;
import spiglet.visitor.GJNoArguVisitor;
import spiglet.visitor.GJVisitor;
import spiglet.visitor.GJVoidVisitor;
import spiglet.visitor.Visitor;

public class CallStmt implements Node//传参压栈表达式
{
	private static final long serialVersionUID = -5277071680126239976L;
	public NodeToken f0;
	public SimpleExp f1;
	
	public CallStmt(SimpleExp n0)
	{
		f0 = new NodeToken("CALL");
		f1 = n0;
	}
	
	public void accept(Visitor v) { }
	
	public <R,A> R accept(GJVisitor<R,A> v, A argu)
	{ 
		return v.visit(this, argu);
	}
	
	public <R> R accept(GJNoArguVisitor<R> v) { return null; }
	
	public <A> void accept(GJVoidVisitor<A> v, A argu)
	{
		v.visit(this, argu);
	}
}

