package net.csongradyp.badger.provider.unlock.provider;

import net.csongradyp.badger.provider.date.IDateProvider;
import net.csongradyp.badger.domain.achievement.TimeRangeAchievementBean;
import net.csongradyp.badger.event.IAchievementUnlockedEvent;
import net.csongradyp.badger.event.message.AchievementUnlockedEvent;
import net.csongradyp.badger.factory.UnlockedEventFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Named
public class TimeRangeUnlockedProvider extends UnlockedProvider<TimeRangeAchievementBean> {

    @Inject
    private IDateProvider dateProvider;
    @Inject
    private UnlockedEventFactory unlockedEventFactory;

    @Override
    public Optional<IAchievementUnlockedEvent> getUnlockable(final TimeRangeAchievementBean timeAchievement, final Long score) {
        final List<TimeRangeAchievementBean.TimeTriggerPair> timeTriggers = timeAchievement.getTrigger();
        for (TimeRangeAchievementBean.TimeTriggerPair timeTrigger : timeTriggers) {
            final Date startTrigger = timeTrigger.getStartTrigger();
            final Date endTrigger = timeTrigger.getEndTrigger();
            if (startTrigger.before(endTrigger)) {
                if (dateProvider.isCurrentTimeAfter(startTrigger) && dateProvider.isCurrentTimeBefore(endTrigger) && !isUnlocked(timeAchievement.getId())) {
                    final AchievementUnlockedEvent achievementUnlockedEvent = unlockedEventFactory.createEvent(timeAchievement, dateProvider.currentTime());
                    return Optional.of(achievementUnlockedEvent);
                }
            } else if (dateProvider.isCurrentTimeBefore(startTrigger) && dateProvider.isCurrentTimeAfter(endTrigger) && !isUnlocked(timeAchievement.getId())) {
                final AchievementUnlockedEvent achievementUnlockedEvent = unlockedEventFactory.createEvent(timeAchievement, dateProvider.currentTime());
                return Optional.of(achievementUnlockedEvent);
            }
        }
        return Optional.empty();
    }

    public void setDateProvider(IDateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }
}
