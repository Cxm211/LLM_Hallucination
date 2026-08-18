static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        Calendar endCopy = (Calendar) end.clone(); // Clone to avoid modifying original end
        endCopy.add( field, -1 * difference );
        int endValue = endCopy.get(field);
        int startValue = start.get(field);
        if (endValue < startValue) {
            int newdiff = startValue - endValue;
            endCopy.add( field, newdiff );
            return newdiff;
        } else {
            return 0;
        }
    }