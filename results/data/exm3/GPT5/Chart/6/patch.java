public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShapeList)) {
            return false;
        }
        ShapeList that = (ShapeList) obj;
        int countThis = this.getItemCount();
        if (countThis != that.getItemCount()) {
            return false;
        }
        for (int i = 0; i < countThis; i++) {
            java.awt.Shape s1 = this.getShape(i);
            java.awt.Shape s2 = that.getShape(i);
            if (s1 == s2) {
                continue;
            }
            if (s1 == null || s2 == null) {
                return false;
            }
            if (s1 instanceof java.awt.geom.Line2D && s2 instanceof java.awt.geom.Line2D) {
                java.awt.geom.Line2D l1 = (java.awt.geom.Line2D) s1;
                java.awt.geom.Line2D l2 = (java.awt.geom.Line2D) s2;
                if (l1.getX1() != l2.getX1() || l1.getY1() != l2.getY1()
                        || l1.getX2() != l2.getX2() || l1.getY2() != l2.getY2()) {
                    return false;
                }
                continue;
            }
            if (s1 instanceof java.awt.geom.Rectangle2D && s2 instanceof java.awt.geom.Rectangle2D) {
                java.awt.geom.Rectangle2D r1 = (java.awt.geom.Rectangle2D) s1;
                java.awt.geom.Rectangle2D r2 = (java.awt.geom.Rectangle2D) s2;
                if (r1.getX() != r2.getX() || r1.getY() != r2.getY()
                        || r1.getWidth() != r2.getWidth() || r1.getHeight() != r2.getHeight()) {
                    return false;
                }
                continue;
            }
            if (!s1.equals(s2)) {
                return false;
            }
        }
        return true;
    }