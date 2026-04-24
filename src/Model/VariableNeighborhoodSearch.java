package Model;
import java.util.*;
public class VariableNeighborhoodSearch {
	private final int FACTOR_NO_IMPROVEMENT = 10;
	private final int TABU_ITERATIONS = 2;
	private final int NUMBER_OF_PIVOT_CHANGES_DURING_ONE_FULL_EXECUTION = 4;
	private final int PROBABILITY_OF_NOT_REUSING_PIVOTS = 7;
	private int startRemoveAt = 0;
	private int removeNConsecutiveVisits = 1;
	
	    private final int MAX_ITERATIONS_WITHOUT_IMPROVEMENT = 2000;
	    private final int K_MAX = 6; // Nombre maximum de voisinages
	    private final int TABU_TENURE = 5;
	    
 		int numberOfTimesWithNoImprovement = 0;
	    private Solution bestSolution;
	    private ProblemInput problemInput;

	    public void solve(ProblemInput problemInput) {
	        this.problemInput = problemInput;
	        int currentIteration = 0;
			int pivotChangeCounter = 0;
	       
	        //    Étape 1: Initialisation
	         Solution currentSolution = generateInitialSolution(problemInput);
	          bestSolution = (Solution) currentSolution.clone();
	           System.out.println("Solution initiale VNS: " +currentSolution.toString());
	          System.out.println("Score: " + currentSolution.getScore());
	          System.out.println("Total money spent: " + currentSolution.getTotalMoneySpent());
	          System.out.println("Tour sizes: " + Arrays.toString(currentSolution.getTourSizes()));
	           System.out.println("score Solution initiale VNS: " + currentSolution.getScore());

	          int iterationsWithoutImprovement = 0;
	             int totalIterations = 0;
  
	        // Étape 2: Boucle principale VNS
	          while (iterationsWithoutImprovement < MAX_ITERATIONS_WITHOUT_IMPROVEMENT) {
	              int k = 1;
	            boolean improvementFound = false;

	           //   Étape 3: Exploration des voisinages (k = 1 à K_MAX)
	           while (k <= K_MAX && !improvementFound) {

		            
		             
	        	   //    Étape 3a: Shaking - Génération aléatoire dans le voisinage N_k
	        	   Solution shakenSolution = (Solution)currentSolution ;
					
	        	   
	        	   shakenSolution.shakeStep ( startRemoveAt, removeNConsecutiveVisits, TABU_ITERATIONS, currentIteration );
	        		
	                
	              //   Étape 3b: Recherche locale (Variable Neighborhood Descent)
	           
	           Solution localOptimum = variableNeighborhoodDescent(shakenSolution);
	                
	           //  Étape 3c: Critère d'acceptation
	          if (localOptimum.getScore() > currentSolution.getScore()) {
	           currentSolution = localOptimum;
	          improvementFound = true;
	                    
	          //   Mettre à jour la meilleure solution
	          if (currentSolution.getScore() > bestSolution.getScore()) {
	                        bestSolution = (Solution) currentSolution.clone();
	                        System.out.println("Nouvelle meilleure solution VNS: " + bestSolution.getScore() + 
	                                         " (itération " + totalIterations + ")");
	                        iterationsWithoutImprovement = 0;
	                    }
	                } else {
	                    k++; // Passer au voisinage suivant
	                }
	            }

	            if (!improvementFound) {
	                iterationsWithoutImprovement++;
	            }

	            totalIterations++;

	            // Diversification périodique
	            if (totalIterations % 50 == 0) {
	                currentSolution = intensificationDiversification(currentSolution);
	            }
	        }

	        System.out.println("VNS terminé. Meilleur score: " + bestSolution.getScore());
	    }

	    /**
	     * Étape 1: Génération de la solution initiale
	     */
	          private Solution generateInitialSolution(ProblemInput problemInput) {
	        Solution solution = new Solution(problemInput);
	        
	        //  Construction avec insertion de pivots
	        if (!solution.insertPivots(0, 0, 7)) {
	        	//  Fallback: construction par insertion gloutonne
	            constructSimpleSolution(solution);
	        }
	        
	        //  Amélioration initiale avec recherche locale
	        solution = variableNeighborhoodDescent(solution);
	        
	        return solution;
	    } 

