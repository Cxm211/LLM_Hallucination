    void setInitialBuffer(byte[] out, int outPos, int outAvail) {
        if (out != null && out.length - outPos >= outAvail) {
            buffer = out;
            pos = outPos;
            readPos = outPos;
        } else {
            buffer = null;
        }
    }