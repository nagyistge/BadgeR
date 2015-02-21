package com.noe.badger;

import com.noe.badger.event.handler.AchievementUnlockedHandlerWrapper;
import com.noe.badger.event.handler.IAchievementUnlockedHandler;
import com.noe.badger.event.handler.IAchievementUpdateHandler;
import com.noe.badger.event.EventBus;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Badger {

    private static final String CONTEXT_XML_PATH = "META-INF/beans.xml";

    private final AchievementController controller;

    private Badger() {
        final ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONTEXT_XML_PATH);
        applicationContext.registerShutdownHook();
        controller = applicationContext.getBean(AchievementController.class);
    }

    public Badger(final InputStream inputStream, final String baseName) {
        this();
        controller.setSource(inputStream);
        controller.setInternationalizationBaseName(baseName);
    }

    public Badger(final File achievementIni, final String baseName) {
        this();
        controller.setSource(achievementIni);
        controller.setInternationalizationBaseName(baseName);
    }

    public Badger(final String achievementIniLocation, final String baseName) throws IOException {
        this();
        controller.setSource(new File(achievementIniLocation));
        controller.setInternationalizationBaseName(baseName);
    }

    public void setLocale(final Locale locale) {
        controller.setLocale(locale);
    }

    public void unlock(final String id) {
        controller.unlock(id);
    }

    public void incrementCounter(final String id) {
        controller.incrementAndCheck(id);
    }

    public Long getCurrentScore(final String id) {
        return controller.getCurrentScore(id);
    }

    public void setScore(final String id, final Long score) {
        controller.setScoreAndCheck(id, score);
    }

    public void subscribeOnUnlock(final IAchievementUnlockedHandler achievementUnlockedHandler) {
        EventBus.subscribeOnUnlock(new AchievementUnlockedHandlerWrapper(achievementUnlockedHandler));
    }

//    public void unSubscribeOnUnlock(final IAchievementUnlockedHandler achievementUnlockedHandler) {
//        EventBus.unSubscribeOnUnlock(achievementUnlockedHandler);
//    }

    public void subscribeOnUnlock(final IAchievementUpdateHandler achievementUpdateHandler) {
        EventBus.subscribeOnUpdate(achievementUpdateHandler);
    }

//    public void unSubscribeOnUpdate(final IAchievementUpdateHandler achievementUpdateHandler) {
//        EventBus.unSubscribeOnUpdate(achievementUpdateHandler);
//    }

}