package genetic;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Population {
    private Individual population[];
    private double populationFitness = -1;
    private String boolExpr;

    public Population(int populationSize, String boolExpr) {
        this.population = new Individual[populationSize];
        this.boolExpr = boolExpr;
        for(int i=0; i<populationSize; i++){
            int[] chromosome = Encoder.initChromosome(boolExpr);
            population[i] = new Individual(chromosome, boolExpr);
        }
    }

    public Individual[] getIndividuals() {
        return this.population;
    }
    
    public String getBoolExpr() {
        return boolExpr;
    }

    public Individual getFittest(int offset) {
        Arrays.sort(this.population, new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
                if (o1.getFitness() > o2.getFitness()) {
                    return -1;
                } else if (o1.getFitness() < o2.getFitness()) {
                    return 1;
                }
                return 0;
            }
        });

        return this.population[offset];
    }
    
    public double getFittness(){
        double fittness = 0;
        for(Individual individual: population){
            fittness += individual.getFitness();
        }
        return fittness;
    }

    public void setPopulationFitness(double fitness) {
        this.populationFitness = fitness;
    }

    public double getPopulationFitness() {
        return this.populationFitness;
    }

    public int size() {
        return this.population.length;
    }

    public Individual setIndividual(int offset, Individual individual) {
        return population[offset] = individual;
    }

    public Individual getIndividual(int offset) {
        return population[offset];
    }
    
    public void shuffle() {
        Random rnd = new Random();
        for (int i = population.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            Individual a = population[index];
            population[index] = population[i];
            population[i] = a;
        }
    }
}