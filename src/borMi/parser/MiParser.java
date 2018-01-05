package borMi.parser;

import java.util.ArrayList;
import com.microsoft.z3.Context;

/**
 * 	Statement -> Expression ';'
 * 
 *  每个term的两边强制加上括号, 防止遗忘
 * 	Expression -> '(' Term ')' '|' Expression
 * 			   -> '(' Term ')'
 * 
 * 	Term -> Primary '&' Term
 * 		 -> Primary
 * 
 * 	Primary -> '!' 'Variable'
 * 			-> 'Variable'
 *	
 *	parse and build AST
 * 
 * @author misen
 *
 */

public class MiParser {
	Scanner scanner;
	public MiParser(Scanner scanner){
		this.scanner = scanner;
	}

	public MiNode getAST(){
		try{
			return statement();
		}catch(Exception e){
			System.out.println("parse wrong");
		}
		return null;
	}
	
	public MiNode statement(){ 
		MiNode tmp = expression();
		scanner.consume(";");
		return tmp;
	}
	
	public MiNode expression(){ 
		ArrayList<MiNode> children = new ArrayList<MiNode>();
		Context ctx = new Context();

		scanner.consume("(");
		MiNode node = term(ctx);
		scanner.consume(")");

		children.add(node);
		while(scanner.match("|")){
			scanner.consume("|");
			scanner.consume("(");
			node = term(ctx);
			scanner.consume(")");
			children.add(node);
		}

		if(children.size()==1){
			return children.get(0);
		}

		return new MiNode(ctx, "|", children);
	}
	
	public MiNode term(Context ctx){
		ArrayList<MiNode> children = new ArrayList<MiNode>();
		MiNode node = primary(ctx);
		children.add(node);
		while(scanner.match("&")){
			scanner.consume("&");
			node = primary(ctx);
			children.add(node);
		}

		if(children.size()==1){
			return children.get(0);
		}

		return new MiNode(ctx, "&", children);	
	}
	
	public MiNode primary(Context ctx){
		if(scanner.match("!")){
			scanner.consume("!");
			String value = scanner.getCurrentToken().value;
			scanner.consume("Variable");
			ArrayList<MiNode> children = new ArrayList<MiNode>();
			children.add(new MiNode(ctx, value));
			return new MiNode(ctx, "!", children);
		}
		else if(scanner.match("Variable")){
			String value = scanner.getCurrentToken().value;
			scanner.consume("Variable");
			return new MiNode(ctx, value);
		}
		return null;
	}

}
