package spiglet.syntaxtree.special;

import spiglet.syntaxtree.IntegerLiteral;
import spiglet.syntaxtree.Node;
import spiglet.syntaxtree.NodeToken;
import spiglet.syntaxtree.Temp;
import spiglet.visitor.GJNoArguVisitor;
import spiglet.visitor.GJVisitor;
import spiglet.visitor.GJVoidVisitor;
import spiglet.visitor.Visitor;

public class MoveTempStmt implements Node//MOVE TEMP1 TEMP2
{
	private static final long serialVersionUID = -5277071680126239976L;
	public NodeToken f0;
	public Temp f1;
	public Temp f2;
	
	public MoveTempStmt(int x, int y)
	{
		f0 = new NodeToken("MOVE");
		f1 = new Temp(new IntegerLiteral(new NodeToken(Integer.toString(x))));
		f2 = new Temp(new IntegerLiteral(new NodeToken(Integer.toString(y))));
	}
	
	public MoveTempStmt(Temp n0, Temp n1)
	{
		f0 = new NodeToken("MOVE");
		f1 = n0;
		f2 = n1;
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

