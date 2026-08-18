public void clearProperty(String key) {
    if (containsKey(key)) {
        for (int i = 0; i < keysAsListed.size(); i++) {
            if ((keysAsListed.get(i)).equals(key)) {
                keysAsListed.remove(i);
                break;
            }
        }
        remove(key);
    }
}