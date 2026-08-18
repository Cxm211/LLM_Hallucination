public OpenMapRealMatrix(int rowDimension, int columnDimension) {
    super(rowDimension, columnDimension);
    this.rows = rowDimension;
    this.columns = columnDimension;
    long expectedSize = ((long) rowDimension) * ((long) columnDimension);
    this.entries = new OpenIntToDoubleHashMap(0.0);
}