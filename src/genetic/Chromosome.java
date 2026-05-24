package genetic;

import model.Schedule;

// מייצג פתרון יחיד בתוך האוכלוסייה של האלגוריתם הגנטי
public class Chromosome {
    // השיבוץ של הפתרון והציון שלו
    private Schedule schedule;
    private double fitnessScore;

    // בונה כרומוזום חדש עם שיבוץ וציון
    public Chromosome(Schedule schedule, double fitnessScore) {
        this.schedule = schedule;
        this.fitnessScore = fitnessScore;
    }

    // מחזיר את לוח השיבוץ של הכרומוזום
    public Schedule getSchedule() {
        return schedule;
    }

    // מחזיר את ציון הכשירות של הכרומוזום
    public double getFitnessScore() {
        return fitnessScore;
    }

    // מעדכן את ציון הכשירות של הכרומוזום
    public void setFitnessScore(double fitnessScore) {
        this.fitnessScore = fitnessScore;
    }
}
