package org.shingo.shingoapp.middle.SEvent;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;
import org.shingo.shingoapp.middle.SObject;

/**
 * This class holds the data
 * for Rooms at a venue.
 *
 * @author Dustin Homan
 */
public class SRoom extends SObject implements Comparable<SObject>,Parcelable {
    private double[] location = new double[2];
    private int floor;

    public SRoom(){}

    public SRoom(String id, String name, double[] location, int floor) {
        super(id, name);
        this.location = location;
        this.floor = floor;
    }

    @Override
    public void fromJSON(String json){
        super.fromJSON(json);
        try {
            JSONObject jsonRoom = new JSONObject(json);
            this.floor = (jsonRoom.getString("floor").equals("null") ? 0 : jsonRoom.getInt("floor"));
            location[0] = jsonRoom .getDouble("y");
            location[1] = jsonRoom.getDouble("x");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public double[] getLocation(){
        return location;
    }

    public int getFloor(){
        return floor;
    }

    @Override
    public int compareTo(SObject a){
        if(!(a instanceof SRoom))
            throw new ClassCastException(a.getName() + " is not a SRoom");
        int floorCompare = this.floor - ((SRoom) a).floor;
        if(floorCompare == 0){
            return this.name.compareTo(a.getName());
        }

        return floorCompare;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDoubleArray(this.location);
        dest.writeInt(this.floor);
        dest.writeString(this.id);
        dest.writeString(this.name);
    }

    protected SRoom(Parcel in) {
        this.location = in.createDoubleArray();
        this.floor = in.readInt();
        this.id = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<SRoom> CREATOR = new Parcelable.Creator<SRoom>() {
        @Override
        public SRoom createFromParcel(Parcel source) {
            return new SRoom(source);
        }

        @Override
        public SRoom[] newArray(int size) {
            return new SRoom[size];
        }
    };
}
