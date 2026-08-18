public boolean evaluate(T object) {
    return equator == null ? iValue.equals(object) : equator.equate(iValue, object);
}