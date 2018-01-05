package processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import borMi.classifier.Classifier;
import borMi.classifier.Partition;
import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import borMi.visitor.BorVisitor;

public class BorMiProcessor {
    public HashSet<TestCase> trueTestCases;
    public HashSet<TestCase> falseTestCases;

    // Mi的析取表达式的语法修改过, 这里可能在划分的时候会出问题
    public BorMiProcessor(String dnf) throws IOException{
        //BorMi的输入是dnf, 类似于"a&b|c&d", 不带括号, 默认&的优先级比|高
        Classifier c = new Classifier(dnf);
        
        ArrayList<BorNode> nodeList = getNodes(c);
        
        //把所有的划分连起来, 再调用一次BOR
        BorNode root = buildTree(nodeList);
        root.accept(new BorVisitor());
        
        this.trueTestCases = root.trueTestCases;
        this.falseTestCases = root.falseTestCases;
    }

    private ArrayList<BorNode> getNodes(Classifier c) throws IOException{
        //对于划分, 一边建立对应的树, 一边运算, 返回root
        ArrayList<BorNode> nodeList = new ArrayList<BorNode>();
        while(c.hasNext()){
            Partition part = c.nextPartition();
            BorNode tmp = new BorNode("");
            if(part.isSingular()){
                BorProcessor b = new BorProcessor(part.getPredicate());
                tmp.trueTestCases = b.trueTestCases;
                tmp.falseTestCases = b.falseTestCases;
            }
            else if(part.isNonseparable()){
                MiProcessor mi = new MiProcessor(part.getPredicate());
                tmp.trueTestCases = mi.trueTestCases;
                tmp.falseTestCases = mi.falseTestCases;
            }
            nodeList.add(tmp);
        }

        return nodeList;
    }
    
    private BorNode buildTree(ArrayList<BorNode> nodeList){
        BorNode root;
        if(nodeList.size()==1){
            root = nodeList.get(0);
        }
        else{
            BorNode left = new BorNode("");
            BorNode right = new BorNode("");

            left.trueTestCases = nodeList.get(0).trueTestCases;
            left.falseTestCases = nodeList.remove(0).falseTestCases;
            right.trueTestCases = nodeList.get(0).trueTestCases;
            right.falseTestCases = nodeList.remove(0).falseTestCases;

            root = new BorNode(left, right, "|");
            
            while(nodeList.size()!=0){
                BorNode tmp = new BorNode("");
                tmp.trueTestCases = nodeList.get(0).trueTestCases;
                tmp.falseTestCases = nodeList.remove(0).falseTestCases;
                root = new BorNode(root, tmp, "|");
            }
        }

        return root;
    }
}
