public String setValue(String val) {
        String oldVal = this.val;
        if (parent != null) {
            int i = parent.indexOfKey(this.key);
            if (i != Attributes.NotFound) {
                oldVal = parent.vals[i];
                parent.vals[i] = val;
            }
        }
        this.val = val;
        return Attributes.checkNotNull(oldVal);
    }