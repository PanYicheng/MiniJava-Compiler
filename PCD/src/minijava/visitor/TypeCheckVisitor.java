package minijava.visitor;

import java.util.Enumeration;
import java.util.HashSet;

import minijava.symboltable.MClass;
import minijava.symboltable.MClassList;
import minijava.symboltable.MIdentifier;
import minijava.symboltable.MMethod;
import minijava.symboltable.MActualParamList;
import minijava.symboltable.MType;
import minijava.symboltable.MVar;
import minijava.syntaxtree.AllocationExpression;
import minijava.syntaxtree.AndExpression;
import minijava.syntaxtree.ArrayAllocationExpression;
import minijava.syntaxtree.ArrayAssignmentStatement;
import minijava.syntaxtree.ArrayLength;
import minijava.syntaxtree.ArrayLookup;
import minijava.syntaxtree.ArrayType;
import minijava.syntaxtree.AssignmentStatement;
import minijava.syntaxtree.Block;
import minijava.syntaxtree.BooleanType;
import minijava.syntaxtree.BracketExpression;
import minijava.syntaxtree.ClassDeclaration;
import minijava.syntaxtree.ClassExtendsDeclaration;
import minijava.syntaxtree.CompareExpression;
import minijava.syntaxtree.Expression;
import minijava.syntaxtree.ExpressionList;
import minijava.syntaxtree.ExpressionRest;
import minijava.syntaxtree.FalseLiteral;
import minijava.syntaxtree.FormalParameter;
import minijava.syntaxtree.FormalParameterList;
import minijava.syntaxtree.FormalParameterRest;
import minijava.syntaxtree.Goal;
import minijava.syntaxtree.Identifier;
import minijava.syntaxtree.IfStatement;
import minijava.syntaxtree.IntegerLiteral;
import minijava.syntaxtree.IntegerType;
import minijava.syntaxtree.MainClass;
import minijava.syntaxtree.MessageSend;
import minijava.syntaxtree.MethodDeclaration;
import minijava.syntaxtree.MinusExpression;
import minijava.syntaxtree.Node;
import minijava.syntaxtree.NodeList;
import minijava.syntaxtree.NodeListOptional;
import minijava.syntaxtree.NodeOptional;
import minijava.syntaxtree.NodeSequence;
import minijava.syntaxtree.NodeToken;
import minijava.syntaxtree.NotExpression;
import minijava.syntaxtree.PlusExpression;
import minijava.syntaxtree.PrimaryExpression;
import minijava.syntaxtree.PrintStatement;
import minijava.syntaxtree.Statement;
import minijava.syntaxtree.ThisExpression;
import minijava.syntaxtree.TimesExpression;
import minijava.syntaxtree.TrueLiteral;
import minijava.syntaxtree.Type;
import minijava.syntaxtree.TypeDeclaration;
import minijava.syntaxtree.VarDeclaration;
import minijava.syntaxtree.WhileStatement;
import minijava.typecheck.TypeError;


public class TypeCheckVisitor extends GJDepthFirst<MIdentifier, MType>//�����ű������ͼ��
{
	private MClassList classList;
	
	// Auto class visitors--probably don't need to be overridden.
	public MIdentifier visit(NodeList n, MType argu)
	{
		MIdentifier ret=null;
		
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			e.nextElement().accept(this,argu);
		}
		
		return ret;
	}
	
	public MIdentifier visit(NodeListOptional n, MType argu)
	{
		if (n.present())
		{
			MIdentifier ret=null;
			
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
			{
				e.nextElement().accept(this,argu);
			}
			
			return ret;
		}
		
		else return null;
	}

	public MIdentifier visit(NodeOptional n, MType argu)
	{
		if (n.present()) return n.node.accept(this,argu);
		else return null;
	}
	
	public MIdentifier visit(NodeSequence n, MType argu)
	{
		MIdentifier ret=null;
		
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			e.nextElement().accept(this,argu);
		}
		
		return ret;
	}
	
	public MIdentifier visit(NodeToken n, MType argu) { return null; }
	
	
