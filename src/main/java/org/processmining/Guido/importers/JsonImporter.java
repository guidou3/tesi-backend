package org.processmining.Guido.importers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import org.processmining.Guido.CustomElements.CustomElements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JsonImporter {
    public static CustomElements importJson(File file) {
        try {
            Gson gson = new Gson();

            return gson.fromJson(filterJson(file), CustomElements.class);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static JsonElement filterJson(File file) throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(file));
        return Streams.parse(reader).getAsJsonObject().get("definitions");
    }
}
