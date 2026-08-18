public boolean evaluate(T object) {
            return (iValue == null) ? (object == null) : iValue.equals(object);
    }