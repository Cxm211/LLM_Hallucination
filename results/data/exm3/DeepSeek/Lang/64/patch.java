    public int compareTo(Object other) {
        if (other == null) {
            throw new NullPointerException();
        }
        if (getClass() != other.getClass()) {
            throw new ClassCastException();
        }
        return iValue - ((ValuedEnum) other).iValue;
    }