package spiglet.symboltable;

public class STemp implements Comparable<Object>//临时单元类
{	
	private int nIndex, nStart, nEnd, nLocation;
	private boolean bDead;
		
	public STemp(int nIndex)
	{
		this.nIndex = nIndex;//设置序号
	}
	
	public int getIndex()
	{
		return nIndex;
	}
	
	public void setStart(int nStart)
	{
		this.nStart = nStart;
	}
	
	public int getStart()
	{
		return nStart;
	}
	
	public void setEnd(int nEnd)
	{
		this.nEnd = nEnd;
	}
	
	public int getEnd()
	{
		return nEnd;
	}
	
	public void setLocation(int nLocation)
	{
		this.nLocation = nLocation;
	}
	
	public int getLocation()
	{
		return nLocation;
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof STemp)) return false;
		STemp other = (STemp) o;
		return nIndex == other.nIndex;
	}
	
	public void die()
	{
		bDead = true;
	}
	
	public boolean isDead()
	{
		return bDead;
	}
	
	public int compareTo(Object arg0)
	{
		STemp other = (STemp) arg0;
		return other.nEnd - nEnd;
	}
}
