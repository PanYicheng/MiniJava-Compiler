package kanga.visitor;

import java.util.Enumeration;

import kanga.syntaxtree.ALoadStmt;
import kanga.syntaxtree.AStoreStmt;
import kanga.syntaxtree.BinOp;
import kanga.syntaxtree.CJumpStmt;
import kanga.syntaxtree.CallStmt;
import kanga.syntaxtree.ErrorStmt;
import kanga.syntaxtree.Exp;
import kanga.syntaxtree.Goal;
import kanga.syntaxtree.HAllocate;
import kanga.syntaxtree.HLoadStmt;
import kanga.syntaxtree.HStoreStmt;
import kanga.syntaxtree.IntegerLiteral;
import kanga.syntaxtree.JumpStmt;
import kanga.syntaxtree.Label;
import kanga.syntaxtree.MoveStmt;
import kanga.syntaxtree.NoOpStmt;
import kanga.syntaxtree.Node;
import kanga.syntaxtree.NodeList;
import kanga.syntaxtree.NodeListOptional;
import kanga.syntaxtree.NodeOptional;
import kanga.syntaxtree.NodeSequence;
import kanga.syntaxtree.NodeToken;
import kanga.syntaxtree.Operator;
import kanga.syntaxtree.PassArgStmt;
import kanga.syntaxtree.PrintStmt;
import kanga.syntaxtree.Procedure;
import kanga.syntaxtree.Reg;
import kanga.syntaxtree.SimpleExp;
import kanga.syntaxtree.SpilledArg;
import kanga.syntaxtree.Stmt;
import kanga.syntaxtree.StmtList;

public class ConvertToMipsVisitor extends GJNoArguDepthFirst<String>
{
	private int nNormalStack;//非fp,ra的栈
	private String szCode = "";//生成的代码
	
	public String toString()//返回代码
	{
		return szCode;
	}
	
	private void print(String szLine)
	{
		szCode += "\t" + szLine + "\n";//直接换行
	}
	
	//
	// Auto class visitors--probably don't need to be overridden.
	//
	public String visit(NodeList n)
	{
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
			e.nextElement().accept(this);
		return null;
	}

	public String visit(NodeListOptional n)
	{
		if (n.present())
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
				e.nextElement().accept(this);
		return null;
	}

	public String visit(NodeOptional n)
	{
		if (n.present())
		{
			String szRet = n.node.accept(this);
			if (n.node instanceof Label) szCode += szRet + ":";//如果是标签，添加之
		}
		return null;
	}

	public String visit(NodeSequence n)
	{
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
			e.nextElement().accept(this);
		return null;
	}

	public String visit(NodeToken n) { return n.tokenImage; }

	//
	// User-generated visitor methods below
	//

	/**
	 * f0 -> "MAIN"
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 * f12 -> ( Procedure() )*
	 * f13 -> <EOF>
	 */
	public String visit(Goal n)//整个程序
	{		
		int nF5 = Integer.parseInt(n.f5.f0.tokenImage);
		int nStack = nNormalStack = 4 * nF5;//乘以四以变为字节大小
		nStack += 4;//加上ra的大小
		
		szCode += ".data\n";
		szCode += ".align 0\n";
		szCode += "endl: .asciiz \"\\n\"\n\n";
		
		szCode += ".data\n";
		szCode += ".align 0\n";
		szCode += "error: .asciiz \"ERROR: abnormal termination\\n\"\n\n";
				
		szCode += ".text\n";
		szCode += ".globl main\n";
		
		szCode += "main:\n";//加上标题
		
		print("sw $ra, -4($sp)");//保存返回地址
		
		print("move $fp, $sp");//设置上帧首址
		print("subu $sp, $sp, " + nStack);//设置本帧栈长
				
		n.f10.accept(this);
		
		print("addu $sp, $sp, " + nStack);//设置本帧栈长
		print("lw $ra, -4($sp)");//获取上帧首址
		
		print("j $ra");//返回
		szCode += "\n";
		
		n.f12.accept(this);//其他过程
		return null;
	}

