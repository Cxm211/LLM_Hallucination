public Object clone() throws CloneNotSupportedException {
    Object clone;
    if (getItemCount() > 0) {
        clone = createCopy(0, getItemCount() - 1);
    } else {
        clone = createCopy(0, 0);
    }
    return clone;
}
