package piglet.visitor;

import java.util.Enumeration;

import piglet.piglet2spiglet.PExp;
import piglet.syntaxtree.BinOp;
import piglet.syntaxtree.CJumpStmt;
import piglet.syntaxtree.Call;
import piglet.syntaxtree.ErrorStmt;
import piglet.syntaxtree.Exp;
import piglet.syntaxtree.Goal;
import piglet.syntaxtree.HAllocate;
import piglet.syntaxtree.HLoadStmt;
import piglet.syntaxtree.HStoreStmt;
import piglet.syntaxtree.IntegerLiteral;
import piglet.syntaxtree.JumpStmt;
import piglet.syntaxtree.Label;
import piglet.syntaxtree.MoveStmt;
import piglet.syntaxtree.NoOpStmt;
import piglet.syntaxtree.Node;
import piglet.syntaxtree.NodeList;
import piglet.syntaxtree.NodeListOptional;
import piglet.syntaxtree.NodeOptional;
import piglet.syntaxtree.NodeSequence;
import piglet.syntaxtree.NodeToken;
import piglet.syntaxtree.Operator;
import piglet.syntaxtree.PrintStmt;
import piglet.syntaxtree.Procedure;
import piglet.syntaxtree.Stmt;
import piglet.syntaxtree.StmtExp;
import piglet.syntaxtree.StmtList;
import piglet.syntaxtree.Temp;

public class Convert2SPigletVisitor extends GJDepthFirst<PExp, Node>
{	
	private int nCurrentTemp;//��ǰ����һ����ʱ����
	private String szCode = "";//����Ĵ���
	
	public String getCode()//��ȡ����
	{
		return szCode;
	}
	
	public Convert2SPigletVisitor(int nNextTemp)//������һ�����õ���ʱ����
	{
		nCurrentTemp = nNextTemp;
	}
	
	private void print(String szNewCode)//��Ӵ���
	{
		szCode += szNewCode;
	}
	
	private void print(PExp exp)//��ӱ��ʽ
	{
		if (exp != null) print(exp.toString());
	}
	
	private void println()//����
	{
		szCode += "\n";
	}
	
	private void println(String szNewCode)//��Ӵ������
	{
		szCode += szNewCode;
		println();
	}
	
	private void println(PExp exp)
	{
		if (exp != null) print(exp.toString());//��ӱ��ʽ����
		println();
	}
	
	public void format()//��ʽ������������
	{
		if (szCode == null) return;
		
		String[] lines = szCode.split("\\n");
		szCode = "";
		
		int nTab = 0, i;//��������
		
		for (String szLine : lines)
		{
			if (szLine.indexOf("END") == 0) --nTab;//��END��������
						
			if (szLine.indexOf("----") >= 0)
			{
				--nTab;
				for (i = 0; i < nTab; ++i) szCode += "\t";
				++nTab;
				szLine = szLine.replaceAll("----", "");
			}
			else for (i = 0; i < nTab; ++i) szCode += "\t";
			
			szCode += szLine + "\n";
			
			if (szLine.indexOf("BEGIN") == 0 || szLine.indexOf("MAIN") == 0) ++nTab;//��BEGIN/MAIN��������
		}
	}
	
