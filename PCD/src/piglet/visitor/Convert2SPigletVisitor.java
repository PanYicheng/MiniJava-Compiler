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
	private int nCurrentTemp;//当前的下一个临时变量
	private String szCode = "";//输出的代码
	
	public String getCode()//获取代码
	{
		return szCode;
	}
	
	public Convert2SPigletVisitor(int nNextTemp)//设置下一个可用的临时变量
	{
		nCurrentTemp = nNextTemp;
	}
	
	private void print(String szNewCode)//添加代码
	{
		szCode += szNewCode;
	}
	
	private void print(PExp exp)//添加表达式
	{
		if (exp != null) print(exp.toString());
	}
	
	private void println()//换行
	{
		szCode += "\n";
	}
	
	private void println(String szNewCode)//添加代码后换行
	{
		szCode += szNewCode;
		println();
	}
	
	private void println(PExp exp)
	{
		if (exp != null) print(exp.toString());//添加表达式后换行
		println();
	}
	
	public void format()//格式化，调整缩进
	{
		if (szCode == null) return;
		
		String[] lines = szCode.split("\\n");
		szCode = "";
		
		int nTab = 0, i;//缩进个数
		
		for (String szLine : lines)
		{
			if (szLine.indexOf("END") == 0) --nTab;//遇END减少缩进
						
			if (szLine.indexOf("----") >= 0)
			{
				--nTab;
				for (i = 0; i < nTab; ++i) szCode += "\t";
				++nTab;
				szLine = szLine.replaceAll("----", "");
			}
			else for (i = 0; i < nTab; ++i) szCode += "\t";
			
			szCode += szLine + "\n";
			
			if (szLine.indexOf("BEGIN") == 0 || szLine.indexOf("MAIN") == 0) ++nTab;//遇BEGIN/MAIN增加缩进
		}
	}
	
	public PExp visit(NodeList n, Node argu)//结点列表
	{
		PExp ret = null;
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			Node next = e.nextElement();
			PExp exp = next.accept(this, argu);
			if (next instanceof Exp)//是表达式
			{
				if (argu instanceof Call && !exp.isTemp())//如果是实参，必须是TEMP
				{
					int nTemp = nCurrentTemp++;
					print("MOVE TEMP " + nTemp + " ");
					println(exp);
					exp = new PExp("TEMP " + nTemp, PExp.TEMP);
				}
				
				if (ret == null) ret = exp;//添加表达式
				else
				{
					ret.append(" ");
					ret.append(exp);
				}
			}
		}
		return ret;
	}

	public PExp visit(NodeListOptional n, Node argu)//可选结点列表
	{
		PExp ret = null;
		if (n.present())
		{
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
			{
				Node next = e.nextElement();
				PExp exp = next.accept(this, argu);
				if (next instanceof Exp)//是表达式
				{
					if (argu instanceof Call && !exp.isTemp())//如果是实参，必须是TEMP
					{
						int nTemp = nCurrentTemp++;
						print("MOVE TEMP " + nTemp + " ");
						println(exp);
						exp = new PExp("TEMP " + nTemp, PExp.TEMP);
					}
					
					if (ret == null) ret = exp;//添加表达式
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

	public PExp visit(NodeOptional n, Node argu)//可选结点
	{
		if (n.present())
		{
			PExp exp = n.node.accept(this, argu);
			if (argu instanceof StmtList && n.node instanceof Label)//是标签
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

	public PExp visit(NodeSequence n, Node argu)//结点序列
	{
		PExp ret = null;
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			Node next = e.nextElement();
			PExp exp = next.accept(this, argu);//是表达式
			if (next instanceof Exp)
			{
				if (argu instanceof Call && !exp.isTemp())//如果是实参，必须是TEMP
				{
					int nTemp = nCurrentTemp++;
					print("MOVE TEMP " + nTemp + " ");
					println(exp);
					exp = new PExp("TEMP " + nTemp, PExp.TEMP);
				}
				
				if (ret == null) ret = exp;//添加表达式
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
	public PExp visit(Goal n, Node argu)//主程序体
	{
		println("MAIN");
		n.f1.accept(this, n);//主程序体
		println("END");
		
		n.f3.accept(this, n);//其他程序
		
		format();//格式化
		return null;
	}

	/**
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public PExp visit(StmtList n, Node argu)//多个语句
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
	public PExp visit(Procedure n, Node argu)//过程
	{
		println();//换行隔开
		println(n.f0.accept(this, n) + " [ " + n.f2.accept(this, n) + " ]");//声明
		n.f4.accept(this, n);//过程体
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
	public PExp visit(Stmt n, Node argu)//语句
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
	public PExp visit(CJumpStmt n, Node argu)//条件跳转
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		
		if (!exp1.isTemp())//表达式1必须是TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//移入TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
		}
		
		println("CJUMP " + exp1 + " " + exp2);
		return null;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public PExp visit(JumpStmt n, Node argu)//跳转
	{
		PExp exp = n.f1.accept(this, n);
		println("JUMP " + exp);//直接跳转
		return null;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Exp()
	 * f2 -> IntegerLiteral()
	 * f3 -> Exp()
	 */
	public PExp visit(HStoreStmt n, Node argu)//存储
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		PExp exp3 = n.f3.accept(this, n);
		
		if (!exp1.isTemp())//表达式1必须是TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//移入TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
		}
				
		if (!exp3.isTemp())//表达式3必须是TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp3);//移入TEMP
			exp3 = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
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
	public PExp visit(HLoadStmt n, Node argu)//读取
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		PExp exp3 = n.f3.accept(this, n);
		
		if (!exp2.isTemp())//表达式2必须是TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp2);//移入TEMP
			exp2 = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
		}
		
		println("HLOAD " + exp1 + " " + exp2 + " " + exp3);
		return null;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 */
	public PExp visit(MoveStmt n, Node argu)//移入
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f2.accept(this, n);
		
		println("MOVE " + exp1 + " " + exp2);//直接移入
		return null;
	}

	/**
	 * f0 -> "PRINT"
	 * f1 -> Exp()
	 */
	public PExp visit(PrintStmt n, Node argu)//打印
	{
		PExp exp = n.f1.accept(this, n);
		
		if (!exp.isSimple())//必须是简单表达式
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp);//移入TEMP
			exp = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
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
	public PExp visit(Exp n, Node argu)//表达式
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
	public PExp visit(StmtExp n, Node argu)//语句表达式
	{
		PExp ret = null;
		
		if (argu instanceof Procedure)//如果是过程体
		{
			println("BEGIN");
			n.f1.accept(this, n);
			PExp exp = n.f3.accept(this, n);
			
			if (!exp.isSimple())//返回的必须是简单表达式
			{
				int nTemp = nCurrentTemp++;
				println("MOVE TEMP " + nTemp + " " + exp);//移入TEMP
				exp = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
			}
			
			println("RETURN " + exp);
			println("END");
			
			ret = exp;
		}
		else
		{
			n.f1.accept(this, n);//直接打印语句
			ret = n.f3.accept(this, n);//返回返回值
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
	public PExp visit(Call n, Node argu)//过程调用
	{
		PExp exp1 = n.f1.accept(this, n);
		PExp exp2 = n.f3.accept(this, n);
		
		PExp ret = new PExp("CALL ");
		
		if (!exp1.isSimple())//表达式1必须是简单表达式
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//移入TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
		}
		ret.append(exp1);
		ret.append(" (" + exp2 + ")");//实参
		
		return ret;
	}

	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> Exp()
	 */
	public PExp visit(HAllocate n, Node argu)//分配内存
	{
		PExp exp = n.f1.accept(this, n);
		PExp ret = new PExp("HALLOCATE ");
		
		if (!exp.isSimple())//表达式必须是简单表达式
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp);//移入TEMP
			exp = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
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
		
		if (!exp1.isTemp())//表达式1必须是TEMP
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp1);//移入TEMP
			exp1 = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
		}
		ret.append(" " + exp1);
		
		if (!exp2.isSimple())//表达式2必须是简单表达式
		{
			int nTemp = nCurrentTemp++;
			println("MOVE TEMP " + nTemp + " " + exp2);//移入TEMP
			exp2 = new PExp("TEMP " + nTemp, PExp.TEMP);//替换成TEMP
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
	public PExp visit(Operator n, Node argu)//比较运算符
	{
		return new PExp(n.f0.choice.toString());
	}

	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public PExp visit(Temp n, Node argu)//临时变量
	{
		PExp ret= new PExp("TEMP ", PExp.TEMP);
		ret.append(n.f1.accept(this, n));
		return ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public PExp visit(IntegerLiteral n, Node argu)//数字
	{
		return new PExp(n.f0.toString(), PExp.SIMPLE);
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public PExp visit(Label n, Node argu)//标签
	{
		return new PExp(n.f0.toString(), PExp.SIMPLE);
	}
}

