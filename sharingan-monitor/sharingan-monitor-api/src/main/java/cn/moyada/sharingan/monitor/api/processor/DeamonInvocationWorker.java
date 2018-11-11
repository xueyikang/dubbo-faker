package cn.moyada.sharingan.monitor.api.processor;

import cn.moyada.sharingan.monitor.api.entity.Record;
import cn.moyada.sharingan.monitor.api.handler.InvocationHandler;
import cn.moyada.sharingan.monitor.api.receiver.InvocationReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author xueyikang
 * @since 1.0
 **/
public abstract class DeamonInvocationWorker extends AbstractInvocationWorker<Collection<Record>, Record> {

    protected int size;

    protected List<Record> nextQueue;

    public DeamonInvocationWorker(InvocationHandler<Collection<Record>> handler,
                                  InvocationReceiver<Record> receiver,
                                  int size) {
        super(handler, receiver);
        this.nextQueue = new ArrayList<>(size);
        this.size = size;
        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
    }

    class ShutdownHookThread extends Thread {

        @Override
        public void run() {
            if (nextQueue.isEmpty()) {
                return;
            }

            handler.handle(nextQueue);
        }
    }
}