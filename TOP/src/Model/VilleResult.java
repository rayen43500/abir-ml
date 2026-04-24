package Model;
import java.util.List;
public class VilleResult {

	
	    private String villeChoisie;
	    private int villeId;
	    private Statistiques statistiques;
	    private List<POI> topPois;
	    
	    // Getters et Setters
	    public static class Statistiques {
	        private int nbPois;
	        private double scoreMoyen;
	        private double dureeTotale;
	        private int nbFeatures;
	        
	        // Getters et Setters
	    }
	    
	    public static class POI {
	        private int id;
	        private double score;
	        private double duration;
	        private List<Integer> features;
	        
	        // Getters et Setters
	    }
	
	
}