	/**
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public String visit(StmtList n)//语句列表
	{
		n.f0.accept(this);
		return null;
	}

	/**
	 * f0 -> Label()
	 * f1 -> "["
	 * f2 -> IntegerLiteral()
	 * f3 -> "]"
	 * f4 -> "["
	 * f5 -> IntegerLiteral()
	 * f6 -> "]"
	 * f7 -> "["
	 * f8 -> IntegerLiteral()
	 * f9 -> "]"
	 * f10 -> StmtList()
	 * f11 -> "END"
	 */
	public String visit(Procedure n)//过程
	{
		String szF0 = n.f0.accept(this);
		
		int nF5 = Integer.parseInt(n.f5.f0.tokenImage);
		int nStack = nNormalStack = 4 * nF5;//乘以四以变为字节大小
		nStack += 8;//加上fp, ra的大小
		
		szCode += ".text\n";
		szCode += szF0 + ":\n";//加上标题
		
		print("sw $fp, -8($sp)");//保存上上帧首址
		print("sw $ra, -4($sp)");//保存返回地址
		
		print("move $fp, $sp");//设置上帧首址
		print("subu $sp, $sp, " + nStack);//设置本帧栈长
				
		n.f10.accept(this);
		
		print("addu $sp, $sp, " + nStack);//设置本帧栈长
		
		print("lw $fp, -8($sp)");//获取返回地址
		print("lw $ra, -4($sp)");//获取上帧首址
		
		print("j $ra");//返回
		szCode += "\n";
		
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
	 *       | ALoadStmt()
	 *       | AStoreStmt()
	 *       | PassArgStmt()
	 *       | CallStmt()
	 */
	public String visit(Stmt n)//语句
	{
		n.f0.accept(this);
		return null;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n)//空指令
	{
		print("nop");
		return null;
	}

	/**
	 * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n)//出错退出
	{
        print("la $a0, error");
        print("li $v0, 4");
        print("syscall");//输出错误
		print("li $v0, 10");
		print("syscall");//结束程序
		return null;
	}

	/**
	 * f0 -> "CJUMP"
	 * f1 -> Reg()
	 * f2 -> Label()
	 */
	public String visit(CJumpStmt n)//条件跳转
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		print("beqz " + szF1 + ", " + szF2);//输出beqz指令
		return null;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public String visit(JumpStmt n)//无条件跳转
	{
		print ("j " + n.f1.accept(this));//输出j指令
		return null;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Reg()
	 * f2 -> IntegerLiteral()
	 * f3 -> Reg()
	 */
	public String visit(HStoreStmt n)//内存存取
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		String szF3 = n.f3.accept(this);
		print("sw " + szF3 + ", " + szF2 + "(" + szF1 + ")");//输出lw指令
		return null;
	}

	/**
	 * f0 -> "HLOAD"
	 * f1 -> Reg()
	 * f2 -> Reg()
	 * f3 -> IntegerLiteral()
	 */
	public String visit(HLoadStmt n)//内存读取
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		String szF3 = n.f3.accept(this);
		print("lw " + szF1 + ", " + szF3 + "(" + szF2 + ")");//输出lw指令
		return null;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Reg()
	 * f2 -> Exp()
	 */
	public String visit(MoveStmt n)//移动
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		
		Node choice = n.f2.f0.choice;
		if (choice instanceof HAllocate) print("move " + szF1 + ", " + szF2);//直接移动
		else
			if (choice instanceof SimpleExp)
			{
				Node choice2 = ((SimpleExp) choice).f0.choice;
				if (choice2 instanceof IntegerLiteral)
					print("li " + szF1 + ", " + szF2);//移动立即数
				else
					if (choice2 instanceof Label)
						print("la " + szF1 + ", " + szF2);//移动标签
					else
						print("move " + szF1 + ", " + szF2);//直接移动
			}
			else
				if (choice instanceof BinOp)//比较运算
				{
					BinOp bin = (BinOp) choice;
					szF2 = bin.f1.accept(this);
					String szF3 = bin.f2.accept(this);
					
					String szOp = bin.f0.f0.choice.toString();
					Node choice2 = ((BinOp) choice).f2.f0.choice;
					
					if (choice2 instanceof IntegerLiteral)
					{
						String szNewF3 = szF2.equals("$v0") ? "$v1" : "$v0";
						print("li " + szNewF3 + ", " + szF3);
						szF3 = szNewF3;
					}
					else if (choice2 instanceof Label)
					{
						String szNewF3 = szF2.equals("$v0") ? "$v1" : "$v0";
						print("la " + szNewF3 + ", " + szF3);
						szF3 = szNewF3;
					}
					
					if (szOp.equals("LT"))
						print("slt " + szF1 + ", " + szF2 + ", " + szF3);
					else
						if (szOp.equals("PLUS"))
							print("add " + szF1 + ", " + szF2 + ", " + szF3);
						else
							if (szOp.equals("MINUS"))
								print("sub " + szF1 + ", " + szF2 + ", " + szF3);
							else
								print("mul " + szF1 + ", " + szF2 + ", " + szF3);
				}
		
		return null;
	}

