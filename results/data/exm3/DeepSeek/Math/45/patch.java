    public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(0, 0);
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }