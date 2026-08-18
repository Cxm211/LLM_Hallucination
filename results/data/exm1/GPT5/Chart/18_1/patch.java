public void removeValue(int index) {
        if (index < 0 || index >= this.keys.size()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        this.keys.remove(index);
        this.values.remove(index);
        rebuildIndex();
    }