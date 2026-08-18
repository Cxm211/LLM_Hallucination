    public Complex add(Complex rhs)
        throws NullArgumentException {
        MathUtils.checkNotNull(rhs);
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }

// trigger testcase
@Test
    public void testAddNaN() {
        Complex x = new Complex(3.0, 4.0);
        Complex z = x.add(Complex.NaN);
        Assert.assertTrue(z.isNaN());
        z = new Complex(1, nan);
        Complex w = x.add(z);
        Assert.assertTrue(Double.isNaN(w.getReal()));
        Assert.assertTrue(Double.isNaN(w.getImaginary()));
    }
