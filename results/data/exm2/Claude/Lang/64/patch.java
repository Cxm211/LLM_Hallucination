public int compareTo(Object other) {
    if (other == null) {
        throw new NullPointerException("Cannot compare to null");
    }
    if (this.getClass() != other.getClass()) {
        throw new ClassCastException("Cannot compare different enum types");
    }
    return iValue - ((ValuedEnum) other).iValue;
}