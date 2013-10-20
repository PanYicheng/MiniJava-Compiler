package minijava.visitor;
import java.util.Enumeration;
import java.util.Vector;

import minijava.symboltable.MActualParamList;
import minijava.symboltable.MClass;
import minijava.symboltable.MClassList;
import minijava.symboltable.MIdentifier;
import minijava.symboltable.MMethod;
import minijava.symboltable.MPiglet;
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

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  YouMPiglet visitors may extend this class.
 */
public class Convert2PigletVisitor extends GJDepthFirst<MPiglet, MType>//MiniJava转换Piglet
{
	private int nCurrentTemp, nCurrentLabel;//当前Temp，当前标签
	private MClassList classList;
	
	//
	// Auto class visitors--probably don't need to be overridden.
	//
	public MPiglet visit(NodeList n, MType argu)
	{
		MPiglet ret = new MPiglet(null, null, null);
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
		{
			ret.append(e.nextElement().accept(this,argu));
		}
		return ret;//叠加代码
	}

	public MPiglet visit(NodeListOptional n, MType argu)
	{
		if (n.present())
		{
			MPiglet ret = new MPiglet(null, null, null);
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
			{
				ret.append(e.nextElement().accept(this,argu));
			}
			
			return ret;//叠加代码
		}
		return null;
	}

	public MPiglet visit(NodeOptional n, MType argu)
	{
		if (n.present()) return n.node.accept(this,argu);
		else return null;
	}

	public MPiglet visit(NodeSequence n, MType argu)
	{
		MPiglet ret = new MPiglet(null, null, null);
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
		{
			ret.append(e.nextElement().accept(this,argu));
		}
		return ret;//叠加代码
	}

	public MPiglet visit(NodeToken n, MType argu) { return null; }

