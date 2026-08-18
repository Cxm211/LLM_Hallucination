public int compareTo(Object other) {
    if (this.getClass() != other.getClass()) {
        throw new ClassCastException("Different enum types may not be compared");
    }
    return iValue - ((ValuedEnum) other).iValue;
}