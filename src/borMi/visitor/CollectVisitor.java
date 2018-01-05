package borMi.visitor;

import java.util.HashSet;

import borMi.parser.BorNode;

public class CollectVisitor implements Visitor {
	public static HashSet<String> variables = new HashSet<String>();

	public void visit(BorNode node){
		if(node.getChildrenSize()==0){
			variables.add(node.getValue());
		}
		if(node.getChildrenSize()==1){
			node.getFirstChild().accept(this);
		}
		if(node.getChildrenSize()==2){
			node.getFirstChild().accept(this);
			node.getSecondChild().accept(this);
		}
	}
}
