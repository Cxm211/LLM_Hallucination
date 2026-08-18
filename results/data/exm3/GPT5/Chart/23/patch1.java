public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MinMaxCategoryRenderer)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        MinMaxCategoryRenderer that = (MinMaxCategoryRenderer) obj;
        if (this.plotLines != that.plotLines) {
            return false;
        }
        if (!java.util.Objects.equals(this.groupPaint, that.groupPaint)) {
            return false;
        }
        if (!java.util.Objects.equals(this.groupStroke, that.groupStroke)) {
            return false;
        }
        return true;
    }