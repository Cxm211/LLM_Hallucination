public void setReference(JavaType ref)
    {
        if (_referencedType != null) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
        // Ensure super-type information is available through this recursive placeholder
        // so that calls like getSuperClass() work as expected.
        _superClass = (ref == null) ? null : ref.getSuperClass();
    }