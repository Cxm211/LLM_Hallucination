// ===== FIXED org.apache.commons.math.linear.OpenMapRealVector :: ebeDivide(RealVector) [lines 342-351] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-49-fixed/src/main/java/org/apache/commons/math/linear/OpenMapRealVector.java =====
    public OpenMapRealVector ebeDivide(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v.getEntry(iter.key()));
        }
        return res;
    }

// ===== FIXED org.apache.commons.math.linear.OpenMapRealVector :: ebeDivide(double[]) [lines 355-364] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-49-fixed/src/main/java/org/apache/commons/math/linear/OpenMapRealVector.java =====
    public OpenMapRealVector ebeDivide(double[] v) {
        checkVectorDimensions(v.length);
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() / v[iter.key()]);
        }
        return res;
    }

// ===== FIXED org.apache.commons.math.linear.OpenMapRealVector :: ebeMultiply(RealVector) [lines 367-376] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-49-fixed/src/main/java/org/apache/commons/math/linear/OpenMapRealVector.java =====
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }

// ===== FIXED org.apache.commons.math.linear.OpenMapRealVector :: ebeMultiply(double[]) [lines 380-389] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-49-fixed/src/main/java/org/apache/commons/math/linear/OpenMapRealVector.java =====
    public OpenMapRealVector ebeMultiply(double[] v) {
        checkVectorDimensions(v.length);
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v[iter.key()]);
        }
        return res;
    }
