package com.interrupt.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonUtilTest {
    @Test
    void shouldSerializeObject() {
        class TestClassFixture {
            public boolean propertyA = true;
            private String propertyB = "Hello World";
            private transient int propertyC = 10;
        }

        TestClassFixture object = new TestClassFixture();

        String actual = JsonUtil.toJson(object);
        String expected = "{\n\"propertyA\": true,\n\"propertyB\": \"Hello World\"\n}";

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideShouldDeserializeObject")
    void shouldDeserializeObject(String json, String propertyA, String propertyB, String propertyC) {
        TestClassDeserializationFixture actual = JsonUtil.fromJson(TestClassDeserializationFixture.class, json);

        assertEquals(propertyA, actual.propertyA);
        assertEquals(propertyB, actual.propertyB);
        assertEquals(propertyC, actual.propertyC);
    }

    static Stream<Arguments> provideShouldDeserializeObject() {
        return Stream.of(
            Arguments.of("{\"propertyB\":\"Spam Eggs\"}", null, "Spam Eggs", "Foo Bar"),
            Arguments.of("{\"class\":\"com.interrupt.utils.TestClassDeserializationFixture\",\"propertyB\":\"Spam Eggs\"}", null, "Spam Eggs", "Foo Bar"),
            Arguments.of("{\"propertyD\":true}", null, "Hello World", "Foo Bar")
        );
    }
}

class TestClassDeserializationFixture {
    public String propertyA;
    public String propertyB = "Hello World";
    public String propertyC = "Foo Bar";

    public TestClassDeserializationFixture() {
    }
}
