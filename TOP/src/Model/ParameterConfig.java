package Model;

public class ParameterConfig {
    public double min;
    public double max;
    public double currentValue;
    
    public ParameterConfig(double min, double max, double initial) {
        this.min = min;
        this.max = max;
        this.currentValue = initial;
    }
    
    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getCurrentValue() { return currentValue; }
    
    public void setCurrentValue(double value) {
        this.currentValue = Math.max(min, Math.min(max, value));
    }
}