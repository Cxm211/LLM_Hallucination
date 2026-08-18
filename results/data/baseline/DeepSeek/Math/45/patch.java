public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        this.rows = rowDimension;
        this.columns = columnDimension;
        if ((long) rowDimension * columnDimension > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The product of dimensions exceeds Integer.MAX_VALUE");
        }
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }