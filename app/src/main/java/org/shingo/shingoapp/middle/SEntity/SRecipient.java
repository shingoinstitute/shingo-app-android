package org.shingo.shingoapp.middle.SEntity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.shingo.shingoapp.middle.SObject;
import org.shingo.shingoapp.R;

/**
 * This class holds the data
 * for Shingo Recipients.
 *
 * @author Dustin Homan
 */
public class SRecipient extends SEntity implements Comparable<SObject>,Parcelable {
    private String author;
    public SRecipientAward award;

    public SRecipient(String id, String name, String author, String summary, Bitmap image,
                      SRecipientAward award){
        super(id, name, summary, image);
        this.author = author;
        this.award = award;
    }

    @Override
    public void fromJSON(String json) {
        super.fromJSON(json);
        try {
            JSONObject jsonRecipient = new JSONObject(json);
            this.author = (jsonRecipient.getString("Author").equals("null") ? null : jsonRecipient.getString("Author"));
            getTypeFromString(jsonRecipient.getString("Award"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDetail() {
        if(award == SRecipientAward.ResearchAward){
            return author;
        } else {
            return String.valueOf(award);
        }
    }

    @Override
    public View getContent(Context context) {
        View contentView = View.inflate(context, R.layout.srecipient_content_view, null);
        ((TextView)contentView.findViewById(R.id.srecipient_name)).setText(name);
        ((TextView)contentView.findViewById(R.id.srecipient_summary)).setText(Html.fromHtml(summary));
        return contentView;
    }

    @Override
    protected void getTypeFromString(String type) {
        this.award = SRecipientAward.valueOf(type);
    }

    public String getAuthor(){
        return author;
    }

    @Override
    public int compareTo(@NonNull SObject a){
        if(!(a instanceof SRecipient))
            throw new ClassCastException(a.getName() + " is not a SRecipient");
        int enumCompare = this.award.compareTo(((SRecipient) a).award);
        if(enumCompare == 0)
            return this.name.compareTo(a.getName());

        return enumCompare;
    }

    public enum SRecipientAward {
        ShingoPrize("Shingo Prize"),
        SilverMedallion("Silver Medallion"),
        BronzeMedallion("Bronze Medallion"),
        ResearchAward("Research Award");

        private String awardName;
        SRecipientAward(String awardName){
            this.awardName = awardName;
        }

        @Override
        public String toString(){
            return awardName;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeInt(this.award == null ? -1 : this.award.ordinal());
        dest.writeParcelable(this.image, flags);
        dest.writeString(this.imageUrl);
        dest.writeString(this.summary);
        dest.writeString(this.id);
        dest.writeString(this.name);
    }

    protected SRecipient(Parcel in) {
        this.author = in.readString();
        int tmpAward = in.readInt();
        this.award = tmpAward == -1 ? null : SRecipientAward.values()[tmpAward];
        this.image = in.readParcelable(Bitmap.class.getClassLoader());
        this.imageUrl = in.readString();
        this.summary = in.readString();
        this.id = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<SRecipient> CREATOR = new Parcelable.Creator<SRecipient>() {
        @Override
        public SRecipient createFromParcel(Parcel source) {
            return new SRecipient(source);
        }

        @Override
        public SRecipient[] newArray(int size) {
            return new SRecipient[size];
        }
    };
}
