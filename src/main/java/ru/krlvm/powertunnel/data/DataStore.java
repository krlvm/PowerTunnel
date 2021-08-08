package ru.krlvm.powertunnel.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Data store class
 *
 * @author krlvm
 */
public class DataStore {

    //Constants
    public static final DataStore GOVERNMENT_BLACKLIST = new DataStore("government-blacklist", Collections.singletonList("*"));
    public static final DataStore ISP_STUB_LIST = new DataStore("isp-stub");
    public static final DataStore USER_BLACKLIST = new DataStore("user-blacklist");
    public static final DataStore USER_WHITELIST = new DataStore("user-whitelist");

    private static final char EXTENSION_SEPARATOR = '.';

    private final String fileName;
    protected List<String> loadedLines;
    protected List<String> defaults = new ArrayList<>();

    /**
     * DataStore constructor
     *
     * @param fileName - data store file name
     */
    public DataStore(String fileName) {
        this.fileName = fileName;
    }

    /**
     * DataStore constructor
     *
     * @param fileName - data store file name
     * @param defaults - default lines
     */
    public DataStore(String fileName, List<String> defaults) {
        this.fileName = fileName;
        this.defaults = defaults;
    }

    /**
     * DataStore constructor
     *
     * @param fileName - data store file name
     * @param defaultLine - default line
     */
    public DataStore(String fileName, String defaultLine) {
        this.fileName = fileName;
        this.defaults = Collections.singletonList(defaultLine);
    }

    /**
     * Retrieves data
     *
     * @return - data store data
     * @throws IOException - read failure
     */
    public List<String> load() throws IOException {
        return filteredLoad(null);
    }

    /**
     * Retrieves filtered data
     *
     * @param filter - filter
     *
     * @return - filtered data store data
     * @throws IOException - read failure
     */
    public List<String> filteredLoad(final Filter filter) throws IOException {
        loadedLines = new ArrayList<>();
        File file = getFile();
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if(filter != null) {
                    if(!filter.accept(line)) {
                        continue;
                    }
                }
                loadedLines.add(line);
            }
            reader.close();
        } else {
            create(file);
            if(defaults != null) {
                write(defaults);
            }
            loadedLines = defaults;
        }
        if(filter != null && loadedLines.isEmpty()) {
            loadedLines = null;
        }
        return loadedLines;
    }

    /**
     * Creates data store
     *
     * @param file - data store file
     * @throws IOException - write failure
     */
    public void create(File file) throws IOException {
        write(file, new ArrayList<>());
    }

    /**
     * Creates data store
     *
     * @param line - data store content
     * @throws IOException - write failure
     */
    public void write(String line) throws IOException {
        write(getFile(), Collections.singletonList(line));
    }

    /**
     * Creates data store
     *
     * @param lines - data store contents
     * @throws IOException - write failure
     */
    public void write(Collection<String> lines) throws IOException {
        write(getFile(), lines);
    }

    /**
     * Creates data store
     *
     * @param file - data store file
     * @param lines - data store contents
     * @throws IOException - write failure
     */
    public void write(File file, Collection<String> lines) throws IOException {
        if(file.exists()) {
            file.delete();
        }
        int current = 0;
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (String line : lines) {
            writer.write(line);
            if(++current != lines.size()) {
                writer.write("\r\n");
            }
        }
        writer.flush();
        writer.close();
    }

    /**
     * Retrieves data store file extension
     *
     * @return data store file extension
     */
    public String getFileExtension() {
        return "txt";
    }

    /**
     * Retrieves data store contents (loaded lines)
     *
     * @return data store contents
     */
    public List<String> getLoadedLines() {
        return loadedLines;
    }

    /**
     * Retrieves data store contents (in one line)
     *
     * @return data store contents
     */
    public String inline() {
        StringBuilder builder = new StringBuilder();
        for (String line : loadedLines) {
            builder.append(line);
        }
        return builder.toString();
    }

    /**
     * Retrieves data store file
     *
     * @return data store file
     */
    public File getFile() {
        return new File(getFileName() + EXTENSION_SEPARATOR + getFileExtension());
    }

    /**
     * Retrieves data store file name
     *
     * @return data store file name
     */
    public String getFileName() {
        return fileName;
    }

    public interface Filter {
        boolean accept(String line);
    }
}