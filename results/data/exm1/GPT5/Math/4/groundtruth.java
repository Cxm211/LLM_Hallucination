// ===== FIXED org.apache.commons.math3.geometry.euclidean.threed.SubLine :: intersection(SubLine, boolean) [lines 110-130] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-4-fixed/src/main/java/org/apache/commons/math3/geometry/euclidean/threed/SubLine.java =====
    public Vector3D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // compute the intersection on infinite line
        Vector3D v1D = line.intersection(subLine.line);
        if (v1D == null) {
            return null;
        }

        // check location of point with respect to first sub-line
        Location loc1 = remainingRegion.checkPoint(line.toSubSpace(v1D));

        // check location of point with respect to second sub-line
        Location loc2 = subLine.remainingRegion.checkPoint(subLine.line.toSubSpace(v1D));

        if (includeEndPoints) {
            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v1D : null;
        } else {
            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v1D : null;
        }

    }

// ===== FIXED org.apache.commons.math3.geometry.euclidean.twod.SubLine :: intersection(SubLine, boolean) [lines 110-134] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-4-fixed/src/main/java/org/apache/commons/math3/geometry/euclidean/twod/SubLine.java =====
    public Vector2D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // retrieve the underlying lines
        Line line1 = (Line) getHyperplane();
        Line line2 = (Line) subLine.getHyperplane();

        // compute the intersection on infinite line
        Vector2D v2D = line1.intersection(line2);
        if (v2D == null) {
            return null;
        }

        // check location of point with respect to first sub-line
        Location loc1 = getRemainingRegion().checkPoint(line1.toSubSpace(v2D));

        // check location of point with respect to second sub-line
        Location loc2 = subLine.getRemainingRegion().checkPoint(line2.toSubSpace(v2D));

        if (includeEndPoints) {
            return ((loc1 != Location.OUTSIDE) && (loc2 != Location.OUTSIDE)) ? v2D : null;
        } else {
            return ((loc1 == Location.INSIDE) && (loc2 == Location.INSIDE)) ? v2D : null;
        }

    }
