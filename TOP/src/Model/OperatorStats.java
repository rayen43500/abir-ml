package Model;

public class OperatorStats {
    private double successRate;
    private double uses;
    private double totalUses;
    
    public OperatorStats(double successRate, double uses, double totalUses) {
        this.successRate = successRate;
        this.uses = uses;
        this.totalUses = totalUses;
    }
    
    // Getters and setters
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    public double getUses() { return uses; }
    public void incrementUses() { this.uses++; this.totalUses++; }
    public double getTotalUses() { return totalUses; }
}