/*
 * Copyright (C) 2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iq80.leveldb;

/**
 * 选项
 */
public class Options {
    private boolean createIfMissing = true;
    private boolean errorIfExists;
    private int writeBufferSize = 4 << 20;

    private int maxOpenFiles = 1000;

    private int blockRestartInterval = 16;
    private int blockSize = 4 * 1024;
    private CompressionType compressionType = CompressionType.SNAPPY;
    private boolean verifyChecksums = true;
    private boolean paranoidChecks;
    private DBComparator comparator;
    private Logger logger;
    private long cacheSize;

    static void checkArgNotNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("The " + name + " argument cannot be null");
        }
    }

    public boolean createIfMissing() {
        return createIfMissing;
    }

    public Options createIfMissing(boolean createIfMissing) {
        this.createIfMissing = createIfMissing;
        return this;
    }

    public boolean errorIfExists() {
        return errorIfExists;
    }

    public Options errorIfExists(boolean errorIfExists) {
        this.errorIfExists = errorIfExists;
        return this;
    }

    public int writeBufferSize() {
        return writeBufferSize;
    }

    /**
     * writeBufferSize:memetable的大小超过writebuffersize时，转为immemtable
     * @param writeBufferSize bufferSize
     * @return option
     */
    public Options writeBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
        return this;
    }

    /**
     * maxOpenFile:DB可打开的最大文件数
     * @return int
     */
    public int maxOpenFiles() {
        return maxOpenFiles;
    }

    public Options maxOpenFiles(int maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
        return this;
    }

    /**
     * blockRestartInterval: block重启点之间的key的个数
     * @return int
     */
    public int blockRestartInterval() {
        return blockRestartInterval;
    }

    public Options blockRestartInterval(int blockRestartInterval) {
        this.blockRestartInterval = blockRestartInterval;
        return this;
    }

    /**
     * blockSize:每一个block大小，指压缩数据大小
     * @return block size
     */
    public int blockSize() {
        return blockSize;
    }

    public Options blockSize(int blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    /**
     * CompressionType:压缩block的方式
     * @return CompressionType
     */
    public CompressionType compressionType() {
        return compressionType;
    }

    public Options compressionType(CompressionType compressionType) {
        checkArgNotNull(compressionType, "compressionType");
        this.compressionType = compressionType;
        return this;
    }

    /**
     * verifChecksums:所有读取数据都会校验
     * @return boolean
     */
    public boolean verifyChecksums() {
        return verifyChecksums;
    }

    public Options verifyChecksums(boolean verifyChecksums) {
        this.verifyChecksums = verifyChecksums;
        return this;
    }


    /**
     * cacheSize:缓存大小
     * @return long
     */
    public long cacheSize() {
        return cacheSize;
    }

    public Options cacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    /**
     * DBcomparator:
     * @return DBComparator
     */
    public DBComparator comparator() {
        return comparator;
    }

    public Options comparator(DBComparator comparator) {
        this.comparator = comparator;
        return this;
    }


    /**
     * logger:db产生的处理和错误日志写入指定文件，若为空，则在同一个目录中创建一个文件写入db
     * @return logger
     */
    public Logger logger() {
        return logger;
    }

    public Options logger(Logger logger) {
        this.logger = logger;
        return this;
    }


    /**
     * paranoidchecks:如果为true，数据处理过程会严格检查数据，检查到任何数据都会提前停止
     * @return boolean
     */
    public boolean paranoidChecks() {
        return paranoidChecks;
    }

    public Options paranoidChecks(boolean paranoidChecks) {
        this.paranoidChecks = paranoidChecks;
        return this;
    }
}
