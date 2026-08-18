public int read() throws IOException {
    int current = super.read();
    if (current == '\n') {
        lineCounter++;
    } else if (current == '\r') {
        lineCounter++;
    }
    lastChar = current;
    return current;
}