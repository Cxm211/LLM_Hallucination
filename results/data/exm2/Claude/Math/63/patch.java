public static boolean equals(double x, double y) {
    return equals(x, y, 1);
}

public static boolean equals(double x, double y, int maxUlps) {
    return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
}