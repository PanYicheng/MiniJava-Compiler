package spiglet.visitor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.PriorityQueue;
import spiglet.symboltable.SMethod;
import spiglet.symboltable.SStatement;
import spiglet.symboltable.STemp;
import spiglet.syntaxtree.BinOp;
import spiglet.syntaxtree.CJumpStmt;
import spiglet.syntaxtree.ErrorStmt;
import spiglet.syntaxtree.Exp;
import spiglet.syntaxtree.HAllocate;
import spiglet.syntaxtree.HLoadStmt;
import spiglet.syntaxtree.HStoreStmt;
import spiglet.syntaxtree.IntegerLiteral;
import spiglet.syntaxtree.JumpStmt;
import spiglet.syntaxtree.Label;
import spiglet.syntaxtree.MoveStmt;
import spiglet.syntaxtree.NoOpStmt;
import spiglet.syntaxtree.Operator;
import spiglet.syntaxtree.PrintStmt;
import spiglet.syntaxtree.SimpleExp;
import spiglet.syntaxtree.Temp;
import spiglet.syntaxtree.special.CallStmt;
import spiglet.syntaxtree.special.MoveArgStmt;
import spiglet.syntaxtree.special.MoveCallStmt;
import spiglet.syntaxtree.special.MoveTempStmt;
import spiglet.syntaxtree.special.PassArgStmt;
import spiglet.syntaxtree.special.Reg;
import spiglet.syntaxtree.special.ReturnStmt;

public class RegisterAllocatorVisitor extends GJDepthFirst<String, SStatement>
{
	private HashSet<Reg> lstUsedSReg = new HashSet<Reg>();
	private Hashtable<Reg, Integer> mapUsedTReg = new Hashtable<Reg, Integer>();
	
	private HashSet<Reg> lstFreeReg = new HashSet<Reg>();
	private PriorityQueue<STemp> heap = new PriorityQueue<STemp>();
	
	private Hashtable<Integer, Reg> mapReg = new Hashtable<Integer, Reg>();
	private Hashtable<String, Reg> mapRegName = new Hashtable<String, Reg>();
	private Hashtable<Integer, STemp> mapTemp = new Hashtable<Integer, STemp>();
	
	private HashSet<Integer> lstSpilled = new HashSet<Integer>();
	private int nSpilledSize;
	
	private SStatement[] arrStmt;
	private boolean bUsedV;
	
	private SMethod method;
	private String szCode = "";//Êä³ö´úÂë
	
	private void println(String szLine)
	{
		szCode += "\t" + szLine + "\n";
	}
	
	private void printLabel(SStatement stmt)
	{
		String szLabel = stmt.getLabel();
		if (szLabel != null) szCode += method.getLabelKangaName(szLabel);
	}
	
	public String toString()
	{
		return szCode;
	}
	
	public STemp getTemp(Temp temp)
	{
		int nTemp = Integer.parseInt(temp.f1.f0.tokenImage);
		return mapTemp.get(nTemp);
	}
	
	public RegisterAllocatorVisitor(SMethod method)
	{
		this.method = method;
		
		for (int i = 0; i < 8; ++i) mapReg.put(i, new Reg(i, "s" + i));//s¼Ä´æÆ÷
		for (int i = 0; i < 10; ++i) mapReg.put(i + 8, new Reg(i + 8, "t" + i));//t¼Ä´æÆ÷
		for (int i = 0; i < 4; ++i) mapReg.put(i + 18, new Reg(i + 18, "a" + i));//a¼Ä´æÆ÷
		for (int i = 0; i < 2; ++i) mapReg.put(i + 22, new Reg(i + 22, "v" + i));//v¼Ä´æÆ÷
		
		for (Reg reg : mapReg.values()) mapRegName.put(reg.toString(), reg);
		for (int i = 0; i < 18; ++i) lstFreeReg.add(mapReg.get(i));
		
		if (method.getArgs() > 4) nSpilledSize = method.getArgs() - 4;
	}
	
