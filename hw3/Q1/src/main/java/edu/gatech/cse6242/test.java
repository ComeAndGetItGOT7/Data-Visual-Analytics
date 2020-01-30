
import java.util.StringTokenizer;


public class test {
	public static void main(String[] args){
 		String s="this is a test";
		StringTokenizer st = new StringTokenizer(s); 
		while(st.hasMoreTokens())
			System.out.println(st.nextToken()); 

	}

}