public void setReference(JavaType ref)
    {
        if (ref == null) {
            return;
        }
        if (_referencedType != null) {
            if (!_referencedType.equals(ref)) {
                throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
            }
            return;
        }
        _referencedType = ref;
    }