	private void allocate()//¼Ä´æÆ÷·ÖÅä
	{
		HashSet<Reg> lstUsedTReg = new HashSet<Reg>();
		
		arrStmt =	method.getStmts();//ÖØÐÂ½¨Á¢ÐòºÅ		
		mapTemp.putAll(method.getTemp());

		STemp[] arrTemp = mapTemp.values().toArray(new STemp[0]);
		Arrays.sort(arrTemp, new Comparator<Object>()
		{
			
			public int compare(Object arg0, Object arg1)
			{
				STemp a, b;
				a = (STemp) arg0;
				b = (STemp) arg1;
				return a.getStart() - b.getStart();
			}	
		});
		
		for (int x : method.getTempArgs().keySet())
		{
			if (x < 4) continue;
			int nSpilled = x - 4;
			
			lstSpilled.add(x);
			if (nSpilled > nSpilledSize) ++nSpilledSize;
		}
		
		for (int i = 0; i < arrTemp.length; ++i)
		{
			STemp tmp = arrTemp[i];
			
			for (int j = 0; j < i; ++j)
			{
				STemp tmp1 = arrTemp[j];
				
				if (tmp1.getEnd() >= tmp.getStart() || tmp1.isDead()) continue;
				tmp1.die();
				
				int nLocation = tmp1.getLocation();
				if (nLocation < 18)
				{
					heap.remove(tmp1);
					lstFreeReg.add(mapReg.get(nLocation));
				}
				else lstSpilled.remove(nLocation - 24);
			}
			
			if (!lstFreeReg.isEmpty())
			{
				Reg reg = lstFreeReg.iterator().next();
				lstFreeReg.remove(reg);
				
				if (reg.getIndex() < 8) lstUsedSReg.add(reg);
				else lstUsedTReg.add(reg);
				
				tmp.setLocation(reg.getIndex());
				
				heap.add(tmp);
			}
			else
			{
				int nSpilled, nLocation;
				
				for (nSpilled = 0; nSpilled < nSpilledSize && lstSpilled.contains(nSpilled); ++nSpilled);
				if (nSpilled == nSpilledSize) ++nSpilledSize;
				
				nLocation = nSpilled + 24;
				STemp tmp1 = heap.poll();
				if (tmp1.getEnd() < tmp.getEnd())
				{
					tmp.setLocation(nLocation);
					heap.add(tmp1);					
				}
				else
				{
					tmp.setLocation(tmp1.getLocation());
					tmp1.setLocation(nLocation);
					heap.add(tmp);
				}
				
				lstSpilled.add(nSpilled);
			}
		}
		
		for (int i = 0; i < method.getArgs(); ++i)
		{
			STemp tmp = mapTemp.get(i);
			if (tmp == null) continue;
			
			int nLocation = tmp.getLocation();			
			if (i < 4)
			{
				if (nLocation < 18)
				{
					Reg reg = mapReg.get(nLocation);
					println("MOVE " + reg + " a" + i);					
				}
				else
				{
					int nSpilled = nLocation - 24;
					println("ASTORE SPILLEDARG " + nSpilled + " a" + i);
				}
			}
			else
			{
				if (nLocation < 18)
				{
					Reg reg = mapReg.get(nLocation);
					println("ALOAD " + reg + " SPILLEDARG " + (i - 4));
				}
				else
				{
					int nSpilled = nLocation - 24;
					println("ALOAD v1 SPILLEDARG " + (i - 4));
					println("ASTORE SPILLEDARG " + nSpilled + " v1");
				}
			}
		}
		
		for (Reg reg : lstUsedTReg)
		{
			int nSpilled = nSpilledSize++;
			mapUsedTReg.put(reg, nSpilled);
		}
	}

	public String genCode()
	{
		allocate();
		
		for (SStatement stmt : arrStmt)
		{
			bUsedV = false;
			printLabel(stmt);
			stmt.getNode().accept(this, stmt);
		}
		
		for (Reg reg : lstUsedSReg)
		{
			int nSpilled = nSpilledSize++;
			szCode = "\tASTORE SPILLEDARG " + nSpilled + " " + reg + "\n" + szCode;
			szCode = szCode + "\tALOAD " + reg + " SPILLEDARG " + nSpilled + "\n";
		}
		
		szCode = method.getName() + " [" + method.getArgs() + "][" + nSpilledSize + "][" + method.getMaxCallArgs() + "]\n" + szCode;
		szCode += "END\n";
		return szCode;
	}
		
