package Model;
import java.util.*;
public interface MLPredictor {
	
	Map<String, Double> suggestParameters(PerformanceMetrics metrics, 
                                        Map<String, Double> solutionFeatures, 
                                        int iteration);
    
    String predictBestOperator(Solution current, List<String> availableOperators);
    
    double predictImprovementProbability(Solution solution, String operator);
}