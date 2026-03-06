package com.lmh.coverage;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.lmh.coverage.utils.myBasicOperations.OuntputNonMatchingString;

@SpringBootTest
@Slf4j
class CoverageApplicationTests {


    @Test
    void CEPRex() {

        String regex = "([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}";

        OuntputNonMatchingString(regex);

    }



}