	//
	// User-generated visitor methods below
	//

	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */
	public MPiglet visit(Goal n, MType argu)//整个框架
	{
		classList = (MClassList) argu;//符号表
		nCurrentTemp = classList.alloc(20);//分配偏移地址，过程名，局部变量TEMP编号
		
		MPiglet ret = n.f0.accept(this, argu);
		ret.append(n.f1.accept(this, argu));
		n.f2.accept(this, argu);
		
		ret.format();//调整缩进
		return ret;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> "public"
	 * f4 -> "static"
	 * f5 -> "String"
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
	public MPiglet visit(MainClass n, MType argu)//主类声明
	{
		MPiglet ret = new MPiglet(null, null, "MAIN\n");		
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();
		MClass newClass = classList.get(szClass);//获取主类
		
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
		
		ret.append(n.f14.accept(this, newMethod));//获取代码
		
		n.f15.accept(this, newMethod);
		n.f16.accept(this, newClass);
		
		ret.append("END\n");
		
		return ret;
	}

	/**
	 * f0 -> ClassDeclaration()
	 *		 | ClassExtendsDeclaration()
	 */
	public MPiglet visit(TypeDeclaration n, MType argu)//类声明(不含或含基类)
	{
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 */
	public MPiglet visit(ClassDeclaration n, MType argu)//类声明(不含基类)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//类名
		MClass newClass = classList.get(szClass);//获取类
		
		n.f2.accept(this, newClass);//类内
		n.f3.accept(this, newClass);
		
		MPiglet ret = n.f4.accept(this, newClass);//只返回方法
		
		n.f5.accept(this, newClass);
		
		return ret;
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
	public MPiglet visit(ClassExtendsDeclaration n, MType argu)//类声明(含基类)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//类名
		MClass newClass = classList.get(szClass);//获取类
		
		n.f2.accept(this, newClass);//类内
		n.f3.accept(this, newClass);
		n.f4.accept(this, newClass);
		n.f5.accept(this, newClass);
		
		MPiglet ret = n.f6.accept(this, newClass);//只返回方法
		
		n.f7.accept(this, newClass);
		
		return ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public MPiglet visit(VarDeclaration n, MType argu)//变量声明
	{
		MPiglet ret = null;
		n.f0.accept(this, argu);
		
		String szVar = n.f1.accept(this, argu).getName();
		if (argu instanceof MMethod)//是局部变量
		{
			MVar _var = ((MMethod) argu).getVar(szVar);
			ret = new MPiglet(null, null, "MOVE TEMP " + _var.getTemp() + " 0\n");
		}
		
		n.f2.accept(this, argu);	
		return ret;
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
	public MPiglet visit(MethodDeclaration n, MType argu)//方法声明
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		String szMethod = n.f2.accept(this, argu).getName();//方法名
		MMethod newMethod = ((MClass) argu).getMethod(szMethod);//获取方法
		
		MPiglet ret = new MPiglet(null, null, "\n" + newMethod.getPigletName() + " [ " + (newMethod.getPigletParamCount() + 1) + " ]\nBEGIN\n");
		
		n.f3.accept(this, newMethod);//方法内
		n.f4.accept(this, newMethod);
		n.f5.accept(this, newMethod);
		n.f6.accept(this, newMethod);
		
		ret.append(n.f7.accept(this, newMethod));//包含定义
		ret.append(n.f8.accept(this, newMethod));//包含语句
		
		n.f9.accept(this, newMethod);
		
		ret.append("RETURN ");//return语句
		ret.append(n.f10.accept(this, newMethod));
		ret.append("\nEND\n");
		
		n.f11.accept(this, newMethod);
		n.f12.accept(this, newMethod);
		
		return ret;
	}

	/**
	 * f0 -> FormalParameter()
	 * f1 -> ( FormalParameterRest() )*
	 */
	public MPiglet visit(FormalParameterList n, MType argu)//形参列表
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 */
	public MPiglet visit(FormalParameter n, MType argu)//形参声明
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 */
	public MPiglet visit(FormalParameterRest n, MType argu)//形参声明
	{		
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ArrayType()
	 *		 | BooleanType()
	 *		 | IntegerType()
	 *		 | Identifier()
	 */
	public MPiglet visit(Type n, MType argu)//类型
	{	
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
	 */
	public MPiglet visit(ArrayType n, MType argu)//数组
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "boolean"
	 */
	public MPiglet visit(BooleanType n, MType argu)//布尔
	{	
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "int"
	 */
	public MPiglet visit(IntegerType n, MType argu)//整型
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> Block()
	 *		 | AssignmentStatement()
	 *		 | ArrayAssignmentStatement()
	 *		 | IfStatement()
	 *		 | WhileStatement()
	 *		 | PrintStatement()
	 */
	public MPiglet visit(Statement n, MType argu)
	{
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> "{"
	 * f1 -> ( Statement() )*
	 * f2 -> "}"
	 */
	public MPiglet visit(Block n, MType argu)
	{
		n.f0.accept(this, argu);
		MPiglet ret = n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return ret;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 */
	public MPiglet visit(AssignmentStatement n, MType argu)//赋值语句
	{
		MPiglet ret = new MPiglet(null, null, null);
		String szVar = n.f0.accept(this, argu).getName();//获取标识符
		
		MVar _var = ((MIdentifier) argu).getVar(szVar);//获取变量
		if (_var.isTemp()) ret.append("MOVE TEMP " + _var.getTemp() + " ");//直接是temp
		else ret.append("HSTORE TEMP " + _var.getTemp() + " " + _var.getOffset() + " ");
		
		n.f1.accept(this, argu);
		
		ret.append(n.f2.accept(this, argu));
		ret.append("\n");
		
		n.f3.accept(this, argu);
		
		return ret;
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
	public MPiglet visit(ArrayAssignmentStatement n, MType argu)//数组赋值语句
	{
		int nLabel1, nLabel2, nLengthTemp, nArrayTemp;
		String szIndexExp;//下标
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, null);
		MPiglet idf = n.f0.accept(this, argu);
		MVar _var = ((MIdentifier) argu).getVar(idf.getName());//获取标识符
				
		if (_var.isTemp())//直接是temp
		{
			nArrayTemp = _var.getTemp();
		}
		else//移入TEMP
		{
			nArrayTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nArrayTemp);
			ret.append("\nBEGIN\n");
			ret.append("HLOAD TEMP " + nCurrentTemp + " TEMP " + _var.getTemp() + " " + _var.getOffset() + "\n");
			ret.append("RETURN TEMP " + nCurrentTemp + "\n");//载入变量
			ret.append("END\n");
		}
		
		n.f1.accept(this, argu);
		
		ret.append("CJUMP LT 0 TEMP " + nArrayTemp + " L" + nLabel1 + "\n");//检查数组是否为NULL
		
		nLengthTemp = nCurrentTemp++;//数组长度
		ret.append("HLOAD TEMP " + nLengthTemp + " " + " TEMP " + nArrayTemp + " 0\n");//载入
		
		MPiglet exp = n.f2.accept(this, argu);
		if (exp.isDigit() || exp.isTemp())//下标是数字或TEMP
		{
			szIndexExp = exp.toString();
		}
		else//载入下标
		{
			int nIndexTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nIndexTemp + " ");
			ret.append(exp);
			ret.append("\n");
			
			szIndexExp = "TEMP " + nIndexTemp;
		}
		
		ret.append("CJUMP LT 0 PLUS 1 " + szIndexExp + " L" + nLabel1 + "\n");//检查数组是否下越界
		ret.append("CJUMP LT " + szIndexExp + " TEMP " + nLengthTemp + " L" + nLabel1 + "\n");//检查数组是否上越界
		ret.append("HSTORE PLUS TEMP " + nArrayTemp + " TIMES 4 PLUS 1 " + szIndexExp + " 0 ");//存入元素
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		
		ret.append(n.f5.accept(this, argu));//表达式
		ret.append("\n");
		
		ret.append("JUMP L" + nLabel2 + "\n");
		ret.append("L" + nLabel1 + "\tERROR\n");//数组为NULL或越界，报错
		ret.append("L" + nLabel2 + "\tNOOP\n");
		
		n.f6.accept(this, argu);
		
		return ret;
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
	public MPiglet visit(IfStatement n, MType argu)//条件语句
	{
		int nLabel1, nLabel2;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MPiglet ret = new MPiglet(null, null, "CJUMP ");//条件判断
		ret.append(n.f2.accept(this, argu));
		ret.append(" L" + nLabel1 + "\n");//如果条件不成立跳至标签1
		
		n.f3.accept(this, argu);
		
		ret.append(n.f4.accept(this, argu));
		ret.append("JUMP L" + nLabel2 + "\n");//直接跳到标签
		
		n.f5.accept(this, argu);
		
		ret.append("L" + nLabel1 + "\t");//标签1
		ret.append(n.f6.accept(this, argu));
		ret.append("L" + nLabel2 + "\tNOOP\n");//标签2
		
		return ret;
	}

	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 */
	public MPiglet visit(WhileStatement n, MType argu)//循环语句
	{
		int nLabel1, nLabel2;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MPiglet ret = new MPiglet(null, null, "L" + nLabel1 + "\tCJUMP ");
		ret.append(n.f2.accept(this, argu));
		ret.append(" L" + nLabel2 + "\n");
		
		n.f3.accept(this, argu);
		
		ret.append(n.f4.accept(this, argu));
		ret.append("JUMP L" + nLabel1 + "\n");//返回继续判断
		ret.append("L" + nLabel2 + "\tNOOP\n");//标签2
		
		return ret;
	}

	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 */
	public MPiglet visit(PrintStatement n, MType argu)//打印语句
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MPiglet ret = new MPiglet(null, null, "PRINT ");//输出结果
		ret.append(n.f2.accept(this, argu));
		ret.append("\n");
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		
		return ret;
	}

	/**
	 * f0 -> AndExpression()
	 *		 | CompareExpression()
	 *		 | PlusExpression()
	 *		 | MinusExpression()
	 *		 | TimesExpression()
	 *		 | ArrayLookup()
	 *		 | ArrayLength()
	 *		 | MessageSend()
	 *		 | PrimaryExpression()
	 */
	public MPiglet visit(Expression n, MType argu)//表达式
	{
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	public MPiglet visit(AndExpression n, MType argu)//逻辑与表达式（短路与）
	{
		int nLabel1, nLabel2, nTemp;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		nTemp = nCurrentTemp++;//要返回的值
		
		MPiglet ret = new MPiglet(null, null, "\nBEGIN\n");
		
		ret.append("CJUMP ");
		ret.append(n.f0.accept(this, argu));
		ret.append(" L" + nLabel1 + "\n");//第一个表达式为0则不再进入第二个表达式的计算（同标准JAVA）
		
		n.f1.accept(this, argu);
		
		ret.append("CJUMP ");
		ret.append(n.f2.accept(this, argu));
		ret.append(" L" + nLabel1 + "\n");
		
		ret.append("MOVE TEMP " + nTemp + " 1\n");
		ret.append("JUMP L" + nLabel2 + "\n");
		
		ret.append("L" + nLabel1 + "\tMOVE TEMP " + nTemp + " 0\n");
		ret.append("L" + nLabel2 + "\tNOOP\n");
		
		ret.append("RETURN TEMP " + nTemp + "\n");
		ret.append("END");
		return ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 */
	public MPiglet visit(CompareExpression n, MType argu)//比较表达式
	{
		MPiglet ret = new MPiglet(null, null, "LT ");
		ret.append(n.f0.accept(this, argu));		
		n.f1.accept(this, argu);
		ret.append(" ");
		ret.append(n.f2.accept(this, argu));
		
		return ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 */
	public MPiglet visit(PlusExpression n, MType argu)//加法表达式
	{
		MPiglet ret = new MPiglet(null, null, "PLUS ");
		ret.append(n.f0.accept(this, argu));		
		n.f1.accept(this, argu);
		ret.append(" ");
		ret.append(n.f2.accept(this, argu));
		
		return ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 */
	public MPiglet visit(MinusExpression n, MType argu)//减法表达式
	{
		MPiglet ret = new MPiglet(null, null, "MINUS ");
		ret.append(n.f0.accept(this, argu));		
		n.f1.accept(this, argu);
		ret.append(" ");
		ret.append(n.f2.accept(this, argu));
		
		return ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 */
	public MPiglet visit(TimesExpression n, MType argu)//乘法表达式
	{
		MPiglet ret = new MPiglet(null, null, "TIMES ");
		ret.append(n.f0.accept(this, argu));		
		n.f1.accept(this, argu);
		ret.append(" ");
		ret.append(n.f2.accept(this, argu));
		
		return ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 */
	public MPiglet visit(ArrayLookup n, MType argu)//取数组元素
	{
		int nLabel1, nLabel2, nArrayTemp, nLengthTemp, nElementTemp;
		String szIndexExp;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, "\nBEGIN\n");
		
		MPiglet exp1 = n.f0.accept(this, argu);//获取数组表达式
		if (exp1.isTemp())//是TEMP（不可能是立即数）
		{
			nArrayTemp = exp1.getTemp();
		}
		else//不是TEMP，载入
		{
			nArrayTemp = nCurrentTemp++;
			ret.append("MOVE TEMP " + nArrayTemp + " ");
			ret.append(exp1);
			ret.append("\n");
		}
		
		ret.append("CJUMP LT 0 TEMP " + nArrayTemp + " L" + nLabel1 + "\n");//数组是否为NULL
		
		nLengthTemp = nCurrentTemp++;
		ret.append("HLOAD TEMP " + nLengthTemp + " TEMP " + nArrayTemp + " 0\n");//载入数组长度
		
		n.f1.accept(this, argu);
		
		MPiglet exp2 = n.f2.accept(this, argu);
		if (exp2.isDigit() || exp2.isTemp())//下标是数字或TEMP
		{
			szIndexExp = exp2.toString();
		}
		else//载入下标
		{
			int nIndexTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nIndexTemp + " ");
			ret.append(exp2);
			ret.append("\n");
			
			szIndexExp = "TEMP " + nIndexTemp;
		}
		
		ret.append("CJUMP LT 0 PLUS 1 " + szIndexExp + " L" + nLabel1 + "\n");//检查数组是否下越界
		ret.append("CJUMP LT " + szIndexExp + " TEMP " + nLengthTemp + " L" + nLabel1 + "\n");//检查数组是否上越界
		
		nElementTemp = nCurrentTemp++;
		ret.append("HLOAD TEMP " + nElementTemp + " PLUS TEMP " + nArrayTemp + " TIMES 4 PLUS 1 " + szIndexExp + " 0\n");
		
		n.f3.accept(this, argu);
		
		ret.append("JUMP L" + nLabel2 + "\n");
		ret.append("L" + nLabel1 + "\tERROR\n");//数组越界或为NULL
		ret.append("L" + nLabel2 + "\tNOOP\n");
		
		ret.append("RETURN TEMP " + nElementTemp + "\n");
		ret.append("END");
		
		return ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 */
	public MPiglet visit(ArrayLength n, MType argu)//取数组长度
	{
		int nLabel1, nLabel2, nArrayTemp, nLengthTemp;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, "\nBEGIN\n");
		
		MPiglet exp1 = n.f0.accept(this, argu);
		if (exp1.isTemp())//是TEMP
		{
			nArrayTemp = exp1.getTemp();
		}
		else//不是TEMP，载入
		{
			nArrayTemp = nCurrentTemp++;
			ret.append("MOVE TEMP " + nArrayTemp + " ");
			ret.append(exp1);
			ret.append("\n");
		}
		
		ret.append("CJUMP LT 0 TEMP " + nArrayTemp + " L" + nLabel1 + "\n");//是否为NULL
		
		nLengthTemp = nCurrentTemp++;
		ret.append("HLOAD TEMP " + nLengthTemp + " TEMP " + nArrayTemp + " 0\n");
			
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
				
		ret.append("JUMP L" + nLabel2 + "\n");
		ret.append("L" + nLabel1 + "\tERROR\n");//数组为NULL
		ret.append("L" + nLabel2 + "\tNOOP\n");
		
		ret.append("RETURN TEMP " + nLengthTemp + "\n");
		ret.append("END");
		
		return ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 */
	public MPiglet visit(MessageSend n, MType argu)//方法调用
	{
		int nLabel1, nLabel2, nMethodListTemp, nMethodTemp, nObjectTemp;
				
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, "CALL\nBEGIN\n");
		MPiglet exp = n.f0.accept(this, argu);//获取表达式
		
		if (exp.isTemp())//获取表达式
		{
			nObjectTemp = exp.getTemp();
		}
		else
		{
			nObjectTemp = nCurrentTemp++;
			ret.append("MOVE TEMP " + nObjectTemp + " ");
			ret.append(exp);
			ret.append("\n");
		}
		
		ret.append("CJUMP LT 0 TEMP " + nObjectTemp + " L" + nLabel1 + "\n");//对象是否为空
		
		nMethodListTemp = nCurrentTemp++;//方法表
		ret.append("HLOAD TEMP " + nMethodListTemp + " TEMP " + nObjectTemp + " 0\n");//载入方法表
		
		n.f1.accept(this, argu);
		
		String szClass = exp.getType();
		MClass _class = classList.get(szClass);//获取类
		
		String szMethod = n.f2.accept(this, argu).getName();
		MMethod _method = _class.getMethod(szMethod);//获取方法
		
		String szReturnType = _method.getType();
		MClass returnClass = classList.get(szReturnType);
		if (!returnClass.isBasicType()) ret.setType(szReturnType);//设置返回类型
		
		nMethodTemp = nCurrentTemp++;//方法
		ret.append("HLOAD TEMP " + nMethodTemp + " TEMP " + nMethodListTemp + " " + _method.getOffset() + "\n");//偏移位置
		
		n.f3.accept(this, argu);
		
		MActualParamList paramList = new MActualParamList(n.f3.beginLine, (MIdentifier) argu);//实参列表
		
		n.f4.accept(this, paramList);
		
		Vector<MPiglet> pigletList = paramList.getPiglets();//参数的中间代码表
		int nParamList = pigletList.size();//参数个数
		boolean bExtraParam = nParamList > 18;//参数个数是否超出
		
		String preEnd = "", postEnd = "";
		
		if (bExtraParam)
		{
			int nExtraParamTemp = nCurrentTemp++;//额外的参数
			preEnd += "MOVE TEMP " + nExtraParamTemp + " HALLOCATE " + _method.getExtraParamSize() + "\n";
			
			for (int i = 18; i < nParamList; ++i)
			{
				preEnd += "HSTORE TEMP " + nExtraParamTemp + " " + (4 * (i - 18)) + " ";//间接存储
				preEnd += pigletList.get(i);
				preEnd += "\n";
			}
			
			for (int i = 0; i < 18; ++i)//加入各参数
			{
				postEnd += " ";
				postEnd += pigletList.get(i);
			}
			
			postEnd += " TEMP " + nExtraParamTemp;//额外参数
		}
		else
		{
			for (int i = 0; i < nParamList; ++i)//加入各参数
			{
				postEnd += " ";
				postEnd += pigletList.get(i);
			}
		}
		
		ret.append(preEnd);
		
		ret.append("JUMP L" + nLabel2 + "\n");
		ret.append("L" + nLabel1 + "\tERROR\n");
		ret.append("L" + nLabel2 + "\tNOOP\n");
		
		ret.append("RETURN TEMP " + nMethodTemp + "\n");
		ret.append("END (TEMP " + nObjectTemp);//二次间接地址
		
		ret.append(postEnd);
		ret.append(")");
		
		n.f5.accept(this, argu);
		
		return ret;
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	public MPiglet visit(ExpressionList n, MType argu)//实参列表
	{
		((MActualParamList) argu).addPiglet(n.f0.accept(this, argu));//加入中间代码
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	public MPiglet visit(ExpressionRest n, MType argu)//实参
	{	
		n.f0.accept(this, argu);
		((MActualParamList) argu).addPiglet(n.f1.accept(this, argu));//加入中间代码
		return null;
	}

	/**
	 * f0 -> IntegerLiteral()
	 *		 | TrueLiteral()
	 *		 | FalseLiteral()
	 *		 | Identifier()
	 *		 | ThisExpression()
	 *		 | ArrayAllocationExpression()
	 *		 | AllocationExpression()
	 *		 | NotExpression()
	 *		 | BracketExpression()
	 */
	public MPiglet visit(PrimaryExpression n, MType argu)//基本表达式
	{
		MPiglet ret = n.f0.accept(this, argu);
		
		if (ret.toString() == null)//是变量
		{
			MVar _var = ((MIdentifier) argu).getVar(ret.getName());
			
			String szType = _var.getType();
			MClass _class = classList.get(szType);
			if (_class != null && !_class.isBasicType()) ret.setType(szType);//设置类型（基本类型不设置）
			
			if (_var.isTemp())//直接是temp
			{
				ret.append("TEMP " + _var.getTemp());
			}
			else
			{
				ret.append("\nBEGIN\n");
				ret.append("HLOAD TEMP " + nCurrentTemp + " TEMP " + _var.getTemp() + " " + _var.getOffset() + "\n");
				ret.append("RETURN TEMP " + nCurrentTemp + "\n");//载入变量
				ret.append("END");
				
				++nCurrentTemp;
			}
		}
		
		return ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public MPiglet visit(IntegerLiteral n, MType argu)//数字
	{
		return new MPiglet(null, null, n.f0.toString());
	}

	/**
	 * f0 -> "true"
	 */
	public MPiglet visit(TrueLiteral n, MType argu)//逻辑真
	{
		return new MPiglet(null, null, "1");
	}

	/**
	 * f0 -> "false"
	 */
	public MPiglet visit(FalseLiteral n, MType argu)//逻辑假
	{
		return new MPiglet(null, null, "0");
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public MPiglet visit(Identifier n, MType argu)//标识符
	{
		return new MPiglet(n.f0.toString(), null, null);//返回标识符
	}

	/**
	 * f0 -> "this"
	 */
	public MPiglet visit(ThisExpression n, MType argu)//this指针
	{
		MIdentifier parent = ((MIdentifier) argu).getParent();
		
		while (parent != null)
		{
			if (parent instanceof MClass) return new MPiglet(null, parent.getName(), "TEMP 0");//返回TEMP0
			parent = parent.getParent();//返回父标识符中的类
		}
		
		return new MPiglet(null, null, "TEMP 0");
	}

	/**
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 */
	public MPiglet visit(ArrayAllocationExpression n, MType argu)//创建数组
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		
		int nArrayTemp, nSizeTemp, nLoopTemp, nRearTemp, nLabel1, nLabel2, nLabel3, nLabel4;
		String szLengthExp;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		nLabel3 = nCurrentLabel++;
		nLabel4 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, "\nBEGIN\n");
		
		MPiglet exp = n.f3.accept(this, argu);
		if (exp.isDigit() || exp.isTemp())//数组长度是立即数或者TEMP
		{
			szLengthExp = exp.toString();
		}
		else//载入数组长度
		{
			int nLengthTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nLengthTemp + " ");
			ret.append(exp);
			ret.append("\n");
			
			szLengthExp = "TEMP " + nLengthTemp;
		}
		
		ret.append("CJUMP LT 0 " + szLengthExp + " L" + nLabel3 + "\n");//检查数组长度是否正确
		
		nSizeTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nSizeTemp + " TIMES 4 PLUS 1 "+ szLengthExp + "\n");//数组占内存大小
		
		nArrayTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nArrayTemp + " HALLOCATE TEMP " + nSizeTemp + "\n");//分配内存
		ret.append("HSTORE TEMP " + nArrayTemp + " 0 " + szLengthExp + "\n");//存入数组长度
		
		nRearTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nRearTemp + " PLUS TEMP " + nArrayTemp + " TEMP " + nSizeTemp + "\n");//数组末端位置
		
		nLoopTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nLoopTemp + " PLUS 4 TEMP " + nArrayTemp + "\n");//从第一个元素开始循环
		
		ret.append("L" + nLabel1 + "\tCJUMP LT TEMP " + nLoopTemp + " TEMP " + nRearTemp + " L" + nLabel2 + "\n");//到结束为止
		ret.append("HSTORE TEMP " + nLoopTemp + " 0 0\n");//每个元素均设为0
		ret.append("MOVE TEMP " + nLoopTemp + " PLUS 4 TEMP " + nLoopTemp + "\n");//下一个元素
		ret.append("JUMP L" + nLabel1 + "\n");//继续循环
		
		ret.append("L" + nLabel2 + "\tJUMP L" + nLabel4 + "\n");
		ret.append("L" + nLabel3 + "\tERROR\n");//数组为NULL或越界，报错
		ret.append("L" + nLabel4 + "\tNOOP\n");
		
		ret.append("RETURN TEMP " + nArrayTemp + "\n");//返回数组
		ret.append("END");
		
		n.f4.accept(this, argu);
		return ret;
	}

	/**
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 */
	public MPiglet visit(AllocationExpression n, MType argu)//创建对象
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();
		MClass _class = classList.get(szClass);
		
		int nSize, nMethodSize, nObjectTemp, nMethodTemp;//总分配内存，方法表分配内存，对象的TEMP位置，方法表的TEMP位置
		
		nMethodSize = _class.getMethodSize();
		nSize = _class.getSize();
		
		nMethodTemp = nCurrentTemp++;
		nObjectTemp = nCurrentTemp++;
		
		MPiglet ret = new MPiglet(null, _class.getName(), "\nBEGIN\n");
		
		ret.append("MOVE TEMP " + nMethodTemp + " HALLOCATE " + nMethodSize + "\n");
		ret.append("MOVE TEMP " + nObjectTemp + " HALLOCATE " + nSize + "\n");
		
		for (int i = 0; i < nMethodSize; i += 4) ret.append("HSTORE TEMP " + nMethodTemp + 
				" " + i + " " + _class.getMethodByOffset(i).getPigletName() + "\n");//存入各方法
		ret.append("HSTORE TEMP " + nObjectTemp + " 0 TEMP " + nMethodTemp + "\n");//存入方法表
		
		for (int i = 4; i < nSize; i += 4) ret.append("HSTORE TEMP " + nObjectTemp + " " + i + " 0\n");//变量
		
		ret.append("RETURN TEMP " + nObjectTemp + "\n");//返回对象
		ret.append("END");
		
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		
		return ret;
	}

	/**
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	public MPiglet visit(NotExpression n, MType argu)//逻辑非
	{
		n.f0.accept(this, argu);
		
		MPiglet ret = new MPiglet(null, null, "MINUS 1 ");//拿1去减
		ret.append(n.f1.accept(this, argu));//减去表达式
		
		return ret;
	}

	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 */
	public MPiglet visit(BracketExpression n, MType argu)//括号表达式
	{
		n.f0.accept(this, argu);
		MPiglet ret = n.f1.accept(this, argu);//直接返回括号中的值
		n.f2.accept(this, argu);
		return ret;
	}
}
