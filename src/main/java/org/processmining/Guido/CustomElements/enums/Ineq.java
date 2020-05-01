package org.processmining.Guido.CustomElements.enums;
import com.google.gson.annotations.SerializedName;

public enum Ineq {
    @SerializedName("==")
    EQUAL("=="),

    @SerializedName("!=")
    INEQUAL("!="),

    @SerializedName("<")
    LESS("<"),

    @SerializedName("<=")
    LESSEQUAL("<="),

    @SerializedName(">")
    GREATER(">"),

    @SerializedName(">=")
    GREATEREQUAL(">=");

    private String textRep;
    Ineq(String textRep) {
        this.textRep = textRep;
    }

    @Override
    public String toString() {
        return textRep;
    }

    public String getOpposite() {
        if(this == Ineq.EQUAL)
            return "!=";
        else if(this == Ineq.INEQUAL)
            return "==";
        else if(this == Ineq.LESS)
            return ">=";
        else if(this == Ineq.LESSEQUAL)
            return ">";
        else if(this == Ineq.GREATER)
            return "<=";
        else
            return "<";
    }

}
