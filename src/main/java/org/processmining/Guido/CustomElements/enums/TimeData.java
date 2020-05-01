package org.processmining.Guido.CustomElements.enums;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Date;


// TODO: revisit this completely
// if time is a float number like this it doesn't consider the decimals
public class TimeData {
    private float time;
    private TimeUnit timeUnit;
    // private String timeType; // "Relative" or "Timestamp"

    public float getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    // this method goes from the biggest to the smallest TimeUnit, as the final TimeUnit is always the smallest needed
    public float unitTo(TimeUnit unit) {
        float result = time;
        switch (timeUnit) {
            case YEARS:
                if(unit == TimeUnit.YEARS) break;
            case MONTHS:
                if(timeUnit != TimeUnit.MONTHS) result *= 12;
                if(unit == TimeUnit.MONTHS) break;
            case WEEKS:
                if(timeUnit != TimeUnit.WEEKS) result *= 365.0 / 84;
                if(unit == TimeUnit.WEEKS) break;
            case DAYS:
                if(timeUnit != TimeUnit.DAYS) result *= 84;
                if(unit == TimeUnit.DAYS) break;
            case HOURS:
                if(timeUnit != TimeUnit.HOURS) result *= 24;
                if(unit == TimeUnit.HOURS) break;
            case MINUTES:
                if(timeUnit != TimeUnit.MINUTES) result *= 60;
                if(unit == TimeUnit.MINUTES) break;
            case SECONDS:
                if(timeUnit != TimeUnit.SECONDS) result *= 60;
                if(unit == TimeUnit.SECONDS) break;
            case MILLISECONDS:
                if(timeUnit != TimeUnit.MILLISECONDS) result *= 1000;
                if(unit == TimeUnit.MILLISECONDS) break;
            default:
                throw new IllegalStateException("Unexpected value: " + unit);
        }
        return result;
    }

    public float to(TimeUnit unit) {
        float time;
        switch (unit) {
            case SECONDS:
                time = toSeconds();
                break;
            case MINUTES:
                time = toMinutes();
                break;
            case HOURS:
                time = toHours();
                break;
            case DAYS:
                time = toDays();
                break;
            case WEEKS:
                time = toWeeks();
                break;
            case MONTHS:
                time = toMonths();
                break;
            case YEARS:
                time = toYears();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + unit);
        }
        return time;
    }

    public float toSeconds() {
        return between1().getSeconds();
    }

    public float toMinutes() {
        return between1().toMinutes();
    }

    public float toHours() {
        return between1().toHours();
    }

    public float toDays() {
        return between2().getDays();
    }

    public float toWeeks() {
        return (float) between2().getDays() / 7;
    }

    public float toMonths() {
        return between2().getMonths();
    }

    public float toYears() {
        return between2().getYears();
    }

    private Duration between1() {
        LocalDateTime date1 = LocalDateTime.now();
        LocalDateTime date2;
        if(timeUnit.ordinal() <= TimeUnit.HOURS.ordinal())
            date2 = date1.plus(getDuration());
        else
            date2 = date1.plus(getPeriod());

        return Duration.between(date1, date2);
    }

    private Period between2() {
        LocalDate date1 = LocalDate.now();
        LocalDate date2;
        if(timeUnit == TimeUnit.MILLISECONDS || timeUnit == TimeUnit.SECONDS || timeUnit == TimeUnit.MINUTES ||
                timeUnit == TimeUnit.HOURS)
            date2 = date1.plus(getDuration());
        else
            date2 = date1.plus(getPeriod());

        return Period.between(date1, date2);
    }

    private Duration getDuration() {
        switch (timeUnit) {
            case MILLISECONDS:
                return Duration.ofMillis((long) time);
            case SECONDS:
                return Duration.ofSeconds((long) time);
            case MINUTES:
                return Duration.ofMinutes((long) time);
            case HOURS:
                return Duration.ofHours((long) time);
            case DAYS:
                return Duration.ofDays((long) time);
            default:
                throw new IllegalStateException("Unexpected value: " + timeUnit);
        }
    }

    private Period getPeriod() {
        switch (timeUnit) {
            case DAYS:
                return Period.ofDays((int) time);
            case WEEKS:
                return Period.ofWeeks((int) time);
            case MONTHS:
                return Period.ofMonths((int) time);
            case YEARS:
                return Period.ofYears((int) time);
            default:
                throw new IllegalStateException("Unexpected value: " + timeUnit);
        }
    }

}
