package borMi.visitor;


import java.util.HashSet;
import java.util.Iterator;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;

public class BorVisitor implements Visitor {

    public void visit(BorNode node) {
        if(node.getValue().equals("|")){
            visit(node.getFirstChild());
            visit(node.getSecondChild());
            orWriter(node);
        }
        else if(node.getValue().equals("&")){
            visit(node.getFirstChild());
            visit(node.getSecondChild());
            andWriter(node);
        }
        else if(node.getValue().equals("!")){
            visit(node.getFirstChild());
            notWriter(node);
        }
        else{ 
            return;
        }
        
    }
    
    private void notWriter(BorNode node){
        if(node.getValue()!="!"||node.getChildrenSize()!=1)
            System.out.println("BorVisitor::notWriter Error!");

        node.setTrueTestCases(node.getFirstChild().getFalseTestCases());
        node.setFalseTestCases(node.getFirstChild().getTrueTestCases());
    }
    
    private void orWriter(BorNode node){
        if(!node.getValue().equals("|")||node.getChildrenSize()!=2)
            System.out.println("BorVisitor::orWriter Error!");
        
        BorNode left = node.getFirstChild();
        BorNode right = node.getSecondChild();
        
        //update falseConstraint
        node.setFalseTestCases(onProduct(left.getFalseTestCases(), right.getFalseTestCases()));
        node.setFalseTestCases(onProduct(left.getFalseTestCases(), right.getFalseTestCases()));
        
        //update trueConstraints
        node.setTrueTestCases(product(left.getTrueTestCases(), left.getFalseTestCases(), right.getTrueTestCases(), right.getFalseTestCases(), "getTrueTestCases"));

    }

    private void andWriter(BorNode node){
        if(!node.getValue().equals("&")||node.getChildrenSize()!=2)
            System.out.println("BorVisitor::andWriter Error!");
        
        BorNode left = node.getFirstChild();
        BorNode right = node.getSecondChild();

        //update trueConstraints
        node.setTrueTestCases(onProduct(left.getTrueTestCases(), right.getTrueTestCases()));
        
        //update falseConstraints
        node.setFalseTestCases(product(left.getTrueTestCases(), left.getFalseTestCases(), right.getTrueTestCases(), right.getFalseTestCases(), "getContraintsF"));
    }

    private HashSet<TestCase> product(HashSet<TestCase> leftT, HashSet<TestCase> leftF, HashSet<TestCase> rightT, HashSet<TestCase> rightF, String command){
        HashSet<TestCase> result = new HashSet<TestCase>();
        if(command.equals("getTrueTestCases")){
            Iterator rightIt = rightF.iterator();
            TestCase rightC = (TestCase)rightIt.next();
            for(TestCase c: leftT){
                TestCase tmp = new TestCase();
                tmp.putAll(c);
                tmp.putAll(rightC);
                result.add(tmp);
            }

            Iterator leftIt = leftF.iterator();
            TestCase leftC = (TestCase)leftIt.next();
            for(TestCase c: rightT){
                TestCase tmp = new TestCase();
                tmp.putAll(leftC);
                tmp.putAll(c);
                result.add(tmp);
            }
        }
        else if(command.equals("getContraintsF")){
            Iterator rightIt = rightT.iterator();
            TestCase rightC = (TestCase)rightIt.next();
            for(TestCase c: leftF){
                TestCase tmp = new TestCase();
                tmp.putAll(c);
                tmp.putAll(rightC);
                result.add(tmp);
            }

            Iterator leftIt = leftT.iterator();
            TestCase leftC = (TestCase)leftIt.next();
            for(TestCase c: rightF){
                TestCase tmp = new TestCase();
                tmp.putAll(leftC);
                tmp.putAll(c);
                result.add(tmp);
            }
        }
        else{
            System.out.println("BorVisitor::product Error!");
        }
        
        return result;
    }
    
    private HashSet<TestCase> onProduct(HashSet<TestCase> leftSet, HashSet<TestCase> rightSet){ //待清理
        HashSet<TestCase> result = new HashSet<TestCase>();
        int leftSize = leftSet.size();
        int rightSize = rightSet.size();

        if(leftSize<rightSize){
            Iterator leftIt = leftSet.iterator();
            Iterator rightIt = rightSet.iterator();

            while(rightIt.hasNext()){
                if(!leftIt.hasNext()){
                    leftIt = leftSet.iterator(); 
                }

                TestCase leftC = (TestCase)leftIt.next();
                TestCase rightC = (TestCase)rightIt.next();
                TestCase c = new TestCase();

                c.putAll(leftC); 
                c.putAll(rightC);
                
                result.add(c);
            }

        }
        else{
            Iterator leftIt = leftSet.iterator();
            Iterator rightIt = rightSet.iterator();

            while(leftIt.hasNext()){
                if(!rightIt.hasNext()){
                    rightIt = rightSet.iterator(); 
                }

                TestCase leftC = (TestCase)leftIt.next();
                TestCase rightC = (TestCase)rightIt.next();
                TestCase c = new TestCase();

                c.putAll(leftC); 
                c.putAll(rightC);
                
                result.add(c);
            }
        }

        return result;
    }
}
