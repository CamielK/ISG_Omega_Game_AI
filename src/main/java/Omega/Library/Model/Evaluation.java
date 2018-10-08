package Omega.Library.Model;

import Omega.Library.Enum.Operator;

import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    public int gameEvalScore            = 1;                    // Real game score (all group sizes multiplied)
    public List<Integer> groupScores    = new ArrayList<>();    // Size for each group

    /**
     * Returns the number of groups that match the given parameters
     * @param threshold int
     * @param operator Operator
     * @return int
     */
    public int groupsWithSize(int threshold, Operator operator) {
        int groupCount = 0;
        for (Integer group : groupScores) {
            switch (operator) {
                case EQUAL:                 if (group == threshold) groupCount++;   break;
                case GREATER:               if (group > threshold)  groupCount++;   break;
                case SMALLER:               if (group < threshold)  groupCount++;   break;
                case GREATER_OR_EQUAL:      if (group >= threshold) groupCount++;   break;
                case SMALLER_OR_EQUAL:      if (group <= threshold) groupCount++;   break;
            }
        }
        return groupCount;
    }
}
