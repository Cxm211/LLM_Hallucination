// buggy code
    public Timer(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

