package borMi.parser;
/**
 * 	Token类示例: <'(', '('>,  value = '(', kind = '('
 * 				<'|', '|'> 
 * 				<'a', 'Variable'> value = 'a', kind = 'Variable'
 * 	Input: 以';'结尾的布尔表达式(不含有任何空格, 并且假定布尔表达式没有语法错误)
 */	
import java.util.ArrayList;

public class Scanner {
	ArrayList<Token> tokens; 
	
	public boolean hasMoreToken(){
		return tokens.size()!=1; //剩下最后的";"
	}
	
	public Token getCurrentToken(){
		return tokens.get(0);
	}

	public boolean match(String kind){ //Check当前Token的类型, 这里比较丑陋
		if(tokens.size()==0){
			System.out.println("There are no more tokens!");
			return false;
		}
		return tokens.get(0).kind.equals(kind);
	}
	
	public void consume(String s){
		if(!tokens.get(0).kind.equals(s))
			System.out.println("Scanner:: consume: Expect "+s+" But "+tokens.get(0).kind);
		tokens.remove(0);
	}
	
	private void SplitIntoTokens(String s){
		s = s.replaceAll(" ", ""); //去空格

		String split = "()|&!;";
		
		for(int i=0; i<s.length(); i++){
			if(split.contains(s.charAt(i)+"")){
				tokens.add(new Token(s.charAt(i)+"", s.charAt(i)+""));
			}
			else{
				StringBuffer buffer = new StringBuffer();
				while(!split.contains(s.charAt(i)+"")){
					buffer.append(s.charAt(i));
					i++;
				}
				tokens.add(new Token(buffer.toString(), "Variable"));
				i--;
			}
		}
	}
	
	public Scanner(String s){
		tokens = new ArrayList<Token>();
		SplitIntoTokens(s);
	}

}
