public OpenMapRealMatrix(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
        long lRow = rowDimension;
        long lCol = columnDimension;
        if (lRow * lCol > Integer.MAX_VALUE) {
            throw new MathIllegalArgumentException(LocalizedFormats.NUMBER_TOO_LARGE, lRow * lCol, Integer.MAX_VALUE);
        }
        this.rows = rowDimension;
        this.columns = columnDimension;
        this.entries = new OpenIntToDoubleHashMap(0.0);
    }