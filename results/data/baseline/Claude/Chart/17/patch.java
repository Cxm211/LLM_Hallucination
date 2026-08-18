public Object clone() throws CloneNotSupportedException {
    TimeSeries clone = (TimeSeries) super.clone();
    if (this.data != null) {
        clone.data = (List) ObjectUtilities.deepClone(this.data);
    }
    return clone;
}