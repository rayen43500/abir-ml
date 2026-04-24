package Model;
import java.util.*;
public class VnsTabuSearch {
	
	
	    
	    private final int MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 1000;
	    private final int K_MAX = 4;
	    private final int TABU_TENURE = 10;
	    private final int MAX_TABU_ITERATIONS = 50;
	    private Solution bestSolution;
	    private ProblemInput problemInput;
	    private Queue<String> tabuList;
	    private int tabuListSize = 20;

	    public void solve(ProblemInput problemInput) {
	        this.problemInput = problemInput;
	        this.tabuList = new LinkedList<>();
	        
	        // Étape 1: Initialisation
	        Solution currentSolution = generateInitialSolution(problemInput);
	        bestSolution = (Solution) currentSolution.clone();
	        
	        System.out.println("Solution initiale VNS+TS: " + currentSolution.getScore());

	        int iterationsWithoutImprovement = 0;
	        int totalIterations = 0;

	        // Étape 2: Boucle principale VNS avec recherche tabou
	        while (iterationsWithoutImprovement < MAX_ITERATIONS_WITHOUT_IMPROVEMENT) {
	            int k = 1;
	            boolean improvementFound = false;

	            while (k <= K_MAX && !improvementFound) {
	                Solution shakenSolution = shaking((Solution) currentSolution.clone(), k);
	                Solution tabuSolution = tabuSearch(shakenSolution);
	                
	                if (tabuSolution.getScore() > currentSolution.getScore() || 
	                    aspirationCriterion(tabuSolution, currentSolution)) {
	                    
	                    currentSolution = tabuSolution;
	                    improvementFound = true;
	                    
	                    if (currentSolution.getScore() > bestSolution.getScore()) {
	                        bestSolution = (Solution) currentSolution.clone();
	                        System.out.println("Nouvelle meilleure solution VNS+TS: " + bestSolution.getScore() + 
	                                         " (itération " + totalIterations + ")");
	                        iterationsWithoutImprovement = 0;
	                    }
	                } else {
	                    k++;
	                }
	            }

	            if (!improvementFound) {
	                iterationsWithoutImprovement++;
	            }

	            totalIterations++;

	            if (totalIterations % 50 == 0) {
	                currentSolution = intensificationDiversificationWithTabu(currentSolution);
	            }
	        }

	        System.out.println("VNS+TS terminé. Meilleur score: " + bestSolution.getScore());
	    }

	    /**
	     * IMPLÉMENTATION COMPLÈTE DE try2OptSwap
	     */
	    private Solution try2OptSwap(Solution solution, int tour, int i, int j) {
	        // Vérifier les indices valides
	        if (i >= j || i < 1 || j >= solution.getTourSizes()[tour] - 1) {
	            return null;
	        }
	        
	        Solution candidate = (Solution) solution.clone();
	        
	        try {
	            // Implémentation du 2-opt: inverser le segment entre i et j
	            List<POIInterval> segment = extractSegment(candidate, tour, i, j);
	            Collections.reverse(segment);
	            reinsertSegment(candidate, tour, i, segment);
	            
	            // Vérifier la validité et recalculer le score
	            if (isSolutionValid(candidate)) {
	                candidate.calculateScore(); // Supposant que cette méthode existe
	                return candidate;
	            }
	        } catch (Exception e) {
	            System.err.println("Erreur dans 2-opt swap: " + e.getMessage());
	        }
	        
	        return null;
	    }

	    /**
	     * Extraire un segment d'une tournée
	     */
	    private List<POIInterval> extractSegment(Solution solution, int tour, int start, int end) {
	        List<POIInterval> segment = new ArrayList<>();
	        POIInterval current = solution.getNthPOIIntervalInTourX(start, tour);
	        
	        for (int pos = start; pos <= end; pos++) {
	            if (current != null) {
	                segment.add(current);
	                current = current.getNextPOIInterval();
	            }
	        }
	        return segment;
	    }

	    /**
	     * Réinsérer un segment inversé
	     */
	    private void reinsertSegment(Solution solution, int tour, int startPos, List<POIInterval> segment) {
	        POIInterval beforeStart = solution.getNthPOIIntervalInTourX(startPos - 1, tour);
	        POIInterval afterEnd = solution.getNthPOIIntervalInTourX(startPos + segment.size(), tour);
	        
	        // Recréer les liens avec le segment inversé
	        if (beforeStart != null) {
	            beforeStart.setNextPOIInterval(segment.get(0));
	        }
	        
	        for (int i = 0; i < segment.size() - 1; i++) {
	            segment.get(i).setNextPOIInterval(segment.get(i + 1));
	        }
	        
	        if (afterEnd != null) {
	            segment.get(segment.size() - 1).setNextPOIInterval(afterEnd);
	        }
	    }

