    public Object clone() throws CloneNotSupportedException {
        if (getItemCount() == 0) {
            return new TimeSeries(getName());
        } else {
            return createCopy(0, getItemCount() - 1);
        }
    }