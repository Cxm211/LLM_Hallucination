public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        if (((long) rowDimension) * ((long) columnDimension) > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("matrix too large");
        }
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }