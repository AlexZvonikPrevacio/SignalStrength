package pervacio.com.wifisignalstrength.speedMeasurer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;

import fr.bmartel.speedtest.SpeedTestSocket;
import pervacio.com.wifisignalstrength.speedMeasurer.speedListeners.AbstractSpeedListener;

public class Router implements ISpeedListenerFinishCallback {

    private List<ListenerAndHandlerWrapper> mListenerAndHandlers;
    private SpeedTestSocket mSpeedTestSocket;
    private LastListenerFinished mLastListenerFinished;
    private int mSerialNumber;

    public Router(List<ListenerAndHandlerWrapper> listenerAndHandlers, LastListenerFinished lastListenerFinished) {
        mListenerAndHandlers = listenerAndHandlers;
        mSpeedTestSocket = new SpeedTestSocket();
        mLastListenerFinished = lastListenerFinished;
    }

    /**
     * Calls when task finishes
     *
     * @param speedListener callback listener
     */
    @Override
    public void onSpeedListenerFinish(AbstractSpeedListener speedListener) {
        Log.w("Router", "onSpeedListenerFinish");
        if (mListenerAndHandlers.size() == mSerialNumber) {
            if (mLastListenerFinished != null) {
                Log.d("Router", "onSpeedListenerFinish() called  mListenerAndHandlers.size() == mSerialNumber " + (mListenerAndHandlers.size() == mSerialNumber));
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mLastListenerFinished.onLastTaskCompleted();
                    }
                });
            }
        } else {
            startTask(mSerialNumber++);
        }
    }

    public void route() {
        startTask(mSerialNumber++);
    }

    /**
     * Create handlers from callbacks and start tasks one by one
     *
     * @param serialNumber number of the task
     */
    private void startTask(int serialNumber) {
        if (mListenerAndHandlers.size() > serialNumber) {
            ListenerAndHandlerWrapper listenerAndHandler = mListenerAndHandlers.get(serialNumber);

            WorkerThread.WorkerTask mWorkerTask = listenerAndHandler.mWorkerTask;
            Handler.Callback mCallback = listenerAndHandler.mCallback;
            SpeedListenerHandler handler = new SpeedListenerHandler(Looper.getMainLooper(), mCallback);

            mWorkerTask.execute(mSpeedTestSocket, handler, this);
        }
    }

    public interface LastListenerFinished {
        void onLastTaskCompleted();
    }

}