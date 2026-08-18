public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        if (rowDimension < 1 || columnDimension < 1) {
            throw new NotStrictlyPositiveException(rowDimension < 1 ? rowDimension : columnDimension);
        }
        super(rowDimension, columnDimension);
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }