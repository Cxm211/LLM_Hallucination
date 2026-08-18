public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        // Always rebuild the index after a removal to keep key->index mapping consistent
        rebuildIndex();
    }