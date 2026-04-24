package Model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ProblemInput {
	private int tourCount;
	private int visitablePOICount;
	private POI startingPOI;
	private POI endingPOI;
	private POI[] visitablePOIs;
	private int budgetLimit;
	private int[] maxAllowedVisitsForEachType;
	private int[][] patternsForEachTour;
	private boolean solomon;
	private HashMap<Integer, ArrayList<POI>> POIsForEachPatternType;
	private HashMap<Integer, HashMap<Integer, Integer>> travelDistances;

	private ProblemInput() {

	}

	private ProblemInput(int tourCount, int visitablePOICount, POI startingPOI, POI endingPOI, POI[] visitablePOIs, 
            int budgetLimit, int[] maxAllowedVisitsForEachType, int[][] patternsForEachTour, 
            HashMap<Integer, ArrayList<POI>> POIsForEachPatternType, boolean solomon) { 
this.tourCount = tourCount;
this.visitablePOICount = visitablePOICount;
this.startingPOI = startingPOI;
this.endingPOI = endingPOI;
this.solomon = solomon;
this.budgetLimit = budgetLimit;
this.maxAllowedVisitsForEachType = maxAllowedVisitsForEachType;
this.patternsForEachTour = patternsForEachTour;
this.POIsForEachPatternType = POIsForEachPatternType;

// Nettoyer visitablePOIs avant utilisation
this.visitablePOIs = cleanPOIArray(visitablePOIs);

assignTravelDistances();
}

private POI[] cleanPOIArray(POI[] originalArray) {
if (originalArray == null) return new POI[0];

ArrayList<POI> cleanList = new ArrayList<>();
for (POI poi : originalArray) {
if (poi != null) {
    cleanList.add(poi);
}
}
return cleanList.toArray(new POI[0]);
}
	
	/*private ProblemInput(int tourCount, int visitablePOICount, POI startingPOI, POI endingPOI, POI[] visitablePOIs, 
						int budgetLimit, int[] maxAllowedVisitsForEachType, int[][] patternsForEachTour, 
						HashMap<Integer, ArrayList<POI>> POIsForEachPatternType, boolean solomon) { 
		this.tourCount = tourCount;
		this.visitablePOICount = visitablePOICount;
		this.startingPOI = startingPOI;
		this.endingPOI = endingPOI;
		this.visitablePOIs = visitablePOIs;
		this.solomon = solomon;
		this.budgetLimit = budgetLimit;
		this.maxAllowedVisitsForEachType = maxAllowedVisitsForEachType;
		this.patternsForEachTour = patternsForEachTour;
		this.POIsForEachPatternType = POIsForEachPatternType;

		assignTravelDistances();
	}*/
	public void assignTravelDistances() {
	    // Vérifications initiales renforcées
	    if (startingPOI == null) {
	        throw new IllegalStateException("Erreur critique: startingPOI est null");
	    }
	    
	    if (visitablePOIs == null) {
	        throw new IllegalStateException("Erreur critique: visitablePOIs est null");
	    }
	    
	    HashMap<Integer, HashMap<Integer, Integer>> travelDistances = new HashMap<>();
	    
	    // Filtrer les POI null
	    ArrayList<POI> validPOIs = new ArrayList<>();
	    for (POI poi : visitablePOIs) {
	        if (poi != null) {
	            validPOIs.add(poi);
	        }
	    }
	    
	    System.out.println("POIs valides pour les distances: " + validPOIs.size() + "/" + visitablePOIs.length);
	    
	    // Liste de tous les POIs importants
	    ArrayList<POI> allPOIs = new ArrayList<>();
	    allPOIs.add(startingPOI);
	    if (endingPOI != null) {
	        allPOIs.add(endingPOI);
	    }
	    allPOIs.addAll(validPOIs);
	    
	    // Calculer toutes les distances entre toutes les paires
	    for (int i = 0; i < allPOIs.size(); i++) {
	        POI fromPOI = allPOIs.get(i);
	        HashMap<Integer, Integer> distancesFromCurrent = new HashMap<>();
	        
	        for (int j = i + 1; j < allPOIs.size(); j++) {
	            POI toPOI = allPOIs.get(j);
	            int distance = getDistance(fromPOI, toPOI);
	            distancesFromCurrent.put(toPOI.getID(), distance);
	        }
	        
	        if (!distancesFromCurrent.isEmpty()) {
	            travelDistances.put(fromPOI.getID(), distancesFromCurrent);
	        }
	    }

	    this.travelDistances = travelDistances;
	    System.out.println("Distances calculées pour " + travelDistances.size() + " POIs");
	}
	/*public void assignTravelDistances() {
	    HashMap<Integer, HashMap<Integer, Integer>> travelDistances = new HashMap<>();
	    HashMap<Integer, Integer> travelDistancesForPOINumberX;
	    
	    // Vérifier startingPOI
	    if (startingPOI == null) {
	        System.err.println("Erreur: startingPOI est null");
	        return;
	    }
	    
	    // Vérifier visitablePOIs
	    if (visitablePOIs == null) {
	        System.err.println("Erreur: visitablePOIs est null");
	        return;
	    }
	    
	    travelDistancesForPOINumberX = new HashMap<>();
	    +
	    // Premier loop - startingPOI vers tous les visitablePOIs
	    for(POI currentPOI: visitablePOIs) {
	        if (currentPOI != null) {
	            travelDistancesForPOINumberX.put(currentPOI.getID(), getDistance(startingPOI, currentPOI));
	        } else {
	            System.err.println("Attention: POI null trouvé dans visitablePOIs");
	        }
	    }
	    travelDistances.put(startingPOI.getID(), travelDistancesForPOINumberX);

	    // Deuxième loop - entre les visitablePOIs
	    for(int i = 0; i < visitablePOIs.length - 1; i++) {
	        // Vérifier le POI à l'index i
	        if (visitablePOIs[i] == null) {
	            System.err.println("Attention: POI à l'index " + i + " est null, skipping...");
	            continue;
	        }
	        
	        travelDistancesForPOINumberX = new HashMap<>();
	        
	        for(int j = i + 1; j < visitablePOIs.length; j++) {
	            // Vérifier le POI à l'index j
	            if (visitablePOIs[j] != null) {
	                travelDistancesForPOINumberX.put(visitablePOIs[j].getID(), 
	                    getDistance(visitablePOIs[i], visitablePOIs[j]));
	            } else {
	                System.err.println("Attention: POI à l'index " + j + " est null");
	            }
	        }
	        travelDistances.put(visitablePOIs[i].getID(), travelDistancesForPOINumberX);
	    }

	    this.travelDistances = travelDistances;
	}*/

	/*public void assignTravelDistances() {
		HashMap<Integer, HashMap<Integer, Integer>> travelDistances = new HashMap<>();
		HashMap<Integer, Integer> travelDistancesForPOINumberX;
		travelDistancesForPOINumberX = new HashMap<>();
		for(POI currentPOI: visitablePOIs) {
			travelDistancesForPOINumberX.put(currentPOI.getID(), getDistance(startingPOI, currentPOI));
		}
		travelDistances.put(startingPOI.getID(), travelDistancesForPOINumberX);

		for(int i = 0; i < visitablePOIs.length - 1; i++) {
			travelDistancesForPOINumberX = new HashMap<>();
			for(int j = i + 1; j < visitablePOIs.length; j++) {
				travelDistancesForPOINumberX.put(visitablePOIs[j].getID(), getDistance(visitablePOIs[i], visitablePOIs[j]));
			}
			travelDistances.put(visitablePOIs[i].getID(), travelDistancesForPOINumberX);
		}

		this.travelDistances = travelDistances;
	}*/

	private int getDistance(POI fromPOI, POI toPOI) {
		
		 if (fromPOI == toPOI) {
		        return 0;
		    }
		 else {
			 try {
		double euclidianDistance = MathExtension.getEuclidianDistanceOfTwoPOIs(fromPOI, toPOI);
		if(solomon) {
			return ((int)(euclidianDistance / 100)) * 10;
		}
		else {
			return (int)(euclidianDistance / 10);
		}}
			 catch (Exception e) {
			        System.err.println("Erreur dans getDistanceFromPOIToPOI: " + e.getMessage());
			        return 1000;
			    }
			}
	}
	public static ProblemInput getProblemInputFromFile(String filePath) throws FileNotFoundException {
	    Scanner scanner = new Scanner(new File(filePath));
	    int tourCount = 0;
	    int visitablePOICount = 0;
	    POI startingPOI = null;
	    POI endingPOI = null;
	    POI[] visitablePOIs = null;
	    int budgetLimit = 0;
	    int[] maxAllowedVisitsForEachType = null;
	    int[][] patternsForEachTour = null;
	    HashMap<Integer, ArrayList<POI>> POIsForEachPatternType = new HashMap<>();
	    
	    int lineCounter = 0;
	    int visitablePOICounter = 0;
	    
	    while(scanner.hasNextLine()) {
	        String currentLine = scanner.nextLine().trim();
	        
	        // Ignorer les lignes vides
	        if (currentLine.isEmpty()) {
	            lineCounter++;
	            continue;
	        }
	        
	        if(lineCounter == 0) {
	            String[] firstLineComponents = currentLine.split(" ");
	            tourCount = Integer.parseInt(firstLineComponents[0]);
	            visitablePOICount = Integer.parseInt(firstLineComponents[1]);
	            visitablePOIs = new POI[visitablePOICount];
	            patternsForEachTour = new int[tourCount][];
	            budgetLimit = Math.round(Float.parseFloat(firstLineComponents[2]) * 100);
	        }
	        else if(lineCounter == 1) {
	            String[] typesLineComponents = currentLine.split(" ");
	            maxAllowedVisitsForEachType = new int[typesLineComponents.length];
	            for(int type = 0; type < typesLineComponents.length; type++) {
	                maxAllowedVisitsForEachType[type] = Integer.parseInt(typesLineComponents[type]);
	            }
	        }
	        else if(lineCounter == 2) {
	            String[] patternsLineComponents = currentLine.split(" ");
	            for(int tour = 0; tour < tourCount; tour++) {
	                patternsForEachTour[tour] = new int[Integer.parseInt(patternsLineComponents[tour])];
	            }
	        }
	        else if(lineCounter > 2 && lineCounter < 3 + tourCount) {
	            String[] patternsLineForTourComponents = currentLine.split(" ");
	            int currentTour = lineCounter - 3;
	            for(int patternCount = 0; patternCount < patternsLineForTourComponents.length; patternCount++) {
	                int patternType = Integer.parseInt(patternsLineForTourComponents[patternCount]) - 1;
	                patternsForEachTour[currentTour][patternCount] = patternType;
	                if(!POIsForEachPatternType.containsKey(patternType)) {
	                    POIsForEachPatternType.put(patternType, new ArrayList<POI>());
	                }
	            }
	        }
	        else if(lineCounter == 3 + tourCount) {
	            // CORRECTION IMPORTANTE : startingPOI et endingPOI doivent être créés à partir de lignes différentes
	            startingPOI = parsePOIFromLine(currentLine);
	            
	            // Lire la ligne suivante pour endingPOI
	            if (scanner.hasNextLine()) {
	                currentLine = scanner.nextLine().trim();
	                endingPOI = parsePOIFromLine(currentLine);
	                lineCounter++; // Incrémenter car on lit une ligne supplémentaire
	            } else {
	                System.err.println("Erreur: ligne manquante pour endingPOI");
	                endingPOI = null;
	            }
	        }
	        else {
	            // Vérifier qu'on ne dépasse pas la taille du tableau
	            if (visitablePOICounter < visitablePOIs.length) {
	                POI parsedPOI = parsePOIFromLine(currentLine);
	                if (parsedPOI != null) {
	                    visitablePOIs[visitablePOICounter] = parsedPOI;
	                    
	                    // Ajouter aux types
	                    for(int type : parsedPOI.getTypes()) {
	                        if(POIsForEachPatternType.containsKey(type)) {
	                            POIsForEachPatternType.get(type).add(parsedPOI);
	                        }
	                    }
	                    parsedPOI.createTabuInfo(tourCount);
	                } else {
	                    System.err.println("Erreur: POI null créé à partir de la ligne: " + currentLine);
	                }
	                visitablePOICounter++;
	            } else {
	                System.err.println("Attention: trop de POIs dans le fichier, ligne ignorée: " + currentLine);
	            }
	        }
	        lineCounter++;
	    }
	    scanner.close();

	    // VÉRIFICATION FINALE DES DONNÉES
	    if (startingPOI == null) {
	        System.err.println("Erreur critique: startingPOI est null");
	    }
	    if (endingPOI == null) {
	        System.err.println("Erreur critique: endingPOI est null");
	    }
	    
	    // Compter les POI null dans visitablePOIs
	    int nullCount = 0;
	    for (POI poi : visitablePOIs) {
	        if (poi == null) nullCount++;
	    }
	    if (nullCount > 0) {
	        System.err.println("Attention: " + nullCount + " POI(s) null dans visitablePOIs");
	    }

	    boolean solomon = filePath.contains("Solomon");

	    return new ProblemInput(tourCount, visitablePOICount, startingPOI, endingPOI, visitablePOIs, 
	                            budgetLimit, maxAllowedVisitsForEachType, patternsForEachTour, POIsForEachPatternType, solomon);
	}
	
	/*public static ProblemInput getProblemInputFromFile(String filePath) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(filePath));
		int tourCount = 0;
		int visitablePOICount = 0;
		POI startingPOI = null;
		POI endingPOI = null;
		POI[] visitablePOIs = null;
		int budgetLimit = 0;
		int[] maxAllowedVisitsForEachType = null;
		int[][] patternsForEachTour = null;
		HashMap<Integer, ArrayList<POI>> POIsForEachPatternType =  new HashMap<>();
		
		int lineCounter = 0;
		int visitablePOICounter = 0;
		while(scanner.hasNextLine()) {
			String currentLine = scanner.nextLine();
			if(lineCounter == 0) {
				String[] firstLineComponents = currentLine.split(" ");
				tourCount = Integer.parseInt(firstLineComponents[0]);
				visitablePOICount = Integer.parseInt(firstLineComponents[1]);
				visitablePOIs = new POI[visitablePOICount];
				patternsForEachTour = new int[tourCount][];
				budgetLimit = Math.round(Float.parseFloat(firstLineComponents[2]) * 100);
			}
			else if(lineCounter == 1) {
				String[] typesLineComponents = currentLine.split(" ");
				maxAllowedVisitsForEachType = new int[typesLineComponents.length];
				for(int type = 0; type < typesLineComponents.length; type++) {
					maxAllowedVisitsForEachType[type] = Integer.parseInt(typesLineComponents[type]);
				}
			}
			else if(lineCounter == 2) {
				String[] patternsLineComponents = currentLine.split(" ");
				for(int tour = 0; tour < tourCount; tour++) {
					patternsForEachTour[tour] = new int[Integer.parseInt(patternsLineComponents[tour])];
				}
			}
			else if(lineCounter > 2 && lineCounter < 3 + tourCount) {
				String[] patternsLineForTourComponents = currentLine.split(" ");
				int currentTour = lineCounter - 3;
				for(int patternCount = 0; patternCount < patternsLineForTourComponents.length; patternCount++) {
					int patternType = Integer.parseInt(patternsLineForTourComponents[patternCount]) - 1;
					patternsForEachTour[currentTour][patternCount] = patternType;
					if(!POIsForEachPatternType.keySet().contains(patternType)) {
						POIsForEachPatternType.put(patternType, new ArrayList<POI>());
					}
				}
			}
			else if(lineCounter == 3 + tourCount) {
				startingPOI = parsePOIFromLine(currentLine);
				endingPOI = parsePOIFromLine(currentLine);
			}
			else {
				visitablePOIs[visitablePOICounter] = parsePOIFromLine(currentLine);
				for(int type: visitablePOIs[visitablePOICounter].getTypes()) {
					if(POIsForEachPatternType.keySet().contains(type)) {
						POIsForEachPatternType.get(type).add(visitablePOIs[visitablePOICounter]);
					}
				}
				visitablePOIs[visitablePOICounter].createTabuInfo(tourCount);
				visitablePOICounter++;
			}
			lineCounter++;
		}
		scanner.close();

		boolean solomon = filePath.contains("Solomon");

		return new ProblemInput(tourCount, visitablePOICount, startingPOI, endingPOI, visitablePOIs, 
								budgetLimit, maxAllowedVisitsForEachType, patternsForEachTour, POIsForEachPatternType, solomon);
	}*/
	public static POI parsePOIFromLine(String line) {
	    try {
	        String[] lineComponents = line.split("\\s+"); // Utiliser \\s+ pour gérer plusieurs espaces
	        if (lineComponents.length < 7) {
	            System.err.println("Erreur: ligne POI incomplète: " + line);
	            return null;
	        }
	        
	        int ID = Integer.parseInt(lineComponents[0]);
	        long xCoordinate = Math.round(Double.parseDouble(lineComponents[1]) * 1000);
	        long yCoordinate = Math.round(Double.parseDouble(lineComponents[2]) * 1000);
	        int duration = Math.round(Float.parseFloat(lineComponents[3]) * 100);
	        int score = Math.round(Float.parseFloat(lineComponents[4]) * 100);
	        int openingTime = Math.round(Float.parseFloat(lineComponents[5]) * 100);
	        int closingTime = Math.round(Float.parseFloat(lineComponents[6]) * 100);
	        int entranceFee = 0;
	        ArrayList<Integer> types = new ArrayList<>();

	        if(lineComponents.length > 7) {
	            entranceFee = Math.round(Float.parseFloat(lineComponents[7]) * 100);
	            for(int component = 8; component < lineComponents.length; component++) {
	                if(lineComponents[component].equals("1")) {
	                    types.add(component - 8);
	                }
	            }
	        }

	        return new POI(ID, xCoordinate, yCoordinate, duration, score, openingTime, closingTime, entranceFee, types);
	    } catch (Exception e) {
	        System.err.println("Erreur lors du parsing de la ligne POI: " + line);
	        System.err.println("Exception: " + e.getMessage());
	        return null;
	    }
	}
	/*public static POI parsePOIFromLine(String line) {
		String[] lineComponents = line.split(" ");
		int ID = Integer.parseInt(lineComponents[0]);
		long xCoordinate = Math.round(Double.parseDouble(lineComponents[1]) * 1000);
		long yCoordinate = Math.round(Double.parseDouble(lineComponents[2]) * 1000);
		int duration = Math.round(Float.parseFloat(lineComponents[3]) * 100);
		int score = Math.round(Float.parseFloat(lineComponents[4]) * 100);
		int openingTime = Math.round(Float.parseFloat(lineComponents[5]) * 100);
		int closingTime = Math.round(Float.parseFloat(lineComponents[6]) * 100);
		int entranceFee = 0;
		ArrayList<Integer> types = new ArrayList<>();

		if(lineComponents.length > 7) {
			entranceFee = Math.round(Float.parseFloat(lineComponents[7]) * 100);
			for(int component = 8; component < lineComponents.length; component++) {
				if(lineComponents[component].equals("1")) {
					types.add(component - 8);
				}
			}
		}

		return new POI(ID, xCoordinate, yCoordinate, duration, score, openingTime, closingTime, entranceFee, types);
	}*/

	public int getTourCount() {
		return this.tourCount;
	}

	public int getVisitablePOICount() {
		return this.visitablePOICount;
	}

	public POI getStartingPOI() {
		return this.startingPOI;
	}

	public POI getEndingPOI() {
		return this.endingPOI;
	}

	public POI[] getVisitablePOIs() {
		return this.visitablePOIs;
	}

	public int getBudgetLimit() {
		return this.budgetLimit;
	}

	public int[] getMaxAllowedVisitsForEachType() {
		return this.maxAllowedVisitsForEachType;
	}

	public int getMaxAllowedVisitsForType(int type) {
		return this.maxAllowedVisitsForEachType[type];
	}

	public int[] getPatternsForTour(int tour) {
		return this.patternsForEachTour[tour];
	}

	public ArrayList<POI> getPOIsForPatternType(int type) {
		return this.POIsForEachPatternType.get(type);
	}

	public int getDistanceFromPOIToPOI(int fromPOIID, int toPOIID) {
		if(fromPOIID < toPOIID) {
			return this.travelDistances.get(fromPOIID).get(toPOIID);
		}
		else if(fromPOIID == toPOIID) {
			return 0;
		}
		else {
			return this.travelDistances.get(toPOIID).get(fromPOIID);
		}
	}
}