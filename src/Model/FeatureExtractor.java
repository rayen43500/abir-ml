package Model;

import java.util.*;
import java.lang.reflect.*;

/**
 * Classe pour extraire les caractéristiques (features) des solutions
 * pour l'apprentissage automatique dans le VNS adaptatif
 */
public class FeatureExtractor {
    
    /**
     * Extrait toutes les caractéristiques d'une solution pour le ML
     */
    public static Map<String, Double> extractAllFeatures(Solution solution) {
        Map<String, Double> features = new HashMap<>();
        
        if (solution == null) {
            return getDefaultFeatures();
        }
        
        try {
            // 1. Features basées sur le score et la qualité
            extractScoreFeatures(solution, features);
            
            // 2. Features basées sur la structure des tournées
            extractTourStructureFeatures(solution, features);
            
            // 3. Features basées sur la distribution
            extractDistributionFeatures(solution, features);
            
            // 4. Features basées sur la complexité
            extractComplexityFeatures(solution, features);
            
            // 5. Features temporelles (si disponibles)
            extractTemporalFeatures(solution, features);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'extraction des features: " + e.getMessage());
            return getDefaultFeatures();
        }
        
        return features;
    }
    
    /**
     * Features liées au score et à la qualité
     */
    private static void extractScoreFeatures(Solution solution, Map<String, Double> features) {
        double score = solution.getScore();
        features.put("score", score);
        features.put("score_normalized", normalizeScore(score));
        features.put("log_score", Math.log(Math.max(1, score)));
    }
    
    /**
     * Features liées à la structure des tournées
     */
    private static void extractTourStructureFeatures(Solution solution, Map<String, Double> features) {
        int[] tourSizes = solution.getTourSizes();
        int tourCount = tourSizes.length;
        
        features.put("tour_count", (double) tourCount);
        features.put("total_poi_count", (double) calculateTotalPOICount(tourSizes));
        
        if (tourCount > 0) {
            double avgTourSize = calculateAverage(tourSizes);
            double maxTourSize = Arrays.stream(tourSizes).max().orElse(0);
            double minTourSize = Arrays.stream(tourSizes).min().orElse(0);
            
            features.put("avg_tour_size", avgTourSize);
            features.put("max_tour_size", maxTourSize);
            features.put("min_tour_size", minTourSize);
            features.put("tour_size_range", maxTourSize - minTourSize);
            
            // Ratio de remplissage (supposant une capacité maximale de 20)
            features.put("avg_utilization", avgTourSize / 20.0);
            features.put("max_utilization", maxTourSize / 20.0);
        } else {
            features.put("avg_tour_size", 0.0);
            features.put("max_tour_size", 0.0);
            features.put("min_tour_size", 0.0);
            features.put("tour_size_range", 0.0);
            features.put("avg_utilization", 0.0);
            features.put("max_utilization", 0.0);
        }
    }
    
    /**
     * Features liées à la distribution et l'équilibre
     */
    private static void extractDistributionFeatures(Solution solution, Map<String, Double> features) {
        int[] tourSizes = solution.getTourSizes();
        
        if (tourSizes.length > 1) {
            double variance = calculateVariance(tourSizes);
            double stdDev = Math.sqrt(variance);
            double mean = calculateAverage(tourSizes);
            double cv = (mean > 0) ? stdDev / mean : 0; // Coefficient de variation
            
            features.put("tour_size_variance", variance);
            features.put("tour_size_std_dev", stdDev);
            features.put("coefficient_of_variation", cv);
            features.put("balance_ratio", calculateBalanceRatio(tourSizes));
            features.put("gini_coefficient", calculateGiniCoefficient(tourSizes));
            
        } else {
            features.put("tour_size_variance", 0.0);
            features.put("tour_size_std_dev", 0.0);
            features.put("coefficient_of_variation", 0.0);
            features.put("balance_ratio", 1.0);
            features.put("gini_coefficient", 0.0);
        }
    }
    
    /**
     * Features liées à la complexité
     */
    private static void extractComplexityFeatures(Solution solution, Map<String, Double> features) {
        int[] tourSizes = solution.getTourSizes();
        int totalPOIs = calculateTotalPOICount(tourSizes);
        int tourCount = tourSizes.length;
        
        // Complexité basée sur la taille et la distribution
        double sizeComplexity = totalPOIs * 0.7 + tourCount * 0.3;
        double distributionComplexity = features.getOrDefault("tour_size_variance", 0.0) * 10;
        
        features.put("size_complexity", sizeComplexity);
        features.put("distribution_complexity", distributionComplexity);
        features.put("overall_complexity", sizeComplexity + distributionComplexity);
        
        // Entropie de la distribution des tailles
        features.put("entropy", calculateEntropy(tourSizes));
    }
    
