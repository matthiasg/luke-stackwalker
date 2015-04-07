package de.uni_heidelberg.cos.agw.stackwalker;

import java.util.ArrayList;
import java.util.List;

/**
 * A data parameter that is represented in the file names via a tag
 * followed by a value.
 * <p>
 * <h4>Example</h4>
 * <p>A measurement over time of multiple channels, the data is split into
 * single files for each combination of DataTypes.</p>
 * <ul>
 * <li>Experiment1Time001Chn1.csv</li>
 * <li>Experiment1Time001Chn2.csv</li>
 * <li>Experimene1Time002Chn1.csv</li>
 * <li>Experiment1Time002Chn2.csv</li>
 * <li>...
 * </ul>
 * <p>
 * <p>In this example, DataTypes are Timepoint and Channel, their file name
 * tags are "Time" and "Chn", respectively. Their value is the value of
 * all digits directly following the file name tags.</p>
 */
public class DataType {

    public static final List<DataType> LIST = new ArrayList<DataType>();
    protected int blockStart = -1;
    protected int blockSize = -1;
    /**
     * the name of the DataType.
     */
    private String name = "";

    /**
     * the file name tag
     */
    private String fileNameTag = "";

    /**
     * whether or not the DataType is activated and will be used to compute
     * the data file hierarchy
     */
    private boolean isActive = true;

    private boolean hasFixedBlockStart = false;
    private boolean hasFixedBlockSize = false;
    private boolean isInitialized = true;

    public DataType() {
    }

    public DataType(final String name, final String fileNameTag) {
        setName(name);
        setFileNameTag(fileNameTag);
    }

    public static boolean initializeAll(final String template) {
        for (final DataType type : LIST) {
            if (!type.isActive()) {
                continue;
            }
            if (!type.initialize(template)) {
                return false;
            }
        }
        return true;
    }

    public static boolean initializeAll(final List<String> templates) {
        for (final String template : templates) {
            if (initializeAll(template)) {
                return true;
            }
        }
        return false;
    }

    public int getLevel() {
        return LIST.indexOf(this);
    }

    /**
     * Returns the file name tag of this DataType.
     *
     * @return the file name tag of this DataType
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the file name tag.
     *
     * @return the file name tag
     */
    public String getFileNameTag() {
        return fileNameTag;
    }

    /**
     * Sets the file name tag.
     *
     * @param tag the file name tag
     */
    public void setFileNameTag(final String tag) {
        fileNameTag = tag;
        blockStart = -1;
        blockSize = -1;
        isInitialized = false;
    }

    /**
     * Returns whether or not this DataType is used for processing.
     *
     * @return whether or not this DataType is used for processing
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets whether or not this DataType is used for processing.
     *
     * @param state whether or not this DataType is used for processing
     */
    public void setActive(final boolean state) {
        isActive = state;
    }

    public boolean hasFixedNumBlockSize() {
        return hasFixedBlockSize;
    }

    public void setFixedNumBlockSize(final boolean state) {
        if (state == hasFixedBlockSize)
            return;
        hasFixedBlockSize = state;
        blockStart = -1;
        blockSize = -1;
        isInitialized = false;
    }

    public boolean hasFixedNumBlockStart() {
        return hasFixedBlockStart;
    }

    public void setFixedNumBlockStart(final boolean state) {
        if (state == hasFixedBlockStart)
            return;
        hasFixedBlockStart = state;
        blockStart = -1;
        blockSize = -1;
        isInitialized = false;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean initialize(final String template) {
        if (isInitialized)
            return true;

        if (!hasFixedBlockStart && !hasFixedBlockSize) {
            isInitialized = true;
            return true;
        }

        int start = template.lastIndexOf(fileNameTag);
        while (start != -1) {
            int size = 0;
            for (final char c : template.substring(start + fileNameTag.length()).toCharArray()) {
                if (Character.isDigit(c)) {
                    size++;
                } else {
                    break;
                }
            }
            if (size > 0) {
                if (hasFixedBlockStart) {
                    blockStart = start;
                }
                if (hasFixedBlockSize)
                    blockSize = fileNameTag.length() + size;
                isInitialized = true;
                return true;
            }
            start = template.lastIndexOf(fileNameTag, start);
        }
        return false;
    }

    // expects the type to be initialized!
    public int getValue(final String template) {
        int start;
        if (hasFixedBlockStart && fileNameTag.equals(template.substring(blockStart, blockStart + fileNameTag.length()))) {
            start = blockStart;
        } else {
            start = template.lastIndexOf(fileNameTag);
        }

        while (start != -1) {
            final int numBlockStart = start + fileNameTag.length();

            if (hasFixedBlockSize) {
                try {
                    return Integer.parseInt(template.substring(numBlockStart, start + blockSize));
                } catch (NumberFormatException e) {
                }
            }

            int size = 0;
            for (final char c : template.substring(numBlockStart).toCharArray()) {
                if (Character.isDigit(c)) {
                    size++;
                } else if (size > 0) {
                    return Integer.parseInt(template.substring(numBlockStart, numBlockStart + size));
                }
            }

            start = template.lastIndexOf(fileNameTag, start);
        }

        return -1;
    }
}