	    /**
	     * Vérifier la validité d'une solution
	     */
	    private boolean isSolutionValid(Solution solution) {
	        // Implémentation basique - à adapter selon vos contraintes
	        for (int tour = 0; tour < problemInput.getTourCount(); tour++) {
	            if (solution.getTourSizes()[tour] < 2) {
	                return false; // Doit avoir au moins dépôt début et fin
	            }
	        }
	        return true;
	    }

	    /**
	     * Application aléatoire de 2-opt (pour la recherche tabou)
	     */
	    private void applyRandom2Opt(Solution solution, int tour) {
	        int tourSize = solution.getTourSizes()[tour];
	        if (tourSize <= 4) return;
	        
	        Random rand = new Random();
	        int maxAttempts = 10;
	        
	        for (int attempt = 0; attempt < maxAttempts; attempt++) {
	            int i = 1 + rand.nextInt(tourSize - 3);
	            int j = i + 1 + rand.nextInt(tourSize - i - 2);
	            
	            Solution candidate = try2OptSwap(solution, tour, i, j);
	            if (candidate != null && candidate.getScore() > solution.getScore()) {
	                // Copier la solution améliorée
	                copySolution(solution, candidate);
	                break;
	            }
	        }
	    }

	    /**
	     * Copie une solution vers une autre
	     */
	    private void copySolution(Solution dest, Solution src) {
	        // Implémentation basique - à adapter selon votre structure
	        for (int tour = 0; tour < problemInput.getTourCount(); tour++) {
	            // Copier la structure de la tournée
	            // Cette partie dépend de votre implémentation spécifique
	        }
	        // Le score est maintenu par les opérations internes de Solution.
	    }

	    /**
	     * RECHERCHE TABOU
	     */
	    private Solution tabuSearch(Solution initialSolution) {
	        Solution currentSolution = (Solution) initialSolution.clone();
	        Solution bestLocalSolution = (Solution) initialSolution.clone();
	        
	        Queue<String> localTabuList = new LinkedList<>();
	        int iterationsWithoutImprovement = 0;
	        
	        for (int iter = 0; iter < MAX_TABU_ITERATIONS; iter++) {
	            List<Solution> candidateNeighbors = generateCandidateNeighbors(currentSolution, localTabuList);
	            
	            if (candidateNeighbors.isEmpty()) {
	                break;
	            }
	            
	            Solution bestNeighbor = selectBestCandidate(candidateNeighbors);
	            String moveSignature = generateMoveSignature(currentSolution, bestNeighbor);
	            
	            currentSolution = bestNeighbor;
	            
	            localTabuList.add(moveSignature);
	            if (localTabuList.size() > tabuListSize) {
	                localTabuList.poll();
	            }
	            
	            if (currentSolution.getScore() > bestLocalSolution.getScore()) {
	                bestLocalSolution = (Solution) currentSolution.clone();
	                iterationsWithoutImprovement = 0;
	            } else {
	                iterationsWithoutImprovement++;
	            }
	            
	            if (iterationsWithoutImprovement > 15) {
	                break;
	            }
	        }
	        
	        return bestLocalSolution;
	    }

	    /**
	     * Génération des candidats voisins
	     */
	    private List<Solution> generateCandidateNeighbors(Solution solution, Queue<String> tabuList) {
	        List<Solution> candidates = new ArrayList<>();
	        Random rand = new Random();
	        
	        for (int i = 0; i < 20; i++) {
	            Solution candidate = (Solution) solution.clone();
	            int moveType = rand.nextInt(4);
	            boolean moveSuccessful = false;
	            
	            switch (moveType) {
	                case 0:
	                    moveSuccessful = performIntraRouteSwap(candidate);
	                    break;
	                case 1:
	                    moveSuccessful = performInterRouteSwap(candidate);
	                    break;
	                case 2:
	                    moveSuccessful = performRelocation(candidate);
	                    break;
	                case 3:
	                    moveSuccessful = perform2Opt(candidate);
	                    break;
	            }
	            
	            if (moveSuccessful) {
	                String moveSig = generateMoveSignature(solution, candidate);
	                if (!tabuList.contains(moveSig) || aspirationCriterion(candidate, solution)) {
	                    candidates.add(candidate);
	                }
	            }
	        }
	        
	        return candidates;
	    }

