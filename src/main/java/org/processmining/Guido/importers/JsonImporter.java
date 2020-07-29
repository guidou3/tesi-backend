package org.processmining.Guido.importers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import org.processmining.Guido.CustomElements.CustomElements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class JsonImporter {
    public static CustomElements importJson(File file) {
        try {
            Gson gson = new Gson();

            return gson.fromJson(new FileReader(file), CustomElements.class);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
