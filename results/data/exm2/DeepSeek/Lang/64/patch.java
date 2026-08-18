public int compareTo(Object other) {
    if (this.getClass() != other.getClass()) {
        throw new ClassCastException();
    }
    return iValue - ((ValuedEnum) other).iValue;
}