package org.processmining.Guido.CustomElements.enums;

import com.google.gson.annotations.SerializedName;

public enum ResType {

    @SerializedName("Occurrence")
    OCCURRENCE,
    @SerializedName("Absence")
    ABSENCE,
    @SerializedName("Instance")
    INSTANCE;

    public String getAbbreviation() {
        return this.toString().substring(0, 1);
    }

}
