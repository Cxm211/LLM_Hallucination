// ===== FIXED org.apache.commons.collections.functors.EqualPredicate :: EqualPredicate [lines 81-85] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-17-fixed/src/main/java/org/apache/commons/collections/functors/EqualPredicate.java =====
    public EqualPredicate(T object) {
        // do not use the DefaultEquator to keep backwards compatibility
        // the DefaultEquator returns also true if the two object references are equal
        this(object, null);
    }

// ===== FIXED org.apache.commons.collections.functors.EqualPredicate :: evaluate(T) [lines 107-113] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-17-fixed/src/main/java/org/apache/commons/collections/functors/EqualPredicate.java =====
    public boolean evaluate(T object) {
        if (equator != null) {
            return equator.equate(iValue, object);
        } else {
            return iValue.equals(object);
        }
    }
