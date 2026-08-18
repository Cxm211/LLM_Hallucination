public int compareTo(Object other) {
    if (other == null || other.getClass() != this.getClass()) {
        throw new ClassCastException();
    }
    return iValue - ((ValuedEnum) other).iValue;
}