package genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// מייצג אוכלוסייה של פתרונות באלגוריתם הגנטי
public class Population {
    // רשימת כל הכרומוזומים באוכלוסייה
    private List<Chromosome> chromosomes;

    // בונה אוכלוסייה ריקה
    public Population() {
        this.chromosomes = new ArrayList<>();
    }

    // מוסיף כרומוזום חדש לאוכלוסייה
    public void addChromosome(Chromosome chromosome) {
        chromosomes.add(chromosome);
    }

    // מחזיר את רשימת הכרומוזומים ללא אפשרות שינוי חיצונית
    public List<Chromosome> getChromosomes() {
        return Collections.unmodifiableList(chromosomes);
    }

    // מחזיר את הכרומוזום עם הציון הגבוה ביותר
    public Chromosome getBestChromosome() {
        if (chromosomes.isEmpty()) {
            return null;
        }

        Chromosome best = chromosomes.get(0);
        for (int i = 1; i < chromosomes.size(); i++) {
            if (chromosomes.get(i).getFitnessScore() > best.getFitnessScore()) {
                best = chromosomes.get(i);
            }
        }
        return best;
    }

    // ממיין את האוכלוסייה לפי ציון מהגבוה לנמוך
    public void sortByFitness() {
        chromosomes.sort(Comparator.comparingDouble(Chromosome::getFitnessScore).reversed());
    }

    // מחזיר את מספר הכרומוזומים באוכלוסייה
    public int size() {
        return chromosomes.size();
    }
}
