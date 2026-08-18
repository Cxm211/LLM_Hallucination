public static boolean equals(double x, double y) {
        if (Double.isNaN(x) && Double.isNaN(y)) {
            return true;
        }
        // Distinguish +0.0 and -0.0 to keep consistency with Double.hashCode/equals
        if (x == y) {
            return Double.doubleToLongBits(x) == Double.doubleToLongBits(y);
        }
        return false;
    }