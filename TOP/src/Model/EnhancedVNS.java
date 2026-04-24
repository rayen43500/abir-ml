package Model;
import java.util.*;

public class EnhancedVNS {
    private DynamicParameterTuning parameterTuning;
    private AdaptiveOperatorSelection operatorSelection;
    private Solution bestSolution;
    private ProblemInput problemInput;
    
    // Paramètres de configuration
    private static final int MAX_ITERATIONS = 1000;
    private static final int MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 200;
    private static final int ADJUSTMENT_FREQUENCY = 50;
    private static final int K_MAX = 4;
    
    public EnhancedVNS() {
        this.parameterTuning = new DynamicParameterTuning();
        this.operatorSelection = new AdaptiveOperatorSelection();
    }
    
    public void solve(ProblemInput problemInput) {
        this.problemInput = problemInput;
        
        // 1. Initialisation
        System.out.println("🚀 Démarrage Enhanced VNS avec apprentissage automatique...");
        Solution currentSolution = generateInitialSolution(problemInput);
        bestSolution = (Solution) currentSolution.clone();
        
        System.out.println("✅ Solution initiale: " + currentSolution.getScore());
        parameterTuning.printCurrentParameters();
        
        int iterationsWithoutImprovement = 0;
        int totalIterations = 0;
        Solution previousSolution = null;
        
        // 2. Boucle principale
        while (iterationsWithoutImprovement < MAX_ITERATIONS_WITHOUT_IMPROVEMENT && 
               totalIterations < MAX_ITERATIONS) {
            
            int k = 1;
            boolean improvementFound = false;
            
            // 3. Exploration des voisinages
            while (k <= K_MAX && !improvementFound) {
                
                // 3a. Shaking avec intensité dynamique
                double shakingIntensity = parameterTuning.getParameterValue("SHAKING_INTENSITY");
                Solution shakenSolution = shaking((Solution) currentSolution.clone(), k, shakingIntensity);
                
                // 3b. Sélection d'opérateur adaptative
                List<String> availableOperators = Arrays.asList("2-opt", "swap", "relocation", "crossover");
                String bestOperator = operatorSelection.selectOperator();
                Solution localSolution = applyLocalSearch(shakenSolution, bestOperator);
                
                // 3c. Calcul des métriques de performance
                double improvement = calculateImprovement(localSolution, currentSolution);
                double diversity = calculateDiversity(localSolution);
                double currentScore = localSolution.getScore();
                
                // 3d. Enregistrement des performances pour le ML
                parameterTuning.recordIteration(improvement, diversity, currentScore);
                
                // 3e. Critère d'acceptation
                if (localSolution.getScore() > currentSolution.getScore()) {
                    currentSolution = localSolution;
                    improvementFound = true;
                    
                    // Mise à jour de la meilleure solution
                    if (currentSolution.getScore() > bestSolution.getScore()) {
                        bestSolution = (Solution) currentSolution.clone();
                        iterationsWithoutImprovement = 0;
                        
                        System.out.printf("🎯 Nouvelle meilleure solution: %.2f (itération %d)%n", 
                                        bestSolution.getScore(), totalIterations);
                    }
                    
                    // Mise à jour des performances de l'opérateur
                    operatorSelection.updateOperatorPerformance(bestOperator, true, improvement);
                    
                } else {
                    k++;
                    operatorSelection.updateOperatorPerformance(bestOperator, false, 0);
                }
            }
            
            // 4. Ajustement dynamique des paramètres
            if (totalIterations % ADJUSTMENT_FREQUENCY == 0) {
                parameterTuning.adjustParameters(currentSolution, totalIterations);
                
                // Affichage périodique des paramètres
                if (totalIterations % 100 == 0) {
                    parameterTuning.printCurrentParameters();
                }
            }
            
            // 5. Gestion de la stagnation
            if (!improvementFound) {
                iterationsWithoutImprovement++;
                
                // Diversification forcée après longue stagnation
                if (iterationsWithoutImprovement > 100) {
                    currentSolution = applyDiversification(currentSolution);
                    iterationsWithoutImprovement = 0;
                    System.out.println("🔄 Diversification appliquée");
                }
            }
            
            totalIterations++;
            
            // 6. Affichage de progression
            if (totalIterations % 50 == 0) {
                printProgress(totalIterations, currentSolution.getScore(), bestSolution.getScore(), 
                            iterationsWithoutImprovement);
            }
        }
        
        // 7. Résultats finaux
        printFinalResults(totalIterations, bestSolution);
    }
    
    /**
     * Génération de la solution initiale
     */
    private Solution generateInitialSolution(ProblemInput problemInput) {
        Solution solution = new Solution(problemInput);
        
        // Construction gloutonne initiale
        while (solution.notStuckInLocalOptimum()) {
            solution.insertStep();
        }
        
        // Amélioration locale rapide
        solution = applyQuickLocalSearch(solution);
        
        return solution;
    }
    
    /**
     * Shaking avec intensité contrôlée
     */
    private Solution shaking(Solution solution, int k, double intensity) {
        Solution shaken = (Solution) solution.clone();
        Random rand = new Random();
        
        // Nombre de perturbations basé sur l'intensité
        int perturbations = (int) (intensity * 10) + 1;
        
        for (int i = 0; i < perturbations; i++) {
            switch (k) {
                case 1:
                    randomSwapBetweenRoutes(shaken);
                    break;
                case 2:
                    randomMoveBetweenRoutes(shaken);
                    break;
                case 3:
                    destroyAndRepair(shaken, intensity);
                    break;
                case 4:
                    combinedShaking(shaken);
                    break;
                default:
                    randomSwapBetweenRoutes(shaken);
            }
        }
        
        return shaken;
    }
    