    /**
     * Features temporelles (si disponibles dans votre Solution)
     */
    private static void extractTemporalFeatures(Solution solution, Map<String, Double> features) {
        try {
            // Essayer d'extraire des informations temporelles si elles existent
            // Ces méthodes peuvent ne pas exister dans votre classe Solution
            Method getTotalTimeMethod = solution.getClass().getMethod("getTotalTime");
            Method getAvgTimePerTourMethod = solution.getClass().getMethod("getAverageTimePerTour");
            
            double totalTime = (Double) getTotalTimeMethod.invoke(solution);
            double avgTimePerTour = (Double) getAvgTimePerTourMethod.invoke(solution);
            
            features.put("total_time", totalTime);
            features.put("avg_time_per_tour", avgTimePerTour);
            features.put("time_efficiency", solution.getScore() / Math.max(1, totalTime));
            
        } catch (NoSuchMethodException e) {
            // Méthodes temporelles non disponibles - ignorer silencieusement
            features.put("total_time", 0.0);
            features.put("avg_time_per_tour", 0.0);
            features.put("time_efficiency", 0.0);
        } catch (Exception e) {
            System.err.println("⚠️  Impossible d'extraire les features temporelles: " + e.getMessage());
            features.put("total_time", 0.0);
            features.put("avg_time_per_tour", 0.0);
            features.put("time_efficiency", 0.0);
        }
    }
    
    // =========================================================================
    // MÉTHODES DE CALCUL
    // =========================================================================
    
    private static double normalizeScore(double score) {
        // Normalisation basique entre 0 et 1
        // Ajustez ces valeurs selon votre échelle de scores
        return score / (score + 1000);
    }
    
    private static int calculateTotalPOICount(int[] tourSizes) {
        // Soustraire les dépôts (début et fin de chaque tournée)
        int total = 0;
        for (int size : tourSizes) {
            total += Math.max(0, size - 2); // -2 pour les dépôts
        }
        return total;
    }
    
    private static double calculateAverage(int[] values) {
        if (values.length == 0) return 0.0;
        return Arrays.stream(values).average().orElse(0.0);
    }
    
    private static double calculateVariance(int[] values) {
        if (values.length < 2) return 0.0;
        double mean = calculateAverage(values);
        return Arrays.stream(values)
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
    }
    
    private static double calculateBalanceRatio(int[] tourSizes) {
        if (tourSizes.length < 2) return 1.0;
        double max = Arrays.stream(tourSizes).max().orElse(1);
        double min = Arrays.stream(tourSizes).min().orElse(1);
        return (min / max); // 1.0 = parfaitement équilibré
    }
    
    private static double calculateGiniCoefficient(int[] tourSizes) {
        if (tourSizes.length == 0) return 0.0;
        
        // Tri des valeurs
        int[] sorted = Arrays.copyOf(tourSizes, tourSizes.length);
        Arrays.sort(sorted);
        
        // Calcul du coefficient de Gini
        double n = sorted.length;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (2 * i - n + 1) * sorted[i];
        }
        
