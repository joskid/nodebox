package nodebox.function;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import nodebox.util.ReflectionUtils;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class DataFunctions {


    public static final FunctionLibrary LIBRARY;

    static {
        LIBRARY = JavaLibrary.ofClass("data", DataFunctions.class,
                "lookup", "makeStrings", "importCSV");
    }

    /**
     * Try a number of ways to lookup a key in an object.
     *
     * @param o   The object to search
     * @param key The key to find.
     * @return The value of the key if found, otherwise null.
     */
    public static Object lookup(Object o, String key) {
        if (o == null || key == null) return null;
        if (o instanceof Map) {
            Map m = (Map) o;
            return m.get(key);
        } else {
            return ReflectionUtils.get(o, key, null);
        }
    }

    /**
     * Make a list of strings from a big string with separators.
     * Whitespace is not stripped.
     *
     * @param s         The input string, e.g. "a;b;c"
     * @param separator The separator, e.g. ";". If the separator is empty, return each character separately.
     * @return A list of strings.
     */
    public static List<String> makeStrings(String s, String separator) {
        if (s == null) {
            return ImmutableList.of();
        }
        if (separator == null || separator.isEmpty()) {
            return ImmutableList.copyOf(Splitter.fixedLength(1).split(s));
        }
        return ImmutableList.copyOf(Splitter.on(separator).split(s));
    }

    /**
     * Import the CSV from a file.
     * <p/>
     * This method assumes the first row is the header row. It will not be returned: instead, it will serves as the
     * keys for the maps we return.
     *
     * @param fileName The file to read in.
     * @return A list of maps.
     */
    public static List<Map<String, String>> importCSV(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) return ImmutableList.of();
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            CSVReader reader = new CSVReader(in);
            ImmutableList.Builder<Map<String, String>> b = ImmutableList.builder();
            String[] headers = reader.readNext();
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }
            String[] row;

            while ((row = reader.readNext()) != null) {
                ImmutableMap.Builder<String, String> mb = ImmutableMap.builder();
                for (int i = 0; i < row.length; i++) {
                    String header = headers[i];
                    String value = row[i].trim();
                    mb.put(header, value);
                }
                b.add(mb.build());
            }

            return b.build();
        } catch (IOException e) {
            throw new RuntimeException("Could not read file " + fileName + ": " + e.getMessage(), e);
        }
    }

}
