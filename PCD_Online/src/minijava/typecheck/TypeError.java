package minijava.typecheck;

public class TypeError implements Comparable<TypeError>//���ʹ����࣬�ɱȽ�
{
	private String szMsg;//������Ϣ
	private int nLine;//�к�
	
	public TypeError(int nLine, String szMsg)
	{
		this.szMsg = szMsg;
		this.nLine = nLine;
	}
	
	public String toString()//������ת��Ϊ�ַ������
	{
		return "Line " + nLine + ": " + szMsg; 
	}
	
	public int compareTo(TypeError other)//�ȽϺ����������кűȽ�
	{
		return nLine - other.nLine; 
	}
}

