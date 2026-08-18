public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        if (rowDimension < 0 || columnDimension < 0) {
            throw new IllegalArgumentException("row and column dimensions must be >= 0");
        }
        super(rowDimension, columnDimension);
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }