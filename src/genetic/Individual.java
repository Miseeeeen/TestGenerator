package genetic;

public class Individual {
    private int[] chromosome;
    private double fitness = -1;
    private String boolExpr;

    public Individual(int[] chromosome, String boolExpr) {
        this.chromosome = chromosome;
        this.boolExpr = boolExpr;
    }

    public String getBoolExpr() {
        return boolExpr;
    }

    public int[] getChromosome() {
        return this.chromosome;
    }

    public int getChromosomeLength() {
        return this.chromosome.length;
    }

    public void setGene(int offset, int gene) {
        this.chromosome[offset] = gene;
    }

    public int getGene(int offset) {
        return this.chromosome[offset];
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness() {
        return this.fitness;
    }

    public String toString() {
        String output = "";
        for (int gene = 0; gene < this.chromosome.length; gene++) {
            output += this.chromosome[gene];
        }
        return output + " -> " + fitness;
    }
}
