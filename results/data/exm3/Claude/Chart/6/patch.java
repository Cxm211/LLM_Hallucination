public boolean equals(Object obj) {

    if (obj == this) {
        return true;
    }
    if (!(obj instanceof ShapeList)) {
        return false;
    }
    ShapeList that = (ShapeList) obj;
    int listSize = size();
    if (listSize != that.size()) {
        return false;
    }
    for (int i = 0; i < listSize; i++) {
        Shape shape1 = getShape(i);
        Shape shape2 = that.getShape(i);
        if (shape1 == null) {
            if (shape2 != null) {
                return false;
            }
        } else {
            if (shape2 == null) {
                return false;
            }
            if (!ShapeUtilities.equal(shape1, shape2)) {
                return false;
            }
        }
    }
    return true;
}