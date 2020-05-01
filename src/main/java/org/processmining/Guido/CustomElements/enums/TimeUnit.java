package org.processmining.Guido.CustomElements.enums;
import com.google.gson.annotations.SerializedName;

public enum TimeUnit {


    @SerializedName("ms")
    MILLISECONDS(1, "ms"), //

    @SerializedName("s")
    SECONDS(1000, "s"), //

    @SerializedName("m")
    MINUTES(1000 * 60, "m"), //

    @SerializedName("h")
    HOURS(1000 * 60 * 60, "h"), //

    @SerializedName("d")
    DAYS(1000 * 60 * 60 * 24, "d"),

    @SerializedName("w")
    WEEKS(1000 * 60 * 60 * 24 * 7, "w"),

    @SerializedName("mth")
    MONTHS(1000 * 60 * 60 * 24 * 30L, "mth"),

    @SerializedName("y")
    YEARS(1000 * 60 * 60 * 24 * 365L, "y");

    private final String unit;
    private final long factor;

    private TimeUnit(long millisecondFactor, String unit) {
        this.factor = millisecondFactor;
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public String toString() {
        return getUnit();
    }

    public Long getFactor() {
        return factor;
    }

    public float toMilliseconds() {
        return factor;
    }

    public float toSeconds() {
        return (float) factor / 1000;
    }

    public float toMinutes() {
        return (float) factor / 1000 / 60;
    }

    public float toHours() {
        return (float) factor / 1000 / 60 / 60;
    }

    public float toDays() {
        return (float) factor / 1000 / 60 / 60 / 24;
    }

    public float toWeeks() {
        return (float) factor / 1000 / 60 / 60 / 24 / 7;
    }

    // this one needs a control as the number of days in a month is not the same
    public float toMonths() {
        return (float) factor / 1000 / 60 / 60 / 24 / 30;
    }

    // this one needs a control as the number of days in a year is not the same
    public float toYears() {
        return (float) factor / 1000 / 60 / 60 / 24 / 365;
    }

    public boolean moreThan(TimeUnit other) {
        return this.ordinal() > other.ordinal();
    }

}

