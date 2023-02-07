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
package org.iq80.leveldb.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public class Finalizer<T> {
    public static final FinalizerMonitor IGNORE_FINALIZER_MONITOR = new FinalizerMonitor() {
        @Override
        public void unexpectedException(Throwable throwable) {
        }
    };

    private final int threads;

    /**
     * 垃圾回收监控器，出现一场的时候，会调用这个接口的方法实现
     */
    private final FinalizerMonitor monitor;

    private final ConcurrentHashMap<FinalizerPhantomReference<T>, Object> references = new ConcurrentHashMap<>();
    private final ReferenceQueue<T> referenceQueue = new ReferenceQueue<>();
    private final AtomicBoolean destroyed = new AtomicBoolean();
    private ExecutorService executor;

    public Finalizer() {
        this(1, IGNORE_FINALIZER_MONITOR);
    }

    public Finalizer(int threads) {
        this(1, IGNORE_FINALIZER_MONITOR);
    }

    public Finalizer(int threads, FinalizerMonitor monitor) {
        this.monitor = monitor;
        checkArgument(threads >= 1, "threads must be at least 1");
        this.threads = threads;
    }

    public synchronized void addCleanup(T item, Callable<?> cleanup) {
        requireNonNull(item, "item is null");
        requireNonNull(cleanup, "cleanup is null");
        checkState(!destroyed.get(), "%s is destroyed", getClass().getName());

        if (executor == null) {
            // create executor
            ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("FinalizerQueueProcessor-%d").setDaemon(true).build();
            executor = Executors.newFixedThreadPool(threads, threadFactory);

            // start queue processor jobs
            for (int i = 0; i < threads; i++) {
                executor.submit(new FinalizerQueueProcessor());
            }
        }

        // create a reference to the item so we are notified when it is garbage collected
        FinalizerPhantomReference<T> reference = new FinalizerPhantomReference<>(item, referenceQueue, cleanup);

        // we must keep a strong reference to the reference object so we are notified when the item
        // is no longer reachable (if the reference object is garbage collected we are never notified)
        references.put(reference, Boolean.TRUE);
    }

    public synchronized void destroy() {
        destroyed.set(true);
        if (executor != null) {
            executor.shutdownNow();
        }
        for (FinalizerPhantomReference<T> r : references.keySet()) {
            try {
                r.cleanup();
            } catch (Exception e) {
            }
        }
    }

    public interface FinalizerMonitor {
        void unexpectedException(Throwable throwable);
    }

    /**
     * 使用虚引用的包装类，主要用来标识什么时候进行垃圾回收
     * @param <T>
     */
    private static class FinalizerPhantomReference<T> extends PhantomReference<T> {
        private final AtomicBoolean cleaned = new AtomicBoolean(false);
        private final Callable<?> cleanup;

        private FinalizerPhantomReference(T referent, ReferenceQueue<? super T> queue, Callable<?> cleanup) {
            super(referent, queue);
            this.cleanup = cleanup;
        }

        private void cleanup() throws Exception {
            if (cleaned.compareAndSet(false, true)) {
                cleanup.call();
            }
        }
    }

    /**
     * 垃圾回收队列处理器
     */
    private class FinalizerQueueProcessor implements Runnable {
        @Override
        public void run() {
            /**
             * 标识是否销毁，当销毁的时候，结束循环调用
             */
            while (!destroyed.get()) {
                /**
                 * 获取下一个已经进行GC 通知的引用对象
                 */
                // get the next reference to cleanup
                FinalizerPhantomReference<T> reference;
                try {
                    reference = (FinalizerPhantomReference<T>) referenceQueue.remove();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // remove the reference object itself from our list of references
                /**
                 * 从列表中移除引用
                 */
                references.remove(reference);

                boolean rescheduleAndReturn = false;
                try {
                    reference.cleanup();
                    rescheduleAndReturn = Thread.currentThread().isInterrupted();
                } catch (Throwable userException) {
                    try {
                        monitor.unexpectedException(userException);
                    } catch (Exception ignored) {
                        // todo consider a broader notification
                    }

                    if (userException instanceof InterruptedException) {
                        rescheduleAndReturn = true;
                        Thread.currentThread().interrupt();
                    } else if (userException instanceof Error) {
                        rescheduleAndReturn = true;
                    }
                }

                if (rescheduleAndReturn) {
                    synchronized (Finalizer.this) {
                        if (!destroyed.get()) {
                            executor.submit(new FinalizerQueueProcessor());
                        }
                    }
                    return;
                }
            }
        }
    }
}
