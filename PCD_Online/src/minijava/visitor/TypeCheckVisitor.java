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


public class TypeCheckVisitor extends GJDepthFirst<MIdentifier, MType>//建符号表后的类型检查
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
	public MIdentifier visit(Goal n, MType argu)//主类和所有类
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
	public MIdentifier visit(MainClass n, MType argu)//主类声明
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//主类名
		MClass newClass = classList.get(szClass, n.f1.f0.beginLine);//获取主类
		
		n.f2.accept(this, newClass);//主类内
		n.f3.accept(this, newClass);
		n.f4.accept(this, newClass);
		n.f5.accept(this, newClass);
		n.f6.accept(this, newClass);
		
		MMethod newMethod = newClass.getMethod("main");//获取main方法
		
		n.f7.accept(this, newMethod);//main方法内
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
	public MIdentifier visit(TypeDeclaration n, MType argu)//类声明(不含或含基类)
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
	public MIdentifier visit(ClassDeclaration n, MType argu)//类声明(不含基类)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//类名
		MClass newClass = classList.get(szClass, n.f1.f0.beginLine);//获取类
		
		n.f2.accept(this, newClass);//类内
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
	public MIdentifier visit(ClassExtendsDeclaration n, MType argu)//类声明(含基类)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//类名
		MClass newClass = classList.get(szClass, n.f1.f0.beginLine);//获取类
		
		n.f2.accept(this, newClass);//类内部
		n.f3.accept(this, newClass);
		
		String szBase = newClass.getBase();//基类名
		
		if (szBase != null)
		{
			if (!classList.contains(szBase))//基类未定义
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
					if (szBase.equals(newClass.getName()))//出现循环继承
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
	public MIdentifier visit(VarDeclaration n, MType argu)//变量声明
	{
		String szType =  n.f0.accept(this, argu).getName();//类型名
		
		if (!classList.contains(szType))//类型未定义
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
	public MIdentifier visit(MethodDeclaration n, MType argu)//方法声明
	{
		n.f0.accept(this, argu);
		
		String szType =  n.f1.accept(this, argu).getName();//类型名
		
		if (!classList.contains(szType))//类型未定义
		{
			classList.addError(new TypeError(n.f2.f0.beginLine, "Undefined type: " + "\"" + szType + "\""));
		}
		
		String szMethod = n.f2.accept(this, argu).getName();//方法名
		
		MMethod newMethod = ((MClass) argu).getMethod(szMethod, n.f2.f0.beginLine);//获取方法
		MMethod baseMethod = ((MClass) argu).getMethodInBase(szMethod);//查找基类中的同名方法
		
		if (baseMethod != null && !newMethod.equals(baseMethod))//如果是重载而非覆盖则报错
		{
			classList.addError(new TypeError(n.f2.f0.beginLine, "Multiple methods declarations: " + "\"" + ((MIdentifier) argu).getName() + "." + szMethod + "\""));
		}

		n.f3.accept(this, newMethod);//方法内
		n.f4.accept(this, newMethod);
		n.f5.accept(this, newMethod);
		n.f6.accept(this, newMethod);
		n.f7.accept(this, newMethod);
		n.f8.accept(this, newMethod);
		n.f9.accept(this, newMethod);
		
		MIdentifier exp = n.f10.accept(this, newMethod);//返回表达式
		
		if (exp != null)//如果表达式内无(但可能是)未定义变量
		{
			String szExpType = exp.getType();
			
			if (szExpType == null)//无类型,即未定义变量
			{
				classList.addError(new TypeError(n.f9.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!classList.classEqualsOrDerives(szType, szExpType))//类型不匹配
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
	public MIdentifier visit(FormalParameterList n, MType argu)//形参列表
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public MIdentifier visit(FormalParameter n, MType argu)//形参声明
	{
		String szType =  n.f0.accept(this, argu).getName();//变量名
		
		if (!classList.contains(szType))//变量未定义
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
	public MIdentifier visit(FormalParameterRest n, MType argu)//形参
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
	public MIdentifier visit(Type n, MType argu)//类型
	{		
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public MIdentifier visit(ArrayType n, MType argu)//数组型
	{
		return new MIdentifier("int[]", null, null);
	}

	/**
	* f0 -> "boolean"
	*/
	public MIdentifier visit(BooleanType n, MType argu)//布尔型
	{
		return new MIdentifier("boolean", null, null);
	}

	/**
	* f0 -> "int"
	*/
	public MIdentifier visit(IntegerType n, MType argu)//整型
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
	public MIdentifier visit(Statement n, MType argu)//语句
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "{"
	* f1 -> ( Statement() )*
	* f2 -> "}"
	*/
	public MIdentifier visit(Block n, MType argu)//块
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
	public MIdentifier visit(AssignmentStatement n, MType argu)//赋值语句
	{
		String szVar = n.f0.accept(this, argu).getName();//变量名
		MVar newVar = ((MIdentifier) argu).getVar(szVar); //获取变量
		//未定义变量
		if (newVar == null) classList.addError(new TypeError(n.f0.f0.beginLine, "Undefined variable: " + "\"" + szVar + "\""));
		
		n.f1.accept(this, argu);
				
		MIdentifier exp = n.f2.accept(this, argu);//表达式
		
		if (exp != null)//不包含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f0.f0.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (newVar != null && !classList.classEqualsOrDerives(newVar.getType(), szExpType))//类型是否匹配
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
	public MIdentifier visit(ArrayAssignmentStatement n, MType argu)//数组赋值
	{
		String szVar = n.f0.accept(this, argu).getName();//变量名
		MVar newVar = ((MIdentifier) argu).getVar(szVar); //获取变量
		
		if (newVar == null)//未定义变量
		{
			classList.addError(new TypeError(n.f0.f0.beginLine, "Undefined variable: " + "\"" + szVar + "\""));
		}
		else
		{
			if (!newVar.getType().equals("int[]"))//类型不匹配
			{
				classList.addError(new TypeError(n.f0.f0.beginLine, "Not an array: " + "\"" + szVar + "\""));
			}
		}
				
		n.f1.accept(this, argu);
		
		MIdentifier exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Index is not an integer"));
				}
			}
		}
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		
		exp = n.f5.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f4.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
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
	public MIdentifier visit(IfStatement n, MType argu)//条件语句
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MIdentifier exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//类型不匹配
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
	public MIdentifier visit(WhileStatement n, MType argu)//循环语句
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);

		MIdentifier exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//类型不匹配
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
	public MIdentifier visit(PrintStatement n, MType argu)//打印语句
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MIdentifier exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
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
	public MIdentifier visit(Expression n, MType argu)//表达式
	{
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "&&"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(AndExpression n, MType argu)//逻辑与表达式
	{
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '&&' is not a boolean"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("boolean"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '&&' is not a boolean"));
				}
			}
		}
		
		return new MIdentifier(null, "boolean", null);//返回布尔表达式
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(CompareExpression n, MType argu)//比较表达式
	{
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '<' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '<' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "boolean", null);//返回布尔表达式
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(PlusExpression n, MType argu)//加法表达式
	{
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '+' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '+' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//返回整型表达式
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(MinusExpression n, MType argu)//减法表达式
	{
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '-' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '-' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//返回整型表达式
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	public MIdentifier visit(TimesExpression n, MType argu)//乘法表达式
	{
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '*' is not an integer"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Right expression of '*' is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//返回整型表达式
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public MIdentifier visit(ArrayLookup n, MType argu)//取数组中某值
	{
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int[]"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '[' is not an array"));
				}
			}
		}
		
		n.f1.accept(this, argu);

		exp = n.f2.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Index is not an integer"));
				}
			}
		}
		
		return new MIdentifier(null, "int", null);//返回整型表达式
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public MIdentifier visit(ArrayLength n, MType argu)//数组长度
	{
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int[]"))//类型不匹配
				{
					classList.addError(new TypeError(n.f1.beginLine, "Left expression of '[' is not an array"));
				}
			}
		}
		
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		
		return new MIdentifier(null, "int", null);//返回整型表达式
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( ExpressionList() )?
	* f5 -> ")"
	*/
	public MIdentifier visit(MessageSend n, MType argu)//对象方法
	{
		boolean bFlag = true;
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp == null) bFlag = false;//表达式含未定义变量,不再检查
		
		if (bFlag && exp.getType() == null)//获取类型,发现是未定义变量,不再检查
		{
			classList.addError(new TypeError(n.f1.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			bFlag = false;
		}
		
		MClass newClass = null;
		
		if (bFlag)
		{
			newClass = classList.get(exp.getType());
			if (newClass == null) bFlag = false;//找不到类,不再检查
		}
		
		n.f1.accept(this, argu);
		
		exp = n.f2.accept(this, argu);
		
		MMethod newMethod = null;
		
		if (bFlag)
		{
			newMethod = newClass.getMethod(exp.getName());//获取方法
			if (newMethod == null)//方法未定义
			{
				classList.addError(new TypeError(n.f1.beginLine, "Undefined method: " + "\"" + newClass.getName() + "." + exp.getName() + "\""));
				bFlag = false;
			}
		}
		
		n.f3.accept(this, argu);
		
		MActualParamList paramList = new MActualParamList(n.f3.beginLine, (MIdentifier) argu);//创建实参列表
		
		n.f4.accept(this, paramList);
		
		if (bFlag)
		{
			int nParamFlag = newMethod.checkParam(paramList);
			if (nParamFlag > 0)//检查参数
			{
				classList.addError(new TypeError(n.f1.beginLine, "Argument " + (nParamFlag == 1 ? "number" : "type")
						+"s not match: " + "\"" + newClass.getName() + "." + exp.getName() + "\""));
				bFlag = false;
			}
		}
		
		n.f5.accept(this, argu);
		
		return bFlag ? newMethod : null;//如果未出错,返回类型
	}

	/**
	* f0 -> Expression()
	* f1 -> ( ExpressionRest() )*
	*/
	public MIdentifier visit(ExpressionList n, MType argu)//实参列表
	{	
		MActualParamList paramList = (MActualParamList) argu;//实参列表
		MIdentifier exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(paramList.getLine(), "Undefined variable: " + "\"" + exp.getName() + "\""));
				exp = null;
			}
		}
		
		paramList.addParam(exp != null ? exp.getType() : null);//实参列表只加入类型
		n.f1.accept(this, argu);
		
		return null;
	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	public MIdentifier visit(ExpressionRest n, MType argu)//实参
	{
		n.f0.accept(this, argu);
		
		MActualParamList paramList = (MActualParamList) argu;//实参列表
		MIdentifier exp = n.f1.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f0.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
				exp = null;
			}
		}
		
		paramList.addParam(exp != null ? exp.getType() : null);//实参列表只加入类型
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
	public MIdentifier visit(PrimaryExpression n, MType argu)//基本表达式
	{
		MIdentifier ret = n.f0.accept(this, argu);
		
		if (ret != null && ret.getType() == null)//如果是变量
		{
			String szVar = ret.getName();//变量名
			MVar newVar = ((MIdentifier) argu).getVar(szVar); //获取变量
			
			if (newVar != null) ret = newVar;//未定义变量
		}
		
		return ret;
	}

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public MIdentifier visit(IntegerLiteral n, MType argu)//整型数字
	{
		n.f0.accept(this, argu);
		return new MIdentifier(null, "int", null);//整型表达式
	}

	/**
	* f0 -> "true"
	*/
	public MIdentifier visit(TrueLiteral n, MType argu)//逻辑真
	{
		n.f0.accept(this, argu);
		return new MIdentifier(null, "boolean", null);//布尔表达式
	}

	/**
	* f0 -> "false"
	*/
	public MIdentifier visit(FalseLiteral n, MType argu)//逻辑假
	{
		n.f0.accept(this, argu);
		return new MIdentifier(null, "boolean", null);//布尔表达式
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public MIdentifier visit(Identifier n, MType argu)//标识符
	{
		return new MIdentifier(n.f0.toString(), null, null);//返回名称
	}

	/**
	* f0 -> "this"
	*/
	public MIdentifier visit(ThisExpression n, MType argu)//this指针
	{
		n.f0.accept(this, argu);
		
		MIdentifier parent = ((MIdentifier) argu).getParent();
		
		while (parent != null)
		{
			if (parent instanceof MClass) return new MIdentifier("this", parent.getName(), null);
			parent = parent.getParent();//返回父标识符中的类
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
	public MIdentifier visit(ArrayAllocationExpression n, MType argu)//新数组
	{		
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		
		MIdentifier exp = n.f3.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//获取类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f2.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
			}
			else
			{
				if (!exp.getType().equals("int"))//类型不匹配
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
	public MIdentifier visit(AllocationExpression n, MType argu)//新对象
	{
		MIdentifier ret = null;
		
		n.f0.accept(this, argu);
		
		String szType = n.f1.accept(this, argu).getName();//类型名称
		//未定义类型
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
	public MIdentifier visit(NotExpression n, MType argu)//逻辑非表达式
	{
		n.f0.accept(this, argu);
		
		MIdentifier exp = n.f1.accept(this, argu);//获取表达式
		
		if (exp != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = exp.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f0.beginLine, "Undefined variable: " + "\"" + exp.getName() + "\""));
				return null;
			}
			else
			{
				if (!exp.getType().equals("boolean"))//类型不匹配
				{
					classList.addError(new TypeError(n.f0.beginLine, "Condition expression is not a boolean"));
				}
			}
		}
		else return null;
		
		return new MIdentifier(null, "boolean", null);//返回布尔型表达式
	}

	/**
	* f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	public MIdentifier visit(BracketExpression n, MType argu)//括号表达式
	{
		n.f0.accept(this, argu);
		
		MIdentifier ret = n.f1.accept(this, argu);//获取表达式
		
		if (ret != null)//表达式不含(但可能是)未定义变量
		{
			String szExpType = ret.getType();//表达式类型
			
			if (szExpType == null)//表达式是未定义变量
			{
				classList.addError(new TypeError(n.f0.beginLine, "Undefined variable: " + "\"" + ret.getName() + "\""));
				ret = null;
			}
		}
		
		n.f2.accept(this, argu);
		
		return ret;//直接返回表达式
	}
	

}


