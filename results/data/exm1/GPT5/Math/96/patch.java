public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof Complex)) {
            return false;
        }
        Complex rhs = (Complex) other;
        if (rhs.isNaN()) {
            return this.isNaN();
        }
        return (this.real == rhs.getReal()) && (this.imaginary == rhs.getImaginary());
    }