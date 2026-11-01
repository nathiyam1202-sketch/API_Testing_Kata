package com.booking.hooks;

import com.booking.context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class TestContextHooks {
    @Before
    public void initializeScenarioContext() {
        TestContext.startScenario();
    }

    @After
    public void clearScenarioContext() {
        TestContext.clear();
    }
}
