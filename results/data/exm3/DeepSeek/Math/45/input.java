// buggy function
    public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }

// trigger testcase
// org/apache/commons/math/linear/OpenMapRealMatrixTest.java::testMath679
public void testMath679() {
        new OpenMapRealMatrix(3, Integer.MAX_VALUE);
    }
