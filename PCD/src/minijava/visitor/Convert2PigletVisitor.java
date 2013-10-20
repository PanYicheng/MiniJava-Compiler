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
public class Convert2PigletVisitor extends GJDepthFirst<MPiglet, MType>//MiniJavaת��Piglet
{
	private int nCurrentTemp, nCurrentLabel;//��ǰTemp����ǰ��ǩ
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
		return ret;//���Ӵ���
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
			
			return ret;//���Ӵ���
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
		return ret;//���Ӵ���
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
	public MPiglet visit(Goal n, MType argu)//�������
	{
		classList = (MClassList) argu;//���ű�
		nCurrentTemp = classList.alloc(20);//����ƫ�Ƶ�ַ�����������ֲ�����TEMP���
		
		MPiglet ret = n.f0.accept(this, argu);
		ret.append(n.f1.accept(this, argu));
		n.f2.accept(this, argu);
		
		ret.format();//��������
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
	public MPiglet visit(MainClass n, MType argu)//��������
	{
		MPiglet ret = new MPiglet(null, null, "MAIN\n");		
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();
		MClass newClass = classList.get(szClass);//��ȡ����
		
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
		
		ret.append(n.f14.accept(this, newMethod));//��ȡ����
		
		n.f15.accept(this, newMethod);
		n.f16.accept(this, newClass);
		
		ret.append("END\n");
		
		return ret;
	}

	/**
	 * f0 -> ClassDeclaration()
	 *		 | ClassExtendsDeclaration()
	 */
	public MPiglet visit(TypeDeclaration n, MType argu)//������(�����򺬻���)
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
	public MPiglet visit(ClassDeclaration n, MType argu)//������(��������)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//����
		MClass newClass = classList.get(szClass);//��ȡ��
		
		n.f2.accept(this, newClass);//����
		n.f3.accept(this, newClass);
		
		MPiglet ret = n.f4.accept(this, newClass);//ֻ���ط���
		
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
	public MPiglet visit(ClassExtendsDeclaration n, MType argu)//������(������)
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();//����
		MClass newClass = classList.get(szClass);//��ȡ��
		
		n.f2.accept(this, newClass);//����
		n.f3.accept(this, newClass);
		n.f4.accept(this, newClass);
		n.f5.accept(this, newClass);
		
		MPiglet ret = n.f6.accept(this, newClass);//ֻ���ط���
		
		n.f7.accept(this, newClass);
		
		return ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public MPiglet visit(VarDeclaration n, MType argu)//��������
	{
		MPiglet ret = null;
		n.f0.accept(this, argu);
		
		String szVar = n.f1.accept(this, argu).getName();
		if (argu instanceof MMethod)//�Ǿֲ�����
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
	public MPiglet visit(MethodDeclaration n, MType argu)//��������
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		String szMethod = n.f2.accept(this, argu).getName();//������
		MMethod newMethod = ((MClass) argu).getMethod(szMethod);//��ȡ����
		
		MPiglet ret = new MPiglet(null, null, "\n" + newMethod.getPigletName() + " [ " + (newMethod.getPigletParamCount() + 1) + " ]\nBEGIN\n");
		
		n.f3.accept(this, newMethod);//������
		n.f4.accept(this, newMethod);
		n.f5.accept(this, newMethod);
		n.f6.accept(this, newMethod);
		
		ret.append(n.f7.accept(this, newMethod));//��������
		ret.append(n.f8.accept(this, newMethod));//�������
		
		n.f9.accept(this, newMethod);
		
		ret.append("RETURN ");//return���
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
	public MPiglet visit(FormalParameterList n, MType argu)//�β��б�
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 */
	public MPiglet visit(FormalParameter n, MType argu)//�β�����
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 */
	public MPiglet visit(FormalParameterRest n, MType argu)//�β�����
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
	public MPiglet visit(Type n, MType argu)//����
	{	
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
	 */
	public MPiglet visit(ArrayType n, MType argu)//����
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "boolean"
	 */
	public MPiglet visit(BooleanType n, MType argu)//����
	{	
		n.f0.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> "int"
	 */
	public MPiglet visit(IntegerType n, MType argu)//����
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
	public MPiglet visit(AssignmentStatement n, MType argu)//��ֵ���
	{
		MPiglet ret = new MPiglet(null, null, null);
		String szVar = n.f0.accept(this, argu).getName();//��ȡ��ʶ��
		
		MVar _var = ((MIdentifier) argu).getVar(szVar);//��ȡ����
		if (_var.isTemp()) ret.append("MOVE TEMP " + _var.getTemp() + " ");//ֱ����temp
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
	public MPiglet visit(ArrayAssignmentStatement n, MType argu)//���鸳ֵ���
	{
		int nLabel1, nLabel2, nLengthTemp, nArrayTemp;
		String szIndexExp;//�±�
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, null);
		MPiglet idf = n.f0.accept(this, argu);
		MVar _var = ((MIdentifier) argu).getVar(idf.getName());//��ȡ��ʶ��
				
		if (_var.isTemp())//ֱ����temp
		{
			nArrayTemp = _var.getTemp();
		}
		else//����TEMP
		{
			nArrayTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nArrayTemp);
			ret.append("\nBEGIN\n");
			ret.append("HLOAD TEMP " + nCurrentTemp + " TEMP " + _var.getTemp() + " " + _var.getOffset() + "\n");
			ret.append("RETURN TEMP " + nCurrentTemp + "\n");//�������
			ret.append("END\n");
		}
		
		n.f1.accept(this, argu);
		
		ret.append("CJUMP LT 0 TEMP " + nArrayTemp + " L" + nLabel1 + "\n");//��������Ƿ�ΪNULL
		
		nLengthTemp = nCurrentTemp++;//���鳤��
		ret.append("HLOAD TEMP " + nLengthTemp + " " + " TEMP " + nArrayTemp + " 0\n");//����
		
		MPiglet exp = n.f2.accept(this, argu);
		if (exp.isDigit() || exp.isTemp())//�±������ֻ�TEMP
		{
			szIndexExp = exp.toString();
		}
		else//�����±�
		{
			int nIndexTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nIndexTemp + " ");
			ret.append(exp);
			ret.append("\n");
			
			szIndexExp = "TEMP " + nIndexTemp;
		}
		
		ret.append("CJUMP LT 0 PLUS 1 " + szIndexExp + " L" + nLabel1 + "\n");//��������Ƿ���Խ��
		ret.append("CJUMP LT " + szIndexExp + " TEMP " + nLengthTemp + " L" + nLabel1 + "\n");//��������Ƿ���Խ��
		ret.append("HSTORE PLUS TEMP " + nArrayTemp + " TIMES 4 PLUS 1 " + szIndexExp + " 0 ");//����Ԫ��
		
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		
		ret.append(n.f5.accept(this, argu));//���ʽ
		ret.append("\n");
		
		ret.append("JUMP L" + nLabel2 + "\n");
		ret.append("L" + nLabel1 + "\tERROR\n");//����ΪNULL��Խ�磬����
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
	public MPiglet visit(IfStatement n, MType argu)//�������
	{
		int nLabel1, nLabel2;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MPiglet ret = new MPiglet(null, null, "CJUMP ");//�����ж�
		ret.append(n.f2.accept(this, argu));
		ret.append(" L" + nLabel1 + "\n");//�������������������ǩ1
		
		n.f3.accept(this, argu);
		
		ret.append(n.f4.accept(this, argu));
		ret.append("JUMP L" + nLabel2 + "\n");//ֱ��������ǩ
		
		n.f5.accept(this, argu);
		
		ret.append("L" + nLabel1 + "\t");//��ǩ1
		ret.append(n.f6.accept(this, argu));
		ret.append("L" + nLabel2 + "\tNOOP\n");//��ǩ2
		
		return ret;
	}

	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 */
	public MPiglet visit(WhileStatement n, MType argu)//ѭ�����
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
		ret.append("JUMP L" + nLabel1 + "\n");//���ؼ����ж�
		ret.append("L" + nLabel2 + "\tNOOP\n");//��ǩ2
		
		return ret;
	}

	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 */
	public MPiglet visit(PrintStatement n, MType argu)//��ӡ���
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		
		MPiglet ret = new MPiglet(null, null, "PRINT ");//������
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
	public MPiglet visit(Expression n, MType argu)//���ʽ
	{
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	public MPiglet visit(AndExpression n, MType argu)//�߼�����ʽ����·�룩
	{
		int nLabel1, nLabel2, nTemp;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		nTemp = nCurrentTemp++;//Ҫ���ص�ֵ
		
		MPiglet ret = new MPiglet(null, null, "\nBEGIN\n");
		
		ret.append("CJUMP ");
		ret.append(n.f0.accept(this, argu));
		ret.append(" L" + nLabel1 + "\n");//��һ�����ʽΪ0���ٽ���ڶ������ʽ�ļ��㣨ͬ��׼JAVA��
		
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
	public MPiglet visit(CompareExpression n, MType argu)//�Ƚϱ��ʽ
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
	public MPiglet visit(PlusExpression n, MType argu)//�ӷ����ʽ
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
	public MPiglet visit(MinusExpression n, MType argu)//�������ʽ
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
	public MPiglet visit(TimesExpression n, MType argu)//�˷����ʽ
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
	public MPiglet visit(ArrayLookup n, MType argu)//ȡ����Ԫ��
	{
		int nLabel1, nLabel2, nArrayTemp, nLengthTemp, nElementTemp;
		String szIndexExp;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, "\nBEGIN\n");
		
		MPiglet exp1 = n.f0.accept(this, argu);//��ȡ������ʽ
		if (exp1.isTemp())//��TEMP������������������
		{
			nArrayTemp = exp1.getTemp();
		}
		else//����TEMP������
		{
			nArrayTemp = nCurrentTemp++;
			ret.append("MOVE TEMP " + nArrayTemp + " ");
			ret.append(exp1);
			ret.append("\n");
		}
		
		ret.append("CJUMP LT 0 TEMP " + nArrayTemp + " L" + nLabel1 + "\n");//�����Ƿ�ΪNULL
		
		nLengthTemp = nCurrentTemp++;
		ret.append("HLOAD TEMP " + nLengthTemp + " TEMP " + nArrayTemp + " 0\n");//�������鳤��
		
		n.f1.accept(this, argu);
		
		MPiglet exp2 = n.f2.accept(this, argu);
		if (exp2.isDigit() || exp2.isTemp())//�±������ֻ�TEMP
		{
			szIndexExp = exp2.toString();
		}
		else//�����±�
		{
			int nIndexTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nIndexTemp + " ");
			ret.append(exp2);
			ret.append("\n");
			
			szIndexExp = "TEMP " + nIndexTemp;
		}
		
		ret.append("CJUMP LT 0 PLUS 1 " + szIndexExp + " L" + nLabel1 + "\n");//��������Ƿ���Խ��
		ret.append("CJUMP LT " + szIndexExp + " TEMP " + nLengthTemp + " L" + nLabel1 + "\n");//��������Ƿ���Խ��
		
		nElementTemp = nCurrentTemp++;
		ret.append("HLOAD TEMP " + nElementTemp + " PLUS TEMP " + nArrayTemp + " TIMES 4 PLUS 1 " + szIndexExp + " 0\n");
		
		n.f3.accept(this, argu);
		
		ret.append("JUMP L" + nLabel2 + "\n");
		ret.append("L" + nLabel1 + "\tERROR\n");//����Խ���ΪNULL
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
	public MPiglet visit(ArrayLength n, MType argu)//ȡ���鳤��
	{
		int nLabel1, nLabel2, nArrayTemp, nLengthTemp;
		
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, "\nBEGIN\n");
		
		MPiglet exp1 = n.f0.accept(this, argu);
		if (exp1.isTemp())//��TEMP
		{
			nArrayTemp = exp1.getTemp();
		}
		else//����TEMP������
		{
			nArrayTemp = nCurrentTemp++;
			ret.append("MOVE TEMP " + nArrayTemp + " ");
			ret.append(exp1);
			ret.append("\n");
		}
		
		ret.append("CJUMP LT 0 TEMP " + nArrayTemp + " L" + nLabel1 + "\n");//�Ƿ�ΪNULL
		
		nLengthTemp = nCurrentTemp++;
		ret.append("HLOAD TEMP " + nLengthTemp + " TEMP " + nArrayTemp + " 0\n");
			
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
				
		ret.append("JUMP L" + nLabel2 + "\n");
		ret.append("L" + nLabel1 + "\tERROR\n");//����ΪNULL
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
	public MPiglet visit(MessageSend n, MType argu)//��������
	{
		int nLabel1, nLabel2, nMethodListTemp, nMethodTemp, nObjectTemp;
				
		nLabel1 = nCurrentLabel++;
		nLabel2 = nCurrentLabel++;
		
		MPiglet ret = new MPiglet(null, null, "CALL\nBEGIN\n");
		MPiglet exp = n.f0.accept(this, argu);//��ȡ���ʽ
		
		if (exp.isTemp())//��ȡ���ʽ
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
		
		ret.append("CJUMP LT 0 TEMP " + nObjectTemp + " L" + nLabel1 + "\n");//�����Ƿ�Ϊ��
		
		nMethodListTemp = nCurrentTemp++;//������
		ret.append("HLOAD TEMP " + nMethodListTemp + " TEMP " + nObjectTemp + " 0\n");//���뷽����
		
		n.f1.accept(this, argu);
		
		String szClass = exp.getType();
		MClass _class = classList.get(szClass);//��ȡ��
		
		String szMethod = n.f2.accept(this, argu).getName();
		MMethod _method = _class.getMethod(szMethod);//��ȡ����
		
		String szReturnType = _method.getType();
		MClass returnClass = classList.get(szReturnType);
		if (!returnClass.isBasicType()) ret.setType(szReturnType);//���÷�������
		
		nMethodTemp = nCurrentTemp++;//����
		ret.append("HLOAD TEMP " + nMethodTemp + " TEMP " + nMethodListTemp + " " + _method.getOffset() + "\n");//ƫ��λ��
		
		n.f3.accept(this, argu);
		
		MActualParamList paramList = new MActualParamList(n.f3.beginLine, (MIdentifier) argu);//ʵ���б�
		
		n.f4.accept(this, paramList);
		
		Vector<MPiglet> pigletList = paramList.getPiglets();//�������м�����
		int nParamList = pigletList.size();//��������
		boolean bExtraParam = nParamList > 18;//���������Ƿ񳬳�
		
		String preEnd = "", postEnd = "";
		
		if (bExtraParam)
		{
			int nExtraParamTemp = nCurrentTemp++;//����Ĳ���
			preEnd += "MOVE TEMP " + nExtraParamTemp + " HALLOCATE " + _method.getExtraParamSize() + "\n";
			
			for (int i = 18; i < nParamList; ++i)
			{
				preEnd += "HSTORE TEMP " + nExtraParamTemp + " " + (4 * (i - 18)) + " ";//��Ӵ洢
				preEnd += pigletList.get(i);
				preEnd += "\n";
			}
			
			for (int i = 0; i < 18; ++i)//���������
			{
				postEnd += " ";
				postEnd += pigletList.get(i);
			}
			
			postEnd += " TEMP " + nExtraParamTemp;//�������
		}
		else
		{
			for (int i = 0; i < nParamList; ++i)//���������
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
		ret.append("END (TEMP " + nObjectTemp);//���μ�ӵ�ַ
		
		ret.append(postEnd);
		ret.append(")");
		
		n.f5.accept(this, argu);
		
		return ret;
	}

	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	public MPiglet visit(ExpressionList n, MType argu)//ʵ���б�
	{
		((MActualParamList) argu).addPiglet(n.f0.accept(this, argu));//�����м����
		n.f1.accept(this, argu);
		return null;
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	public MPiglet visit(ExpressionRest n, MType argu)//ʵ��
	{	
		n.f0.accept(this, argu);
		((MActualParamList) argu).addPiglet(n.f1.accept(this, argu));//�����м����
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
	public MPiglet visit(PrimaryExpression n, MType argu)//�������ʽ
	{
		MPiglet ret = n.f0.accept(this, argu);
		
		if (ret.toString() == null)//�Ǳ���
		{
			MVar _var = ((MIdentifier) argu).getVar(ret.getName());
			
			String szType = _var.getType();
			MClass _class = classList.get(szType);
			if (_class != null && !_class.isBasicType()) ret.setType(szType);//�������ͣ��������Ͳ����ã�
			
			if (_var.isTemp())//ֱ����temp
			{
				ret.append("TEMP " + _var.getTemp());
			}
			else
			{
				ret.append("\nBEGIN\n");
				ret.append("HLOAD TEMP " + nCurrentTemp + " TEMP " + _var.getTemp() + " " + _var.getOffset() + "\n");
				ret.append("RETURN TEMP " + nCurrentTemp + "\n");//�������
				ret.append("END");
				
				++nCurrentTemp;
			}
		}
		
		return ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public MPiglet visit(IntegerLiteral n, MType argu)//����
	{
		return new MPiglet(null, null, n.f0.toString());
	}

	/**
	 * f0 -> "true"
	 */
	public MPiglet visit(TrueLiteral n, MType argu)//�߼���
	{
		return new MPiglet(null, null, "1");
	}

	/**
	 * f0 -> "false"
	 */
	public MPiglet visit(FalseLiteral n, MType argu)//�߼���
	{
		return new MPiglet(null, null, "0");
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public MPiglet visit(Identifier n, MType argu)//��ʶ��
	{
		return new MPiglet(n.f0.toString(), null, null);//���ر�ʶ��
	}

	/**
	 * f0 -> "this"
	 */
	public MPiglet visit(ThisExpression n, MType argu)//thisָ��
	{
		MIdentifier parent = ((MIdentifier) argu).getParent();
		
		while (parent != null)
		{
			if (parent instanceof MClass) return new MPiglet(null, parent.getName(), "TEMP 0");//����TEMP0
			parent = parent.getParent();//���ظ���ʶ���е���
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
	public MPiglet visit(ArrayAllocationExpression n, MType argu)//��������
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
		if (exp.isDigit() || exp.isTemp())//���鳤��������������TEMP
		{
			szLengthExp = exp.toString();
		}
		else//�������鳤��
		{
			int nLengthTemp = nCurrentTemp++;
			
			ret.append("MOVE TEMP " + nLengthTemp + " ");
			ret.append(exp);
			ret.append("\n");
			
			szLengthExp = "TEMP " + nLengthTemp;
		}
		
		ret.append("CJUMP LT 0 " + szLengthExp + " L" + nLabel3 + "\n");//������鳤���Ƿ���ȷ
		
		nSizeTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nSizeTemp + " TIMES 4 PLUS 1 "+ szLengthExp + "\n");//����ռ�ڴ��С
		
		nArrayTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nArrayTemp + " HALLOCATE TEMP " + nSizeTemp + "\n");//�����ڴ�
		ret.append("HSTORE TEMP " + nArrayTemp + " 0 " + szLengthExp + "\n");//�������鳤��
		
		nRearTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nRearTemp + " PLUS TEMP " + nArrayTemp + " TEMP " + nSizeTemp + "\n");//����ĩ��λ��
		
		nLoopTemp = nCurrentTemp++;
		ret.append("MOVE TEMP " + nLoopTemp + " PLUS 4 TEMP " + nArrayTemp + "\n");//�ӵ�һ��Ԫ�ؿ�ʼѭ��
		
		ret.append("L" + nLabel1 + "\tCJUMP LT TEMP " + nLoopTemp + " TEMP " + nRearTemp + " L" + nLabel2 + "\n");//������Ϊֹ
		ret.append("HSTORE TEMP " + nLoopTemp + " 0 0\n");//ÿ��Ԫ�ؾ���Ϊ0
		ret.append("MOVE TEMP " + nLoopTemp + " PLUS 4 TEMP " + nLoopTemp + "\n");//��һ��Ԫ��
		ret.append("JUMP L" + nLabel1 + "\n");//����ѭ��
		
		ret.append("L" + nLabel2 + "\tJUMP L" + nLabel4 + "\n");
		ret.append("L" + nLabel3 + "\tERROR\n");//����ΪNULL��Խ�磬����
		ret.append("L" + nLabel4 + "\tNOOP\n");
		
		ret.append("RETURN TEMP " + nArrayTemp + "\n");//��������
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
	public MPiglet visit(AllocationExpression n, MType argu)//��������
	{
		n.f0.accept(this, argu);
		
		String szClass = n.f1.accept(this, argu).getName();
		MClass _class = classList.get(szClass);
		
		int nSize, nMethodSize, nObjectTemp, nMethodTemp;//�ܷ����ڴ棬����������ڴ棬�����TEMPλ�ã��������TEMPλ��
		
		nMethodSize = _class.getMethodSize();
		nSize = _class.getSize();
		
		nMethodTemp = nCurrentTemp++;
		nObjectTemp = nCurrentTemp++;
		
		MPiglet ret = new MPiglet(null, _class.getName(), "\nBEGIN\n");
		
		ret.append("MOVE TEMP " + nMethodTemp + " HALLOCATE " + nMethodSize + "\n");
		ret.append("MOVE TEMP " + nObjectTemp + " HALLOCATE " + nSize + "\n");
		
		for (int i = 0; i < nMethodSize; i += 4) ret.append("HSTORE TEMP " + nMethodTemp + 
				" " + i + " " + _class.getMethodByOffset(i).getPigletName() + "\n");//���������
		ret.append("HSTORE TEMP " + nObjectTemp + " 0 TEMP " + nMethodTemp + "\n");//���뷽����
		
		for (int i = 4; i < nSize; i += 4) ret.append("HSTORE TEMP " + nObjectTemp + " " + i + " 0\n");//����
		
		ret.append("RETURN TEMP " + nObjectTemp + "\n");//���ض���
		ret.append("END");
		
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		
		return ret;
	}

	/**
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	public MPiglet visit(NotExpression n, MType argu)//�߼���
	{
		n.f0.accept(this, argu);
		
		MPiglet ret = new MPiglet(null, null, "MINUS 1 ");//��1ȥ��
		ret.append(n.f1.accept(this, argu));//��ȥ���ʽ
		
		return ret;
	}

	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 */
	public MPiglet visit(BracketExpression n, MType argu)//���ű��ʽ
	{
		n.f0.accept(this, argu);
		MPiglet ret = n.f1.accept(this, argu);//ֱ�ӷ��������е�ֵ
		n.f2.accept(this, argu);
		return ret;
	}
}
