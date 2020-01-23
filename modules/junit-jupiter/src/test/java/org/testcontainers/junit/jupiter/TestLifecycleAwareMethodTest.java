package org.testcontainers.junit.jupiter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.junit.jupiter.TestLifecycleAwareContainerMock.AFTER_TEST;
import static org.testcontainers.junit.jupiter.TestLifecycleAwareContainerMock.BEFORE_TEST;

// The order of @ExtendsWith and @Testcontainers is crucial for the tests
@ExtendWith({TestLifecycleAwareMethodTest.SharedContainerAfterAllTestExtension.class})
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestLifecycleAwareMethodTest {
    @Container
    private final TestLifecycleAwareContainerMock testContainer = new TestLifecycleAwareContainerMock();

    @Container
    private static final TestLifecycleAwareContainerMock SHARED_CONTAINER = new TestLifecycleAwareContainerMock();

    private static TestLifecycleAwareContainerMock startedTestContainer;

    @BeforeAll
    static void beforeAll() {
        assertThat(SHARED_CONTAINER.getLifecycleMethodCalls()).containsExactly(BEFORE_TEST);

    }

    @Test
    @Order(1)
    void should_prepare_before_and_after_test() {
        // we can only test for a call to afterTest() after this test has been finished.
        startedTestContainer = testContainer;
    }

    @Test
    @Order(2)
    void should_call_beforeTest_first_afterTest_later_with_filesystem_friendly_name() {
        assertThat(startedTestContainer.getLifecycleMethodCalls())
            .containsExactly(BEFORE_TEST, AFTER_TEST);
    }

    @Test
    void should_have_a_filesystem_friendly_name_container_has_started() {
        assertThat(startedTestContainer.getLifecycleFilesystemFriendlyNames())
            .containsExactly(
                "org.testcontainers.junit.jupiter.TestLifecycleAwareMethodTest-should_prepare_before_and_after_test"
            );
    }

    @Test
    void static_container_should_have_a_filesystem_friendly_name_after_container_has_started() {
        assertThat(SHARED_CONTAINER.getLifecycleFilesystemFriendlyNames())
            .containsExactly(
                "org.testcontainers.junit.jupiter.TestLifecycleAwareMethodTest-static"
            );
    }

    static class SharedContainerAfterAllTestExtension implements AfterAllCallback {
        // Unfortunately it's not possible to write a @Test that is run after all tests
        @Override
        public void afterAll(ExtensionContext context) {
            assertThat(SHARED_CONTAINER.getLifecycleMethodCalls())
                .containsExactly(BEFORE_TEST, AFTER_TEST);
        }
    }
}