	/**
	 * f0 -> "NOOP"
	 */
	public String visit(NoOpStmt n, SStatement argu)
	{
		println("NOOP");
		return null;
	}

	/**
	 * f0 -> "ERROR"
	 */
	public String visit(ErrorStmt n, SStatement argu)
	{
		println("ERROR");
		return null;
	}

	/**
	 * f0 -> "CJUMP"
	 * f1 -> Temp()
	 * f2 -> Label()
	 */
	public String visit(CJumpStmt n, SStatement argu)
	{
		String szF1 = n.f1.accept(this, argu);
		String szF2 = method.getLabelKangaName(n.f2.accept(this, argu));
		println("CJUMP " + szF1 + " " + szF2);
		return null;
	}

	/**
	 * f0 -> "JUMP"
	 * f1 -> Label()
	 */
	public String visit(JumpStmt n, SStatement argu)
	{
		String szF1 = method.getLabelKangaName(n.f1.accept(this, argu));
		println("JUMP " + szF1);
		return null;
	}

	/**
	 * f0 -> "HSTORE"
	 * f1 -> Temp()
	 * f2 -> IntegerLiteral()
	 * f3 -> Temp()
	 */
	public String visit(HStoreStmt n, SStatement argu)
	{
		String szF1 = n.f1.accept(this, argu);
		String szF2 = n.f2.accept(this, argu);
		String szF3 = n.f3.accept(this, argu);
		println("HSTORE " + szF1 + " " + szF2 + " " + szF3);
		return null;
	}

	/**
	 * f0 -> "HLOAD"
	 * f1 -> Temp()
	 * f2 -> Temp()
	 * f3 -> IntegerLiteral()
	 */
	public String visit(HLoadStmt n, SStatement argu)
	{
		String szF2 = n.f2.accept(this, argu);
		String szF3 = n.f3.accept(this, argu);
		
		STemp tmp = getTemp(n.f1);
		int nLocation = tmp.getLocation();
		if (nLocation < 18)
		{
			String szF1 = mapReg.get(nLocation).toString();
			println("HLOAD " + szF1 + " " + szF2 + " " + szF3);
		}
		else
		{
			println("HLOAD v1 " + szF2 + " " + szF3);
			println("ASTORE SPILLEDARG " + (nLocation - 24) + " v1");
		}
		
		return null;
	}

	/**
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Exp()
	 */
	public String visit(MoveStmt n, SStatement argu)
	{
		String szF2 = n.f2.accept(this, argu);
		
		STemp tmp = getTemp(n.f1);
		int nLocation = tmp.getLocation();
		if (nLocation < 18)
		{
			String szF1 = mapReg.get(nLocation).toString();
			println("MOVE " + szF1 + " " + szF2);
		}
		else
		{
			println("MOVE v1 " + szF2);
			println("ASTORE SPILLEDARG " + (nLocation - 24) + " v1");
		}
		return null;
	}

	/**
	 * f0 -> "PRINT"
	 * f1 -> SimpleExp()
	 */
	public String visit(PrintStmt n, SStatement argu)
	{
		String szF1 = n.f1.accept(this, argu);
		println("PRINT " + szF1);
		return null;
	}
	
	/**
	 * f0 -> "RETURN"
	 * f1 -> SimpleExp()
	 */
	public String visit(ReturnStmt n, SStatement argu)
	{
		String szF1 = n.f1.accept(this, argu);
		println("MOVE v0 " + szF1);
		return null;
	}
	
	/**
	 * f0 -> "PASSARG"
	 * f1 -> IntegerLiteral()
	 * f2 -> Temp()
	 */
	public String visit(PassArgStmt n, SStatement argu)
	{
		String szF1 = n.f1.accept(this, argu);
		String szF2 = n.f2.accept(this, argu);
		println("PASSARG " + szF1 + " " + szF2);
		return null;
	}
	
	/**
	 * f0 -> "MOVE"
	 * f1 -> Reg()
	 * f2 -> Temp()
	 */
	public String visit(MoveArgStmt n, SStatement argu)
	{
		String szF1 = n.f1.toString();
		String szF2 = n.f2.accept(this, argu);
		println("MOVE " + szF1 + " " + szF2);
		return null;
	}
	
