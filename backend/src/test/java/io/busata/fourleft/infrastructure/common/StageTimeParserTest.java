package io.busata.fourleft.infrastructure.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StageTimeParserTest {

    @InjectMocks
    StageTimeParser parser;

    @Test
    public void testParser() {
       final var duration =  parser.createDuration("07:12.490");
        System.out.println(duration);
    }

    @Test
    public void testParserWithHours() {
       final var duration =  parser.createDuration("01:20:05.778");
        System.out.println(duration);
    }
}