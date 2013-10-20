package minijava.visitor;

import java.util.Enumeration;

import minijava.symboltable.MClass;
import minijava.symboltable.MClassList;
import minijava.symboltable.MIdentifier;
import minijava.symboltable.MMethod;
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


public class BuildSymbolTableVisitor extends GJDepthFirst<String, MType>//�����ű���Ҫ�������
{
	private MClassList classList;//���ű�
	
	// Auto class visitors--probably don't need to be overridden.
	public String visit(NodeList n, MType argu)
	{
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			e.nextElement().accept(this,argu);
		}
		
		return null;
	}
	
	public String visit(NodeListOptional n, MType argu)
	{
		if (n.present())
		{
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
			{
				e.nextElement().accept(this,argu);
			}
			
		}
		
		return null;
	}

	public String visit(NodeOptional n, MType argu)
	{
		if (n.present()) return n.node.accept(this,argu);
		else return null;
	}
	
	public String visit(NodeSequence n, MType argu)
	{		
		for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
		{
			e.nextElement().accept(this,argu);
		}
		
		return null;
	}
	
	public String visit(NodeToken n, MType argu) { return null; }
	
	
//User-generated visitor methods below

	/**
	* f0 -> MainClass()
	* f1 -> ( TypeDeclaration() )*
	* f2 -> <EOF>
	*/
	public String visit(Goal n, MType argu)//����������������
	{
		this.classList = (MClassList) argu;//���÷��ű�
		
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
	public String visit(MainClass n, MType argu)//����
	{
		n.f0.accept(this, classList);
		
		String szClass = n.f1.accept(this, classList);//������
		MClass newClass = new MClass(szClass);//��������
		
		classList.insert(newClass, n.f1.f0.beginLine);//ֱ�Ӳ�������
		
		n.f2.accept(this, newClass);//�����ڲ�
		n.f3.accept(this, newClass);
		n.f4.accept(this, newClass);
		n.f5.accept(this, newClass);
		n.f6.accept(this, newClass);
		
		MMethod newMethod = new MMethod("main", "void", newClass);//����main����
		newClass.insertMethod(newMethod, n.f1.f0.beginLine);//����main����
		
		n.f7.accept(this, newMethod);//main�����ڲ�
		n.f8.accept(this, newMethod);
		n.f9.accept(this, newMethod);
		n.f10.accept(this, newMethod);
		
		String szVar = n.f11.accept(this, newMethod);//��������
		MVar newVar = new MVar(szVar, "String []", newMethod);//��������
		newMethod.insertParam(newVar);//�������
		
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
	public String visit(TypeDeclaration n, MType argu)//�������������򺬻��ࣩ
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
	public String visit(ClassDeclaration n, MType argu)//������(��������)
	{
		n.f0.accept(this, classList);
		
		String szClass = n.f1.accept(this, classList);//����
		MClass newClass = new MClass(szClass);//������
		
		if (!(classList).insert(newClass, n.f1.f0.beginLine))//������ű�����
		{
			classList.addError(new TypeError(n.f1.f0.beginLine, "Multiple class declarations: " + "\"" + szClass + "\""));
		}	
			
		n.f2.accept(this, newClass);//���ڲ�
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
	public String visit(ClassExtendsDeclaration n, MType argu)//�������������ࣩ
	{	
		n.f0.accept(this, classList);
		
		String szClass = n.f1.accept(this, classList);//����
		MClass newClass = new MClass(szClass);//������
		
		if (!(classList).insert(newClass, n.f1.f0.beginLine))//������ű�����
		{
			classList.addError(new TypeError(n.f1.f0.beginLine, "Multiple class declarations: " + "\"" + szClass + "\""));
		}
			
		n.f2.accept(this, newClass);//���ڲ�
		
		String szBase = n.f3.accept(this, newClass);//������
		newClass.setBase(szBase);//���û���
		
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
	public String visit(VarDeclaration n, MType argu)//��������
	{
		String szType = n.f0.accept(this, argu);//����
		String szVar = n.f1.accept(this, argu);//������
		
		MVar newVar = new MVar(szVar, szType, (MIdentifier) argu);//��������
		if (!((MIdentifier) argu).insertVar(newVar))//���븸��ʶ��,����
		{
			classList.addError(new TypeError(n.f1.f0.beginLine, "Multiple variables declarations: " + "\"" + szVar + "\""));
		}
		
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
	public String visit(MethodDeclaration n, MType argu)//��������
	{
		n.f0.accept(this, argu);
		
		String szType = n.f1.accept(this, argu);//��������
		String szMethod = n.f2.accept(this, argu);//������
		
		MMethod newMethod = new MMethod(szMethod, szType, (MClass) argu);//�·���
		if (!((MClass) argu).insertMethod(newMethod, n.f2.f0.beginLine))//���븸��,����
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
		n.f10.accept(this, newMethod);
		n.f11.accept(this, newMethod);
		n.f12.accept(this, newMethod);
		
		return null;
	}

	/**
	* f0 -> FormalParameter()
	* f1 -> ( FormalParameterRest() )*
	*/
	public String visit(FormalParameterList n, MType argu)//�����б�
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> Type()
	* f1 -> Identifier()
	*/
	public String visit(FormalParameter n, MType argu)//��������
	{
		String szType = n.f0.accept(this, argu);//��������
		String szVar = n.f1.accept(this, argu);//������
		
		MVar newVar = new MVar(szVar, szType, (MIdentifier) argu);//��������
		if (!((MMethod) argu).insertParam(newVar))//��������
		{
			classList.addError(new TypeError(n.f1.f0.beginLine, "Multiple parameters declarations: " + "\"" + szVar + "\""));
		}
		
		return null;
	}

	/**
	* f0 -> ","
	* f1 -> FormalParameter()
	*/
	public String visit(FormalParameterRest n, MType argu)//�����б�
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
	public String visit(Type n, MType argu)//����
	{		
		return n.f0.accept(this, argu);
	}

	/**
	* f0 -> "int"
	* f1 -> "["
	* f2 -> "]"
	*/
	public String visit(ArrayType n, MType argu) {//������
		return "int[]";
	}

	/**
	* f0 -> "boolean"
	*/
	public String visit(BooleanType n, MType argu) {//������
		return "boolean";
	}

	/**
	* f0 -> "int"
	*/
	public String visit(IntegerType n, MType argu) {//����
		return "int";
	}

	/**
	* f0 -> Block()
	*       | AssignmentStatement()
	*       | ArrayAssignmentStatement()
	*       | IfStatement()
	*       | WhileStatement()
	*       | PrintStatement()
	*/
	public String visit(Statement n, MType argu)//���
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "{"
	* f1 -> ( Statement() )*
	* f2 -> "}"
	*/
	public String visit(Block n, MType argu)//�����
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
	public String visit(AssignmentStatement n, MType argu)//��ֵ���
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
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
	public String visit(ArrayAssignmentStatement n, MType argu)//���鸳ֵ���
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
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
	public String visit(IfStatement n, MType argu)//�������
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
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
	public String visit(WhileStatement n, MType argu)//ѭ�����
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
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
	public String visit(PrintStatement n, MType argu)//��ӡ���
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
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
	public String visit(Expression n, MType argu)//���ʽ
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "&&"
	* f2 -> PrimaryExpression()
	*/
	public String visit(AndExpression n, MType argu)//�߼�����ʽ
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "<"
	* f2 -> PrimaryExpression()
	*/
	public String visit(CompareExpression n, MType argu)//�Ƚϱ��ʽ
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "+"
	* f2 -> PrimaryExpression()
	*/
	public String visit(PlusExpression n, MType argu)//�ӷ����ʽ
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "-"
	* f2 -> PrimaryExpression()
	*/
	public String visit(MinusExpression n, MType argu)//�������ʽ
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "*"
	* f2 -> PrimaryExpression()
	*/
	public String visit(TimesExpression n, MType argu)//�˷����ʽ
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "["
	* f2 -> PrimaryExpression()
	* f3 -> "]"
	*/
	public String visit(ArrayLookup n, MType argu)//ȡ����ĳһ��
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> "length"
	*/
	public String visit(ArrayLength n, MType argu)//���鳤��
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	/**
	* f0 -> PrimaryExpression()
	* f1 -> "."
	* f2 -> Identifier()
	* f3 -> "("
	* f4 -> ( ExpressionList() )?
	* f5 -> ")"
	*/
	public String visit(MessageSend n, MType argu)//���ö��󷽷�
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		n.f5.accept(this, argu);
		return null;
	}

	/**
	* f0 -> Expression()
	* f1 -> ( ExpressionRest() )*
	*/
	public String visit(ExpressionList n, MType argu)//ʵ���б�
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> ","
	* f1 -> Expression()
	*/
	public String visit(ExpressionRest n, MType argu)//ʵ��
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
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
	public String visit(PrimaryExpression n, MType argu)//�������ʽ
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> <INTEGER_LITERAL>
	*/
	public String visit(IntegerLiteral n, MType argu)//��������
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "true"
	*/
	public String visit(TrueLiteral n, MType argu)//�߼���
	{	
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "false"
	*/
	public String visit(FalseLiteral n, MType argu)//�߼���
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> <IDENTIFIER>
	*/
	public String visit(Identifier n, MType argu)//��ʶ��
	{
		return n.f0.toString();//���ر�ʶ����
	}

	/**
	* f0 -> "this"
	*/
	public String visit(ThisExpression n, MType argu)//thisָ��
	{
		n.f0.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "new"
	* f1 -> "int"
	* f2 -> "["
	* f3 -> Expression()
	* f4 -> "]"
	*/
	public String visit(ArrayAllocationExpression n, MType argu)//������
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		n.f4.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "new"
	* f1 -> Identifier()
	* f2 -> "("
	* f3 -> ")"
	*/
	public String visit(AllocationExpression n, MType argu)//�¶���
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		n.f3.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "!"
	* f1 -> Expression()
	*/
	public String visit(NotExpression n, MType argu)//�߼��Ǳ��ʽ
	{
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		return null;
	}

	/**
	* f0 -> "("
	* f1 -> Expression()
	* f2 -> ")"
	*/
	public String visit(BracketExpression n, MType argu)//���ű��ʽ
	{	
		n.f0.accept(this, argu);
		n.f1.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}
	

}
