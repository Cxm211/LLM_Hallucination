// ===== FIXED org.apache.commons.math.linear.BigMatrixImpl :: operate(BigDecimal[]) [lines 985-1000] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-98-fixed/src/java/org/apache/commons/math/linear/BigMatrixImpl.java =====
    public BigDecimal[] operate(BigDecimal[] v) throws IllegalArgumentException {
        if (v.length != this.getColumnDimension()) {
            throw new IllegalArgumentException("vector has wrong length");
        }
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        final BigDecimal[] out = new BigDecimal[nRows];
        for (int row = 0; row < nRows; row++) {
            BigDecimal sum = ZERO;
            for (int i = 0; i < nCols; i++) {
                sum = sum.add(data[row][i].multiply(v[i]));
            }
            out[row] = sum;
        }
        return out;
    }

// ===== FIXED org.apache.commons.math.linear.RealMatrixImpl :: operate(double[]) [lines 773-789] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-98-fixed/src/java/org/apache/commons/math/linear/RealMatrixImpl.java =====
    public double[] operate(double[] v) throws IllegalArgumentException {
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        if (v.length != nCols) {
            throw new IllegalArgumentException("vector has wrong length");
        }
        final double[] out = new double[nRows];
        for (int row = 0; row < nRows; row++) {
            final double[] dataRow = data[row];
            double sum = 0;
            for (int i = 0; i < nCols; i++) {
                sum += dataRow[i] * v[i];
            }
            out[row] = sum;
        }
        return out;
    }