	public PExp visit(NodeList n, Node argu)//����б�
	{
		PExp ret = null;
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			Node next = e.nextElement();
			PExp exp = next.accept(this, argu);
			if (next instanceof Exp)//�Ǳ��ʽ
			{
				if (argu instanceof Call && !exp.isTemp())//�����ʵ�Σ�������TEMP
				{
					int nTemp = nCurrentTemp++;
					print("MOVE TEMP " + nTemp + " ");
					println(exp);
					exp = new PExp("TEMP " + nTemp, PExp.TEMP);
				}
				
				if (ret == null) ret = exp;//��ӱ��ʽ
				else
				{
					ret.append(" ");
					ret.append(exp);
				}
			}
		}
		return ret;
	}

	public PExp visit(NodeListOptional n, Node argu)//��ѡ����б�
	{
		PExp ret = null;
		if (n.present())
		{
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
			{
				Node next = e.nextElement();
				PExp exp = next.accept(this, argu);
				if (next instanceof Exp)//�Ǳ��ʽ
				{
					if (argu instanceof Call && !exp.isTemp())//�����ʵ�Σ�������TEMP
					{
						int nTemp = nCurrentTemp++;
						print("MOVE TEMP " + nTemp + " ");
						println(exp);
						exp = new PExp("TEMP " + nTemp, PExp.TEMP);
					}
					
					if (ret == null) ret = exp;//��ӱ��ʽ
					else
					{
						ret.append(" ");
						ret.append(exp);
					}
				}
			}
		}
		return ret;
	}

	public PExp visit(NodeOptional n, Node argu)//��ѡ���
	{
		if (n.present())
		{
			PExp exp = n.node.accept(this, argu);
			if (argu instanceof StmtList && n.node instanceof Label)//�Ǳ�ǩ
			{
				print("----");
				print(exp);
				print("\t");
				return null;
			}
			else return exp;
		}
		else return null;
	}

	public PExp visit(NodeSequence n, Node argu)//�������
	{
		PExp ret = null;
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			Node next = e.nextElement();
			PExp exp = next.accept(this, argu);//�Ǳ��ʽ
			if (next instanceof Exp)
			{
				if (argu instanceof Call && !exp.isTemp())//�����ʵ�Σ�������TEMP
				{
					int nTemp = nCurrentTemp++;
					print("MOVE TEMP " + nTemp + " ");
					println(exp);
					exp = new PExp("TEMP " + nTemp, PExp.TEMP);
				}
				
				if (ret == null) ret = exp;//��ӱ��ʽ
				else
				{
					ret.append(" ");
					ret.append(exp);
				}
			}
		}
		return ret;
	}

	public PExp visit(NodeToken n, Node argu) { return null; }

	//
	// User-generated visitor methods below
	//

	/**
	 * f0 -> "MAIN"
	 * f1 -> StmtList()
	 * f2 -> "END"
	 * f3 -> ( Procedure() )*
	 * f4 -> <EOF>
	 */
	public PExp visit(Goal n, Node argu)//��������
	{
		println("MAIN");
		n.f1.accept(this, n);//��������
		println("END");
		
		n.f3.accept(this, n);//��������
		
		format();//��ʽ��
		return null;
	}

	/**
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public PExp visit(StmtList n, Node argu)//������
	{
		n.f0.accept(this, n);
		return null;
	}

	/**
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> StmtExp()
	 */
	public PExp visit(Procedure n, Node argu)//����
	{
		println();//���и���
		println(n.f0.accept(this, n) + " [ " + n.f2.accept(this, n) + " ]");//����
		n.f4.accept(this, n);//������
		return null;
	}

	/**
	 * f0 -> NoOpStmt()
	 *       | ErrorStmt()
	 *       | CJumpStmt()
	 *       | JumpStmt()
	 *       | HStoreStmt()
	 *       | HLoadStmt()
	 *       | MoveStmt()
	 *       | PrintStmt()
	 */
	public PExp visit(Stmt n, Node argu)//���
	{
		n.f0.accept(this, n);
		return null;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public PExp visit(NoOpStmt n, Node argu)//NOOP
	{
		println("NOOP");
		return null;
	}

	/**
	 * f0 -> "ERROR"
	 */
	public PExp visit(ErrorStmt n, Node argu)//ERROR
	{
		println("ERROR");
		return null;
	}

	/**
	 * f0 -> "CJUMP"
	 * f1 -> Exp()
	 * f2 -> Label()
	 */
	public PExp visit(CJumpStmt n, Node argu)//������ת
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		
		if (!exp1.isTemp())//���ʽ1������TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//����TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		
		println("CJUMP " + exp1 + " " + exp2);
		return null;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public PExp visit(JumpStmt n, Node argu)//��ת
	{
		PExp exp = n.f1.accept(this, n);
		println("JUMP " + exp);//ֱ����ת
		return null;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Exp()
	 * f2 -> IntegerLiteral()
	 * f3 -> Exp()
	 */
	public PExp visit(HStoreStmt n, Node argu)//�洢
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		PExp exp3 = n.f3.accept(this, n);
		
		if (!exp1.isTemp())//���ʽ1������TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//����TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
				
		if (!exp3.isTemp())//���ʽ3������TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp3);//����TEMP
			exp3 = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		
		println("HSTORE " + exp1 + " " + exp2 + " " + exp3);
		return null;
	}

	/**
	 * f0 -> "HLOAD"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 * f3 -> IntegerLiteral()
	 */
	public PExp visit(HLoadStmt n, Node argu)//��ȡ
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		PExp exp3 = n.f3.accept(this, n);
		
		if (!exp2.isTemp())//���ʽ2������TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp2);//����TEMP
			exp2 = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		
		println("HLOAD " + exp1 + " " + exp2 + " " + exp3);
		return null;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 */
	public PExp visit(MoveStmt n, Node argu)//����
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		
		println("MOVE " + exp1 + " " + exp2);//ֱ������
		return null;
	}

	/**
	 * f0 -> "PRINT"
	 * f1 -> Exp()
	 */
	public PExp visit(PrintStmt n, Node argu)//��ӡ
	{
		PExp exp = n.f1.accept(this, n);
		
		if (!exp.isSimple())//�����Ǽ򵥱��ʽ
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp);//����TEMP
			exp = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		
		println("PRINT " + exp);
		return null;
	}

	/**
	 * f0 -> StmtExp()
	 *       | Call()
	 *       | HAllocate()
	 *       | BinOp()
	 *       | Temp()
	 *       | IntegerLiteral()
	 *       | Label()
	 */
	public PExp visit(Exp n, Node argu)//���ʽ
	{
		return n.f0.accept(this, n);
	}

	/**
	 * f0 -> "BEGIN"
	 * f1 -> StmtList()
	 * f2 -> "RETURN"
	 * f3 -> Exp()
	 * f4 -> "END"
	 */
	public PExp visit(StmtExp n, Node argu)//�����ʽ
	{
		PExp ret = null;
		
		if (argu instanceof Procedure)//����ǹ�����
		{
			println("BEGIN");
			n.f1.accept(this, n);
			PExp exp = n.f3.accept(this, n);
			
			if (!exp.isSimple())//���صı����Ǽ򵥱��ʽ
			{
				int nTemp = nCurrentTemp++;
				println("MOVE TEMP " + nTemp + " " + exp);//����TEMP
				exp = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
			}
			
			println("RETURN " + exp);
			println("END");
			
			ret = exp;
		}
		else
		{
			n.f1.accept(this, n);//ֱ�Ӵ�ӡ���
			ret = n.f3.accept(this, n);//���ط���ֵ
		}
		
		return ret;
	}

	/**
	 * f0 -> "CALL"
	 * f1 -> Exp()
	 * f2 -> "("
	 * f3 -> ( Exp() )*
	 * f4 -> ")"
	 */
	public PExp visit(Call n, Node argu)//���̵���
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f3.accept(this, n);
		
		PExp ret = new PExp("CALL ");
		
		if (!exp1.isSimple())//���ʽ1�����Ǽ򵥱��ʽ
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//����TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		ret.append(exp1);
		ret.append(" (" + exp2 + ")");//ʵ��
		
		return ret;
	}

	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> Exp()
	 */
	public PExp visit(HAllocate n, Node argu)//�����ڴ�
	{
		PExp exp = n.f1.accept(this, n);
		PExp ret = new PExp("HALLOCATE ");
		
		if (!exp.isSimple())//���ʽ�����Ǽ򵥱��ʽ
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp);//����TEMP
			exp = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		ret.append(exp);
		
		return ret;
	}

	/**
	 * f0 -> Operator()
	 * f1 -> Exp()
	 * f2 -> Exp()
	 */
	public PExp visit(BinOp n, Node argu)
	{
		PExp ret = n.f0.accept(this, n);
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		
		if (!exp1.isTemp())//���ʽ1������TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//����TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		ret.append(" " + exp1);
		
		if (!exp2.isSimple())//���ʽ2�����Ǽ򵥱��ʽ
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp2);//����TEMP
			exp2 = new PExp("TEMP " + nTemp, PExp.TEMP);//�滻��TEMP
		}
		ret.append(" " + exp2);
		
		return ret;
	}

	/**
	 * f0 -> "LT"
	 *       | "PLUS"
	 *       | "MINUS"
	 *       | "TIMES"
	 */
	public PExp visit(Operator n, Node argu)//�Ƚ������
	{
		return new PExp(n.f0.choice.toString());
	}

	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public PExp visit(Temp n, Node argu)//��ʱ����
	{
		PExp ret= new PExp("TEMP ", PExp.TEMP);
		ret.append(n.f1.accept(this, n));
		return ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public PExp visit(IntegerLiteral n, Node argu)//����
	{
		return new PExp(n.f0.toString(), PExp.SIMPLE);
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public PExp visit(Label n, Node argu)//��ǩ
	{
		return new PExp(n.f0.toString(), PExp.SIMPLE);
	}
}

