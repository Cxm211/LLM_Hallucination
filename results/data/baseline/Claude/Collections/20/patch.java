public void remove() {
    checkModCount();
    if (currentIndex == -1) {
        throw new IllegalStateException();
    }
    if (nextIndex == currentIndex) {
        next = next.next();
    }
    parent.remove(currentIndex);
    nextIndex--;
    current = null;
    currentIndex = -1;
    expectedModCount++;
}