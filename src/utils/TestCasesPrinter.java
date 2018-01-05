package utils;

import java.util.HashSet;

import borMi.dataStructure.TestCase;

public class TestCasesPrinter {
	public static int counter = 0;

	public static void print(HashSet<TestCase> set){
		for(TestCase c: set){
			System.out.print(counter+". ");
			for(String value: c.variableSet()) {
				System.out.print(value+":"+c.get(value)+" ");
			}
			counter++;
			System.out.println();
			System.out.println();
		}
	}
}
