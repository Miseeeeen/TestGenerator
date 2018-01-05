package borMi.parser;

import java.util.HashSet;

import borMi.dataStructure.TestCase;

/**
 * 	Statement -> Expression ';'
 * 
 *  Expression -> Term Rest
 *  		   -> Term
 *  
 *  Rest -> '|' Term Rest
 *       -> '&' Term Rest
 *       -> e
 * 
 * 	Term -> '!' Primary
 * 		 -> Primary
 * 
 * 	Primary -> '(' Expression ')'
 * 			-> 'Variable'
 *	
 *	parse and build AST
 * 
 * @author misen
 *
 */

// 参照龙书将左递归改写成右递归
// rest这条解析规则, 空缺的部分先暂时用VOID填上, 等回到上一层时再对VOID进行替换

public class BorParser {
	Scanner scanner;
	public BorParser(Scanner scanner){
		this.scanner = scanner;
	}

	public BorNode getAST(){
		return statement();
	}
	
	public BorNode statement(){ 
		BorNode tmp = expression();
		scanner.consume(";");
		return tmp;
	}
	
	public BorNode expression(){ 
		BorNode term = term();
		
		if(scanner.match("|")||scanner.match("&")){
			BorNode rest = rest();
			BorNode p = rest;
			while(p.getValue().equals("&")||p.getValue().equals("|")){
				p = p.getFirstChild();
			}
			
			p.value = term.value;
			p.children = term.children;
			p.trueTestCases = term.trueTestCases;
			p.falseTestCases = term.falseTestCases;
			
			return rest;
		}
		else{
			return term;
		}
	}
	
	public BorNode rest(){
		if(scanner.match("|")||scanner.match("&")){
			String value = scanner.getCurrentToken().value;
			scanner.consume(value);
			
			BorNode term = term();
			BorNode rest = rest();
			
			BorNode p = rest;
			while(p.getValue().equals("&")||p.getValue().equals("|")){
				p = p.getFirstChild();
			}
			
			p.value = value;
			p.trueTestCases.clear();
			p.falseTestCases.clear();
			p.children.add(new BorNode("VOID"));
			p.children.add(term);

			return rest;
		}
		else{
			return new BorNode("VOID");
		}
	}
	
	public BorNode term(){
		if(scanner.match("!")){
			scanner.consume("!");
			return new BorNode(primary(), "!");
		}
		else{
			return primary();
		}
		
	}
	
	public BorNode primary(){
		if(scanner.match("(")){
			scanner.consume("(");
			BorNode tmp = expression();
			scanner.consume(")");
			return tmp;
		}
		else if(scanner.match("Variable")){
			String value = scanner.getCurrentToken().value;
			scanner.consume("Variable");
			return new BorNode(value);
		}
		return null;
	}

}
