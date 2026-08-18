public Paint getPaint(double value) {
    double range = this.upperBound - this.lowerBound;
    if (range <= 0.0) {
        return new Color(0, 0, 0);
    }
    double v = Math.max(value, this.lowerBound);
    v = Math.min(v, this.upperBound);
    int g;
    if (v >= this.upperBound) {
        g = 255;
    } else if (v <= this.lowerBound) {
        g = 0;
    } else {
        g = (int) ((v - this.lowerBound) / range * 255.0);
    }
    return new Color(g, g, g);
}