	    /* 
	     * Étape 3a: Shaking - Perturbation aléatoire dans le voisinage N_k
	     */
	     private Solution shaking(Solution solution, int k) {
	        Solution shakenSolution = (Solution) solution.clone();
	        
	        switch (k) {
	            case 1:
	                // Voisinage 1: Échange de POIs entre tournées
	                randomSwapBetweenRoutes(shakenSolution);
	                break;
	            case 2:
	                // Voisinage 2: Déplacement de POI entre tournées
	                randomMoveBetweenRoutes(shakenSolution);
	                break;
	            case 3:
	                // Voisinage 3: Destruction-reconstruction partielle
	                destroyAndRepair(shakenSolution, 0.3);
	                break;
	            case 4:
	                // Voisinage 4: Combinaison d'opérateurs
	                combinedShaking(shakenSolution);
	                break;
	            default:
	                randomSwapBetweenRoutes(shakenSolution);
	        }
	        
	        return shakenSolution;
	    }
 
 
	    /**
	     * Étape 3b: Variable Neighborhood Descent - Recherche locale avec plusieurs voisinages
	     */
	           private Solution variableNeighborhoodDescent(Solution solution) {
	        Solution currentSolution = (Solution) solution.clone();
	        boolean improvement = true;
	        int neighborhood = 1;
	        
	        while (improvement && neighborhood <= 4) {
	            improvement = false;
	            
	            Solution bestNeighbor = exploreNeighborhood(currentSolution, neighborhood);
	            
	            if (bestNeighbor.getScore() > currentSolution.getScore()) {
	                currentSolution = bestNeighbor;
	                improvement = true;
	                neighborhood = 1;
	            } else {
	                neighborhood++;
	            }
	        }
	        
	        return currentSolution;
	    }

	    /**
	     * Exploration complète d'un voisinage spécifique
	     */
	    private Solution exploreNeighborhood(Solution solution, int neighborhoodType) {
	        Solution bestSolution = (Solution) solution.clone();
	        
	        switch (neighborhoodType) {
	            case 1:
	                bestSolution = best2OptIntraRoute(solution);
	                break;
	            case 2:
	                bestSolution = bestSwapInterRoute(solution);
	                break;
	            case 3:
	                bestSolution = bestInsertionNeighborhood(solution);
	                break;
	            case 4:
	                bestSolution = bestSegmentExchange(solution);
	                break;
	        }
	        
	        return bestSolution;
	    }

	    /**
	     * OPÉRATEURS DE SHAKING (PERTURBATION) - Adaptés à votre classe Solution
	     */
	    private void randomSwapBetweenRoutes(Solution solution) {
	        int tourCount = problemInput.getTourCount();
	        if (tourCount < 2) return;
	        
	        Random rand = new Random();
	        int route1 = rand.nextInt(tourCount);
	        int route2 = rand.nextInt(tourCount);
	        while (route2 == route1) {
	            route2 = rand.nextInt(tourCount);
	        }
	        
	        // Échanger deux POIs aléatoires entre les tournées
	        if (solution.getTourSizes()[route1] > 2 && solution.getTourSizes()[route2] > 2) {
	            int pos1 = 1 + rand.nextInt(solution.getTourSizes()[route1] - 2);
	            int pos2 = 1 + rand.nextInt(solution.getTourSizes()[route2] - 2);
	            
	            // Utiliser shakeStep pour supprimer et réinsérer
	            solution.shakeStep(pos1, 1, TABU_TENURE, 0);
	            solution.shakeStep(pos2, 1, TABU_TENURE, 0);
	        }
	    }

	    private void randomMoveBetweenRoutes(Solution solution) {
	        int tourCount = problemInput.getTourCount();
	        if (tourCount < 2) return;
	        
	        Random rand = new Random();
	        int sourceRoute = rand.nextInt(tourCount);
	        int targetRoute = rand.nextInt(tourCount);
	        while (targetRoute == sourceRoute) {
	            targetRoute = rand.nextInt(tourCount);
	        }
	        
	        if (solution.getTourSizes()[sourceRoute] > 2) {
	            int posSource = 1 + rand.nextInt(solution.getTourSizes()[sourceRoute] - 2);
	            
	            // Supprimer de la source
	            solution.shakeStep(posSource, 1, TABU_TENURE, 0);
	            
	            // Réinsérer dans la cible (utiliser insertStep)
	            solution.insertStep();
	        }
	    }

