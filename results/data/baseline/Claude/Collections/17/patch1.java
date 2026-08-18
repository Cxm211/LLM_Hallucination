public boolean evaluate(T object) {
    if (equator != null) {
        return equator.equate(iValue, object);
    }
    return (iValue == object) || (iValue != null && iValue.equals(object));
}