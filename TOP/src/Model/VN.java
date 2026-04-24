package Model;

public class VN {
	 
		private final int FACTOR_NO_IMPROVEMENT = 10;
		private final int TABU_ITERATIONS = 2;
		private final int NUMBER_OF_PIVOT_CHANGES_DURING_ONE_FULL_EXECUTION = 4;
		private final int PROBABILITY_OF_NOT_REUSING_PIVOTS = 7;
		private final int K_MAX = 3; // Nombre maximum de voisinages
		private int startRemoveAt = 0;
		private int removeNConsecutiveVisits = 1;
		private Solution bestSolution;

		public void solve(ProblemInput problemInput) {
			final int MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT = FACTOR_NO_IMPROVEMENT * getSizeOfFirstRoute(problemInput);
			final int REMOVE_N_CONSECUTIVE_VISITS_LIMIT = (int)(problemInput.getVisitablePOICount() / (3 * problemInput.getTourCount()));
			final int PIVOT_CHANGE_LIMIT = MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT / (NUMBER_OF_PIVOT_CHANGES_DURING_ONE_FULL_EXECUTION + 1);

			// Étape 1: Initialisation
			Solution currentSolution = new Solution(problemInput);
			bestSolution = (Solution)currentSolution.clone();

			if(!currentSolution.insertPivots(0, 0, PROBABILITY_OF_NOT_REUSING_PIVOTS)) {
				return;
			}

			System.out.println("Solution initiale VNS: " + currentSolution.toString());
			System.out.println("Score: " + currentSolution.getScore());

			int currentIteration = 0;
			int pivotChangeCounter = 0;
			int numberOfTimesWithNoImprovement = 0;
			
			// Étape 2: Boucle principale VNS
			while(numberOfTimesWithNoImprovement < MAXIMUM_NUMBER_OF_TIMES_WITH_NO_IMPROVEMENT) {
				
				// Changement de pivots périodique
				if(pivotChangeCounter == PIVOT_CHANGE_LIMIT) {
					currentSolution.changePivots(PROBABILITY_OF_NOT_REUSING_PIVOTS);
					pivotChangeCounter = 0;
				}

				int k = 1;
				boolean improvementFound = false;
				
				// Étape 3: Exploration des voisinages (VNS)
				while(k <= K_MAX && !improvementFound) {
					
					// Étape 3a: Shaking - Génération dans le voisinage N_k
					Solution shakenSolution = (Solution)currentSolution.clone();
					shakenSolution.shakeStep(startRemoveAt, removeNConsecutiveVisits * k, 
											TABU_ITERATIONS, currentIteration);
					
					if(!shakenSolution.isValid()) {
						System.out.println("Shaken solution is not valid");
						k++;
						continue;
					}
					
					// Étape 3b: Recherche locale (VND - Variable Neighborhood Descent)
					Solution localOptimum = variableNeighborhoodDescent(shakenSolution);
					
					if(!localOptimum.isValid()) {
						System.out.println("Local optimum is not valid");
						k++;
						continue;
					}
					
					// Étape 3c: Critère d'acceptation
					if(localOptimum.getScore() > currentSolution.getScore()) {
						currentSolution = localOptimum;
						improvementFound = true;
						
						// Mettre à jour la meilleure solution
						if(currentSolution.getScore() > bestSolution.getScore()) {
							bestSolution = (Solution)currentSolution.clone();
							System.out.println("Nouvelle meilleure solution VNS: " + bestSolution.getScore() + 
											 " (itération " + currentIteration + ", k=" + k + ")");
							removeNConsecutiveVisits = 1;
							numberOfTimesWithNoImprovement = 0;
						}
					} else {
						k++; // Passer au voisinage suivant
					}
				}
				
				if (!improvementFound) {
					numberOfTimesWithNoImprovement++;
				}
				
				// Mise à jour des paramètres de shaking
				startRemoveAt += removeNConsecutiveVisits;
				removeNConsecutiveVisits++;

				if(startRemoveAt >= currentSolution.sizeOfSmallestTour()) {
					startRemoveAt -= currentSolution.sizeOfSmallestTour();
				}
				if(removeNConsecutiveVisits == REMOVE_N_CONSECUTIVE_VISITS_LIMIT) {
					removeNConsecutiveVisits = 1;
				}

				currentIteration++;
				pivotChangeCounter++;
				
				// Diversification périodique
				if(currentIteration % 50 == 0) {
					currentSolution = intensificationDiversification(currentSolution);
				}
			}
			
			System.out.println("VNS terminé. Meilleur score: " + bestSolution.getScore());
		}

		// Variable Neighborhood Descent (VND)
		private Solution variableNeighborhoodDescent(Solution solution) {
			Solution currentSolution = (Solution)solution.clone();
			boolean improvement = true;
			int neighborhoodIndex = 1;
			final int MAX_VND_NEIGHBORHOODS = 3;
			
			while(improvement) {
				improvement = false;
				neighborhoodIndex = 1;
				
				while(neighborhoodIndex <= MAX_VND_NEIGHBORHOODS) {
					// Appliquer la recherche locale selon le voisinage
					Solution neighborSolution = exploreNeighborhood(currentSolution, neighborhoodIndex);
					
					if(neighborSolution.getScore() > currentSolution.getScore()) {
						currentSolution = neighborSolution;
						improvement = true;
						neighborhoodIndex = 1; // Redémarrer du premier voisinage
					} else {
						neighborhoodIndex++;
					}
				}
			}
			
			return currentSolution;
		}
		
		 
		
		// Explorer un voisinage spécifique
		private Solution exploreNeighborhood(Solution solution, int neighborhoodType) {
			Solution bestNeighbor = (Solution)solution.clone();
			
			switch(neighborhoodType) {
				case 1: // Insertion locale
					while(bestNeighbor.notStuckInLocalOptimum()) {
						bestNeighbor.insertStep();
					}
					break;
				case 2: // Swap de deux visites
					bestNeighbor = localSwapSearch(solution);
					break;
				case 3: // 2-opt
					bestNeighbor = localTwoOptSearch(solution);
					break;
			}
			
			return bestNeighbor;
		}
		
		// Recherche locale par swap
		private Solution localSwapSearch(Solution solution) {
			Solution bestSolution = (Solution)solution.clone();
			boolean improved = true;
			
			while(improved) {
				improved = false;
				// Implémentez votre logique de swap ici
				// Par exemple: échanger deux POI entre tours ou dans le même tour
			}
			
			return bestSolution;
		}
		
		// Recherche locale 2-opt
		private Solution localTwoOptSearch(Solution solution) {
			Solution bestSolution = (Solution)solution.clone();
			// Implémentez votre logique 2-opt ici
			return bestSolution;
		}
		
		// Méthode de diversification
		private Solution intensificationDiversification(Solution solution) {
			Solution diversifiedSolution = (Solution)solution.clone();
			
			// Appliquer des perturbations plus importantes
			for(int i = 0; i < 3; i++) {
				diversifiedSolution.shakeStep(startRemoveAt, removeNConsecutiveVisits * 2, 
											TABU_ITERATIONS, 0);
			}
			
			System.out.println("Diversification appliquée à l'itération courante");
			return diversifiedSolution;
		}

		public Solution getBestSolution() {
			return this.bestSolution;
		}

		public int getSizeOfFirstRoute(ProblemInput problemInput) {
			Solution currentSolution = new Solution(problemInput);
			while(currentSolution.notStuckInLocalOptimum()) {
				currentSolution.insertStep();
			}
			// restore the data for the real algorithm execution
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



