// ===== FIXED org.apache.commons.math.complex.Complex :: equals(Object) [lines 245-267] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-96-fixed/src/java/org/apache/commons/math/complex/Complex.java =====
    public boolean equals(Object other) {
        boolean ret;
        
        if (this == other) { 
            ret = true;
        } else if (other == null) {
            ret = false;
        } else  {
            try {
                Complex rhs = (Complex)other;
                if (rhs.isNaN()) {
                    ret = this.isNaN();
                } else {
                    ret = (real == rhs.real) && (imaginary == rhs.imaginary); 
                }
            } catch (ClassCastException ex) {
                // ignore exception
                ret = false;
            }
        }
      
        return ret;
    }