	    private void destroyAndRepair(Solution solution, double destructionRate) {
	        int totalPOIs = countAssignedPOIs(solution);
	        int toDestroy = Math.max(1, (int) (totalPOIs * destructionRate));
	        
	        // Supprimer des POIs aléatoirement
	        Random rand = new Random();
	        for (int i = 0; i < toDestroy; i++) {
	            int tour = rand.nextInt(problemInput.getTourCount());
	            if (solution.getTourSizes()[tour] > 2) {
	                int pos = 1 + rand.nextInt(solution.getTourSizes()[tour] - 2);
	                solution.shakeStep(pos, 1, TABU_TENURE, 0);
	            }
	        }
	        
	        // Reconstruction par insertion gloutonne
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

	    /**
	     * OPÉRATEURS DE RECHERCHE LOCALE - Adaptés à votre classe Solution
	     */
	    private Solution best2OptIntraRoute(Solution solution) {
	        Solution bestSolution = (Solution) solution.clone();
	        boolean improvement = true;
	        
	        while (improvement) {
	            improvement = false;
	            Solution currentBest = (Solution) bestSolution.clone();
	            
	            // Appliquer 2-opt sur chaque tournée
	            for (int tour = 0; tour < problemInput.getTourCount(); tour++) {
	                if (bestSolution.getTourSizes()[tour] > 4) {
	                    Solution improved = apply2OptToTour(bestSolution, tour);
	                    if (improved.getScore() > currentBest.getScore()) {
	                        currentBest = improved;
	                        improvement = true;
	                    }
	                }
	            }
	            
	            if (improvement) {
	                bestSolution = currentBest;
	            }
	        }
	        
	        return bestSolution;
	    }

	    private Solution bestSwapInterRoute(Solution solution) {
	        Solution bestSolution = (Solution) solution.clone();
	        boolean improvement = true;
	        
	        while (improvement) {
	            improvement = false;
	            
	            for (int i = 0; i < problemInput.getTourCount(); i++) {
	                for (int j = i + 1; j < problemInput.getTourCount(); j++) {
	                    // Essayer d'échanger des POIs entre les tournées i et j
	                    Solution candidate = tryBestSwapBetweenTours(bestSolution, i, j);
	                    if (candidate.getScore() > bestSolution.getScore()) {
	                        bestSolution = candidate;
	                        improvement = true;
	                    }
	                }
	            }
	        }
	        
	        return bestSolution;
	    }

	    private Solution bestInsertionNeighborhood(Solution solution) {
	        Solution bestSolution = (Solution) solution.clone();
	        boolean improvement;
	        
	        do {
	            improvement = false;
	            Solution newSolution = (Solution) bestSolution.clone();
	            
	            // Utiliser la méthode insertStep existante
	            while (newSolution.notStuckInLocalOptimum()) {
	                newSolution.insertStep();
	            }
	            
	            if (newSolution.getScore() > bestSolution.getScore()) {
	                bestSolution = newSolution;
	                improvement = true;
	            }
	        } while (improvement);
	        
	        return bestSolution;
	    }

	    private Solution bestSegmentExchange(Solution solution) {
	        Solution bestSolution = (Solution) solution.clone();
	        
	        // Échanger des segments entre tournées
	        for (int i = 0; i < problemInput.getTourCount(); i++) {
	            for (int j = i + 1; j < problemInput.getTourCount(); j++) {
	                Solution candidate = exchangeSegmentsBetweenTours(bestSolution, i, j);
	                if (candidate.getScore() > bestSolution.getScore()) {
	                    bestSolution = candidate;
	                }
	            }
	        }
	        
	        return bestSolution;
	    }

	    /**
	     * STRATÉGIE D'INTENSIFICATION/DIVERSIFICATION
	     */
	        private Solution intensificationDiversification(Solution currentSolution) {
	         if (Math.random() < 0.7) {
	            // Intensification: recherche locale renforcée
	           return intensifiedLocalSearch(currentSolution);
	        } else {
	            // Diversification: perturbation forte
	        return strongDiversification(currentSolution);
	       }
	      }

	       private Solution intensifiedLocalSearch(Solution solution) {
	        Solution current = (Solution) solution.clone();   
	        
	        //  Recherche locale plus intensive
	        for (int i = 0; i < 5; i++) {
	            Solution candidate = variableNeighborhoodDescent(current);
	            if (candidate.getScore() > current.getScore()) {
	                current = candidate;
	            }
	        }
	        
	        return current;
	    }  

	    private Solution strongDiversification(Solution solution) {
	        Solution diversified = (Solution) solution.clone();
	        
	        // Destruction plus importante
	        destroyAndRepair(diversified, 0.5);
	        
	        // Changement de pivots
	        diversified.changePivots(7);
	        
	        return diversified;
	    }

	    /**
	     * MÉTHODES AUXILIAIRES SPÉCIFIQUES
	     */
	    private void constructSimpleSolution(Solution solution) {
	        // Construction par insertion gloutonne
	        while (solution.notStuckInLocalOptimum()) {
	            solution.insertStep();
	        }
	    }

	    private int countAssignedPOIs(Solution solution) {
	        int count = 0;
	        int[] tourSizes = solution.getTourSizes();
	        for (int tour = 0; tour < problemInput.getTourCount(); tour++) {
	            // Soustraire les dépôts (début et fin)
	            count += Math.max(0, tourSizes[tour] - 2);
	        }
	        return count;
	    }

	    /**
	     * MÉTHODES D'OPÉRATEURS LOCAUX DÉTAILLÉES
	     */
	    private Solution apply2OptToTour(Solution solution, int tour) {
	        Solution best = (Solution) solution.clone();
	        int tourSize = best.getTourSizes()[tour];
	        
	        if (tourSize <= 4) return best; // Pas assez de POIs pour 2-opt
	        
	        boolean improvement = true;
	        while (improvement) {
	            improvement = false;
	            
	            for (int i = 1; i < tourSize - 3; i++) {
	                for (int j = i + 2; j < tourSize - 1; j++) {
	                    Solution candidate = try2OptSwap(best, tour, i, j);
	                    if (candidate != null && candidate.getScore() > best.getScore()) {
	                        best = candidate;
	                        improvement = true;
	                    }
	                }
	            }
	        }
	        
	        return best;
	    }

	    private Solution try2OptSwap(Solution solution, int tour, int i, int j) {
	        // Implémentation simplifiée du 2-opt
	        // Dans une vraie implémentation, vous devriez recalculer tous les temps
	        Solution candidate = (Solution) solution.clone();
	        
	        // Supprimer les segments et les réinsérer dans l'ordre inverse
	        // Cette partie nécessiterait une implémentation plus détaillée
	        // selon votre structure de données interne
	        
	        return candidate;
	    }

	    private Solution tryBestSwapBetweenTours(Solution solution, int tour1, int tour2) {
	        Solution best = (Solution) solution.clone();
	        
	        int size1 = solution.getTourSizes()[tour1];
	        int size2 = solution.getTourSizes()[tour2];
	        
	        if (size1 <= 2 || size2 <= 2) return best;
	        
	        // Essayer d'échanger des POIs entre les deux tournées
	        for (int i = 1; i < size1 - 1; i++) {
	            for (int j = 1; j < size2 - 1; j++) {
	                Solution candidate = trySwapPOIs(best, tour1, i, tour2, j);
	                if (candidate != null && candidate.getScore() > best.getScore()) {
	                    best = candidate;
	                }
	            }
	        }
	        
	        return best;
	    }

	    private Solution trySwapPOIs(Solution solution, int tour1, int pos1, int tour2, int pos2) {
	        // Implémentation de l'échange de deux POIs
	        // Cette méthode nécessiterait l'accès aux POIIntervals spécifiques
	        Solution candidate = (Solution) solution.clone();
	        
	        // Échanger les POIs aux positions données
	        // Cette partie dépend de votre implémentation interne
	        
	        return candidate;
	    }

	    private Solution exchangeSegmentsBetweenTours(Solution solution, int tour1, int tour2) {
	        Solution best = (Solution) solution.clone();
	        
	        // Échanger des segments de différentes tailles
	        for (int segmentSize = 1; segmentSize <= 2; segmentSize++) {
	            Solution candidate = trySegmentExchange(best, tour1, tour2, segmentSize);
	            if (candidate != null && candidate.getScore() > best.getScore()) {
	                best = candidate;
	            }
	        }
	        
	        return best;
	    }

	    private Solution trySegmentExchange(Solution solution, int tour1, int tour2, int segmentSize) {
	        // Échanger des segments entre tournées
	        Solution candidate = (Solution) solution.clone();
	        
	        // Implémentation de l'échange de segments
	        // Cette partie dépend de votre structure interne
	        
	        return candidate;
	    }

	    public Solution getBestSolution() {
	        return this.bestSolution;
	    }
	    
	    /**
	     * Méthode utilitaire pour obtenir la taille d'une tournée (compatibilité)
	     */
	    public int getSizeOfFirstRoute(ProblemInput problemInput) {
	        Solution currentSolution = new Solution(problemInput);
	        while(currentSolution.notStuckInLocalOptimum()) {
	            currentSolution.insertStep();
	        }
	        // Restaurer les données pour l'exécution réelle de l'algorithme
	        for(int tour = 0; tour < problemInput.getTourCount(); tour++) {
	            POIInterval currentPOIInterval = currentSolution.getNthPOIIntervalInTourX(0, tour);
	            while(currentPOIInterval != null) {
	                if(currentPOIInterval.getPOI().getDuration() > 0) {
	                    currentPOIInterval.getPOI().setAssigned(false);
	                }
	                currentPOIInterval = currentPOIInterval.getNextPOIInterval();
	            }
	        }
	        return currentSolution.getTourSizes()[0];
	    }
	}

