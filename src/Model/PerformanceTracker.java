package Model;

import java.util.*;

public class PerformanceTracker {
    private List<Double> recentImprovements;
    private List<Double> diversificationMetrics;
    private List<Double> recentScores;
    private int windowSize;
    private long startTime;
    
    public PerformanceTracker() {
        this.recentImprovements = new ArrayList<>();
        this.diversificationMetrics = new ArrayList<>();
        this.recentScores = new ArrayList<>();
        this.windowSize = 50;
        this.startTime = System.currentTimeMillis();
    }
    
    public void recordIteration(double improvement, double diversity, double currentScore) {
        recentImprovements.add(improvement);
        diversificationMetrics.add(diversity);
        recentScores.add(currentScore);
        
        if (recentImprovements.size() > windowSize) {
            recentImprovements.remove(0);
            diversificationMetrics.remove(0);
            recentScores.remove(0);
        }
    }
    
    public PerformanceMetrics getCurrentMetrics() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        metrics.avgImprovement = calculateAverage(recentImprovements);
        metrics.improvementVariance = calculateVariance(recentImprovements);
        metrics.diversity = calculateAverage(diversificationMetrics);
        metrics.stagnationCount = countStagnation(recentImprovements);
        metrics.currentScore = !recentScores.isEmpty() ? recentScores.get(recentScores.size() - 1) : 0.0;
        metrics.iteration = recentImprovements.size();
        metrics.computationTime = (System.currentTimeMillis() - startTime) / 1000.0;
        metrics.constraintViolations = 0; // À adapter selon votre problème
        
        return metrics;
    }
    
    public PerformanceMetrics getCurrentMetrics(Solution currentSolution) {
        PerformanceMetrics metrics = getCurrentMetrics();
        if (currentSolution != null) {
            metrics.currentScore = currentSolution.getScore();
        }
        return metrics;
    }
    
    private double calculateAverage(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private double calculateVariance(List<Double> values) {
        if (values.size() < 2) return 0.0;
        double mean = calculateAverage(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return variance;
    }
    
    private int countStagnation(List<Double> improvements) {
        return (int) improvements.stream()
                .filter(imp -> Math.abs(imp) <= 0.001)
                .count();
    }
}