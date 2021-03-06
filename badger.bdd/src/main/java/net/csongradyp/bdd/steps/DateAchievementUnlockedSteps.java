package net.csongradyp.bdd.steps;

import javax.inject.Inject;
import net.csongradyp.badger.provider.unlock.provider.DateUnlockedProvider;
import net.csongradyp.bdd.Steps;
import net.csongradyp.bdd.provider.TestDateProvider;
import org.jbehave.core.annotations.Given;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Steps
public class DateAchievementUnlockedSteps {

    @Inject
    private DateUnlockedProvider dateUnlockedProvider;
    @Inject
    private TestDateProvider dateProvider;

    @Given("the current date is $date")
    public void setCurrentDate(final String date) {
        dateProvider.stubDate(date);
        assertThat(dateProvider.currentDateString(), is(equalTo(date)));
        dateUnlockedProvider.setDateProvider(dateProvider);
    }

}