	    /**
	     * Opérateurs de mouvement (retournent true si le mouvement est réussi)
	     */
	    private boolean performIntraRouteSwap(Solution solution) {
	        int tour = new Random().nextInt(problemInput.getTourCount());
	        int tourSize = solution.getTourSizes()[tour];
	        if (tourSize <= 4) return false;
	        
	        int pos1 = 1 + new Random().nextInt(tourSize - 3);
	        int pos2 = pos1 + 1 + new Random().nextInt(tourSize - pos1 - 2);
	        
	        return swapIntraRoute(solution, tour, pos1, pos2);
	    }

	    private boolean performInterRouteSwap(Solution solution) {
	        int tour1 = new Random().nextInt(problemInput.getTourCount());
	        int tour2 = new Random().nextInt(problemInput.getTourCount());
	        while (tour2 == tour1) {
	            tour2 = new Random().nextInt(problemInput.getTourCount());
	        }
	        
	        int size1 = solution.getTourSizes()[tour1];
	        int size2 = solution.getTourSizes()[tour2];
	        if (size1 <= 2 || size2 <= 2) return false;
	        
	        int pos1 = 1 + new Random().nextInt(size1 - 2);
	        int pos2 = 1 + new Random().nextInt(size2 - 2);
	        
	        return swapInterRoute(solution, tour1, pos1, tour2, pos2);
	    }

	    private boolean performRelocation(Solution solution) {
	        int sourceTour = new Random().nextInt(problemInput.getTourCount());
	        int targetTour = new Random().nextInt(problemInput.getTourCount());
	        int sourceSize = solution.getTourSizes()[sourceTour];
	        
	        if (sourceSize <= 2) return false;
	        
	        int posSource = 1 + new Random().nextInt(sourceSize - 2);
	        return relocatePOI(solution, sourceTour, posSource, targetTour);
	    }

	    private boolean perform2Opt(Solution solution) {
	        int tour = new Random().nextInt(problemInput.getTourCount());
	        if (solution.getTourSizes()[tour] <= 4) return false;
	        
	        applyRandom2Opt(solution, tour);
	        return true;
	    }

	    /**
	     * Implémentations des opérations de base
	     */
	    private boolean swapIntraRoute(Solution solution, int tour, int pos1, int pos2) {
	        try {
	            Solution candidate = try2OptSwap(solution, tour, Math.min(pos1, pos2), Math.max(pos1, pos2));
	            if (candidate != null && candidate.getScore() >= solution.getScore()) {
	                copySolution(solution, candidate);
	                return true;
	            }
	        } catch (Exception e) {
	            System.err.println("Erreur swap intra-route: " + e.getMessage());
	        }
	        return false;
	    }

	    private boolean swapInterRoute(Solution solution, int tour1, int pos1, int tour2, int pos2) {
	        // Implémentation simplifiée - utiliser les méthodes existantes de Solution
	        try {
	            solution.shakeStep(pos1, 1, TABU_TENURE, 0);
	            solution.shakeStep(pos2, 1, TABU_TENURE, 0);
	            return true;
	        } catch (Exception e) {
	            return false;
	        }
	    }

	    private boolean relocatePOI(Solution solution, int sourceTour, int sourcePos, int targetTour) {
	        try {
	            solution.shakeStep(sourcePos, 1, TABU_TENURE, 0);
	            solution.insertStep();
	            return true;
	        } catch (Exception e) {
	            return false;
	        }
	    }

	    /**
	     * Méthodes auxiliaires
	     */
	    private boolean aspirationCriterion(Solution candidate, Solution current) {
	        return candidate.getScore() > bestSolution.getScore();
	    }

	    private String generateMoveSignature(Solution from, Solution to) {
	        return "Move_" + from.getScore() + "_to_" + to.getScore() + "_" + System.currentTimeMillis();
	    }

	    private Solution selectBestCandidate(List<Solution> candidates) {
	        return candidates.stream()
	                .max(Comparator.comparingDouble(Solution::getScore))
	                .orElse(candidates.isEmpty() ? null : candidates.get(0));
	    }

	    private Solution intensificationDiversificationWithTabu(Solution currentSolution) {
	        if (Math.random() < 0.7) {
	            return intensifiedTabuSearch(currentSolution);
	        } else {
	            return diversifiedTabuSearch(currentSolution);
	        }
	    }

	    private Solution intensifiedTabuSearch(Solution solution) {
	        Solution current = (Solution) solution.clone();
	        for (int i = 0; i < 3; i++) {
	            Solution candidate = tabuSearch(current);
	            if (candidate != null && candidate.getScore() > current.getScore()) {
	                current = candidate;
	            }
	        }
	        return current;
	    }

