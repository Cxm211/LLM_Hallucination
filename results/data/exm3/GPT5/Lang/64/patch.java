public int compareTo(Object other) {
        if (other == this) {
            return 0;
        }
        if (other == null) {
            throw new NullPointerException("The argument cannot be null");
        }
        if (other.getClass() != this.getClass()) {
            throw new ClassCastException("Different enum class");
        }
        ValuedEnum enumOther = (ValuedEnum) other;
        return this.iValue - enumOther.iValue;
    }