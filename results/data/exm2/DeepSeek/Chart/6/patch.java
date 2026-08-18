    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShapeList)) {
            return false;
        }
        ShapeList other = (ShapeList) obj;
        int count = getShapeCount();
        if (other.getShapeCount() != count) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            Shape s1 = getShape(i);
            Shape s2 = other.getShape(i);
            if (s1 == null) {
                if (s2 != null) {
                    return false;
                }
            } else if (!s1.equals(s2)) {
                return false;
            }
        }
        return true;
    }