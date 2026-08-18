// buggy function
    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        if (this.runningState == STATE_RUNNING) {
            stopTime = System.currentTimeMillis();
        }
        // If SUSPENDED, stopTime was set when suspending; keep it
        this.runningState = STATE_STOPPED;
    }