public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;

        this.isNaN = Double.isNaN(real) || Double.isNaN(imaginary);
        this.isInfinite = !this.isNaN &&
            (Double.isInfinite(real) || Double.isInfinite(imaginary));
    }