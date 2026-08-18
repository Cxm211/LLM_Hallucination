public Complex add(Complex rhs)
        throws NullArgumentException {
        if (rhs == null) {
            throw new NullArgumentException();
        }
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }