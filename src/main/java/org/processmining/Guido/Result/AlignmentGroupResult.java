package org.processmining.Guido.Result;

import org.processmining.Guido.CustomElements.CustomElements;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments.AlignmentGroup;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.processmining.Guido.utils.Utils.getColorForValue;
import static org.processmining.Guido.utils.Utils.getHTMLColorString;

public class AlignmentGroupResult {
    private List<GroupOutput> groups;
    private List<ActivityGraphDetails> activityGraphDetails;
    private CustomElements customElements;
    private Map<String, ConstraintsResult> constraints;

    public AlignmentGroupResult(List<GroupOutput> groups, List<ActivityGraphDetails> activityGraphDetails, CustomElements customElements) {
        this.groups = groups;
        this.activityGraphDetails = activityGraphDetails;
        constraints = new HashMap<>();
        for(GroupOutput group : groups) {
            for(ConstraintSingleResult constraint : group.getConstraints()) {
                if(constraints.get(constraint.getId()) == null)
                    constraints.put(constraint.getId(), new ConstraintsResult(constraint));

                constraints.get(constraint.getId()).addResult(constraint.getResult(), group.getSize());
            }
        }

        if(constraints.size() > 0) {
            this.customElements = customElements.clone();

            Map<String, String> customColors = new HashMap<>();
            for (Map.Entry<String, ConstraintsResult> entry : constraints.entrySet()) {
                List<String> list = this.customElements.getIdMap().get(entry.getKey());
                String color = getHTMLColorString(getColorForValue(entry.getValue().getFitness()));

                if(list != null)
                    for (String s : list)
                        customColors.put(s, color);
                else
                    customColors.put(entry.getKey(), color);
            }

            this.customElements.setColors(customColors);
        }
    }
}
