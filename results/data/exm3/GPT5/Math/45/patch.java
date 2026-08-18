public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        if (rowDimension < 0 || columnDimension < 0) {
            throw new IllegalArgumentException("negative dimensions");
        }
        long total = ((long) rowDimension) * ((long) columnDimension);
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("matrix is too large");
        }
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }