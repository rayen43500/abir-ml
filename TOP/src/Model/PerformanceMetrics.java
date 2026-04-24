package Model;

import java.util.*;

public class PerformanceMetrics {
    public double avgImprovement;
    public double improvementVariance;
    public double diversity;
    public int stagnationCount;
    public double currentScore;
    public int iteration;
    public double computationTime;
    public int constraintViolations;
    
    public PerformanceMetrics() {}
    
    // Getters
    public double getAvgImprovement() { return avgImprovement; }
    public double getImprovementVariance() { return improvementVariance; }
    public double getDiversity() { return diversity; }
    public int getStagnationCount() { return stagnationCount; }
    public double getCurrentScore() { return currentScore; }
    public int getIteration() { return iteration; }
    public double getComputationTime() { return computationTime; }
    public int getConstraintViolations() { return constraintViolations; }
    
    @Override
    public String toString() {
        return String.format("Performance[avgImp=%.4f, diversity=%.2f, stagnation=%d]", 
                           avgImprovement, diversity, stagnationCount);
    }
}