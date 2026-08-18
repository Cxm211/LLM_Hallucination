public Object clone() throws CloneNotSupportedException {
    if (getItemCount() == 0) {
        return createCopy(0, getItemCount() - 1);
    }
    Object clone = createCopy(0, getItemCount() - 1);
    return clone;
}