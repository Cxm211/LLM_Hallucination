// ===== FIXED org.apache.commons.math.linear.CholeskyDecompositionImpl :: CholeskyDecompositionImpl [lines 71-76] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-86-fixed/src/java/org/apache/commons/math/linear/CholeskyDecompositionImpl.java =====
    public CholeskyDecompositionImpl(final RealMatrix matrix)
        throws NonSquareMatrixException,
               NotSymmetricMatrixException, NotPositiveDefiniteMatrixException {
        this(matrix, DEFAULT_RELATIVE_SYMMETRY_THRESHOLD,
             DEFAULT_ABSOLUTE_POSITIVITY_THRESHOLD);
    }
