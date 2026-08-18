    public Complex add(Complex rhs)
        throws NullArgumentException {
        MathUtils.checkNotNull(rhs);
        double sumReal = real + rhs.getReal();
        double sumImag = imaginary + rhs.getImaginary();
        if (Double.isNaN(sumReal) || Double.isNaN(sumImag)) {
            return Complex.NaN;
        }
        return createComplex(sumReal, sumImag);
    }