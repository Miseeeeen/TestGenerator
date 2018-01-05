package borMi.visitor;
import borMi.parser.BorNode;


/**
 * 打印Node组成的Tree的visitor
 * @author misen
 *
 */
public class PrintVisitor implements Visitor{
	public void visit(BorNode node){
			if(node.getChildrenSize()==0){
				System.out.print(node.getValue());
			}

			if(node.getChildrenSize()==1){
				System.out.print("(");
				if(node.getValue().equals("!")){
					System.out.print("not");
				}
				System.out.print(" ");
				node.getFirstChild().accept(this);
				System.out.print(")");
			}

			if(node.getChildrenSize()==2){
				System.out.print("(");
				if(node.getValue().equals("&")){
					System.out.print("and");
				}
				else{
					System.out.print("or");
				}
				System.out.print(" ");
				node.getFirstChild().accept(this);
				System.out.print(" ");
				node.getSecondChild().accept(this);
				System.out.print(")");
			}
	}

}
