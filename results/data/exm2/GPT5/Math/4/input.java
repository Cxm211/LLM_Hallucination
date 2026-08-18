    public Vector3D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // compute the intersection on infinite line
        Vector3D v1D = line.intersection(subLine.line);

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

    public Vector2D intersection(final SubLine subLine, final boolean includeEndPoints) {

        // retrieve the underlying lines
        Line line1 = (Line) getHyperplane();
        Line line2 = (Line) subLine.getHyperplane();

        // compute the intersection on infinite line
        Vector2D v2D = line1.intersection(line2);

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

// trigger testcase
@Test
    public void testIntersectionNotIntersecting() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(1.5, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 3, 0), new Vector3D(2, 3, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

@Test
    public void testIntersectionParallel() {
        final SubLine sub1 = new SubLine(new Vector2D(0, 1), new Vector2D(0, 2));
        final SubLine sub2 = new SubLine(new Vector2D(66, 3), new Vector2D(66, 4));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }
