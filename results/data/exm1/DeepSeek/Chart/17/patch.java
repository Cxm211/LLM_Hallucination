public Object clone() throws CloneNotSupportedException {
    if (getItemCount() == 0) {
        return new TimeSeries(getKey());
    }
    return createCopy(0, getItemCount() - 1);
}