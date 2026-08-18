// ===== FIXED org.apache.commons.math.linear.OpenMapRealMatrix :: OpenMapRealMatrix [lines 48-58] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-45-fixed/src/main/java/org/apache/commons/math/linear/OpenMapRealMatrix.java =====
    public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        long lRow = (long) rowDimension;
        long lCol = (long) columnDimension;
        if (lRow * lCol >= (long) Integer.MAX_VALUE) {
            throw new NumberIsTooLargeException(lRow * lCol, Integer.MAX_VALUE, false);
        }
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }
