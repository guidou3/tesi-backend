package org.processmining.Guido.Result;

import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments;
import org.processmining.plugins.DataConformance.visualization.grouping.GroupedAlignments.AlignmentGroup;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlignmentGroupResult {
    private List<GroupOutput> groups;
    private Map<String, String> labelColorMap;

    public AlignmentGroupResult(List<GroupOutput> groups, Map<String, String> labelColorMap) {
        this.groups = groups;
        this.labelColorMap = labelColorMap;

    }
}
