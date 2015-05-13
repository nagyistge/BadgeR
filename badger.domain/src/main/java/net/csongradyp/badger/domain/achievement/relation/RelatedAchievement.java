package net.csongradyp.badger.domain.achievement.relation;

import net.csongrady.badger.IAchievementController;
import net.csongrady.badger.domain.IAchievement;
import net.csongrady.badger.domain.IRelation;

public class RelatedAchievement implements IRelation {

    private IAchievement achievement;
    private Long currentValue;

    public RelatedAchievement(final IAchievement achievement) {
        this.achievement = achievement;
        currentValue = Long.MIN_VALUE;
    }

    public RelatedAchievement(final IAchievement achievement, final Long currentValue) {
        this.achievement = achievement;
        this.currentValue = currentValue;
    }

    @Override
    public Boolean evaluate(final IAchievementController controller) {
        return !controller.unlockable(currentValue, achievement).isPresent();
    }
}