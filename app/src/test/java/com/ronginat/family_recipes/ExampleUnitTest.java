package com.ronginat.family_recipes;

import android.util.Log;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void parseListTest() {
        Gson gson = new Gson();
        List<String> list = Stream.of("cat1", "cat2", "cat3")
                .collect(Collectors.toList());

        String json = gson.toJson(list);
        assertEquals(json, "[\"cat1\",\"cat2\",\"cat3\"]");
    }
}