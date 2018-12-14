/*
 * Copyright 2018 Warkdev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.jangos.extractor.file;

import eu.jangos.extractor.file.exception.ADTException;
import eu.jangos.extractor.file.exception.FileReaderException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Warkdev
 */
public abstract class FileReader {
    
    protected String filename;
    protected ByteBuffer data;
    protected boolean init = false;

    public abstract void init(byte[] data, String filename) throws IOException, FileReaderException;
    
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }        
    
    protected void checkHeader(String expectedHeader) throws FileReaderException {
        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new FileReaderException(this.filename + " - Expected header " + expectedHeader + ", received header: " + sb.toString());
        }
    }
    
    protected List<Integer> readIntegerChunk(int offset, String expectedHeader) throws ADTException {
        List<Integer> intList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        byte[] header = new byte[4];

        this.data.position(offset);

        this.data.get(header);

        sb = sb.append(new String(header)).reverse();
        if (!sb.toString().equals(expectedHeader)) {
            throw new ADTException("Expected header " + expectedHeader + ", received header: " + sb.toString());
        }

        int size = this.data.getInt();
        int start = this.data.position();
        while (this.data.position() - start < size) {
            intList.add(this.data.getInt());
        }

        return intList;
    }
    
    protected List<String> readStringChunk(int offset, String expectedHeader) throws FileReaderException {
        List<String> stringList = new ArrayList<>();

        this.data.position(offset);

        checkHeader(expectedHeader);

        int size = this.data.getInt();
        int start = this.data.position();
        String temp;
        while (this.data.position() - start < size) {
            temp = readString(this.data);
            if (!temp.isEmpty()) {
                stringList.add(temp);
            }
        }

        return stringList;
    }
    
    protected String readString(ByteBuffer in) {
        StringBuilder sb = new StringBuilder();

        while (in.remaining() > 0) {
            char c = (char) in.get();
            if (c == '\0') {
                break;
            }
            sb.append(c);
        }

        return sb.toString();
    }
    
    protected String readPaddedString(ByteBuffer in, int padding) {
        StringBuilder sb = new StringBuilder();

        while (in.remaining() > 0) {
            char c = (char) in.get();
            if (c == '\0') {
                // There's 0 padding at the end of string and we want to skip them except if there's only one 0.                
                int skip = padding - ((sb.length() + 1) % padding);
                in.position(in.position() + (skip == padding ? 0 : skip));
                break;
            }
            sb.append(c);
        }

        return sb.toString();
    }
    
    protected Map<Integer, String> readStringChunkAsMap(int offset, String expectedHeader) throws FileReaderException {
        Map<Integer, String> stringMap = new HashMap<>();

        this.data.position(offset);

        checkHeader(expectedHeader);

        int size = this.data.getInt();
        int start = this.data.position();
        int recordOffset;
        String temp;
        while (this.data.position() - start < size) {
            recordOffset = this.data.position() - offset - 8;
            temp = readPaddedString(this.data, 4);
            if (!temp.isEmpty()) {
                stringMap.put(recordOffset, temp);
            }
        }

        return stringMap;
    }
}
