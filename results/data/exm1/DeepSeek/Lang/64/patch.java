public int compareTo(Object other) {
    if (other == null) {
        throw new NullPointerException();
    }
    if (other == this) {
        return 0;
    }
    // Check that the other is from the same logical enum class (including different classloaders)
    if (!this.getClass().getName().equals(other.getClass().getName())) {
        throw new ClassCastException("Cannot compare to different enum type");
    }
    try {
        java.lang.reflect.Field field = other.getClass().getDeclaredField("iValue");
        field.setAccessible(true);
        int otherValue = field.getInt(other);
        return Integer.compare(iValue, otherValue);
    } catch (Exception e) {
        throw new ClassCastException("Cannot compare to object without iValue field");
    }
}