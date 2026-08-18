public Object clone() throws CloneNotSupportedException {
        int count = getItemCount();
        if (count <= 0) {
            // For empty series, avoid createCopy() with an invalid range and ensure independence
            TimeSeries copy = (TimeSeries) super.clone();
            copy.data = new java.util.ArrayList();
            return copy;
        }
        return createCopy(0, count - 1);
    }