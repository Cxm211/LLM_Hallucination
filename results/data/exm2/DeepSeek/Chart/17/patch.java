public Object clone() throws CloneNotSupportedException {
    int itemCount = getItemCount();
    if (itemCount <= 0) {
        return super.clone();
    } else {
        return createCopy(0, itemCount - 1);
    }
}