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

import org.iq80.leveldb.Snapshot;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在leveldb快照中每次都是用一个序列号保存当前插入的这一条记录，因此当插入多条相同的记录时，
 * 通过序列号来确定那一条是最新的记录，在leveldb的快照中，在调用一个快照时，
 * 只要获取在当前快照序列号以下的记录，就可以读取到这个快照之前的数据。
 * <p>
 * <p>
 * ReadOption中保持了snapshot，在拿到快照后，取出快照中的sequence number，根据传入的key和sequence number进文件中查找记录，这样就能查找快照之前的数据。
 *
 * UserKey是读写键值对时提供的键，只是一个简单的字符串，用slice表示。Internal Key是SSTable里实际存储的键值，由targetKey  SequenceNumber和ValueType组成的。
 * Internal Key在User Key的后面增加了一个64位的整数，并且将这个整数分为两部分，低位的一个字节是一个ValueType，高位的7个字节是一个SequenceNumber.
 * ValueTyp是为了区分一个键是插入还是删除，删除其实也是一条数据的插入，但是是一条特殊的插入，通过在User Key后面附上kTypeDeletion来说明要删除这个键，
 * kTypeValue说明是插入这个键。
 * SequenceNumber是一个版本号，是全局的，每次有一个键写入时，都会加一，每一个Internal Key里面都包含了不同的SequenceNumber。
 * SequenceNumber是单调递增的，SequenceNumber越大，表示这键越新，如果User Key相同，就会覆盖旧的键。所以就算User Key相同，
 * 对应的Internal Key也是不同的，Internal Key是全局唯一的。当我们更新一个User Key多次时，数据库里面可能保存了多个User Key，
 * 但是它们所在的Internal Key是不同的，并且SequenceNumber可以决定写入的顺序。
 * 当用户写入时，将User Key封装成Internal Key，保留版本信息，存储到SSTable里，当需要读取时，将User Key从Internal Key里提取出来，
 * 所有User Key相同的Internal Key里面SequenceNumber最大的Internal Key就是当前的键，它对应的值就是当前值。
 *
 * @author chenxu
 */
public class SnapshotImpl implements Snapshot {
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Version version;
    private final long lastSequence;

    SnapshotImpl(Version version, long lastSequence) {
        this.version = version;
        this.lastSequence = lastSequence;
        this.version.retain();
    }

    @Override
    public void close() {
        // This is an end user API.. he might screw up and close multiple times.
        // but we don't want the version reference count going bad.
        if (closed.compareAndSet(false, true)) {
            this.version.release();
        }
    }

    public long getLastSequence() {
        return lastSequence;
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return Long.toString(lastSequence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SnapshotImpl snapshot = (SnapshotImpl) o;

        if (lastSequence != snapshot.lastSequence) {
            return false;
        }
        if (!version.equals(snapshot.version)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = version.hashCode();
        result = 31 * result + (int) (lastSequence ^ (lastSequence >>> 32));
        return result;
    }
}
