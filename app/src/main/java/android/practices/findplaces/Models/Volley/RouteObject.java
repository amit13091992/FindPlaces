package android.practices.findplaces.Models.Volley;

import java.util.List;

/**
 * Created by Amit on 12-Dec-18.
 */
public class RouteObject {
    private List<LegsObject> legs;

    public RouteObject(List<LegsObject> legs) {
        this.legs = legs;
    }

    public List<LegsObject> getLegs() {
        return legs;
    }
}
