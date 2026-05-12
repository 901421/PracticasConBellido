package es.unizar.eina.SistemaReservas.util;

import androidx.annotation.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Decorador para ExecutorService que integra el IdlingResource de Espresso.
 */
public class IdlingExecutorService implements ExecutorService {
    private final ExecutorService delegate;

    public IdlingExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        EspressoIdlingResource.increment();
        delegate.execute(() -> {
            try {
                command.run();
            } finally {
                EspressoIdlingResource.decrement();
            }
        });
    }

    @Override public void shutdown() { delegate.shutdown(); }
    @Override public List<Runnable> shutdownNow() { return delegate.shutdownNow(); }
    @Override public boolean isShutdown() { return delegate.isShutdown(); }
    @Override public boolean isTerminated() { return delegate.isTerminated(); }
    @Override public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }
    @NonNull @Override public <T> Future<T> submit(@NonNull Callable<T> task) { 
        EspressoIdlingResource.increment();
        return delegate.submit(() -> {
            try { return task.call(); } finally { EspressoIdlingResource.decrement(); }
        });
    }
    @NonNull @Override public <T> Future<T> submit(@NonNull Runnable task, T result) {
        EspressoIdlingResource.increment();
        return delegate.submit(() -> {
            try { task.run(); return result; } finally { EspressoIdlingResource.decrement(); }
        });
    }
    @NonNull @Override public Future<?> submit(@NonNull Runnable task) {
        EspressoIdlingResource.increment();
        return delegate.submit(() -> {
            try { task.run(); } finally { EspressoIdlingResource.decrement(); }
        });
    }
    @NonNull @Override public <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks) throws InterruptedException { return delegate.invokeAll(tasks); }
    @NonNull @Override public <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks, long timeout, @NonNull TimeUnit unit) throws InterruptedException { return delegate.invokeAll(tasks, timeout, unit); }
    @NonNull @Override public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException { return delegate.invokeAny(tasks); }
    @Override public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks, long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException { return delegate.invokeAny(tasks, timeout, unit); }
}