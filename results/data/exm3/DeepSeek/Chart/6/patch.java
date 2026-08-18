    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShapeList)) {
            return false;
        }
        ShapeList that = (ShapeList) obj;
        int count = this.size();
        if (count != that.size()) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            Shape s1 = this.getShape(i);
            Shape s2 = that.getShape(i);
            if (s1 == null) {
                if (s2 != null) {
                    return false;
                }
            } else {
                if (!s1.equals(s2)) {
                    return false;
                }
            }
        }
        return true;
    }