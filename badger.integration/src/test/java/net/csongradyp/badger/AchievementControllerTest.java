package net.csongradyp.badger;

import net.csongradyp.badger.domain.AchievementType;
import net.csongradyp.badger.domain.IAchievement;
import net.csongradyp.badger.domain.achievement.ScoreAchievementBean;
import net.csongradyp.badger.event.AchievementEventType;
import net.csongradyp.badger.event.EventBus;
import net.csongradyp.badger.event.handler.wrapper.AchievementUnlockedHandlerWrapper;
import net.csongradyp.badger.event.handler.wrapper.ScoreUpdateHandlerWrapper;
import net.csongradyp.badger.event.message.AchievementUnlockedEvent;
import net.csongradyp.badger.event.message.ScoreUpdatedEvent;
import net.csongradyp.badger.factory.UnlockedEventFactory;
import net.csongradyp.badger.parser.IAchievementDefinitionFileParser;
import net.csongradyp.badger.persistence.AchievementDao;
import net.csongradyp.badger.persistence.EventDao;
import net.csongradyp.badger.persistence.entity.AchievementEntity;
import net.csongradyp.badger.provider.unlock.AchievementUnlockProviderFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AchievementControllerTest {

    private static final String ACHIEVEMENT_ID = "test";
    private static final String EVENT_ID = "event";

    @Mock
    private IAchievementDefinitionFileParser mockAchievementParser;
    @Mock
    private AchievementDefinition mockAchievementDefinition;
    @Mock
    private AchievementDao mockAchievementDao;
    @Mock
    private EventDao mockEventDao;
    @Mock
    private UnlockedEventFactory mockUnlockedEventFactory;
    @Mock
    private AchievementUnlockProviderFacade mockAchievementUnlockProviderFacade;

    private ScoreUpdateHandlerWrapper scoreHandler;
    private ScoreUpdatedEvent scoreUpdatedEventEvent;

    private AchievementController underTest;

    @Before
    public void setUp() {
        underTest = new AchievementController();
        underTest.setAchievementUnlockFinder(mockAchievementUnlockProviderFacade);
        underTest.setUnlockedEventFactory(mockUnlockedEventFactory);
        underTest.setAchievementDefinition(mockAchievementDefinition);
        underTest.setAchievementDao(mockAchievementDao);
        underTest.setEventDao(mockEventDao);

        scoreHandler = new ScoreUpdateHandlerWrapper(score -> scoreUpdatedEventEvent = score);
        EventBus.subscribeOnScoreChanged(scoreHandler);
        scoreUpdatedEventEvent = null;
    }

    @After
    public void tearDown() {
        EventBus.unSubscribeOnScoreChanged(scoreHandler.getWrapped());
    }

    @Test
    public void testSetInternationalizationBaseNameSetsResourceBundleWhenInternationalizationFilesArePresentWithTheGivenBaseName() {
        final String baseName = "msg";

        underTest.setInternationalizationBaseName(baseName);

        assertThat(underTest.getResourceBundle(), notNullValue());
        assertThat(underTest.getResourceBundle().getBaseBundleName(), is(equalTo(baseName)));
    }

    @Test
    public void testSetResourceBundleSetsTheBundleForTheEventFactoryAlso() {
        final String baseName = "msg";
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, Locale.ENGLISH);

        underTest.setResourceBundle(resourceBundle);

        verify(mockUnlockedEventFactory).setResourceBundle(resourceBundle);
        assertThat(underTest.getResourceBundle(), notNullValue());
        assertThat(underTest.getResourceBundle().getBaseBundleName(), is(equalTo(baseName)));
    }

    @Test(expected = MissingResourceException.class)
    public void testSetInternationalizationBaseNameThrowsExceptionWhenInternationalizationFilesAreNotPresentWithTheGivenBaseName() {
        underTest.setInternationalizationBaseName("wrong name");
    }

    @Test
    public void testSetLocaleLoadsNewMessageSourceWithNewLocale() {
        underTest.setInternationalizationBaseName("msg");
        final Locale locale = new Locale("hu");

        underTest.setLocale(locale);

        verify(mockUnlockedEventFactory).setResourceBundle(any(ResourceBundle.class));
        assertThat(underTest.getResourceBundle().getLocale(), is(locale));
    }

    @Test
    public void testGetAllReturnsAllAchievementsFromAchievementDefinition() {
        final ArrayList<IAchievement> achievements = new ArrayList<>();
        given(mockAchievementDefinition.getAll()).willReturn(achievements);

        final Collection<IAchievement> result = underTest.getAll();

        assertThat(result, notNullValue());
        assertThat(result, is(achievements));
        assertThat(result.size(), is(equalTo(achievements.size())));
    }

    @Test
    public void testGetAllByOwnerReturnsAllAchievementsFromAchievementDefinitionBelongingToGivenOwner() {
        final String owner = "owner";
        final AchievementEntity entity = new AchievementEntity();
        entity.setId(ACHIEVEMENT_ID);
        given(mockAchievementDao.getAllByOwner(owner)).willReturn(Arrays.asList(entity));
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
        given(mockAchievementDefinition.get(ACHIEVEMENT_ID)).willReturn(Optional.of(achievementBean));

        final Collection<IAchievement> result = underTest.getAllByOwner(owner);

        assertThat(result.size(), is(equalTo(1)));
        assertThat(result.iterator().next(), is(achievementBean));
    }

    @Test
    public void testGetReturnsAchievementsFromAchievementDefinition() {
        final ScoreAchievementBean testAchievement = new ScoreAchievementBean();
        testAchievement.setId(ACHIEVEMENT_ID);
        final AchievementType type = AchievementType.SCORE;
        given(mockAchievementDefinition.get(type, ACHIEVEMENT_ID)).willReturn(Optional.of(testAchievement));

        final Optional<IAchievement> result = underTest.get(type, ACHIEVEMENT_ID);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(testAchievement));
    }

    @Test
    public void testGetReturnsEmptyWhenNoAchievementIsPresentInTheAchievementDefinition() {
        given(mockAchievementDefinition.get(AchievementType.SCORE, ACHIEVEMENT_ID)).willReturn(Optional.<IAchievement>empty());
        final Optional<IAchievement> result = underTest.get(AchievementType.SCORE, ACHIEVEMENT_ID);
        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void testGetAllCallUnderlyingBundle() {
        final ScoreAchievementBean testAchievementBean = new ScoreAchievementBean();
        testAchievementBean.setId(ACHIEVEMENT_ID);
        final ArrayList<IAchievement> parsedAchievements = new ArrayList<>();
        parsedAchievements.add(testAchievementBean);
        when(mockAchievementDefinition.getAll()).thenReturn(parsedAchievements);

        final Collection<IAchievement> achievements = underTest.getAll();

        verify(mockAchievementDefinition).getAll();
        assertThat(achievements.size(), is(equalTo(1)));
        assertThat(achievements.iterator().next(), is(testAchievementBean));
    }

    @Test
    public void testGetAllByEventsReturnsAchievementDefinitionProvidedCollection() {
        final HashMap<String, Set<IAchievement>> achievements = new HashMap<>();
        given(mockAchievementDefinition.getAllByEvents()).willReturn(achievements);
        final Map<String, Set<IAchievement>> result = underTest.getAllByEvents();
        assertThat(achievements, is(result));
    }

    @Test
    public void testGetAllUnlocked() {
        final String testId = "testId";
        final AchievementEntity entity = new AchievementEntity();
        entity.setId(testId);
        given(mockAchievementDao.getAll()).willReturn(Arrays.asList(entity));
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
        given(mockAchievementDefinition.get(testId)).willReturn(Optional.of(achievementBean));

        final Collection<IAchievement> result = underTest.getAllUnlocked();

        assertThat(result.size(), is(1));
        assertThat(result.iterator().next(), is(achievementBean));
    }

    @Test
    public void testTriggerEventWithHighScoreDoesNotSetNewScoreANorPublishScoreChangedeventWhenGivenScoreIsLessThanTheCurrentlyStored() {
        final String event = "test";
        final long score = 10L;
        given(mockEventDao.scoreOf(event)).willReturn(11L);

        underTest.triggerEventWithHighScore(event, score);

        verify(mockEventDao, never()).setScore(event, score);
        assertThat(scoreUpdatedEventEvent, is(nullValue()));
    }

    @Test
    public void testTriggerEventWithHighScoreDoesNotTriggerEventWhenGivenScoreIsEqualToTheCurrentlyStored() {
        final String event = "test";
        final long score = 10L;
        given(mockEventDao.scoreOf(event)).willReturn(score);

        underTest.triggerEventWithHighScore(event, score);

        verify(mockEventDao, never()).setScore(event, score);
        assertThat(scoreUpdatedEventEvent, is(nullValue()));
    }

    @Test
    public void testTriggerEventWithHighScore() {
        final String event = "test";
        final long score = 10L;
        given(mockEventDao.scoreOf(event)).willReturn(9L);
        given(mockEventDao.setScore(event, score)).willReturn(score);

        underTest.triggerEventWithHighScore(event, score);

        verify(mockEventDao).setScore(event, score);
        assertThat(scoreUpdatedEventEvent, is(notNullValue()));
        assertThat(scoreUpdatedEventEvent.getEvent(), is(event));
        assertThat(scoreUpdatedEventEvent.getValue(), is(score));
    }

    @Test
    public void testUnlockPublishUnlockedEventWithProperData() {
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
        achievementBean.setId(ACHIEVEMENT_ID);
        final String triggerValue = "value";
        final AchievementUnlockedEvent unlockedEvent = new AchievementUnlockedEvent(ACHIEVEMENT_ID, "", "", "");
        final AchievementUnlockedHandlerWrapper handler = new AchievementUnlockedHandlerWrapper(achievement -> {
            verifyUnlocalizedAchievement(achievement, triggerValue, Collections.emptySet());
        });
        when(mockAchievementDefinition.get(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievementBean));
        when(mockAchievementDao.isUnlocked(ACHIEVEMENT_ID, 1)).thenReturn(false);
        when(mockUnlockedEventFactory.createEvent(achievementBean, triggerValue)).thenReturn(unlockedEvent);
        EventBus.subscribeOnUnlock(handler);

        underTest.unlock(ACHIEVEMENT_ID, triggerValue);

        verify(mockAchievementDefinition).get(ACHIEVEMENT_ID);
        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID, 1);
        verify(mockAchievementDao).unlock(ACHIEVEMENT_ID, 1, Collections.emptySet());
        EventBus.unSubscribeOnUnlock(handler.getWrapped());
    }

    @Test
    public void testCheckAndUnlockUnlocksAllPossibleAchievements() {
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
        achievementBean.setId(ACHIEVEMENT_ID);
        final String triggerValue = "value";
        final AchievementUnlockedEvent unlockedEvent = new AchievementUnlockedEvent(ACHIEVEMENT_ID, "", "", "");
        final AchievementUnlockedHandlerWrapper handler = new AchievementUnlockedHandlerWrapper(
                achievement -> verifyUnlocalizedAchievement(achievement, triggerValue, Collections.emptySet())
        );
        when(mockAchievementUnlockProviderFacade.findAll()).thenReturn(Arrays.asList(unlockedEvent));
        when(mockAchievementDao.isUnlocked(ACHIEVEMENT_ID, 1)).thenReturn(false);
        when(mockUnlockedEventFactory.createEvent(achievementBean, triggerValue)).thenReturn(unlockedEvent);
        EventBus.subscribeOnUnlock(handler);

        underTest.checkAndUnlock();

        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID, 1);
        verify(mockAchievementDao).unlock(ACHIEVEMENT_ID, 1, Collections.emptySet());
        EventBus.unSubscribeOnUnlock(handler.getWrapped());
    }

    @Test
    public void testUnlockWithGivenOwnersPublishUnlockedEventWithProperData() {
        final String triggerValue = "value";
        final Set<String> owners = new HashSet<>();
        owners.add("owner");
        final AchievementUnlockedEvent unlockedEvent = new AchievementUnlockedEvent(ACHIEVEMENT_ID, "", "", "");
        unlockedEvent.addOwners(owners);
        final AchievementUnlockedHandlerWrapper handler = new AchievementUnlockedHandlerWrapper(
                achievement -> assertThat(achievement, is(unlockedEvent))
        );
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
       achievementBean.setId(ACHIEVEMENT_ID);
        when(mockAchievementDefinition.get(ACHIEVEMENT_ID)).thenReturn(Optional.of(achievementBean));
        when(mockAchievementDao.isUnlocked(ACHIEVEMENT_ID, 1)).thenReturn(false);
        when(mockUnlockedEventFactory.createEvent(any(IAchievement.class), anyString(), anyCollection())).thenReturn(unlockedEvent);
        EventBus.subscribeOnUnlock(handler);

        underTest.unlock(ACHIEVEMENT_ID, triggerValue, owners);

        verify(mockAchievementDefinition).get(ACHIEVEMENT_ID);
        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID, 1);
        verify(mockAchievementDao).unlock(ACHIEVEMENT_ID, 1, owners);
        EventBus.unSubscribeOnUnlock(handler.getWrapped());
    }

    @Test
    public void testTriggerEventIncrementsEventScoreAndUnlocksAllPossibleAchievementsWithNewScore() {
        final long newScore = 42L;
        final AchievementUnlockedEvent unlockedEvent = new AchievementUnlockedEvent(ACHIEVEMENT_ID, "", "", "");
        final AchievementUnlockedHandlerWrapper handler = new AchievementUnlockedHandlerWrapper(achievement -> assertThat(achievement, is(unlockedEvent)));
        final ScoreUpdateHandlerWrapper scoreHandler = new ScoreUpdateHandlerWrapper(e -> {
            assertThat(e.getEvent(), is(EVENT_ID));
            assertThat(e.getValue(), is(equalTo(newScore)));
        });
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
        achievementBean.setId(ACHIEVEMENT_ID);
        when(mockEventDao.increment(EVENT_ID)).thenReturn(newScore);
        when(mockAchievementUnlockProviderFacade.findUnlockables(EVENT_ID)).thenReturn(Collections.singletonList(unlockedEvent));
        when(mockAchievementDao.isUnlocked(ACHIEVEMENT_ID, 1)).thenReturn(false);
        when(mockUnlockedEventFactory.createEvent(any(IAchievement.class), anyString(), anyCollection())).thenReturn(unlockedEvent);
        EventBus.subscribeOnUnlock(handler);
        EventBus.subscribeOnScoreChanged(scoreHandler);

        underTest.triggerEvent(EVENT_ID);

        verify(mockEventDao).increment(EVENT_ID);
        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID, 1);
        verify(mockAchievementDao).unlock(ACHIEVEMENT_ID, 1, Collections.emptySet());
        EventBus.unSubscribeOnUnlock(handler.getWrapped());
        EventBus.unSubscribeOnScoreChanged(scoreHandler.getWrapped());
    }

    @Test
    public void testTriggerEventSetsEventScoreAndUnlocksAllPossibleAchievementsWithNewScore() {
        final long newScore = 42L;
        final AchievementUnlockedEvent unlockedEvent = new AchievementUnlockedEvent(ACHIEVEMENT_ID, "", "", "");
        final AchievementUnlockedHandlerWrapper handler = new AchievementUnlockedHandlerWrapper(achievement -> assertThat(achievement, is(unlockedEvent)));
        final ScoreUpdateHandlerWrapper scoreHandler = new ScoreUpdateHandlerWrapper(e -> {
            assertThat(e.getEvent(), is(EVENT_ID));
            assertThat(e.getValue(), is(equalTo(newScore)));
        });
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
        achievementBean.setId(ACHIEVEMENT_ID);
        when(mockEventDao.setScore(EVENT_ID, newScore)).thenReturn(newScore);
        when(mockAchievementUnlockProviderFacade.findUnlockables(EVENT_ID, newScore)).thenReturn(Collections.singletonList(unlockedEvent));
        when(mockAchievementDao.isUnlocked(ACHIEVEMENT_ID, 1)).thenReturn(false);
        when(mockUnlockedEventFactory.createEvent(any(IAchievement.class), anyString(), anyCollection())).thenReturn(unlockedEvent);
        EventBus.subscribeOnUnlock(handler);
        EventBus.subscribeOnScoreChanged(scoreHandler);

        underTest.triggerEvent(EVENT_ID, newScore);

        verify(mockEventDao).setScore(EVENT_ID, newScore);
        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID, 1);
        verify(mockAchievementDao).unlock(ACHIEVEMENT_ID, 1, Collections.emptySet());
        EventBus.unSubscribeOnUnlock(handler.getWrapped());
        EventBus.unSubscribeOnScoreChanged(scoreHandler.getWrapped());
    }

    @Test
    public void testTriggerEventWithOwnersIncrementsEventScoreAndUnlocksAllPossibleAchievementsWithNewScore() {
        final long newScore = 42L;
        final List<String> owners = Arrays.asList("owner1", "owner2");
        final AchievementUnlockedEvent unlockedEvent = new AchievementUnlockedEvent(ACHIEVEMENT_ID, "", "", "");
        final AchievementUnlockedHandlerWrapper handler = new AchievementUnlockedHandlerWrapper(e -> assertThat(e, is(unlockedEvent)));
        final ScoreUpdateHandlerWrapper scoreHandler = new ScoreUpdateHandlerWrapper(e -> {
            assertThat(e.getEvent(), is(EVENT_ID));
            assertThat(e.getValue(), is(equalTo(newScore)));
        });
        final ScoreAchievementBean achievementBean = new ScoreAchievementBean();
        achievementBean.setId(ACHIEVEMENT_ID);
        when(mockEventDao.increment(EVENT_ID)).thenReturn(newScore);
        when(mockAchievementUnlockProviderFacade.findUnlockables(EVENT_ID, owners)).thenReturn(Collections.singletonList(unlockedEvent));
        when(mockAchievementDao.isUnlocked(ACHIEVEMENT_ID, 1)).thenReturn(false);
        when(mockUnlockedEventFactory.createEvent(any(IAchievement.class), anyString(), anyCollection())).thenReturn(unlockedEvent);
        EventBus.subscribeOnUnlock(handler);
        EventBus.subscribeOnScoreChanged(scoreHandler);

        underTest.triggerEvent(EVENT_ID, owners);

        verify(mockEventDao).increment(EVENT_ID);
        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID, 1);
        verify(mockAchievementDao).unlock(ACHIEVEMENT_ID, 1, Collections.emptySet());
        EventBus.unSubscribeOnUnlock(handler.getWrapped());
        EventBus.unSubscribeOnScoreChanged(scoreHandler.getWrapped());
    }

    @Test
    public void testIsUnlockedCallsUnderlyingDao() {
        underTest.isUnlocked(ACHIEVEMENT_ID);
        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID);
    }

    @Test
    public void testIsUnlockedCallsUnderlyingDaoWithGivenLevel() {
        Integer level = 3;
        underTest.isUnlocked(ACHIEVEMENT_ID, level);
        verify(mockAchievementDao).isUnlocked(ACHIEVEMENT_ID, level);
    }

    @Test
    public void testGetCurrentScoreCallsUnderlyingDao() {
        underTest.getCurrentScore(ACHIEVEMENT_ID);
        verify(mockEventDao).scoreOf(ACHIEVEMENT_ID);
    }

    @Test
    public void testResetCallsDaosDeleteAllMethods() {
        underTest.reset();
        verify(mockAchievementDao).deleteAll();
        verify(mockEventDao).deleteAll();
    }

    private void verifyUnlocalizedAchievement(final AchievementUnlockedEvent achievementUnlockedEvent, final String triggerValue, Set<String> owners) {
        assertThat(achievementUnlockedEvent.getId(), is(equalTo(ACHIEVEMENT_ID)));
        assertThat(achievementUnlockedEvent.getTitle(), is(equalTo(ACHIEVEMENT_ID + ".title")));
        assertThat(achievementUnlockedEvent.getText(), is(equalTo(ACHIEVEMENT_ID + ".text")));
        assertThat(achievementUnlockedEvent.getLevel(), is(equalTo(1)));
        assertThat(achievementUnlockedEvent.getEventType(), is(equalTo(AchievementEventType.UNLOCK)));
        assertThat(achievementUnlockedEvent.getCategory(), is(equalTo("default")));
        assertThat(achievementUnlockedEvent.getOwners(), is(equalTo(owners)));
        assertThat(achievementUnlockedEvent.getTriggerValue(), is(equalTo(triggerValue)));
    }

}
