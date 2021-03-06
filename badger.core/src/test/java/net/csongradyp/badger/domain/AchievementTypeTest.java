package net.csongradyp.badger.domain;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AchievementTypeTest {

    @Test(expected = IllegalArgumentException.class)
    public void testParseThrowsExceptionWhenInvalidTypeIsGiven() throws Exception {
        AchievementType.parse("invalid");
    }

    @Test
    public void testParseReturnsAchievementTypeWhenGivenTypeIsValid() throws Exception {
        AchievementType date = AchievementType.parse("date");
        assertThat(date, is(AchievementType.DATE));

        AchievementType time = AchievementType.parse("time");
        assertThat(time, is(AchievementType.TIME));

        AchievementType timeRange = AchievementType.parse("timeRange");
        assertThat(timeRange, is(AchievementType.TIME_RANGE));

        AchievementType single = AchievementType.parse("single");
        assertThat(single, is(AchievementType.SINGLE));

        AchievementType score = AchievementType.parse("score");
        assertThat(score, is(AchievementType.SCORE));

        AchievementType scoreRange = AchievementType.parse("scoreRange");
        assertThat(scoreRange, is(AchievementType.SCORE_RANGE));

        AchievementType composite = AchievementType.parse("composite");
        assertThat(composite, is(AchievementType.COMPOSITE));
    }
}
