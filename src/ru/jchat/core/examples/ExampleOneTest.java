package ru.jchat.core.examples;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExampleOneTest {

    private ExampleOne exampleOne;

    @Before
    public void init() {
        exampleOne = new ExampleOne();
    }

    @Test
    public void testGet() {
        int[] in = {2,5,4,4,1,4,2,8,9};
        int[] out = {2,8,9};
        Assert.assertArrayEquals(out, exampleOne.get(in));
    }

    @Test
    public void testGetEmpty() {
        int[] in = {2,5,4,4,1,4};
        int[] out = {};
        Assert.assertArrayEquals(out, exampleOne.get(in));
    }

    @Test(expected = RuntimeException.class)
    public void testGetExc() {
        int[] in = {2,5};
        exampleOne.get(in);
    }

    @Test(expected = RuntimeException.class)
    public void testGetEmptyInExc() {
        int[] in = {};
        exampleOne.get(in);
    }

}
