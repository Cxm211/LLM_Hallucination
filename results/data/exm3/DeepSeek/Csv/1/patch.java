    public int read() throws IOException {
        int current = super.read();
        if (current == '\n') {
            lineCounter++;
        } else if (current == '\r') {
            // handle possible \r\n sequence
            super.mark(1);
            int next = super.read();
            if (next != '\n') {
                super.reset();
            }
            lineCounter++;
        }
        lastChar = current;
        return lastChar;
    }