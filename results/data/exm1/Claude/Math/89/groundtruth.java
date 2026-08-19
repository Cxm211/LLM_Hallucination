// ===== FIXED org.apache.commons.math.stat.Frequency :: addValue(Object) [lines 109-115] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-89-fixed/src/java/org/apache/commons/math/stat/Frequency.java =====
    public void addValue(Object v) {
        if (v instanceof Comparable<?>){
            addValue((Comparable<?>) v);            
        } else {
            throw new IllegalArgumentException("Object must implement Comparable");
        }
    }
