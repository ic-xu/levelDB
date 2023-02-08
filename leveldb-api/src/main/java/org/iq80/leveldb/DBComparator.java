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

import java.util.Comparator;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface DBComparator extends Comparator<byte[]> {
    String name();

    /**
     * If {@code start < limit}, returns a short key in [start,limit).
     * Simple comparator implementations should return start unchanged,
     * <p>
     * FindShortestSeparator(start, limit)作用是：如果start < limit，就返回一个key
     */
    byte[] findShortestSeparator(byte[] start, byte[] limit);

    /**
     * FindShortSuccessor。直接对key中第一个以uint8方式+1的字节+1，清除该位后面的数据。
     * <p>
     * returns a 'short key' where the 'short key' is greater than or equal to key.
     * Simple comparator implementations should return key unchanged,
     */
    byte[] findShortSuccessor(byte[] key);
}