        double totalSum = Arrays.stream(sorted).sum();
        return (n > 0 && totalSum > 0) ? sum / (n * totalSum) : 0.0;
    }
    
    private static double calculateEntropy(int[] tourSizes) {
        if (tourSizes.length == 0) return 0.0;
        
        // Calcul de l'entropie de Shannon
        double total = Arrays.stream(tourSizes).sum();
        if (total == 0) return 0.0;
        
        double entropy = 0.0;
        for (int size : tourSizes) {
            if (size > 0) {
                double probability = size / total;
                entropy -= probability * Math.log(probability);
            }
        }
        
        return entropy;
    }
    
    /**
     * Features par défaut en cas d'erreur
     */
    private static Map<String, Double> getDefaultFeatures() {
        Map<String, Double> defaults = new HashMap<>();
        
        // Valeurs par défaut sûres
        defaults.put("score", 0.0);
        defaults.put("score_normalized", 0.0);
        defaults.put("log_score", 0.0);
        defaults.put("tour_count", 1.0);
        defaults.put("total_poi_count", 5.0);
        defaults.put("avg_tour_size", 5.0);
        defaults.put("max_tour_size", 10.0);
        defaults.put("min_tour_size", 0.0);
        defaults.put("tour_size_range", 10.0);
        defaults.put("avg_utilization", 0.5);
        defaults.put("max_utilization", 0.5);
        defaults.put("tour_size_variance", 0.0);
        defaults.put("tour_size_std_dev", 0.0);
        defaults.put("coefficient_of_variation", 0.0);
        defaults.put("balance_ratio", 1.0);
        defaults.put("gini_coefficient", 0.0);
        defaults.put("size_complexity", 5.0);
        defaults.put("distribution_complexity", 0.0);
        defaults.put("overall_complexity", 5.0);
        defaults.put("entropy", 0.0);
        defaults.put("total_time", 0.0);
        defaults.put("avg_time_per_tour", 0.0);
        defaults.put("time_efficiency", 0.0);
        
        return defaults;
    }
    
    /**
     * Extrait un sous-ensemble de features pour un type de prédiction spécifique
     */
    public static Map<String, Double> extractFeaturesForParameterPrediction(Solution solution) {
        Map<String, Double> allFeatures = extractAllFeatures(solution);
        Map<String, Double> selectedFeatures = new HashMap<>();
        
        // Sélection des features les plus importantes pour la prédiction de paramètres
        String[] importantFeatures = {
            "score_normalized", "tour_count", "avg_tour_size", "tour_size_variance",
            "balance_ratio", "overall_complexity", "coefficient_of_variation"
        };
        
        for (String feature : importantFeatures) {
            if (allFeatures.containsKey(feature)) {
                selectedFeatures.put(feature, allFeatures.get(feature));
            }
        }
        
        return selectedFeatures;
    }
    
    /**
     * Extrait un sous-ensemble de features pour la sélection d'opérateurs
     */
    public static Map<String, Double> extractFeaturesForOperatorSelection(Solution solution) {
        Map<String, Double> allFeatures = extractAllFeatures(solution);
        Map<String, Double> selectedFeatures = new HashMap<>();
        
        // Features importantes pour choisir le bon opérateur
        String[] importantFeatures = {
            "score", "tour_size_variance", "balance_ratio", 
            "avg_utilization", "entropy", "gini_coefficient"
        };
        
        for (String feature : importantFeatures) {
            if (allFeatures.containsKey(feature)) {
                selectedFeatures.put(feature, allFeatures.get(feature));
            }
        }
        
        return selectedFeatures;
    }
    
    /**
     * Normalise les features entre 0 et 1 pour l'apprentissage automatique
     */
    public static Map<String, Double> normalizeFeatures(Map<String, Double> features) {
        Map<String, Double> normalized = new HashMap<>();
        
        // Plages de normalisation pour chaque feature (à ajuster selon vos données)
        Map<String, double[]> normalizationRanges = new HashMap<>();
        normalizationRanges.put("score", new double[]{0, 2000});
        normalizationRanges.put("tour_count", new double[]{1, 10});
        normalizationRanges.put("avg_tour_size", new double[]{0, 20});
        normalizationRanges.put("tour_size_variance", new double[]{0, 100});
        normalizationRanges.put("balance_ratio", new double[]{0, 1});
        normalizationRanges.put("overall_complexity", new double[]{0, 50});
        
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            String featureName = entry.getKey();
            double value = entry.getValue();
            
            if (normalizationRanges.containsKey(featureName)) {
                double[] range = normalizationRanges.get(featureName);
                double min = range[0];
                double max = range[1];
                double normalizedValue = (value - min) / (max - min);
                normalized.put(featureName, Math.max(0, Math.min(1, normalizedValue)));
            } else {
                // Normalisation générique pour les features sans plage définie
                normalized.put(featureName, Math.tanh(value / 100)); // Fonction tanh pour limiter les valeurs
            }
        }
        
        return normalized;
    }
    
    /**
     * Affiche les features extraites (pour débogage)
     */
    public static void printFeatures(Map<String, Double> features) {
        System.out.println("📊 Features extraites:");
        System.out.println("-".repeat(50));
        
        features.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> 
                System.out.printf("  %-25s: %8.4f%n", entry.getKey(), entry.getValue())
            );
        
        System.out.println("-".repeat(50));
    }
}