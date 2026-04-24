package Model;
import java.util.*;

public class SolutionFeatureExtractor {
    
    public static Map<String, Double> extractFeatures(Solution solution) {
        Map<String, Double> features = new HashMap<>();
        
        if (solution == null) {
            return getDefaultFeatures();
        }
        
        try {
            // Score basique
            features.put("score", (double) solution.getScore());
            
            // Features des tournées (utilisation sécurisée des méthodes existantes)
            int tourCount = solution.getTourSizes().length;
            features.put("tour_count", (double) tourCount);
            
            // Calculs basiques
            double avgTourSize = calculateAverageTourSize(solution);
            features.put("avg_tour_size", avgTourSize);
            features.put("solution_complexity", avgTourSize * tourCount);
            
        } catch (Exception e) {
            System.err.println("Erreur extraction features: " + e.getMessage());
            return getDefaultFeatures();
        }
        
        return features;
    }
    
    private static double calculateAverageTourSize(Solution solution) {
        int[] tourSizes = solution.getTourSizes();
        if (tourSizes.length == 0) return 0.0;
        
        double sum = 0;
        for (int size : tourSizes) {
            sum += size;
        }
        return sum / tourSizes.length;
    }
    
    private static Map<String, Double> getDefaultFeatures() {
        Map<String, Double> defaults = new HashMap<>();
        defaults.put("score", 0.0);
        defaults.put("tour_count", 1.0);
        defaults.put("avg_tour_size", 5.0);
        defaults.put("solution_complexity", 5.0);
        return defaults;
    }
}