	/**
	 * f0 -> "PRINT"
	 * f1 -> SimpleExp()
	 */
	public String visit(PrintStmt n)
	{
		String szF1 = n.f1.accept(this);//获取表达式
		
		Node choice = n.f1.f0.choice;
		if (choice instanceof IntegerLiteral)
			print("li $a0, " + szF1);//是立即数
		else
			if (choice instanceof Label)
				print("la $a0, " + szF1);//是标签
			else
				print("move $a0, " + szF1);//直接移动
		
		print("li $v0, 1");//设置v0为4
		print("syscall");
		
		print("la $a0, endl");
        print("li $v0, 4");
        print("syscall");//输出换行
		
		return null;
	}

	/**
	 * f0 -> "ALOAD"
	 * f1 -> Reg()
	 * f2 -> SpilledArg()
	 */
	public String visit(ALoadStmt n)
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		print("lw " + szF1 + ", " + szF2);//输出lw指令
		return null;
	}

	/**
	 * f0 -> "ASTORE"
	 * f1 -> SpilledArg()
	 * f2 -> Reg()
	 */
	public String visit(AStoreStmt n)
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		print("sw " + szF2 + ", " + szF1);//输出sw指令
		return null;
	}

	/**
	 * f0 -> "PASSARG"
	 * f1 -> IntegerLiteral()
	 * f2 -> Reg()
	 */
	public String visit(PassArgStmt n)
	{
		int nF1 = Integer.parseInt(n.f1.f0.tokenImage);//转为数字
		String szF2 = n.f2.accept(this);
		
		nF1 = -4 * (nF1 + 2);//保存到未来的ra,fp后面
		print("sw " + szF2 + ", " + nF1 + "($sp)");//输出sw指令
		return null;
	}

	/**
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 */
	public String visit(CallStmt n)
	{
		String szF1 = n.f1.accept(this);//获取表达式
		
		Node choice = n.f1.f0.choice;
		if (choice instanceof IntegerLiteral)
		{
			print("li $v0, " + szF1);//是立即数
			print("jalr $v0");//移到v0再跳转
		}
		else
			if (choice instanceof Label)
				print("jal " + szF1);//是标签
			else
				print("jalr " + szF1);//是寄存器
		return null;
	}

	/**
	 * f0 -> HAllocate()
	 *       | BinOp()
	 *       | SimpleExp()
	 */
	public String visit(Exp n)
	{
		return n.f0.accept(this);//直接返回表达式
	}

	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> SimpleExp()
	 */
	public String visit(HAllocate n)
	{
		String szF1 = n.f1.accept(this);//获取表达式
		
		Node choice = n.f1.f0.choice;
		if (choice instanceof IntegerLiteral)
			print("li $a0, " + szF1);//是立即数
		else
			if (choice instanceof Label)
				print("la $a0, " + szF1);//是标签
			else
				print("move $a0, " + szF1);//直接移动
		
		print("li $v0, 9");//设置v0为9
		print("syscall");
		
		return "$v0";
	}

	/**
	 * f0 -> Operator()
	 * f1 -> Reg()
	 * f2 -> SimpleExp()
	 */
	public String visit(BinOp n)
	{
		return null;//等待MOV指令处理
	}

	/**
	 * f0 -> "LT"
	 *       | "PLUS"
	 *       | "MINUS"
	 *       | "TIMES"
	 */
	public String visit(Operator n)
	{
		return null;//等待MOV指令处理
	}

	/**
	 * f0 -> "SPILLEDARG"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(SpilledArg n)
	{
		int nSpilled = Integer.parseInt(n.f1.f0.tokenImage);//获取栈中位置
		nSpilled *= 4;//乘以四变成内存单元号
		nSpilled = nNormalStack - 4 - nSpilled;//倒转过来
		
		return Integer.toString(nSpilled) + "($sp)";//返回内存位置
	}

	/**
	 * f0 -> Reg()
	 *       | IntegerLiteral()
	 *       | Label()
	 */
	public String visit(SimpleExp n)//SimpleExp直接返回
	{
		return n.f0.accept(this);
	}

	/**
	 * f0 -> "a0"
	 *       | "a1"
	 *       | "a2"
	 *       | "a3"
	 *       | "t0"
	 *       | "t1"
	 *       | "t2"
	 *       | "t3"
	 *       | "t4"
	 *       | "t5"
	 *       | "t6"
	 *       | "t7"
	 *       | "s0"
	 *       | "s1"
	 *       | "s2"
	 *       | "s3"
	 *       | "s4"
	 *       | "s5"
	 *       | "s6"
	 *       | "s7"
	 *       | "t8"
	 *       | "t9"
	 *       | "v0"
	 *       | "v1"
	 */
	public String visit(Reg n)
	{
		return "$" + n.f0.choice.toString();//寄存器前加美元符号
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n)//直接返回数字
	{
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Label n)//直接返回标识符
	{
		return n.f0.tokenImage;
	}
}
