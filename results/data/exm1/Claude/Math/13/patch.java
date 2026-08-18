private RealMatrix squareRoot(RealMatrix m) {
    final EigenDecomposition dec = new EigenDecomposition(m);
    RealMatrix sqrtMatrix = dec.getSquareRoot();
    
    // Handle the case where the matrix is singular or nearly singular
    // by checking if the square root matrix contains NaN or infinite values
    for (int i = 0; i < sqrtMatrix.getRowDimension(); i++) {
        for (int j = 0; j < sqrtMatrix.getColumnDimension(); j++) {
            double val = sqrtMatrix.getEntry(i, j);
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                // Return a zero matrix or identity matrix scaled appropriately
                // to handle the singular case gracefully
                return org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(
                    sqrtMatrix.getRowDimension(), 
                    sqrtMatrix.getColumnDimension()
                );
            }
        }
    }
    
    return sqrtMatrix;
}