// ===== FIXED org.jfree.chart.renderer.GrayPaintScale :: getPaint(double) [lines 123-129] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-24-fixed/source/org/jfree/chart/renderer/GrayPaintScale.java =====
    public Paint getPaint(double value) {
        double v = Math.max(value, this.lowerBound);
        v = Math.min(v, this.upperBound);
        int g = (int) ((v - this.lowerBound) / (this.upperBound 
                - this.lowerBound) * 255.0);
        return new Color(g, g, g);
    }
