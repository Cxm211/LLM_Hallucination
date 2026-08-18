public int compareTo(Object other) {
        if (other == null) {
            throw new NullPointerException("The other enum must not be null");
        }
        if (!(other instanceof ValuedEnum)) {
            throw new ClassCastException("The other object is not a ValuedEnum");
        }
        ValuedEnum otherEnum = (ValuedEnum) other;
        if (!this.getClass().getName().equals(otherEnum.getClass().getName())) {
            throw new ClassCastException("Different enum classes");
        }
        return this.iValue - otherEnum.iValue;
    }