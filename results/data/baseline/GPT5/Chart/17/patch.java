public Object clone() throws CloneNotSupportedException {
        int count = getItemCount();
        if (count <= 0) {
            return super.clone();
        }
        Object clone = createCopy(0, count - 1);
        return clone;
    }