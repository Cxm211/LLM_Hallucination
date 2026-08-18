public Complex add(Complex rhs)
        throws NullArgumentException {
        MathUtils.checkNotNull(rhs);
        return new Complex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }