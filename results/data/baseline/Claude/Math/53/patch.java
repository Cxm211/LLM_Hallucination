public Complex add(Complex rhs)
        throws NullArgumentException {
        MathUtils.checkNotNull(rhs);
        if (isNaN || rhs.isNaN()) {
            return createComplex(Double.NaN, Double.NaN);
        }
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }