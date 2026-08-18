    public int compareTo(Object other) {
        return Integer.compare(iValue, ((ValuedEnum) other).iValue);
    }