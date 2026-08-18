public JavaType resolveSubType(JavaType baseType, String subClass)
        throws JsonMappingException
    {
        if (subClass.indexOf('<') > 0) {
            JavaType t = getTypeFactory().constructFromCanonical(subClass);
            if (t.isTypeOrSubTypeOf(baseType.getRawClass())) {
                return t;
            }
        } else {
            Class<?> cls;
            try {
                cls =  getTypeFactory().findClass(subClass);
            } catch (ClassNotFoundException e) {
                return null;
            } catch (Exception e) {
                String msg = e.getMessage();
                throw invalidTypeIdException(baseType, subClass, String.format(
                        "problem: (%s) %s",
                        e.getClass().getName(),
                        (msg == null) ? "N/A" : msg));
            }
            if (baseType.isTypeOrSuperTypeOf(cls)) {
                return getTypeFactory().constructSpecializedType(baseType, cls);
            }
        }
        throw invalidTypeIdException(baseType, subClass, "Not a subtype");
    }