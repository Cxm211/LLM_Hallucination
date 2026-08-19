// ===== FIXED org.apache.commons.math.complex.Complex :: add(Complex) [lines 150-158] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-53-fixed/src/main/java/org/apache/commons/math/complex/Complex.java =====
    public Complex add(Complex rhs)
        throws NullArgumentException {
        MathUtils.checkNotNull(rhs);
        if (isNaN || rhs.isNaN) {
            return NaN;
        }
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }
