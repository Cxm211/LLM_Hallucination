// buggy code
    public int compareTo(Object other) {
        return iValue - ((ValuedEnum) other).iValue;
    }

