public int compareTo(Object other) {
        if (other == this) {
            return 0;
        }
        if (other == null) {
            throw new NullPointerException("The object to compare to must not be null");
        }
        if (!(other instanceof ValuedEnum)) {
            throw new ClassCastException("Object is not a ValuedEnum");
        }
        ValuedEnum o = (ValuedEnum) other;
        Class<?> thisClass = getClass();
        Class<?> otherClass = o.getClass();
        if (thisClass != otherClass && !thisClass.getName().equals(otherClass.getName())) {
            throw new ClassCastException("Different enum classes");
        }
        if (this.iValue == o.iValue) {
            return 0;
        }
        return this.iValue < o.iValue ? -1 : 1;
    }