    /**
     * Recherche locale avec opérateur spécifique
     */
    private Solution applyLocalSearch(Solution solution, String operator) {
        Solution improved = (Solution) solution.clone();
        
        switch (operator) {
            case "2-opt":
                improved = apply2OptSearch(improved);
                break;
            case "swap":
                improved = applySwapSearch(improved);
                break;
            case "relocation":
                improved = applyRelocationSearch(improved);
                break;
            case "crossover":
                improved = applyCrossoverSearch(improved);
                break;
            default:
                improved = apply2OptSearch(improved);
        }
        
        return improved;
    }
    
    /**
     * Recherche locale rapide pour l'initialisation
     */
    private Solution applyQuickLocalSearch(Solution solution) {
        Solution improved = (Solution) solution.clone();
        
        // Application rapide de 2-opt
        improved = apply2OptSearch(improved);
        
        return improved;
    }
    
    /**
     * Diversification pour échapper aux optima locaux
     */
    private Solution applyDiversification(Solution solution) {
        Solution diversified = (Solution) solution.clone();
        
        // Destruction plus importante (50%)
        destroyAndRepair(diversified, 0.5);
        
        // Recherche locale après diversification
        diversified = applyLocalSearch(diversified, "relocation");
        
        return diversified;
    }
    
    /**
     * Calcul de l'amélioration relative
     */
    private double calculateImprovement(Solution newSolution, Solution currentSolution) {
        if (currentSolution == null || currentSolution.getScore() == 0) {
            return 0.0;
        }
        return (newSolution.getScore() - currentSolution.getScore()) / Math.abs(currentSolution.getScore());
    }
    
    /**
     * Calcul de la diversité (simplifié)
     */
    private double calculateDiversity(Solution solution) {
        // Métrique basique de diversité basée sur la distribution des tournées
        int[] tourSizes = solution.getTourSizes();
        if (tourSizes.length <= 1) return 0.0;
        
        double mean = Arrays.stream(tourSizes).average().orElse(0.0);
        double variance = Arrays.stream(tourSizes)
                .mapToDouble(size -> Math.pow(size - mean, 2))
                .average()
                .orElse(0.0);
        
        // Normalisation entre 0 et 1
        return Math.min(1.0, variance / 100.0);
    }
    
    // =========================================================================
    // OPÉRATEURS DE VOISINAGE (à adapter selon votre implémentation existante)
    // =========================================================================
    
    private void randomSwapBetweenRoutes(Solution solution) {
        // Implémentation existante
        int tourCount = problemInput.getTourCount();
        if (tourCount < 2) return;
        
        Random rand = new Random();
        int route1 = rand.nextInt(tourCount);
        int route2 = rand.nextInt(tourCount);
        while (route2 == route1) route2 = rand.nextInt(tourCount);
        
        if (solution.getTourSizes()[route1] > 2 && solution.getTourSizes()[route2] > 2) {
            // Utiliser vos méthodes existantes...
        }
    }
    
    private void randomMoveBetweenRoutes(Solution solution) {
        // Implémentation existante
    }
    
    private void destroyAndRepair(Solution solution, double destructionRate) {
        // Implémentation existante
    }
    
    private void combinedShaking(Solution solution) {
        // Implémentation existante
    }
    
    private Solution apply2OptSearch(Solution solution) {
        // Implémentation existante du 2-opt
        Solution best = (Solution) solution.clone();
        // ... votre code 2-opt
        return best;
    }
    
    private Solution applySwapSearch(Solution solution) {
        // Implémentation existante des swaps
        Solution best = (Solution) solution.clone();
        // ... votre code swap
        return best;
    }
    
    private Solution applyRelocationSearch(Solution solution) {
        // Implémentation existante des relocations
        Solution best = (Solution) solution.clone();
        // ... votre code relocation
        return best;
    }
    
    private Solution applyCrossoverSearch(Solution solution) {
        // Implémentation existante du crossover
        Solution best = (Solution) solution.clone();
        // ... votre code crossover
        return best;
    }
    
    // =========================================================================
    // MÉTHODES D'AFFICHAGE
    // =========================================================================
    
    private void printProgress(int iteration, double currentScore, double bestScore, int stagnation) {
        System.out.printf("📊 Itération %d - Actuel: %.2f | Meilleur: %.2f | Stagnation: %d%n",
                        iteration, currentScore, bestScore, stagnation);
    }
    
    private void printFinalResults(int totalIterations, Solution bestSolution) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎉 RECHERCHE VNS AMÉLIORÉE TERMINÉE");
        System.out.println("=".repeat(60));
        System.out.printf("Nombre total d'itérations: %d%n", totalIterations);
        System.out.printf("Meilleur score obtenu: %.2f%n", bestSolution.getScore());
        System.out.printf("Paramètres finaux:%n");
        parameterTuning.printCurrentParameters();
        System.out.println("=".repeat(60));
    }
    
    // =========================================================================
    // GETTERS
    // =========================================================================
    
    public Solution getBestSolution() {
        return this.bestSolution;
    }
    
    public DynamicParameterTuning getParameterTuning() {
        return this.parameterTuning;
    }
    
    public AdaptiveOperatorSelection getOperatorSelection() {
        return this.operatorSelection;
    }
}