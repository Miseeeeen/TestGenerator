package borMi.parser;

import java.util.ArrayList;
import com.microsoft.z3.*;

public class MiNode {
	public String value;
	public Context ctx;
	public BoolExpr expr;
	public ArrayList<MiNode> children;

	public MiNode(Context ctx, String value){
		this.value = value;
		this.children = new ArrayList<MiNode>();
		this.ctx = ctx;
		this.expr = ctx.mkBoolConst(value);
	}
	
	public MiNode(Context ctx, String value, ArrayList<MiNode> children){
		this.value = value;
		this.children = children;
		this.ctx = ctx;
		
		if(this.value.equals("!")){
			this.expr = ctx.mkNot(this.children.get(0).expr);
		}
		else if(this.value.equals("&")){
			this.expr = ctx.mkAnd(this.children.get(0).expr, this.children.get(1).expr);
			for(int i=2; i<this.children.size(); i++){
				this.expr = ctx.mkAnd(this.expr, this.children.get(i).expr);
			}
		}
		else if(this.value.equals("|")){
			this.expr = ctx.mkOr(this.children.get(0).expr, this.children.get(1).expr);
			for(int i=2; i<this.children.size(); i++){
				this.expr = ctx.mkOr(this.expr, this.children.get(i).expr);
			}
		}
	}
	
}
