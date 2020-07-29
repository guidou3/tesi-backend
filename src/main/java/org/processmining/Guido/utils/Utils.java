package org.processmining.Guido.utils;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

public class Utils {
    public static String findNewString(Map<String, ?> map, String string) {
        boolean first = true;
        while(map.get(string) != null) {
            if(first) {
                string = string + "_1";
                first = false;
            }
            else {
                int pos = string.lastIndexOf("_");
                int num = Integer.parseInt(string.substring(pos+1));
                string = string.substring(0, pos+1) + (num+1);
            }
        }
        return string;
    }

    public static String findNewStringBis(Map<?, String> map, String string) {
        boolean first = true;

        while(map.containsValue(string)) {
            if(first) {
                string = string + "_1";
                first = false;
            }
            else {
                int pos = string.lastIndexOf("_");
                int num = Integer.parseInt(string.substring(pos+1));
                string = string.substring(0, pos+1) + (num+1);
            }
        }
        return string;
    }

    public static Color getColorForValue(float value) {
//        assert(value<=1 && value>=0);
//        value=0.2F+value*0.8F;
//        float red=(float) (Math.min(value, 0.3333)*3);
//        value=(float) Math.max(value-0.3333, 0);
//        float green=(float) (Math.min(value, 0.3333)*3);
//        value=(float) Math.max(value-0.3333, 0);
//        float blue=(float) (Math.min(value, 0.3333)*3);
//        return new Color(red,green,blue);
        int red = 255, green = 0;
        if(value < 0.5)
            green += (255) * value*2;
        else {
            green = 255;
            red -=  (255) * (value-0.5)*2;
        }
        return new Color(red, green,0);
    }

    public static String getHTMLColorString(Color color) {
        if(color == null) return null;
        String red = Integer.toHexString(color.getRed());
        String green = Integer.toHexString(color.getGreen());
        String blue = Integer.toHexString(color.getBlue());

        return "#" +
                (red.length() == 1? "0" + red : red) +
                (green.length() == 1? "0" + green : green) +
                (blue.length() == 1? "0" + blue : blue);
    }

    public static String getHTMLColorForValue(float value) {
        return getHTMLColorString(getColorForValue(value));
    }

    public static class PairOfStrings {
        public String _1;
        public String _2;

        public PairOfStrings(String s1, String s2) {
            this._1 = s1;
            this._2 = s2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PairOfStrings that = (PairOfStrings) o;
            return Objects.equals(_1, that._1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(_1);
        }
    }
}
