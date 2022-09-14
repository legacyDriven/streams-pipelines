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

public class Collecting {

    public int sum(IntStream intStream) {
        return intStream.reduce(Integer::sum).orElseThrow(NoSuchElementException::new);
    }

    public int production(IntStream intStream) {
        return intStream.reduce(1, (a, b) -> a * b);
    }

    public int oddSum(IntStream intStream) {
        return intStream.filter(s -> s % 2 != 0).sum();
    }

    public Map<Integer, Integer> sumByRemainder(int divisor, IntStream intStream) {
        return intStream
                .boxed()
                .collect(Collectors.groupingBy(s -> s % divisor, Collectors.summingInt(x -> x)));
    }

    public static void main(String[] args) {
        Random random = new Random(654);
        System.out.println(random.nextDouble());
    }

    public Map<Person, Double> totalScores(Stream<CourseResult> results) {
        return results.collect(Collectors.toMap(CourseResult::getPerson,
                this::countAverageCourseResult));
    }

    private double countAverageCourseResult(CourseResult result){
        long res = result.getTaskResults().keySet().stream().filter(n->n.contains(" ")).count();
        if(res>0) {
            return result.getTaskResults().values().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0d);
        } else {
            return Stream.concat(Stream.of(0), result.getTaskResults().values().stream())
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0d);
        }
    }

    public double averageTotalScore(Stream<CourseResult> results) {
        return results.mapToDouble(this::countAverageCourseResult).average().orElse(0d);
    }


    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> results) {

        return new HashMap<>();
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> results) {
        return new HashMap<>();
    }

    public String easiestTask(Stream<CourseResult> results) {
        return "";
    }

    public Collector<CourseResult, ?, String> printableStringCollector() {

        return new Collector<CourseResult, Object, String>() {
            @Override
            public Supplier<Object> supplier() {
                return null;
            }

            @Override
            public BiConsumer<Object, CourseResult> accumulator() {
                return null;
            }

            @Override
            public BinaryOperator<Object> combiner() {
                return null;
            }

            @Override
            public Function<Object, String> finisher() {
                return null;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return null;
            }
        };
    }

}