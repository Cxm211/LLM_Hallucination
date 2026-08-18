// ===== FIXED org.apache.commons.math3.geometry.euclidean.threed.Line :: revert() [lines 86-90] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-9-fixed/src/main/java/org/apache/commons/math3/geometry/euclidean/threed/Line.java =====
    public Line revert() {
        final Line reverted = new Line(this);
        reverted.direction = reverted.direction.negate();
        return reverted;
    }
