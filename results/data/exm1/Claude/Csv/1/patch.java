public int read() throws IOException {
    int current = super.read();
    if (current == '\r' || current == '\n') {
        if (current == '\r') {
            int next = super.read();
            if (next == '\n') {
                lastChar = next;
                lineCounter++;
                return lastChar;
            } else {
                if (next != -1) {
                    ((java.io.PushbackReader) in).unread(next);
                }
                lineCounter++;
                lastChar = current;
                return lastChar;
            }
        } else {
            lineCounter++;
        }
    }
    lastChar = current;
    return lastChar;
}