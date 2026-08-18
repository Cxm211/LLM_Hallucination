    public void setReference(JavaType ref)
    {
        // sanity check; should not be called multiple times
        if (_referencedType != null) {
            // 16-Oct-2015, tatu: Should we allow re-setting? For now, yes, but only if same
            if (_referencedType != ref) {
                throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
            }
            // but if same, just return
            return;
        }
        _referencedType = ref;
    }