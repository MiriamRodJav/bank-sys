package com.demo.example.mscustomer.infrastructure.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodsUtil {

    public static <T, R> List<R> mapSafe(List<T> input, Function<T, R> mapper) {
        return Optional.ofNullable(input)
                .orElseGet(Collections::emptyList) // List::of también vale (Java 9+)
                .stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
}
