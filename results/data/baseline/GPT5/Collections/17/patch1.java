public boolean evaluate(T object) {
            if (equator == null) {
                return iValue == null ? object == null : iValue.equals(object);
            }
            return equator.equate(iValue, object);
    }