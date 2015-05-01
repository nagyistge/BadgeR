package com.noe.badger.event.message;

/**
 * Class for updated event score information.
 */
public class Score {

    private String event;
    private Long value;

    /**
     * @param event Name of the triggered event.
     * @param value New value of event trigger.
     */
    public Score(final String event, final Long value) {
        this.event = event;
        this.value = value;
    }

    /**
     * Returns the name of the event which was triggered.
     *
     * @return Name of the triggered event.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Returns the new value of the event counter/score.
     *
     * @return new value of event counter/score after trigger.
     */
    public Long getValue() {
        return value;
    }
}
