import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;  
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;  
import java.util.Vector;
      
    import javax.servlet.ServletException;  
import javax.servlet.http.HttpServlet;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  

import kanga.KangaParser;
import kanga.visitor.ConvertToMipsVisitor;

import piglet.PigletParser;
import piglet.RuntimeError;
import piglet.visitor.Convert2SPigletVisitor;
import piglet.visitor.CountTempVisitor;
import piglet.visitor.GJPigletInterpreter;
import spiglet.SpigletParser;
import spiglet.symboltable.SMethod;
import spiglet.visitor.CreateFlowGraphVisitor;
import spiglet.visitor.RegisterAllocatorVisitor;

import minijava.MiniJavaParser;
import minijava.symboltable.MClassList;
import minijava.symboltable.MIdentifier;
import minijava.symboltable.MType;
import minijava.visitor.BuildSymbolTableVisitor;
import minijava.visitor.Convert2PigletVisitor;
import minijava.visitor.TypeCheckVisitor;
      
    /** 
     * Servlet implementation class AjaxServlet 
     */  
    public class InterpreterServlet extends HttpServlet
    {
        private static final long serialVersionUID = 1L;  
    	
        private static String htmlSpecialChars(String szOriginal)
    	{		
    		String szHtml = szOriginal.replace("&", "&amp");
    		szHtml = szHtml.replace(" ", "&nbsp;");
    		szHtml = szHtml.replace("<", "&lt;");
    		szHtml = szHtml.replace(">", "&gt;");
    		szHtml = szHtml.replace("'", "&apos;");
    		szHtml = szHtml.replace("\"", "&quot;");
    		szHtml = szHtml.replace("\t", "&#9;");
    		
    		return szHtml;
    	}
        
        private MiniJavaParser mjParser;
    	private PigletParser pgParser;
    	private SpigletParser spgParser;
    	private KangaParser kgParser;
        
        /** 
         * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse 
         *      response) 
         */  
        protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {  
        	response.setContentType("text/html; charset=utf-8");  
        	response.setHeader("Cache-Control", "no-cache"); 
      		PrintWriter out = response.getWriter();  
            InputStream in = request.getInputStream();

        	try
        	{
              
	            if (mjParser == null) mjParser = new MiniJavaParser(in);
	            else MiniJavaParser.ReInit(in);
	            
	            MClassList my_classes = new MClassList();//符号表
				MIdentifier.classList =  my_classes;//设置静态变量，类型表
				
				minijava.syntaxtree.Node mjRoot = MiniJavaParser.Goal();			
				mjRoot.accept(new BuildSymbolTableVisitor(), my_classes);//建符号表
				mjRoot.accept(new TypeCheckVisitor(), my_classes);//其他类型检查
				            
	            if (my_classes != null)
	            {
	            	out.println("<table style=\"width:100%\"><tr>");
	            	            	
	            	out.println("<td class=\"code\">");
	            	out.println("<p style=\"font-weight:bold;\">Structure:</p>");
	            	out.println("<pre>");
	            	out.println(htmlSpecialChars(my_classes.toString()));
	            	out.println("</pre>");
	            	out.println("</td>");
	            	
	            	out.println("<td class=\"code\">");
	            	out.println("<p style=\"font-weight:bold;\">Type Error:</p>");
	            	out.println("<pre>");
	            	my_classes.printErrors(out);
	            	out.println("</pre>");
	            	out.println("</td></tr>");
	            	
	            	if (!my_classes.hasTypeErrors())
	            	{
	            		String szPg = mjRoot.accept(new Convert2PigletVisitor(), my_classes).toString();//转换为中间代码			
	        			
	        			ByteArrayInputStream pgStream = new ByteArrayInputStream(szPg.getBytes());//转换为输入流
	        			if (pgParser == null) pgParser = new PigletParser(pgStream);
	        			else PigletParser.ReInit(pgStream);
	        			pgStream.close();
	        			
	        			piglet.syntaxtree.Node pgRoot = PigletParser.Goal();//读取输入流
	        			CountTempVisitor vc = new CountTempVisitor();
	        			pgRoot.accept(vc);//计算Temp个数
	
	        			Convert2SPigletVisitor va = new Convert2SPigletVisitor(vc.getNextTemp());
	        			pgRoot.accept(va, null);
	        			
	        			String szSpg = va.getCode();//获取中间代码
	        			
	        			ByteArrayInputStream spgStream = new ByteArrayInputStream(szSpg.getBytes());//转换为输入流
	        			if (spgParser == null) spgParser = new SpigletParser(spgStream);
	        			else SpigletParser.ReInit(spgStream);
	        			spgStream.close();
	        			
	        			spiglet.syntaxtree.Node spgRoot = SpigletParser.Goal();
	        			String szKg = "";
	        			
	        			CreateFlowGraphVisitor vd = new CreateFlowGraphVisitor();
	        			spgRoot.accept(vd, null);
	        			
	        			Vector<SMethod> lstMethods = vd.getMethods();
	        			for (SMethod method : lstMethods)
	        			{
	        				RegisterAllocatorVisitor vr = new RegisterAllocatorVisitor(method);				
	        				szKg += vr.genCode() + "\n";//获取中间代码
	        			}
	        			
	        			ByteArrayInputStream kgStream = new ByteArrayInputStream(szKg.getBytes());//转换为输入流
	        			if (kgParser == null) kgParser = new KangaParser(kgStream);
	        			else KangaParser.ReInit(kgStream);
	        			kgStream.close();
	        			
	        			kanga.syntaxtree.Node kgRoot = KangaParser.Goal();
	        			ConvertToMipsVisitor v = new ConvertToMipsVisitor();
	        			
	        			kgRoot.accept(v);
	        			
	        			String szResult;
	        			try
	        			{
	        				ByteArrayOutputStream bos = new ByteArrayOutputStream();
		        			PrintStream ps = new PrintStream(bos);
		        			
	        				pgRoot.accept(new GJPigletInterpreter("MAIN",null, pgRoot, ps),pgRoot);
	        				szResult = new String(bos.toByteArray());
		        			
		        			ps.close();
		        			bos.close();
	        			}
	        			catch (Exception e)
	        			{
	        				szResult = "Runtime Error: " + e.getClass().getSimpleName();
	        			}
	        			catch (Error e)
	        			{
	        				szResult = "Runtime Error: " + e.getClass().getSimpleName();
	        			}
	            		
	            		out.println("<tr>");
		            	out.println("<td class=\"code\">");
		            	out.println("<p style=\"font-weight:bold;\">MIPS:</p>");
		            	
		            	out.println("<pre>");
		            	out.println(htmlSpecialChars(v.toString()));
		            	out.println("</pre>");
		            	out.println("</td>");
		            	
		            	out.println("<td class=\"code\">");
		            	
		            	out.println("<p style=\"font-weight:bold;\">Output:</p>");
	                	
	            		out.println("<pre>");
	            		out.println(htmlSpecialChars(szResult));
	                	out.println("</pre>");
		            	
		            	out.println("</td></tr>");
	            	}
	            	out.println("</table>");
	            }

        	}
        	catch (Exception e) { }
            in.close();
            out.close();
        }
    }  