package Model;

import java.util.*;

import java.util.*;

public class AdaptiveOperatorSelection {
    private Map<String, OperatorStats> operatorStats;
    private double learningRate;
    private double decayFactor;
    
    public AdaptiveOperatorSelection() {
        this.operatorStats = new HashMap<>();
        this.learningRate = 0.1;
        this.decayFactor = 0.95;
        initializeOperators();
    }
    
    private void initializeOperators() {
        String[] operators = {"2-opt", "swap", "relocation", "crossover"};
        for (String op : operators) {
            operatorStats.put(op, new OperatorStats(1.0, 0.0, 1.0));
        }
    }
    
    public String selectOperator() {
        // Sélection basée sur le score UCB (Upper Confidence Bound)
        return operatorStats.entrySet().stream()
                .max(Comparator.comparingDouble(e -> calculateUCB(e.getValue())))
                .get()
                .getKey();
    }
    
    private double calculateUCB(OperatorStats stats) {
        double exploitation = stats.getSuccessRate();
        double exploration = Math.sqrt(2 * Math.log(stats.getTotalUses() + 1) / (stats.getUses() + 1));
        return exploitation + exploration;
    }
    
    public void updateOperatorPerformance(String operator, boolean improvement, double magnitude) {
        OperatorStats stats = operatorStats.get(operator);
        if (stats != null) {
            stats.incrementUses();
            
            if (improvement) {
                double newSuccessRate = stats.getSuccessRate() * (1 - learningRate) 
                                     + learningRate * (1.0 + magnitude);
                stats.setSuccessRate(newSuccessRate);
            } else {
                stats.setSuccessRate(stats.getSuccessRate() * decayFactor);
            }
        }
    }
}

