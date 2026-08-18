public boolean isAnnotationBundle(Annotation ann) {
        if (ann == null) {
            return false;
        }
        return _isBundleType(ann.annotationType(), new java.util.HashSet<Class<?>>());
    }

    private boolean _isBundleType(Class<? extends Annotation> annType, java.util.Set<Class<?>> seen) {
        if (annType == null || !seen.add(annType)) {
            return false;
        }
        if (annType.getAnnotation(JacksonAnnotationsInside.class) != null) {
            return true;
        }
        for (Annotation meta : annType.getAnnotations()) {
            Class<? extends Annotation> metaType = meta.annotationType();
            if (metaType != annType && _isBundleType(metaType, seen)) {
                return true;
            }
        }
        return false;
    }