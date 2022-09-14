package com.efimchick.ifmo;


import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CollectingBad {

    public int sum(IntStream intStream) {
        return intStream.reduce(Integer::sum).orElseThrow(NoSuchElementException::new);
    }

    public int production(IntStream intStream) {
        return intStream.reduce(1, (a, b) -> a * b);
    }

    public int oddSum(IntStream intStream) {
        return intStream.filter(s -> s%2!=0).sum();
    }

    public Map<Integer, Integer> sumByRemainder(int divisor, IntStream intStream) {
        return intStream
                .boxed()
                .collect(Collectors.groupingBy(s -> s % divisor, Collectors.summingInt(x -> x)));
    }
    ////////////////////////////////////////

    public Map<Person, Double> totalScores(Stream<CourseResult> results) {
        return results.collect(Collectors.toMap
                (CourseResult::getPerson,
                        x -> {
                            boolean areProgramTasks = x.getTaskResults().keySet().stream().allMatch(key -> key.startsWith("Lab "));
                            if (areProgramTasks) {
                                return programmingAvg(x);
                            } else
                                return historyAvg(x);
                        }
                ));
    }

    public double programmingAvg(CourseResult courseResult) {
        return courseResult.getTaskResults().values().stream().collect(Collectors.summarizingInt(Integer::intValue)).getAverage();
    }

    public double historyAvg(CourseResult courseResult) {
        if (courseResult.getTaskResults().size()==4)
            return programmingAvg(courseResult);
        else
            return Stream.concat(Stream.of(0), courseResult.getTaskResults().values().stream()).collect(Collectors.summarizingInt(Integer::intValue)).getAverage();
    }



    private boolean isProgResults(Stream<CourseResult> resultStream){
        return resultStream.map(x-> x.getTaskResults()
                        .keySet())
                .anyMatch(x -> x.contains("Lab "));
    }


    private double programmingAverage(CourseResult courseResult){
        return courseResult.getTaskResults().values().stream()
                .mapToDouble(Integer::doubleValue)
                .average().orElse(0.0);
    }

    public double averageTotalScore(Stream<CourseResult> results) {
        return results.mapToDouble(
                x -> {
                    boolean areProgramTasks = x.getTaskResults().keySet().stream().allMatch(key -> key.startsWith("Lab "));
                    if (areProgramTasks) {
                        return programmingAvg(x);
                    } else
                        return historyAvg(x);
                }).average().orElse(0);
    }

    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> results) {
        return results.map(CourseResult::getTaskResults)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)),
                        map -> map.entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue() / 3.))
                ));
    }

    private String mark(double avg){
        if (avg > 90) return "A";
        if (avg >= 83) return "B";
        if (avg >= 75) return "C";
        if (avg >= 68) return "D";
        if (avg >= 60) return "E";
        else return "F";
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> results) {
        return results.collect(
                Collectors.toMap(
                        CourseResult::getPerson,
                        x -> {
                            double avg;
                            if (areProgramming(x)) {
                                avg = x.getTaskResults().values().stream()
                                        .collect(Collectors.summarizingInt(Integer::intValue))
                                        .getAverage();
                            } else {
                                avg = Stream.concat(Stream.of(0), x.getTaskResults().values().stream())
                                        .collect(Collectors.summarizingInt(Integer::intValue))
                                        .getAverage();
                            }
                            return mark(avg);
                        }));}

    private boolean areProgramming(CourseResult courseResult){
        return courseResult.getTaskResults().keySet().stream().allMatch(key -> key.startsWith("Lab "));
    }

    public String easiestTask(Stream<CourseResult> results) {
        return averageScoresPerTask(results).entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No matching tasks found");
    }

    public Collector<CourseResult, ?, String> printableStringCollector() {
        return new Collector<CourseResult, List<CourseResult>, String>() {
            @Override
            public Supplier<List<CourseResult>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<CourseResult>, CourseResult> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<CourseResult>> combiner() {
                return null;
            }

            @Override
            public Function<List<CourseResult>, String> finisher() {

                return courseResults -> {
                    String tasks;
                    Map<String,Integer> programTasks = new TreeMap<>() {{
                        put("Lab 1. Figures", "Lab 1. Figures".length());
                        put("Lab 2. War and Peace","Lab 2. War and Peace".length());
                        put("Lab 3. File Tree","Lab 3. File Tree".length());
                    }};

                    Map<String,Integer> historyTasks = new TreeMap<>() {{
                        put("Phalanxing", "Phalanxing".length());
                        put("Shieldwalling","Shieldwalling".length());
                        put("Tercioing","Tercioing".length());
                        put("Wedging","Wedging".length());
                    }};

                    Map<String,Integer> defaultTaskScores = new HashMap<>(){{
                        put("Phalanxing", 0);
                        put("Shieldwalling",0);
                        put("Tercioing",0);
                        put("Wedging",0);
                    }};

                    List<CourseResult> courseResultList = courseResults.stream().map(c -> new CourseResult(c.getPerson(),c.getTaskResults())).collect(Collectors.toList());

                    if (areProgramming(courseResults.get(0)))
                        tasks= String.join(" | ", programTasks.keySet());
                    else {
                        tasks = String.join(" | ", historyTasks.keySet());
                        for (CourseResult c: courseResultList
                        ) {
                            defaultTaskScores.keySet().forEach(k -> c.getTaskResults().putIfAbsent(k, 0));
                        }
                    }

                    double averageTotalScore = averageTotalScore(Stream.of(courseResults.toArray(new CourseResult[0])));

                    Person longestName= courseResults.stream().max((e1,e2)->
                                    e1.getPerson().getFirstName().length()+e1.getPerson().getLastName().length() > e2.getPerson().getFirstName().length()+e2.getPerson().getLastName().length() ? 1:-1)
                            .get().getPerson();
                    int nameLength = longestName.getFirstName().length()+longestName.getLastName().length();

                    String summary="\n"+String.format(Locale.US,"%-"+(nameLength+1)+"s","Average")+" | "+averageScoresPerTask(Stream.of(courseResults.toArray
                            (new CourseResult[0]))).entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(t->String.format(Locale.US,"%"+t.getKey().length()+".2f",Double.valueOf(String.format(Locale.US,"%.2f", t.getValue())))).collect(Collectors.joining(" | "))
                            +" | "+String.format(Locale.US,"%.2f",averageTotalScore) +" |    "+mark(averageTotalScore)+" |";

                    return String.format(Locale.US,"%-"+(nameLength+1)+"s","Student")+" | "+tasks+" | Total | Mark |\n"+courseResultList.stream()
                            .sorted(Comparator.comparing(p -> p.getPerson().getLastName()))
                            .map(D -> D.getPerson().getLastName() + " " + String.format(Locale.US,"%-"+(nameLength-D.getPerson().getLastName().length())+"s",D.getPerson().getFirstName()) + " | "+
                                    D.getTaskResults().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->String.format(Locale.US,"%"+e.getKey().length()+"s",e.getValue().toString())).collect(Collectors.joining(" | "))+" | "+
                                    totalScores(Stream.of(D)).values().stream().map(aDouble -> String.format(Locale.US,"%.2f", aDouble)).collect(Collectors.joining())+" |    "+
                                    String.join("", defineMarks(Stream.of(D)).values()) +" |")
                            .collect(Collectors.joining("\n"))+summary;

                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.UNORDERED);
            }
        };
    }
}

