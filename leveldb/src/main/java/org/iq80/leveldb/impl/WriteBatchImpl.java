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
package org.iq80.leveldb.impl;

import com.google.common.collect.Maps;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.util.Slice;
import org.iq80.leveldb.util.Slices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;

public class WriteBatchImpl implements WriteBatch {

    /**
     * 可以看到WriteBatch中有一个变量为batch用来保存每一次的操作，
     * <p>
     * 还有一个变量为approximateSize，用来保存key和value的总字节数大小
     */
    private final List<Entry<Slice, Slice>> batch = new ArrayList<>();
    private int approximateSize;

    public int getApproximateSize() {
        return approximateSize;
    }

    public int size() {
        return batch.size();
    }


    /**
     * 这里batch add了immutable的Entry，同时将key和value进行了一层Slice的包装，
     * 在approximateSize值增加了key和value的length，同时增加了12。
     * 在存储当前字符串时，需要有一个8字节的序列号和4字节的记录数作为头，因此在申请空间存放的时候要多加上12个字节的大小。
     * <p>
     * 插入的记录由kTypeValue+key长度+key+value长度+value组成。
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public WriteBatchImpl put(byte[] key, byte[] value) {
        requireNonNull(key, "key is null");
        requireNonNull(value, "value is null");
        batch.add(Maps.immutableEntry(Slices.wrappedBuffer(key), Slices.wrappedBuffer(value)));
        approximateSize += 12 + key.length + value.length;
        return this;
    }

    /**
     * 此put方法传入slice key和slice value，batch add一个immutbaleEntry，将Key和value传入Maps
     * @param key key
     * @param value value
     * @return WriteBatchImpl
     */
    public WriteBatchImpl put(Slice key, Slice value) {
        requireNonNull(key, "key is null");
        requireNonNull(value, "value is null");
        batch.add(Maps.immutableEntry(key, value));
        approximateSize += 12 + key.length() + value.length();
        return this;
    }

    /**
     * batch add一个slice封装的key  approximateSize大小=6+key长度
     * @param key key
     * @return
     */
    @Override
    public WriteBatchImpl delete(byte[] key) {
        requireNonNull(key, "key is null");
        batch.add(Maps.immutableEntry(Slices.wrappedBuffer(key), (Slice) null));
        approximateSize += 6 + key.length;
        return this;
    }

    public WriteBatchImpl delete(Slice key) {
        requireNonNull(key, "key is null");
        batch.add(Maps.immutableEntry(key, (Slice) null));
        approximateSize += 6 + key.length();
        return this;
    }

    @Override
    public void close() {
    }

    /**
     * for循环依次处理add进来的key和value
     * @param handler handler
     * @see org.iq80.leveldb.impl.WriteBatchImpl.Handler
     */
    public void forEach(Handler handler) {
        for (Entry<Slice, Slice> entry : batch) {
            Slice key = entry.getKey();
            Slice value = entry.getValue();
            if (value != null) {
                handler.put(key, value);
            } else {
                handler.delete(key);
            }
        }
    }

    public interface Handler {
        void put(Slice key, Slice value);

        void delete(Slice key);
    }
}
