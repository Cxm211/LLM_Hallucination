static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
    Calendar temp = (Calendar) start.clone();
    int count = 0;
    while (temp.before(end)) {
        temp.add(field, 1);
        count++;
    }
    if (temp.after(end)) {
        count--;
    }
    return count;
}