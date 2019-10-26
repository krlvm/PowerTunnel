package ru.krlvm.powertunnel.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data store class
 *
 * @author krlvm
 */
public class DataStore {

    //Constants
    public static final String GOVERNMENT_BLACKLIST = "government-blacklist";
    public static final String ISP_STUB_LIST = "isp-stub";
    public static final String USER_BLACKLIST = "user-blacklist";
    public static final String USER_WHITELIST = "user-whitelist";

    private static final char EXTENSION_SEPARATOR = '.';

    private final String fileName;
    protected List<String> loadedLines;

    /**
     * DataStore constructor
     *
     * @param fileName - data store file name
     */
    public DataStore(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Retrieves data
     *
     * @return - data store data
     * @throws IOException - read failure
     */
    public List<String> load() throws IOException {
        loadedLines = new ArrayList<>();
        File file = getFile();
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                loadedLines.add(line);
            }
            reader.close();
            return loadedLines;
        } else {
            create(file);
            return loadedLines;
        }
    }

    /**
     * Creates data store
     *
     * @param file - data store file
     * @throws IOException - write failure
     */
    public void create(File file) throws IOException {
        write(file, new ArrayList<String>());
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
     * Retrieves data store file format
     *
     * @return data store file format
     */
    public String getFileFormat() {
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
     * Retrieves data store file
     *
     * @return data store file
     */
    public File getFile() {
        return new File(getFileName() + EXTENSION_SEPARATOR + getFileFormat());
    }

    /**
     * Retrieves data store file name
     *
     * @return data store file name
     */
    public String getFileName() {
        return fileName;
    }
}