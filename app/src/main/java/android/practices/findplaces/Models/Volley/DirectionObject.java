package android.practices.findplaces.Models.Volley;

import java.util.List;

/**
 * Created by Amit on 12-Dec-18.
 */
public class DirectionObject {
    private List<RouteObject> routes;
    private String status;

    public DirectionObject(List<RouteObject> routes, String status) {
        this.routes = routes;
        this.status = status;
    }

    public List<RouteObject> getRoutes() {
        return routes;
    }

    public String getStatus() {
        return status;
    }
}
