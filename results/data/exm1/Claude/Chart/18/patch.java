public void removeValue(int index) {
    this.keys.remove(index);
    this.values.remove(index);
    if (this.keys.size() > 0) {
        rebuildIndex();
    }
}