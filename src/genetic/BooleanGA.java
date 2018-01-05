package genetic;

import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import borMi.parser.BorParser;
import borMi.parser.Scanner;
import utils.TestCasesPrinter;
import utils.DnfGenerator;
import utils.MutationTester;

public class BooleanGA {
    
    public static void drive(String boolExpr){
        int maxGeneration = 50;
        System.out.println(boolExpr);

        // 15dnf, 200, 0.05, 0.97
        GeneticAlgorithm ga = new GeneticAlgorithm(200, 0.05, 0.97, 5, boolExpr);
        Population population = ga.initPopulation();
        ga.evalPopulation(population);

        int generation = 1;
        HashSet<TestCase> testCases = new HashSet<TestCase>();

        while (ga.isTerminationConditionMet(generation, maxGeneration) == false) {
            //System.out.println("population fittness: " + population.getFittness());

            population = ga.crossoverPopulation(population);

            population = ga.mutatePopulation(population);

            ga.evalPopulation(population);
            
            /*
            for(int i=0; i<20; i++){
                Individual indiv = population.getFittest(i);
                testCases.add(Decoder.decode(indiv));
            }
            */

            generation++;
        }

        decode(population, testCases);
        System.out.println("testCases size -> "+testCases.size());
        TestCasesPrinter.print(testCases);
        //System.out.println(score(testCases, boolExpr));
    }

    public static void main(String[] args) {
        String boolExpr = DnfGenerator.getDnf(15);
        //boolExpr = "P06D&P06D-DBJ&!P06D-FBJ&(T13D-SLJ|T13D-XLJ|T64D-SLJ|T64D-XLJ|P06D-OVSJ)";
        int maxGeneration = 50;
        System.out.println(boolExpr);

        // 迭代的年代长一点
        //GeneticAlgorithm ga = new GeneticAlgorithm(10000, 0.001, 0.97, 200, boolExpr);
        // 15dnf, 200, 0.05, 0.97
        GeneticAlgorithm ga = new GeneticAlgorithm(300, 0.05, 0.97, 5, boolExpr);
        Population population = ga.initPopulation();
        ga.evalPopulation(population);

        int generation = 1;
        HashSet<TestCase> testCases = new HashSet<TestCase>();

        while (ga.isTerminationConditionMet(generation, maxGeneration) == false) {
            System.out.println("population fittness: " + population.getFittness());

            population = ga.crossoverPopulation(population);

            population = ga.mutatePopulation(population);

            ga.evalPopulation(population);
            
            /*
            for(int i=0; i<20; i++){
                Individual indiv = population.getFittest(i);
                testCases.add(Decoder.decode(indiv));
            }
            */

            generation++;
        }

        decode(population, testCases);
        System.out.println("testCases size -> "+testCases.size());
        System.out.println(score(testCases, boolExpr));
    }
    
    public static void decode(Population population, HashSet<TestCase> testCases){
        for(Individual individual: population.getIndividuals()){
            testCases.add(Decoder.decode(individual));
        }
    }
    
    public static double score(HashSet<TestCase> testCases, String boolExpr){
        int times = 30;
        double kill = 0;

        for(int i=0; i<times; i++){
            String mutatedExpr = MutationTester.lif(boolExpr);
            for(TestCase c: testCases){
                if(isKilled(c, boolExpr, mutatedExpr)){
                    kill++;
                    break;
                }
            }
        }
        
        return kill/times;
    }
    
    private static boolean isKilled(TestCase constraint, String normalExpr, String mutatedExpr) {
        BorParser ps1 = new BorParser(new Scanner(normalExpr + ";"));
        BorNode normalTree = ps1.getAST();
        BorParser ps2 = new BorParser(new Scanner(mutatedExpr + ";"));
        BorNode mutatedTree = ps2.getAST();

        if(eval(constraint, normalTree)!=eval(constraint, mutatedTree)){
            return true;
        }
        else{
            return false;
        }
    }
    
    private static boolean eval(TestCase c, BorNode root){
        if(root.children.size()==0){
            if(c.get(root.value).equals("true")){
                return true;
            }
            else if(c.get(root.value).equals("false")){
                return false;
            }
            else{
                System.out.println("MutationTester::eval Wrong!");
                return false;
            }
        }
        else if(root.value.equals("!")){
            return !eval(c, root.getFirstChild());
        }
        else if(root.value.equals("&")){
            return eval(c, root.getFirstChild())&&eval(c, root.getSecondChild());
        }
        else if(root.value.equals("|")){
            return eval(c, root.getFirstChild())||eval(c, root.getSecondChild());
        }
        else{
            System.out.println("MutationTester::eval Wrong!");
            return true;
        }
    }

}
