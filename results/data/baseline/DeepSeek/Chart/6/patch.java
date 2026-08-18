    @Override
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
            if (!ShapeUtilities.equal((Shape) this.get(i), (Shape) that.get(i))) {
                return false;
            }
        }
        return true;
    }