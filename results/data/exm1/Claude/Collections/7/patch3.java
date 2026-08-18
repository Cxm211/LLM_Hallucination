public void clearProperty(String key) {
    if (containsKey(key)) {
        // we also need to rebuild the keysAsListed or else
        // things get *very* confusing
        for (int i = 0; i < keysAsListed.size(); i++) {
            if (( keysAsListed.get(i)).equals(key)) {
                keysAsListed.remove(i);
                break;
            }
        }
        remove(key);
    }
}