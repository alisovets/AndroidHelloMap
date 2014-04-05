package alisovets.example.hellogooglemap.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * To store location points with descriptions
 *  
 * @author Alexander Lisovets, 2014
 *
 */
public class PlaceBean implements Parcelable{

	private long id;
	private double latitude;
	private double longitude;
	private String title;
	private String snippet;
	
	/**
	 * creates object
	 */
	public PlaceBean() {

	}

	
	/**
	 * Creates object
	 * @param id
	 * @param latitude
	 * @param longitude
	 * @param title
	 * @param snippet
	 */
	public PlaceBean(long id, double latitude, double longitude, String title,	String snippet){
		set(id, latitude, longitude, title, snippet);
	}
	
	/**
	 * the copy constructor
 	 * @param pb
	 */
	public PlaceBean(PlaceBean pb) {
		set(pb);
	}
	
	/**
	 * creates an object from the Parcelable object 
	 * @param parcel
	 */
	public PlaceBean(Parcel parcel){
		id = parcel.readLong();
		latitude = parcel.readDouble();
		longitude = parcel.readDouble();
		title = parcel.readString();
		snippet = parcel.readString();
	}
	
	
	/**
	 * initializes field of the object from passed object
	 * 
	 * @param pb
	 */
	public void set(PlaceBean pb) {
		this.id = pb.id;
		this.longitude = pb.longitude;
		this.latitude = pb.latitude;
		this.title = pb.title;
		this.snippet =  pb.snippet;
	}
	
	
	/**
	 * initializes all fields the object
	 * @param id
	 * @param latitude
	 * @param longitude
	 * @param title
	 * @param snippet
	 */
	public void set(long id, double latitude, double longitude, String title,	String snippet) {
		this.id = id;
		this.longitude = longitude;
		this.latitude = latitude;
		this.title = title;
		this.snippet =  snippet;
	}
	


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeLong(id);
		parcel.writeDouble(latitude);
		parcel.writeDouble(longitude);
		parcel.writeString(title);
		parcel.writeString(snippet);
	}
	
	/**
	 * the Parcelable creator
	 */
	public static final Parcelable.Creator<PlaceBean> CREATOR = new Parcelable.Creator<PlaceBean>() {
	  
	    public PlaceBean createFromParcel(Parcel parcel) {
	      return new PlaceBean(parcel);
	    }

	    public PlaceBean[] newArray(int size) {
	      return new PlaceBean[size];
	    }
	  };
	
	 

}