	/**
	 * f0 -> "CALL"
	 * f1 -> SimpleExp()
	 */
	public String visit(CallStmt n, SStatement argu)
	{
		String szF1 = n.f1.accept(this, argu);
		
		for (Reg reg : mapUsedTReg.keySet())
		{
			int nSpilled = mapUsedTReg.get(reg);
			println("ASTORE SPILLEDARG " + nSpilled + " " + reg);
		}
		
		println("CALL " + szF1);
		
		for (Reg reg : mapUsedTReg.keySet())
		{
			int nSpilled = mapUsedTReg.get(reg);
			println("ALOAD " + reg +" SPILLEDARG " + nSpilled);
		}
		
		return null;
	}
	
	/**
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Reg()
	 */
	public String visit(MoveCallStmt n, SStatement argu)
	{
		String szF2 = n.f2.toString();
		
		STemp tmp = getTemp(n.f1);
		int nLocation = tmp.getLocation();
		if (nLocation < 18)
		{
			String szF1 = mapReg.get(nLocation).toString();
			println("MOVE " + szF1 + " " + szF2);
		}
		else
		{
			println("MOVE v1 " + szF2);
			println("ASTORE SPILLEDARG " + (nLocation - 24) + " v1");
		}
		return null;
	}
	
	/**
	 * f0 -> "MOVE"
	 * f1 -> Temp()
	 * f2 -> Temp()
	 */
	public String visit(MoveTempStmt n, SStatement argu)
	{
		String szF2 = n.f2.accept(this, argu);
		
		STemp tmp = getTemp(n.f1);
		int nLocation = tmp.getLocation();
		if (nLocation < 18)
		{
			String szF1 = mapReg.get(nLocation).toString();
			println("MOVE " + szF1 + " " + szF2);
		}
		else
		{
			println("MOVE v1 " + szF2);
			println("ASTORE SPILLEDARG " + (nLocation - 24) + " v1");
		}
		
		return null;
	}

	/**
	 * f0 -> Call()
	 *		 | HAllocate()
	 *		 | BinOp()
	 *		 | SimpleExp()
	 */
	public String visit(Exp n, SStatement argu)
	{
		return n.f0.accept(this, argu);
	}
	
	/**
	 * f0 -> "HALLOCATE"
	 * f1 -> SimpleExp()
	 */
	public String visit(HAllocate n, SStatement argu)
	{
		String szF1 = n.f1.accept(this, argu);
		return "HALLOCATE " + szF1;
	}

	/**
	 * f0 -> Operator()
	 * f1 -> Temp()
	 * f2 -> SimpleExp()
	 */
	public String visit(BinOp n, SStatement argu)
	{
		String szF0 = n.f0.accept(this, argu);
		String szF1 = n.f1.accept(this, argu);
		String szF2 = n.f2.accept(this, argu);
		return szF0 + " " + szF1 + " " + szF2;
	}

	/**
	 * f0 -> "LT"
	 *		 | "PLUS"
	 *		 | "MINUS"
	 *		 | "TIMES"
	 */
	public String visit(Operator n, SStatement argu)
	{
		return n.f0.choice.toString();
	}

	/**
	 * f0 -> Temp()
	 *		 | IntegerLiteral()
	 *		 | Label()
	 */
	public String visit(SimpleExp n, SStatement argu)
	{
		return n.f0.accept(this, argu);
	}

	/**
	 * f0 -> "TEMP"
	 * f1 -> IntegerLiteral()
	 */
	public String visit(Temp n, SStatement argu)
	{
		String szF0;
		STemp temp = getTemp(n);
		int nLocation = temp.getLocation();
		
		if (nLocation < 18) szF0 = mapReg.get(nLocation).toString();
		else
		{
			int nSpilled = nLocation - 24;
			szF0 = bUsedV ? "v0" : "v1";
			bUsedV = true;
			println("ALOAD " + szF0 + " SPILLEDARG " + nSpilled);
		}
		
		return szF0;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n, SStatement argu)
	{
		return n.f0.tokenImage;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Label n, SStatement argu)
	{
		return n.f0.tokenImage;
	}
}
