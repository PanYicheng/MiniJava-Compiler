package piglet;
import piglet.syntaxtree.Node;
import piglet.visitor.GJPigletInterpreter;

public class PgInterpreter{
   public static void main(String [] args) {
	  PigletParser parser = new PigletParser(System.in);
      try {
    	  int i = 0;
    	  //while(i < 100)
    	  {
    		  Node root = parser.Goal();
    		  //System.out.println("Program parsed successfully");
    		  root.accept(new GJPigletInterpreter("MAIN",null,root),root);
    		  
    		  //parser.ReInit(System.in);
    	  }
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
}

