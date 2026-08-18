public Object clone() throws CloneNotSupportedException {
    TimeSeries clone = (TimeSeries) super.clone();
    if (this.data != null) {
        clone.data = (List) ObjectUtilities.deepClone(this.data);
    }
    if (this.timePeriodClass != null) {
        clone.timePeriodClass = this.timePeriodClass;
    }
    if (this.domain != null) {
        clone.domain = (Range) this.domain.clone();
    }
    if (this.range != null) {
        clone.range = (Range) this.range.clone();
    }
    clone.maximumItemAge = this.maximumItemAge;
    clone.maximumItemCount = this.maximumItemCount;
    return clone;
}