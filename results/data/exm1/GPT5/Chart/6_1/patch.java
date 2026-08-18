public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShapeList)) {
            return false;
        }
        ShapeList that = (ShapeList) obj;
        if (this.size() != that.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            java.awt.Shape s1 = (java.awt.Shape) this.get(i);
            java.awt.Shape s2 = (java.awt.Shape) that.get(i);
            if (!org.jfree.chart.util.ShapeUtilities.equal(s1, s2)) {
                return false;
            }
        }
        return true;

    }