package processor;

import java.util.HashSet;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import borMi.parser.BorParser;
import borMi.parser.Scanner;
import borMi.visitor.BorVisitor;

public class BorProcessor {
	public HashSet<TestCase> trueTestCases;
	public HashSet<TestCase> falseTestCases;
	
	public BorProcessor(String pr){
		//Bor的输入为任意布尔表达式, 如"a|b&(!c|(d&e))"

		Scanner sc = new Scanner(pr+";");
		BorParser ps = new BorParser(sc);
		BorNode root = ps.getAST();

		root.accept(new BorVisitor());

		this.trueTestCases = root.getTrueTestCases();
		this.falseTestCases = root.getFalseTestCases();

		//ReaderAndWriter.write("./true_constraint", constraintsT);
		//ReaderAndWriter.write("./false_constraint", constraintsF);
	}

}
