package net.csongradyp.badger.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAchievementBean implements IAchievementBean {

    private String id;
    private String category;
    private List<String> event;
    private Integer maxLevel;

    public AbstractAchievementBean() {
        event = new ArrayList<>();
        maxLevel = 1;
        category= "default";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(final String category) {
        if(category != null && !category.isEmpty()) {
            this.category = category;
        }
    }

    @Override
    public Integer getMaxLevel() {
        return maxLevel;
    }

    @Override
    public void setMaxLevel( Integer maxLevel ) {
        this.maxLevel = maxLevel;
    }

    @Override
    public List<String> getEvent() {
        return event;
    }

    @Override
    public void setEvent(String[] event) {
        Collections.addAll(this.event, event);
    }

    @Override
    public void setEvent(List<String> event) {
        this.event = event;
    }

    @Override
    public String getTitleKey() {
        return String.format("%s.%s", id, "title");
    }

    @Override
    public String getTextKey() {
        return String.format("%s.%s", id, "text");
    }

    @Override
    public String toString() {
        return "AchievementBean {" +
               "id='" + id + '\'' +
                ", type='" + getType() + '\'' +
                ", category='" + category + '\'' +
               '}';
    }
}
