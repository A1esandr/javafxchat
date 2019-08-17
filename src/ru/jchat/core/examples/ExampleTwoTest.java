package ru.jchat.core.examples;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExampleTwoTest {
    private ExampleTwo exampleTwo;

    @Before
    public void init() {
        exampleTwo = new ExampleTwo();
    }

    @Test
    public void testGet() {
        int[] in = {1,1,4,4,1,4};
        Assert.assertTrue(exampleTwo.get(in));
    }

    @Test
    public void testGetFour() {
        int[] in = {4,4,4};
        Assert.assertFalse(exampleTwo.get(in));
    }

    @Test
    public void testGetOne() {
        int[] in = {1,1,1};
        Assert.assertFalse(exampleTwo.get(in));
    }

    @Test
    public void testGetOther() {
        int[] in = {1,2,4,1,1};
        Assert.assertFalse(exampleTwo.get(in));
    }

    @Test
    public void testGetEmpty() {
        int[] in = {};
        Assert.assertFalse(exampleTwo.get(in));
    }
}
