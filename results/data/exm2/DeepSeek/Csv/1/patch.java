    public int read() throws IOException {
        int current = super.read();
        if (current == '\n') {
            lineCounter++;
        } else if (current == '\r') {
            super.mark(1);
            int next = super.read();
            if (next != '\n') {
                lineCounter++;
                super.reset();
            } else {
                super.reset();
            }
        }
        lastChar = current;
        return lastChar;
    }