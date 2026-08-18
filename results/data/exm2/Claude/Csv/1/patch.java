public int read() throws IOException {
    int current = super.read();
    if (current == '\r') {
        lineCounter++;
    }
    if (current == '\n' && lastChar != '\r') {
        lineCounter++;
    }
    lastChar = current;
    return lastChar;
}