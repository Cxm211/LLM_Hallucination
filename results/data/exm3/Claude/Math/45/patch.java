public OpenMapRealMatrix(int rowDimension, int columnDimension) {
    super(rowDimension, columnDimension);
    long size = (long) rowDimension * (long) columnDimension;
    if (size > Integer.MAX_VALUE) {
        throw new NumberIsTooLargeException(size, Integer.MAX_VALUE, false);
    }
    this.rows = rowDimension;
    this.columns = columnDimension;
    this.entries = new OpenIntToDoubleHashMap(0.0);
}