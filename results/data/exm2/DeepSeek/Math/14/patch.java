    private RealMatrix squareRoot(RealMatrix m) {
        if (m.isDiagonal()) {
            final int dim = m.getRowDimension();
            final RealMatrix sqrt = org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(dim, dim);
            for (int i = 0; i < dim; i++) {
                sqrt.setEntry(i, i, FastMath.sqrt(m.getEntry(i, i)));
            }
            return sqrt;
        } else {
            final EigenDecomposition dec = new EigenDecomposition(m);
            return dec.getSquareRoot();
        }
    }