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
	private int nNormalStack;//��fp,ra��ջ
	private String szCode = "";//���ɵĴ���
	
	public String toString()//���ش���
	{
		return szCode;
	}
	
	private void print(String szLine)
	{
		szCode += "\t" + szLine + "\n";//ֱ�ӻ���
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
			if (n.node instanceof Label) szCode += szRet + ":";//����Ǳ�ǩ�����֮
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
	public String visit(Goal n)//��������
	{		
		int nF5 = Integer.parseInt(n.f5.f0.tokenImage);
		int nStack = nNormalStack = 4 * nF5;//�������Ա�Ϊ�ֽڴ�С
		nStack += 4;//����ra�Ĵ�С
		
		szCode += ".data\n";
		szCode += ".align 0\n";
		szCode += "endl: .asciiz \"\\n\"\n\n";
		
		szCode += ".data\n";
		szCode += ".align 0\n";
		szCode += "error: .asciiz \"ERROR: abnormal termination\\n\"\n\n";
				
		szCode += ".text\n";
		szCode += ".globl main\n";
		
		szCode += "main:\n";//���ϱ���
		
		print("sw $ra, -4($sp)");//���淵�ص�ַ
		
		print("move $fp, $sp");//������֡��ַ
		print("subu $sp, $sp, " + nStack);//���ñ�֡ջ��
				
		n.f10.accept(this);
		
		print("addu $sp, $sp, " + nStack);//���ñ�֡ջ��
		print("lw $ra, -4($sp)");//��ȡ��֡��ַ
		
		print("j $ra");//����
		szCode += "\n";
		
		n.f12.accept(this);//��������
		return null;
	}

	/**
	 * f0 -> ( ( Label() )? Stmt() )*
	 */
	public String visit(StmtList n)//����б�
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
	public String visit(Procedure n)//����
	{
		String szF0 = n.f0.accept(this);
		
		int nF5 = Integer.parseInt(n.f5.f0.tokenImage);
		int nStack = nNormalStack = 4 * nF5;//�������Ա�Ϊ�ֽڴ�С
		nStack += 8;//����fp, ra�Ĵ�С
		
		szCode += ".text\n";
		szCode += szF0 + ":\n";//���ϱ���
		
		print("sw $fp, -8($sp)");//��������֡��ַ
		print("sw $ra, -4($sp)");//���淵�ص�ַ
		
		print("move $fp, $sp");//������֡��ַ
		print("subu $sp, $sp, " + nStack);//���ñ�֡ջ��
				
		n.f10.accept(this);
		
		print("addu $sp, $sp, " + nStack);//���ñ�֡ջ��
		
		print("lw $fp, -8($sp)");//��ȡ���ص�ַ
		print("lw $ra, -4($sp)");//��ȡ��֡��ַ
		
		print("j $ra");//����
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
	public String visit(Stmt n)//���
	{
		n.f0.accept(this);
		return null;
	}

	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n)//��ָ��
	{
		print("nop");
		return null;
	}

	/**
	 * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n)//�����˳�
	{
        print("la $a0, error");
        print("li $v0, 4");
        print("syscall");//�������
		print("li $v0, 10");
		print("syscall");//��������
		return null;
	}

	/**
	 * f0 -> "CJUMP"
	 * f1 -> Reg()
	 * f2 -> Label()
	 */
	public String visit(CJumpStmt n)//������ת
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		print("beqz " + szF1 + ", " + szF2);//���beqzָ��
		return null;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public String visit(JumpStmt n)//��������ת
	{
		print ("j " + n.f1.accept(this));//���jָ��
		return null;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Reg()
	 * f2 -> IntegerLiteral()
	 * f3 -> Reg()
	 */
	public String visit(HStoreStmt n)//�ڴ��ȡ
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		String szF3 = n.f3.accept(this);
		print("sw " + szF3 + ", " + szF2 + "(" + szF1 + ")");//���lwָ��
		return null;
	}

	/**
	 * f0 -> "HLOAD"
	 * f1 -> Reg()
	 * f2 -> Reg()
	 * f3 -> IntegerLiteral()
	 */
	public String visit(HLoadStmt n)//�ڴ��ȡ
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		String szF3 = n.f3.accept(this);
		print("lw " + szF1 + ", " + szF3 + "(" + szF2 + ")");//���lwָ��
		return null;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Reg()
	 * f2 -> Exp()
	 */
	public String visit(MoveStmt n)//�ƶ�
	{
		String szF1 = n.f1.accept(this);
		String szF2 = n.f2.accept(this);
		
		Node choice = n.f2.f0.choice;
		if (choice instanceof HAllocate) print("move " + szF1 + ", " + szF2);//ֱ���ƶ�
		else
			if (choice instanceof SimpleExp)
			{
				Node choice2 = ((SimpleExp) choice).f0.choice;
				if (choice2 instanceof IntegerLiteral)
					print("li " + szF1 + ", " + szF2);//�ƶ�������
				else
					if (choice2 instanceof Label)
						print("la " + szF1 + ", " + szF2);//�ƶ���ǩ
					else
						print("move " + szF1 + ", " + szF2);//ֱ���ƶ�
			}
			else
				if (choice instanceof BinOp)//�Ƚ�����
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
		String szF1 = n.f1.accept(this);//��ȡ���ʽ
		
		Node choice = n.f1.f0.choice;
		if (choice instanceof IntegerLiteral)
			print("li $a0, " + szF1);//��������
		else
			if (choice instanceof Label)
				print("la $a0, " + szF1);//�Ǳ�ǩ
			else
				print("move $a0, " + szF1);//ֱ���ƶ�
		
		print("li $v0, 1");//����v0Ϊ4
		print("syscall");
		
		print("la $a0, endl");
        print("li $v0, 4");
        print("syscall");//�������
		
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
		print("lw " + szF1 + ", " + szF2);//���lwָ��
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
		print("sw " + szF2 + ", " + szF1);//���swָ��
		return null;
	}

	/**
	 * f0 -> "PASSARG"
	 * f1 -> IntegerLiteral()
	 * f2 -> Reg()
	 */
	public String visit(PassArgStmt n)
	{
		int nF1 = Integer.parseInt(n.f1.f0.tokenImage);//תΪ����
		String szF2 = n.f2.accept(this);
		
		nF1 = -4 * (nF1 + 2);//���浽δ����ra,fp����
		print("sw " + szF2 + ", " + nF1 + "($sp)");//���swָ��
		return null;
	}

	/**
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 */
	public String visit(CallStmt n)
	{
		String szF1 = n.f1.accept(this);//��ȡ���ʽ
		
		Node choice = n.f1.f0.choice;
		if (choice instanceof IntegerLiteral)
		{
			print("li $v0, " + szF1);//��������
			print("jalr $v0");//�Ƶ�v0����ת
		}
		else
			if (choice instanceof Label)
				print("jal " + szF1);//�Ǳ�ǩ
			else
				print("jalr " + szF1);//�ǼĴ���
		return null;
	}

	/**
	 * f0 -> HAllocate()
	 *       | BinOp()
	 *       | SimpleExp()
	 */
	public String visit(Exp n)
	{
		return n.f0.accept(this);//ֱ�ӷ��ر��ʽ
	}

	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> SimpleExp()
	 */
	public String visit(HAllocate n)
	{
		String szF1 = n.f1.accept(this);//��ȡ���ʽ
		
		Node choice = n.f1.f0.choice;
		if (choice instanceof IntegerLiteral)
			print("li $a0, " + szF1);//��������
		else
			if (choice instanceof Label)
				print("la $a0, " + szF1);//�Ǳ�ǩ
			else
				print("move $a0, " + szF1);//ֱ���ƶ�
		
		print("li $v0, 9");//����v0Ϊ9
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
		return null;//�ȴ�MOVָ���
	}

	/**
	 * f0 -> "LT"
	 *       | "PLUS"
	 *       | "MINUS"
	 *       | "TIMES"
	 */
	public String visit(Operator n)
	{
		return null;//�ȴ�MOVָ���
	}

	/**
	 * f0 -> "SPILLEDARG"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(SpilledArg n)
	{
		int nSpilled = Integer.parseInt(n.f1.f0.tokenImage);//��ȡջ��λ��
		nSpilled *= 4;//�����ı���ڴ浥Ԫ��
		nSpilled = nNormalStack - 4 - nSpilled;//��ת����
		
		return Integer.toString(nSpilled) + "($sp)";//�����ڴ�λ��
	}

	/**
	 * f0 -> Reg()
	 *       | IntegerLiteral()
	 *       | Label()
	 */
	public String visit(SimpleExp n)//SimpleExpֱ�ӷ���
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
		return "$" + n.f0.choice.toString();//�Ĵ���ǰ����Ԫ����
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n)//ֱ�ӷ�������
	{
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Label n)//ֱ�ӷ��ر�ʶ��
	{
		return n.f0.tokenImage;
	}
}
