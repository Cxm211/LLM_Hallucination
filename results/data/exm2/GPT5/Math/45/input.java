    public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }

// trigger testcase
public void testMath679() {
        new OpenMapRealMatrix(3, Integer.MAX_VALUE);
    }
