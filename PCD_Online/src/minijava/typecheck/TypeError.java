package minijava.typecheck;

public class TypeError implements Comparable<TypeError>//类型错误类，可比较
{
	private String szMsg;//错误信息
	private int nLine;//行号
	
	public TypeError(int nLine, String szMsg)
	{
		this.szMsg = szMsg;
		this.nLine = nLine;
	}
	
	public String toString()//将错误转换为字符串输出
	{
		return "Line " + nLine + ": " + szMsg; 
	}
	
	public int compareTo(TypeError other)//比较函数，根据行号比较
	{
		return nLine - other.nLine; 
	}
}

