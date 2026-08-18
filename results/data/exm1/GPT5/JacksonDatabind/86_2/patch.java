public void setReference(JavaType ref)
    {
        if (_referencedType != null) {
            // Allow idempotent re-setting to the same reference
            if (_referencedType != ref && !_referencedType.equals(ref)) {
                throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
            }
            return;
        }
        _referencedType = ref;
    }