    private void init() {
        thisYear= Calendar.getInstance(timeZone, locale).get(Calendar.YEAR);

        nameValues= new ConcurrentHashMap<Integer, KeyValue[]>();

        StringBuilder regex= new StringBuilder();
        List<Strategy> collector = new ArrayList<Strategy>();

        Matcher patternMatcher= formatPattern.matcher(pattern);
        if(!patternMatcher.lookingAt()) {
            throw new IllegalArgumentException("Invalid pattern");
        }

        currentFormatField= patternMatcher.group();
        Strategy currentStrategy= getStrategy(currentFormatField);
        while(true) {
            patternMatcher.region(patternMatcher.end(), patternMatcher.regionEnd());
            if(!patternMatcher.lookingAt()) {
                if(currentStrategy.addRegex(this, regex)) {
                    collector.add(currentStrategy);
                }
                break;
            }
            String nextFormatField= patternMatcher.group();
            Strategy nextStrategy = getStrategy(nextFormatField);
            if(currentStrategy.addRegex(this, regex)) {
                collector.add(currentStrategy);
            }
            currentFormatField= nextFormatField;
            currentStrategy= nextStrategy;
        }
        currentFormatField= null;
        strategies= collector.toArray(new Strategy[collector.size()]);
        parsePattern= Pattern.compile(regex.toString());
    }