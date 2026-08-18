public void setInclude(String inc) {
    if (inc != null && inc.equals("")) {
        include = null;
    } else {
        include = inc;
    }
}