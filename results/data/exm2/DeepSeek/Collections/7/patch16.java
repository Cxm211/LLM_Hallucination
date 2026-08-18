    public Object remove(Object key) {
        for (int i = 0; i < keysAsListed.size(); i++) {
            if (keysAsListed.get(i).equals(key)) {
                keysAsListed.remove(i);
                break;
            }
        }
        return super.remove(key);
    }