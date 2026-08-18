    public void close() throws IOException {
        if (!closed) {
            closed = true;
            try {
                finish();
            } finally {
                try {
                    buffer.close();
                } finally {
                    out.close();
                }
            }
        }
    }