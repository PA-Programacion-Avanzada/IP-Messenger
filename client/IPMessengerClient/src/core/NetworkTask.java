package core;

import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;

/**
 * Helper para ejecutar tareas de red en un SwingWorker.
 */
public abstract class NetworkTask<T> extends SwingWorker<T, Void> {

    protected abstract T doTask() throws Exception;
    protected abstract void onSuccess(T result);
    protected abstract void propagateError(Throwable ex);

    @Override
    protected T doInBackground() throws Exception {
        return doTask();
    }

    @Override
    protected void done() {
        try {
            T result = get();
            onSuccess(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            propagateError(e);
        } catch (ExecutionException e) {
            propagateError(e.getCause());
        }
    }
}