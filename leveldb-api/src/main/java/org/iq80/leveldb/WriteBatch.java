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

import java.io.Closeable;

/**
 * 批量操作，批量写和批量删除，
 * <p>
 * LevelDB使用WriteBatch来替代简单的异步写操作，首先将所有的写操作记录到一个batch中，然后执行同步写，这样同步写的开销就被分摊到多个写操作中，降低同步写入的成本。
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface WriteBatch extends Closeable {



    WriteBatch put(byte[] key, byte[] value);


    /**
     * WriteBatch的基本操作是记录一个要插入或删除某个数据的操作
     * ，最基本的操作就是Put和Delete.LevelDB插入和删除数据并不是直接插入、删除数据
     * ，而是插入一条记录（由记录的类型标志位来确定是要插入数据还是删除数据，具体的插入、删除操作将在后台Compaction时进行）
     * @param key key
     * @return WriteBatch
     */
    WriteBatch delete(byte[] key);
}
