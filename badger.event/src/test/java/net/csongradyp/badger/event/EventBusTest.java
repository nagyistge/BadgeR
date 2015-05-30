package net.csongradyp.badger.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.csongradyp.badger.IAchievementController;
import net.csongradyp.badger.event.handler.wrapper.AchievementUnlockedHandlerWrapper;
import net.csongradyp.badger.event.handler.wrapper.ScoreUpdateHandlerWrapper;
import net.csongradyp.badger.event.message.AchievementUnlockedEvent;
import net.csongradyp.badger.event.message.ScoreUpdatedEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EventBusTest {

    @Mock
    private IAchievementController mockController;
    private AchievementUnlockedHandlerWrapper unlockedHandlerWrapper;
    private ScoreUpdateHandlerWrapper scoreUpdateHandlerWrapper;
    private AchievementUnlockedEvent receivedAchievementUnlockedEvent;
    private ScoreUpdatedEvent receivedScoreUpdatedEvent;

    @Before
    public void setUp()  {
        unlockedHandlerWrapper = new AchievementUnlockedHandlerWrapper(achievement -> receivedAchievementUnlockedEvent = achievement);
        EventBus.subscribeOnUnlock(unlockedHandlerWrapper);
        scoreUpdateHandlerWrapper = new ScoreUpdateHandlerWrapper(score -> receivedScoreUpdatedEvent = score);
        EventBus.subscribeOnScoreChanged(scoreUpdateHandlerWrapper);
        EventBus.setController(mockController);
    }

    @After
    public void tearDown()  {
        EventBus.unSubscribeOnUnlock(unlockedHandlerWrapper.getWrapped());
        EventBus.unSubscribeOnScoreChanged(scoreUpdateHandlerWrapper.getWrapped());
        receivedAchievementUnlockedEvent = null;
    }

    @Test
    public void testOnUnlockedEventSubscriptions() throws Exception {
        final  AchievementUnlockedHandlerWrapper handlerWrapper = new AchievementUnlockedHandlerWrapper(achievement -> receivedAchievementUnlockedEvent = achievement);
        EventBus.subscribeOnUnlock(handlerWrapper);
        assertThat(EventBus.getUnlockedSubscribers().size(), is(equalTo(2)));
        assertThat(EventBus.getUnlockedSubscribers().contains(handlerWrapper), is(true));

        EventBus.unSubscribeOnUnlock(handlerWrapper.getWrapped());
        assertThat(EventBus.getUnlockedSubscribers().size(), is(equalTo(1)));
        assertThat(EventBus.getUnlockedSubscribers().contains(handlerWrapper), is(false));
    }

    @Test
    public void testOnScoreChangedEventSubscriptions() throws Exception {
        final ScoreUpdateHandlerWrapper handlerWrapper = new ScoreUpdateHandlerWrapper(score -> receivedScoreUpdatedEvent = score);
        EventBus.subscribeOnScoreChanged(handlerWrapper);
        assertThat(EventBus.getScoreUpdateSubscribers().size(), is(equalTo(2)));
        assertThat(EventBus.getScoreUpdateSubscribers().contains(handlerWrapper), is(true));

        EventBus.unSubscribeOnScoreChanged(handlerWrapper.getWrapped());
        assertThat(EventBus.getScoreUpdateSubscribers().size(), is(equalTo(1)));
        assertThat(EventBus.getScoreUpdateSubscribers().contains(handlerWrapper), is(false));
    }

    @Test
    public void testPublishUnlockedPublishesGivenEvent() throws Exception {
        final String id = "test";
        final String title = "title";
        final String text = "text";
        final String score = "value";
        final int level = 3;
        final AchievementUnlockedEvent achievementUnlockedEvent = new AchievementUnlockedEvent(id, title, text, score);
        achievementUnlockedEvent.setLevel(level);

        EventBus.publishUnlocked(achievementUnlockedEvent);

        assertThat(receivedAchievementUnlockedEvent, notNullValue());
        assertThat(receivedAchievementUnlockedEvent.getId(), is(equalTo(id)));
        assertThat(receivedAchievementUnlockedEvent.getTitle(), is(equalTo(title)));
        assertThat(receivedAchievementUnlockedEvent.getText(), is(equalTo(text)));
        assertThat(receivedAchievementUnlockedEvent.getTriggerValue(), is(equalTo(score)));
        assertThat(receivedAchievementUnlockedEvent.getLevel(), is(equalTo(level)));
    }

    @Test
    public void testPublishScoreChangedPublishesGivenEvent() throws Exception {
        final String event = "event";
        final long value = 123L;
        final ScoreUpdatedEvent scoreUpdatedEvent = new ScoreUpdatedEvent(event, value);

        EventBus.publishScoreChanged(scoreUpdatedEvent);

        assertThat(receivedScoreUpdatedEvent, notNullValue());
        assertThat(receivedScoreUpdatedEvent.getEvent(), is(equalTo(event)));
        assertThat(receivedScoreUpdatedEvent.getValue(), is(equalTo(value)));
    }

    @Test
    public void testTriggerEventWithScoreCallsControllerMethod() throws Exception {
        final String event = "test";
        final long score = 1L;

        EventBus.triggerEvent(event, score);

        verify(mockController).triggerEvent(event, score);
    }

    @Test
    public void testTriggerEventWithOwnersCallsControllerMethod() throws Exception {
        final String event = "test";
        final ArrayList<String> owners = new ArrayList<>();

        EventBus.triggerEvent(event, owners);

        verify(mockController).triggerEvent(event, owners);
    }

    @Test
    public void testCheckAllCallsControllerMethod() throws Exception {
        EventBus.checkAll();
        verify(mockController).checkAndUnlock();
    }

    @Test
    public void testUnlockCallsControllerMethod() throws Exception {
        final String id = "testId";
        final String triggerValue = "value";

        EventBus.unlock(id, triggerValue);

        verify(mockController).unlock(id, triggerValue);
    }

    @Test
    public void testUnlockWithOwnersCallsControllerMethod() throws Exception {
        final String id = "testId";
        final String triggerValue = "value";
        final List<String> owners = Collections.singletonList("owner");

        EventBus.unlock(id, triggerValue, owners);

        verify(mockController).unlock(id, triggerValue, owners);
    }
}
