package calebzhou.rdi.core.client.model;


import calebzhou.rdi.core.client.util.RdiSerializer;

import java.io.Serializable;

public class RdiGeoLocation implements Serializable {
	public static RdiGeoLocation currentGeoLocation;

    public String nation;
    public String province;
    public String city;
    public String district;
    public String isp;
    public GeoLocation location;


	@Override
	public String toString() {
		return RdiSerializer.GSON.toJson(this);
	}
}
