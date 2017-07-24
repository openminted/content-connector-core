package eu.openminted.content.connector.core.util;

import eu.openminted.registry.domain.RightsStatementEnum;

import java.util.List;

public class RightsStmtNameConverter {
    public static RightsStatementEnum convert(String bestLicence) {

        switch (bestLicence) {
            case "Open Access":
                return RightsStatementEnum.OPEN_ACCESS;
            case "12 Months Embargo":
            case "6 Months Embargo":
            case "Embargo":
            case "Restricted":
            case "Closed Access":
                return RightsStatementEnum.RESTRICTED_ACCESS;
        }
        return null;
    }


    public static void convert(List<String> rightsValues, RightsStatementEnum rightsStatementEnum) {

        switch (rightsStatementEnum) {
            case OPEN_ACCESS:
                rightsValues.add("Open Access");
                break;
            case RESTRICTED_ACCESS:
                rightsValues.add("Embargo");
                break;
            default:
                break;
        }
    }

}
