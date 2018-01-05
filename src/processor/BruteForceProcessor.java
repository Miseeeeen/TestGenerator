package processor;

/*
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import borMi.dataStructure.Constraint;
import borMi.parser.MiNode;
import borMi.parser.MiParser;
import borMi.parser.Scanner;
import utils.ConstraintsPrinter;

import java.io.FileWriter;
import java.io.IOException;

  暴力求解的MI
  @author misen

public class BruteForceProcessor {
    public HashSet<Constraint> constraintsT;
    public HashSet<Constraint> constraintsF;
    public String dnf;
    
    private boolean has(HashSet<Constraint> set, Constraint c){
        for(Constraint tmp: set){
            int flag = 0;
            for(String key: tmp.keySet()){
                if(!c.get(key).equals(tmp.get(key))){
                    flag = 1;
                }
            }
            
            if(flag==0){
                return true; 
            }
        }
        
        return false;
    }

    // 输入的dnf不需要以';'结尾
    // MI模块对a&b&c这样的式子不能处理
    public BruteForceProcessor(String dnf) throws IOException {
        this.dnf = dnf + ";";
        Scanner sc = new Scanner(this.dnf);
        MiParser pr = new MiParser(sc);
        MiNode root = pr.getAST();

        HashSet<Constraint> trueConstraints = new HashSet<Constraint>();
        HashSet<Constraint> falseConstraints = new HashSet<Constraint>();

        ArrayList<HashSet<Constraint>> tei = new ArrayList<HashSet<Constraint>>();

        // build a default constraint
        Constraint defaultConstraint = new Constraint();
        Stack<MiNode> stack = new Stack<MiNode>();
        stack.push(root);
        while (!stack.isEmpty()) {
            MiNode tmp = stack.pop();
            if (tmp.children.size() == 0) {
                defaultConstraint.put(tmp.value, "null");
            }
            for (MiNode i : tmp.children) {
                stack.push(i);
            }
        }

        // 假设子节点都是乘
        // -------trueConstraints
        for (MiNode term : root.children) { // 对于每个子项
            Constraint c = (Constraint) defaultConstraint.clone();
            for (MiNode literal : term.children) {
                if (literal.value.equals("!")) {
                    c.put(literal.children.get(0).value, "false");
                }
                else {
                    c.put(literal.value, "true");
                }
            }

            HashSet<Constraint> set = new HashSet<Constraint>();
            set.add(c);

            for (String key : c.keySet()) {
                if (set.iterator().next().get(key).equals("null")) {
                    HashSet<Constraint> fset = new HashSet<Constraint>();

                    for (Constraint cc : set) {
                        Constraint fc = new Constraint();
                        fc.putAll(cc);
                        cc.put(key, "true");
                        fc.put(key, "false");
                        fset.add(fc);
                    }

                    set.addAll(fset);
                }
            }

            tei.add(set);
        }


        for (int j = 0; j < tei.size(); j++) {
            HashSet<Constraint> t = new HashSet<Constraint>();
            t.addAll(tei.get(j));
            HashSet<Constraint> v = new HashSet<Constraint>();
            for (int k = 0; k < tei.size(); k++) {
                if (k != j) {
                    for(Constraint ccc: t){
                        if(!has(tei.get(j), ccc)){
                            v.add(ccc);
                        }
                    }
                }
            }

            try {
                trueConstraints.add(v.iterator().next());
            } catch (Exception e) {

            }
        }

        // -------falseConstraints

        ArrayList<HashSet<Constraint>> feij = new ArrayList<HashSet<Constraint>>();
        for (MiNode term : root.children) { // 对于每个子项
            Constraint c = (Constraint) defaultConstraint.clone();
            for (MiNode literal : term.children) {
                if (literal.value.equals("!")) {
                    c.put(literal.children.get(0).value, "false");
                }
                else {
                    c.put(literal.value, "true");
                }
            }

            HashSet<Constraint> set = new HashSet<Constraint>();
            set.add(c);

            for (String key : c.keySet()) {
                if (set.iterator().next().get(key).equals("null")) {
                    HashSet<Constraint> fset = new HashSet<Constraint>();

                    for (Constraint cc : set) {
                        Constraint fc = new Constraint();
                        fc.putAll(cc);
                        cc.put(key, "true");
                        fc.put(key, "false");
                        fset.add(fc);
                    }

                    set.addAll(fset);
                }
            }

            for (MiNode literal : term.children) {
                if (literal.value.equals("!")) {
                    HashSet<Constraint> newSet = new HashSet<Constraint>();
                    for (Constraint cc : set) {
                        newSet.add((Constraint) cc.clone());
                    }
                    for (Constraint cc : newSet) {
                        if (cc.get(literal.children.get(0).value).equals("true")) {
                            cc.put(literal.children.get(0).value, "false");
                        }
                        else {
                            cc.put(literal.children.get(0).value, "true");
                        }
                    }
                    feij.add(newSet);
                }
                else {
                    HashSet<Constraint> newSet = new HashSet<Constraint>();
                    for (Constraint cc : set) {
                        newSet.add((Constraint) cc.clone());
                    }
                    for (Constraint cc : newSet) {
                        if (cc.get(literal.value).equals("true")) {
                            cc.put(literal.value, "false");
                        }
                        else {
                            cc.put(literal.value, "true");
                        }
                    }

                    feij.add(newSet);
                }
            }
        }

        for (HashSet<Constraint> s : feij) {
            HashSet<Constraint> v = new HashSet<Constraint>();
            for (HashSet<Constraint> ss : tei) {
                for(Constraint ccc: s){
                    if(!has(ss, ccc)){
                        v.add(ccc);
                    }
                }
            }
            if (s.size() > 0) {
                try {
                    falseConstraints.add(v.iterator().next());
                } catch (Exception e) {

                }
            }
        }
    }


}
*/
