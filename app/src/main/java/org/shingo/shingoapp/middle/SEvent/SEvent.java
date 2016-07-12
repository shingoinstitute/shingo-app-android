package org.shingo.shingoapp.middle.SEvent;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shingo.shingoapp.middle.SEntity.SOrganization;
import org.shingo.shingoapp.middle.SEntity.SPerson;
import org.shingo.shingoapp.middle.SEntity.SSponsor;
import org.shingo.shingoapp.middle.SObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class holds data for
 * a Shingo Event.
 *
 * @author Dustin Homan
 */
public class SEvent extends SObject implements Comparable<SObject> {
    private Date start;
    private Date end;
    private String registration;
    private SVenue venue;
    private String displayLocation;
    private String city;
    private String country;
    private String primaryColor;
    private String bannerUrl;
    private Bitmap banner;
    private List<SDay> agenda = new ArrayList<>();
    private List<SPerson> speakers = new ArrayList<>();
    private List<SSession> sessions = new ArrayList<>();
    private List<SOrganization> exhibitors = new ArrayList<>();
    private List<SSponsor> sponsors = new ArrayList<>();
    private static final long TIME_OUT = TimeUnit.MINUTES.toMillis(15);
    private Map<String, Date> lastDataPull = new HashMap<>();

    public SEvent(){}

    public SEvent(String id, String name, Date start, Date end, String registration,
                  SVenue venue, String displayLocation, String city, String country, String primaryColor, List<SDay> agenda, List<SPerson> speakers){
        super(id, name);
        this.start = start;
        this.end = end;
        this.registration = registration;
        this.venue = venue;
        this.displayLocation = displayLocation;
        this.city = city;
        this.country = country;
        this.primaryColor = primaryColor;
        this.agenda = agenda;
        this.speakers = speakers;
    }

    public Date getStart(){
        return start;
    }

    public Date getEnd(){
        return end;
    }

    public String getRegistration(){
        return registration;
    }

    public SVenue getVenue(){
        return venue;
    }

    public String getDisplayLocation() {
        return displayLocation;
    }

    public String getCity() {
        return city;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getCountry() {
        return country;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public Bitmap getBanner() {
        return banner;
    }

    public void setBanner(Bitmap banner) {
        this.banner = banner;
    }

    public List<SDay> getAgenda(){
        return agenda;
    }

    public List<SSession> getSessions(){
        return sessions;
    }

    public List<SSession> getSubsetSessions(List<String> ids){
        if(ids.size() == 0) return new ArrayList<>();
        int start = sessions.size() - 1;
        int end = 0;
        for(String s : ids){
            int i = sessions.indexOf(new SSession(s));
            start = i < start ? i : start;
            end = i > end ? i : end;
        }

        return sessions.subList(start, end + 1);
    }

    public List<SPerson> getSubsetSpeakers(List<String> ids){
        if(ids.size() == 0) return new ArrayList<>();
        int start = speakers.size() - 1;
        int end = 0;
        for(String s : ids){
            int i = speakers.indexOf(new SPerson(s));
            start = i < start ? i : start;
            end = i > end ? i : end;
        }

        return speakers.subList(start, end + 1);
    }

    public List<SPerson> getSpeakers(){
        return speakers;
    }

    public List<SOrganization> getExhibitors() {
        return exhibitors;
    }

    public List<SSponsor> getSponsors() {
        return sponsors;
    }

    @Override
    public int compareTo(@NonNull SObject a){
        if(!(a instanceof SEvent))
            throw new ClassCastException(a.getName() + " is not a SEvent");

        int compareStart = this.start.compareTo(((SEvent) a).getStart());
        if(compareStart == 0){
            int compareEnd = this.end.compareTo(((SEvent) a).getEnd());
            if(compareEnd == 0)
                return this.name.compareTo(a.getName());
        }

        return compareStart;
    }

    @Override
    public void fromJSON(String json){
        super.fromJSON(json);
        try {
            JSONObject jsonEvent = new JSONObject(json);
            if(jsonEvent.has("Start_Date__c"))
                this.start = formatDateString(jsonEvent.getString("Start_Date__c"));
            if(jsonEvent.has("End_Date__c"))
                this.end = formatDateString(jsonEvent.getString("End_Date__c"));
            if(jsonEvent.has("Registration_Link__c"))
                this.registration = (jsonEvent.isNull("Registration_Link__c") ? "http://events.shingo.org" : jsonEvent.getString("Registration_Link__c"));

            this.venue = new SVenue();
            if(jsonEvent.has("Display_Location__c"))
                this.displayLocation = (jsonEvent.isNull("Display_Location__c") ? "" : jsonEvent.getString("Display_Location__c"));
            if(jsonEvent.has("Host_City__c"))
                this.city = (jsonEvent.isNull("Host_City__c") ? "" : jsonEvent.getString("Host_City__c"));
            if(jsonEvent.has("Host_Country__c"))
                this.country = (jsonEvent.isNull("Host_Country__c") ? "" : jsonEvent.getString("Host_Country__c"));
            if(jsonEvent.has("Primary_Color__c"))
                this.primaryColor = (jsonEvent.getString("Primary_Color__c").equals("null") ? "#640921" : jsonEvent.getString("Primary_Color__c"));
            if(jsonEvent.has("Shingo_Day_Agendas__r")) {
                JSONArray jDays = jsonEvent.getJSONObject("Shingo_Day_Agendas__r").getJSONArray("records");
                for (int i = 0; i < jDays.length(); i++) {
                    SDay day = new SDay();
                    day.fromJSON(jDays.getJSONObject(i).toString());
                    agenda.add(day);
                }
            }
            if(jsonEvent.has("Banner_URL__c"))
                this.bannerUrl = jsonEvent.isNull("Banner_URL__c") ? "" : jsonEvent.getString("Banner_URL__c");
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean needsUpdated(String data){
        if(!lastDataPull.containsKey(data)) return true;
        Date now = new Date();
        return now.after(new Date(lastDataPull.get(data).getTime() + TIME_OUT));
    }

    public void updatePullTime(String data){
        if(lastDataPull.containsKey(data)){
            lastDataPull.get(data).setTime(new Date().getTime());
        } else {
            lastDataPull.put(data, new Date());
        }
    }

    public boolean hasCache(String data){
        return lastDataPull.containsKey(data);
    }
}