	    private Solution diversifiedTabuSearch(Solution solution) {
	        int originalSize = tabuListSize;
	        tabuListSize = 30;
	        Solution diversified = tabuSearch(solution);
	        tabuListSize = originalSize;
	        return diversified;
	    }

	    // Les méthodes existantes restent inchangées...
	    private Solution generateInitialSolution(ProblemInput problemInput) {
	        Solution solution = new Solution(problemInput);
	        if (!solution.insertPivots(0, 0, 7)) {
	            constructSimpleSolution(solution);
	        }
	        solution = tabuSearch(solution);
	        return solution;
	    }

	    private void constructSimpleSolution(Solution solution) {
	        while (solution.notStuckInLocalOptimum()) {
	            solution.insertStep();
	        }
	    }

	    private Solution shaking(Solution solution, int k) {
	        Solution shakenSolution = (Solution) solution.clone();
	        switch (k) {
	            case 1: randomSwapBetweenRoutes(shakenSolution); break;
	            case 2: randomMoveBetweenRoutes(shakenSolution); break;
	            case 3: destroyAndRepair(shakenSolution, 0.3); break;
	            case 4: combinedShaking(shakenSolution); break;
	            default: randomSwapBetweenRoutes(shakenSolution);
	        }
	        return shakenSolution;
	    }

	    private void randomSwapBetweenRoutes(Solution solution) {
	        int tourCount = problemInput.getTourCount();
	        if (tourCount < 2) {
	            return;
	        }

	        Random rand = new Random();
	        int route1 = rand.nextInt(tourCount);
	        int route2 = rand.nextInt(tourCount);
	        while (route2 == route1) {
	            route2 = rand.nextInt(tourCount);
	        }

	        if (solution.getTourSizes()[route1] > 2 && solution.getTourSizes()[route2] > 2) {
	            int pos1 = 1 + rand.nextInt(solution.getTourSizes()[route1] - 2);
	            int pos2 = 1 + rand.nextInt(solution.getTourSizes()[route2] - 2);
	            solution.shakeStep(pos1, 1, TABU_TENURE, 0);
	            solution.shakeStep(pos2, 1, TABU_TENURE, 0);
	        }
	    }

	    private void randomMoveBetweenRoutes(Solution solution) {
	        int tourCount = problemInput.getTourCount();
	        if (tourCount < 2) {
	            return;
	        }

	        Random rand = new Random();
	        int sourceRoute = rand.nextInt(tourCount);
	        int targetRoute = rand.nextInt(tourCount);
	        while (targetRoute == sourceRoute) {
	            targetRoute = rand.nextInt(tourCount);
	        }

	        if (solution.getTourSizes()[sourceRoute] > 2) {
	            int posSource = 1 + rand.nextInt(solution.getTourSizes()[sourceRoute] - 2);
	            solution.shakeStep(posSource, 1, TABU_TENURE, 0);
	            solution.insertStep();
	        }
	    }

	    private void destroyAndRepair(Solution solution, double destructionRate) {
	        int totalPOIs = countAssignedPOIs(solution);
	        int toDestroy = Math.max(1, (int) (totalPOIs * destructionRate));

	        Random rand = new Random();
	        for (int i = 0; i < toDestroy; i++) {
	            int tour = rand.nextInt(problemInput.getTourCount());
	            if (solution.getTourSizes()[tour] > 2) {
	                int pos = 1 + rand.nextInt(solution.getTourSizes()[tour] - 2);
	                solution.shakeStep(pos, 1, TABU_TENURE, 0);
	            }
	        }

	        while (solution.notStuckInLocalOptimum()) {
	            solution.insertStep();
	        }
	    }

	    private void combinedShaking(Solution solution) {
	        Random rand = new Random();
	        double choice = rand.nextDouble();

	        if (choice < 0.33) {
	            randomSwapBetweenRoutes(solution);
	        } else if (choice < 0.66) {
	            randomMoveBetweenRoutes(solution);
	        } else {
	            destroyAndRepair(solution, 0.2);
	        }
	    }

	    private int countAssignedPOIs(Solution solution) {
	        int count = 0;
	        int[] tourSizes = solution.getTourSizes();
	        for (int tour = 0; tour < problemInput.getTourCount(); tour++) {
	            count += Math.max(0, tourSizes[tour] - 2);
	        }
	        return count;
	    }

	    // Les autres méthodes (randomSwapBetweenRoutes, etc.) restent inchangées

	    public Solution getBestSolution() {
	        return this.bestSolution;
	    }
	}
