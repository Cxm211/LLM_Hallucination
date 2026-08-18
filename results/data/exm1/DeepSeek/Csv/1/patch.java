public int read() throws IOException {
    int current = super.read();
    if (current == '\n') {
        lineCounter++;
    } else if (current == '\r') {
        lineCounter++;
        int next = lookAhead();
        if (next == '\n') {
            super.read();
        }
    }
    lastChar = current;
    return lastChar;
}