//User-generated visitor methods below

	/**
	* f0 -> MainClass()
	* f1 -> ( TypeDeclaration() )*
	* f2 -> <EOF>
	*/
	public MIdentifier visit(Goal n, MType argu)//�����������
	{
		this.classList = (MClassList) argu;
		
		n.f0.accept(this, classList);
		n.f1.accept(this, classList);
		n.f2.accept(this, classList);
		
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> "public"
	* f4 -> "static"
	* f5 -> "void"
	* f6 -> "main"
	* f7 -> "("
	* f8 -> "String"
	* f9 -> "["
	* f10 -> "]"
	* f11 -> Identifier()
	* f12 -> ")"
	* f13 -> "{"
	* f14 -> PrintStatement()
	* f15 -> "}"
	* f16 -> "}"
	*/
	public MIdentifier visit(MainClass n, MType argu)//��������
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//������
		MClass newClass = classList.get(szClass, n.f1.f0.beginLine);//��ȡ����
		
		n.f2.accept(this, newClass);//������
		n.f3.accept(this, newClass);
		n.f4.accept(this, newClass);
		n.f5.accept(this, newClass);
		n.f6.accept(this, newClass);
		
		MMethod newMethod = newClass.getMethod("main");//��ȡmain����
		
		n.f7.accept(this, newMethod);//main������
		n.f8.accept(this, newMethod);
		n.f9.accept(this, newMethod);
		n.f10.accept(this, newMethod);
		n.f11.accept(this, newMethod);
		n.f12.accept(this, newMethod);
		n.f13.accept(this, newMethod);
		n.f14.accept(this, newMethod);
		n.f15.accept(this, newMethod);
		n.f16.accept(this, newClass);
		
		return null;
	}

	/**
	* f0 -> ClassDeclaration()
	*       | ClassExtendsDeclaration()
	*/
	public MIdentifier visit(TypeDeclaration n, MType argu)//������(�����򺬻���)
	{
		n.f0.accept(this, classList);
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "{"
	* f3 -> ( VarDeclaration() )*
	* f4 -> ( MethodDeclaration() )*
	* f5 -> "}"
	*/
	public MIdentifier visit(ClassDeclaration n, MType argu)//������(��������)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//����
		MClass newClass = classList.get(szClass, n.f1.f0.beginLine);//��ȡ��
		
		n.f2.accept(this, newClass);//����
		n.f3.accept(this, newClass);
		n.f4.accept(this, newClass);
		n.f5.accept(this, newClass);
		
		return null;
	}

	/**
	* f0 -> "class"
	* f1 -> Identifier()
	* f2 -> "extends"
	* f3 -> Identifier()
	* f4 -> "{"
	* f5 -> ( VarDeclaration() )*
	* f6 -> ( MethodDeclaration() )*
	* f7 -> "}"
	*/
	public MIdentifier visit(ClassExtendsDeclaration n, MType argu)//������(������)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//����
		MClass newClass = classList.get(szClass, n.f1.f0.beginLine);//��ȡ��
		
		n.f2.accept(this, newClass);//���ڲ�
		n.f3.accept(this, newClass);
		
		String szBase = newClass.getBase();//������
		
		if (szBase != null)
		{
			if (!classList.contains(szBase))//����δ����
			{
				classList.addError(new TypeError(n.f3.f0.beginLine, "Undefined class: " + "\"" + szBase  + "\""));
			}
			else
			{
				String szBases = newClass.getName();
				HashSet<String> lstBase = new HashSet<String>();
				lstBase.add(newClass.getName());
				
				while (szBase != null)
				{
					szBases += "->" + szBase;
					if (szBase.equals(newClass.getName()))//����ѭ���̳�
					{
						szBases = szBases.substring(0, szBases.length());
						classList.addError(new TypeError(n.f3.f0.beginLine, "Circular extends: " + "\"" + szBases  + "\""));
						break;
					}
					else if (lstBase.contains(szBase)) break;
					
					lstBase.add(szBase);
					
					MClass baseClass = classList.get(szBase);
					if (baseClass != null) szBase = baseClass.getBase();
					else break;
				}
			}
		}
		
		n.f4.accept(this, newClass);
		n.f5.accept(this, newClass);
		n.f6.accept(this, newClass);
		n.f7.accept(this, newClass);
		
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	* f2 -> ";"
	*/
	public MIdentifier visit(VarDeclaration n, MType argu)//��������
	{
		String szType =  n.f0.accept(this, argu).getName();//������
		
		if (!classList.contains(szType))//����δ����
		{
			classList.addError(new TypeError(n.f1.f0.beginLine, "Undefined type: " + "\"" + szType + "\""));
		}
		
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> "public"
	* f1 -> Type()
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( FormalParameterList() )?
	* f5 -> ")"
	* f6 -> "{"
	* f7 -> ( VarDeclaration() )*
	* f8 -> ( Statement() )*
	* f9 -> "return"
	* f10 -> Expression()
	* f11 -> ";"
	* f12 -> "}"
	*/
	public MIdentifier visit(MethodDeclaration n, MType argu)//��������
	{
		n.f0.accept(this, argu);
		
		String szType =  n.f1.accept(this, argu).getName();//������
		
		if (!classList.contains(szType))//����δ����
		{
			classList.addError(new TypeError(n.f2.f0.beginLine, "Undefined type: " + "\"" + szType + "\""));
		}
		
		String szMethod = n.f2.accept(this, argu).getName();//������
		
		MMethod newMethod = ((MClass) argu).getMethod(szMethod, n.f2.f0.beginLine);//��ȡ����
		MMethod baseMethod = ((MClass) argu).getMethodInBase(szMethod);//���һ����е�ͬ������
		
		if (baseMethod != null && !newMethod.equals(baseMethod))//��������ض��Ǹ����򱨴�
		{
			classList.addError(new TypeError(n.f2.f0.beginLine, "Multiple methods declarations: " + "\"" + ((MIdentifier) argu).getName() + "." + szMethod + "\""));
		}

		n.f3.accept(this, newMethod);//������
		n.f4.accept(this, newMethod);
		n.f5.accept(this, newMethod);
		n.f6.accept(this, newMethod);
		n.f7.accept(this, newMethod);
		n.f8.accept(this, newMethod);
		n.f9.accept(this, newMethod);
		
		MIdentifier exp = n.f10.accept(this, newMethod);//���ر��ʽ
		
		if (exp != null)//������ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();
			
			if (szExpType == null)//������,��δ�������
			{
				classList.addError(new TypeError(n.f9.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!classList.classEqualsOrDerives(szType, szExpType))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f9.beginLine, "Return type mismatch: " + "\"" + szType + "=" + szExpType + "\""));
				}
			}
		}
		
		n.f11.accept(this, newMethod);
		n.f12.accept(this, newMethod);
		
		return null;
	}

	/**
	* f0 -> FormalParameter()
	* f1 -> ( FormalParameterRest() )*
	*/
	public MIdentifier visit(FormalParameterList n, MType argu)//�β��б�
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public MIdentifier visit(FormalParameter n, MType argu)//�β�����
	{
		String szType =  n.f0.accept(this, argu).getName();//������
		
		if (!classList.contains(szType))//����δ����
		{
			classList.addError(new TypeError(n.f1.f0.beginLine, "Undefined type: " + "\"" + szType + "\""));
		}
		
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> ","
	* f1 -> FormalParameter()
	*/
	public MIdentifier visit(FormalParameterRest n, MType argu)//�β�
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> ArrayType()
	*       | BooleanType()
	*       | IntegerType()
	*       | Identifier()
	*/
	public MIdentifier visit(Type n, MType argu)//����
	{		
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public MIdentifier visit(ArrayType n, MType argu)//������
	{
		return new MIdentifier("int[]", null, null);
	}

	/**
	* f0 -> "boolean"
	*/
	public MIdentifier visit(BooleanType n, MType argu)//������
	{
		return new MIdentifier("boolean", null, null);
	}

	/**
	* f0 -> "int"
	*/
	public MIdentifier visit(IntegerType n, MType argu)//����
	{
		return new MIdentifier("int", null, null);
	}

	/**
	* f0 -> Block()
	*       | AssignmentStatement()
	*       | ArrayAssignmentStatement()
	*       | IfStatement()
	*       | WhileStatement()
	*       | PrintStatement()
	*/
	public MIdentifier visit(Statement n, MType argu)//���
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "{"
	* f1 -> ( Statement() )*
	* f2 -> "}"
	*/
	public MIdentifier visit(Block n, MType argu)//��
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> Identifier()
	* f1 -> "="
	* f2 -> Expression()
	* f3 -> ";"
	*/
	public MIdentifier visit(AssignmentStatement n, MType argu)//��ֵ���
	{
		String szVar = n.f0.accept(this, argu).getName();//������
		MVar newVar = ((MIdentifier) argu).getVar(szVar); //��ȡ����
		//δ�������
		if (newVar == null) classList.addError(new TypeError(n.f0.f0.beginLine, "Undefined variable: " + "\"" + szVar + "\""));
		
		n.f1.accept(this, argu);
				
		MIdentifier exp = n.f2.accept(this, argu);//���ʽ
		
		if (exp != null)//������(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f0.f0.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (newVar != null && !classList.classEqualsOrDerives(newVar.getType(), szExpType))//�����Ƿ�ƥ��
				{
					classList.addError(new TypeError(n.f0.f0.beginLine, "Type mismatch: " + "\"" + newVar.getType() + "=" + szExpType + "\""));
				}
			}
		}
		
		n.f3.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> Identifier()
	* f1 -> "["
	* f2 -> Expression()
	* f3 -> "]"
	* f4 -> "="
	* f5 -> Expression()
	* f6 -> ";"
	*/
	public MIdentifier visit(ArrayAssignmentStatement n, MType argu)//���鸳ֵ
	{
		String szVar = n.f0.accept(this, argu).getName();//������
		MVar newVar = ((MIdentifier) argu).getVar(szVar); //��ȡ����
		
		if (newVar == null)//δ�������
		{
			classList.addError(new TypeError(n.f0.f0.beginLine, "Undefined variable: " + "\"" + szVar + "\""));
		}
		else
		{
			if (!newVar.getType().equals("int[]"))//���Ͳ�ƥ��
			{
				classList.addError(new TypeError(n.f0.f0.beginLine, "Not an array: " + "\"" + szVar + "\""));
			}
		}
				
		n.f1.accept(this, argu);
		
		MIdentifier exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Index is not an integer"));
				}
			}
		}
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		
		exp = n.f5.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f4.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f4.beginLine, "Type mismatch: " + "\"int=" + szExpType + "\""));
				}
			}
		}
		
		n.f6.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> "if"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	* f5 -> "else"
	* f6 -> Statement()
	*/
	public MIdentifier visit(IfStatement n, MType argu)//�������
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MIdentifier exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Condition expression is not a boolean"));
				}
			}
		}
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		n.f6.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> "while"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> Statement()
	*/
	public MIdentifier visit(WhileStatement n, MType argu)//ѭ�����
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);

		MIdentifier exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Condition expression is not a boolean"));
				}
			}
		}
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> "System.out.println"
	* f1 -> "("
	* f2 -> Expression()
	* f3 -> ")"
	* f4 -> ";"
	*/
	public MIdentifier visit(PrintStatement n, MType argu)//��ӡ���
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MIdentifier exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Unable to print a non-digital expression"));
				}
			}
		}
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);

		return null;
	}

	/**
	* f0 -> AndExpression()
	*       | CompareExpression()
	*       | PlusExpression()
	*       | MinusExpression()
	*       | TimesExpression()
	*       | ArrayLookup()
	*       | ArrayLength()
	*       | MessageSend()
	*       | PrimaryExpression()
	*/
	public MIdentifier visit(Expression n, MType argu)//���ʽ
	{
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "&&"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(AndExpression n, MType argu)//�߼�����ʽ
	{
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '&&' is not a boolean"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '&&' is not a boolean"));
				}
			}
		}
		
		return new MIdentifier(null, "boolean", null);//���ز������ʽ
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(CompareExpression n, MType argu)//�Ƚϱ��ʽ
	{
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '<' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '<' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "boolean", null);//���ز������ʽ
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(PlusExpression n, MType argu)//�ӷ����ʽ
	{
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '+' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '+' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//�������ͱ��ʽ
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(MinusExpression n, MType argu)//�������ʽ
	{
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '-' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '-' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//�������ͱ��ʽ
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(TimesExpression n, MType argu)//�˷����ʽ
	{
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '*' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '*' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//�������ͱ��ʽ
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public MIdentifier visit(ArrayLookup n, MType argu)//ȡ������ĳֵ
	{
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int[]"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '[' is not an array"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Index is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//�������ͱ��ʽ
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public MIdentifier visit(ArrayLength n, MType argu)//���鳤��
	{
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int[]"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '[' is not an array"));
				}
			}
		}
		
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		
		return new MIdentifier(null, "int", null);//�������ͱ��ʽ
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( ExpressionList() )?
	* f5 -> ")"
	*/
	public MIdentifier visit(MessageSend n, MType argu)//���󷽷�
	{
		boolean bFlag = true;
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp == null) bFlag = false;//���ʽ��δ�������,���ټ��
		
		if (bFlag && exp.getType() == null)//��ȡ����,������δ�������,���ټ��
		{
			classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			bFlag = false;
		}
		
		MClass newClass = null;
		
		if (bFlag)
		{
			newClass = classList.get(exp.getType());
			if (newClass == null) bFlag = false;//�Ҳ�����,���ټ��
		}
		
		n.f1.accept(this, argu);
		
		exp = n.f2.accept(this, argu);
		
		MMethod newMethod = null;
		
		if (bFlag)
		{
			newMethod = newClass.getMethod(exp.getName());//��ȡ����
			if (newMethod == null)//����δ����
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined method: " + "\"" + newClass.getName() + "." + exp.getName() + "\""));
				bFlag = false;
			}
		}
		
		n.f3.accept(this, argu);
		
		MActualParamList paramList = new MActualParamList(n.f3.beginLine, (MIdentifier) argu);//����ʵ���б�
		
		n.f4.accept(this, paramList);
		
		if (bFlag)
		{
			int nParamFlag = newMethod.checkParam(paramList);
			if (nParamFlag > 0)//������
			{
				classList.addError(new TypeError(n.f1.beginLine, "Argument " + (nParamFlag == 1 ? "number" : "type")
						+"s not match: " + "\"" + newClass.getName() + "." + exp.getName() + "\""));
				bFlag = false;
			}
		}
		
		n.f5.accept(this, argu);
		
		return bFlag ? newMethod : null;//���δ����,��������
	}

	/**
	* f0 -> Expression()
	* f1 -> ( ExpressionRest() )*
	*/
	public MIdentifier visit(ExpressionList n, MType argu)//ʵ���б�
	{	
		MActualParamList paramList = (MActualParamList) argu;//ʵ���б�
		MIdentifier exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(paramList.getLine(), "Undefined variable: " + "\"" + exp.getName() + "\""));
				exp = null;
			}
		}
		
		paramList.addParam(exp != null ? exp.getType() : null);//ʵ���б�ֻ��������
		n.f1.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	public MIdentifier visit(ExpressionRest n, MType argu)//ʵ��
	{
		n.f0.accept(this, argu);
		
		MActualParamList paramList = (MActualParamList) argu;//ʵ���б�
		MIdentifier exp = n.f1.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f0.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
				exp = null;
			}
		}
		
		paramList.addParam(exp != null ? exp.getType() : null);//ʵ���б�ֻ��������
		return null;
	}

	/**
	* f0 -> IntegerLiteral()
	*       | TrueLiteral()
	*       | FalseLiteral()
	*       | Identifier()
	*       | ThisExpression()
	*       | ArrayAllocationExpression()
	*       | AllocationExpression()
	*       | NotExpression()
	*       | BracketExpression()
	*/
	public MIdentifier visit(PrimaryExpression n, MType argu)//�������ʽ
	{
		MIdentifier ret = n.f0.accept(this, argu);
		
		if (ret != null && ret.getType() == null)//����Ǳ���
		{
			String szVar = ret.getName();//������
			MVar newVar = ((MIdentifier) argu).getVar(szVar); //��ȡ����
			
			if (newVar != null) ret = newVar;//δ�������
		}
		
		return ret;
	}

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public MIdentifier visit(IntegerLiteral n, MType argu)//��������
	{
		n.f0.accept(this, argu);
		return new MIdentifier(null, "int", null);//���ͱ��ʽ
	}

	/**
	* f0 -> "true"
	*/
	public MIdentifier visit(TrueLiteral n, MType argu)//�߼���
	{
		n.f0.accept(this, argu);
		return new MIdentifier(null, "boolean", null);//�������ʽ
	}

	/**
	* f0 -> "false"
	*/
	public MIdentifier visit(FalseLiteral n, MType argu)//�߼���
	{
		n.f0.accept(this, argu);
		return new MIdentifier(null, "boolean", null);//�������ʽ
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public MIdentifier visit(Identifier n, MType argu)//��ʶ��
	{
		return new MIdentifier(n.f0.toString(), null, null);//��������
	}

	/**
	* f0 -> "this"
	*/
	public MIdentifier visit(ThisExpression n, MType argu)//thisָ��
	{
		n.f0.accept(this, argu);
		
		MIdentifier parent = ((MIdentifier) argu).getParent();
		
		while (parent != null)
		{
			if (parent instanceof MClass) return new MIdentifier("this", parent.getName(), null);
			parent = parent.getParent();//���ظ���ʶ���е���
		}
		
		return null;
	}

	/**
	* f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	public MIdentifier visit(ArrayAllocationExpression n, MType argu)//������
	{		
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		
		MIdentifier exp = n.f3.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//��ȡ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f2.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f2.beginLine, "Array length is not an integer"));
				}
			}
		}
		
		n.f4.accept(this, argu);
		
		return new MIdentifier(null, "int[]", null);
	}

	/**
	* f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	public MIdentifier visit(AllocationExpression n, MType argu)//�¶���
	{
		MIdentifier ret = null;
		
		n.f0.accept(this, argu);
		
		String szType = n.f1.accept(this, argu).getName();//��������
		//δ��������
		if (!classList.contains(szType)) classList.addError(new TypeError(n.f1.f0.beginLine, "Undefined type: " + "\"" + szType + "\""));
		else ret = new MIdentifier(null, szType, null);
				
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		
		return ret;
	}

	/**
	* f0 -> "!"
	* f1 -> Expression()
	*/
	public MIdentifier visit(NotExpression n, MType argu)//�߼��Ǳ��ʽ
	{
		n.f0.accept(this, argu);
		
		MIdentifier exp = n.f1.accept(this, argu);//��ȡ���ʽ
		
		if (exp != null)//���ʽ����(��������)δ�������
		{
			String szExpType = exp.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f0.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
				return null;
			}
			else
			{
				if (!exp.getType().equals("boolean"))//���Ͳ�ƥ��
				{
					classList.addError(new TypeError(n.f0.beginLine, "Condition expression is not a boolean"));
				}
			}
		}
		else return null;
		
		return new MIdentifier(null, "boolean", null);//���ز����ͱ��ʽ
	}

	/**
	* f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	public MIdentifier visit(BracketExpression n, MType argu)//���ű��ʽ
	{
		n.f0.accept(this, argu);
		
		MIdentifier ret = n.f1.accept(this, argu);//��ȡ���ʽ
		
		if (ret != null)//���ʽ����(��������)δ�������
		{
			String szExpType = ret.getType();//���ʽ����
			
			if (szExpType == null)//���ʽ��δ�������
			{
				classList.addError(new TypeError(n.f0.beginLine, "Undefined variable: " + "\"" + ret.getName() + "\""));
				ret = null;
			}
		}
		
		n.f2.accept(this, argu);
		
		return ret;//ֱ�ӷ��ر��ʽ
	}
	

}


