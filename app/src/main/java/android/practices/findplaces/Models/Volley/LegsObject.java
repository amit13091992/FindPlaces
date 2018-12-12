package android.practices.findplaces.Models.Volley;

import java.util.List;

/**
 * Created by Amit on 12-Dec-18.
 */
public class LegsObject {
    private List<StepsObject> steps;

    public LegsObject(List<StepsObject> steps) {
        this.steps = steps;
    }

    public List<StepsObject> getSteps() {
        return steps;
    }
}
