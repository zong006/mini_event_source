package vttp.batch5.paf.day27.models;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class Event {
    private String eventId;
    private String tableName;
    private String action;
    private Date eventDate;
    private Map<String, String> fields;

    public Event(String tableName, String action, Date eventDate, Map<String, String> fields) {
        this.eventId = UUID.randomUUID().toString().substring(0, 12);
        this.tableName = tableName;
        this.action = action;
        this.eventDate = eventDate;
        this.fields = fields;
    }
    // public Event() {
    //     this.eventId = UUID.randomUUID().toString().substring(0, 12);
    // }
    public String getEventId() {
        return eventId;
    }

    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public Date getEventDate() {
        return eventDate;
    }
    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
    public Map<String, String> getFields() {
        return fields;
    }
    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
    @Override
    public String toString() {
        return "Event [eventId=" + eventId + ", tableName=" + tableName + ", action=" + action + ", eventDate="
                + eventDate + ", fields=" + fields + "]";
    }




}
