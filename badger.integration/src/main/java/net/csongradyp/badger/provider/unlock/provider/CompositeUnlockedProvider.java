package net.csongradyp.badger.provider.unlock.provider;

import net.csongradyp.badger.IAchievementUnlockFinderFacade;
import net.csongradyp.badger.domain.achievement.CompositeAchievementBean;
import net.csongradyp.badger.event.IAchievementUnlockedEvent;
import net.csongradyp.badger.event.message.AchievementUnlockedEvent;
import net.csongradyp.badger.factory.UnlockedEventFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@Named
public class CompositeUnlockedProvider extends UnlockedProvider<CompositeAchievementBean> {

    @Inject
    private IAchievementUnlockFinderFacade achievementController;
    @Inject
    private UnlockedEventFactory unlockedEventFactory;

    @Override
    public Optional<IAchievementUnlockedEvent> getUnlockable(final CompositeAchievementBean relationBean, final Long score) {
        if (relationBean.evaluate(achievementController)) {
            final AchievementUnlockedEvent achievementUnlockedEvent = unlockedEventFactory.createEvent(relationBean);
            return Optional.of(achievementUnlockedEvent);
        }
        return Optional.empty();
    }
}