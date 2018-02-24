/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wahsis.payment.task;
import com.google.gson.Gson;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

/**
 *
 * @author diepth
 */
public class AddLogTask implements Runnable {

    private static AddLogTask _instance = null;
    private static final Lock createLock_ = new ReentrantLock();
    protected final Logger logger = Logger.getLogger(this.getClass());
    private static final Gson _gson = new Gson();
    private static final BlockingQueue<AddLogMessage> msgQueue = new LinkedBlockingQueue<AddLogMessage>();
    private static final int MSG_QUIT = 3;
    private Thread th = null;
    private Boolean start = false;

    public static AddLogTask getInstance() {
        if (_instance == null) {
            createLock_.lock();
            try {
                if (_instance == null) {
                    _instance = new AddLogTask();
                }
            } finally {
                createLock_.unlock();
            }
        }
        return _instance;
    }

    @Override
    public void run() {
        AddLogMessage msg = null;
        OUTER:
        while (true) {
            try {
                msg = msgQueue.take();
                if (msg != null) {
                    switch (msg.type) {
                        case MSG_QUIT:
                            break OUTER;
                        default:
                            break;
                    }
                }
            } catch (InterruptedException ex) {
                logger.error("AddLogTask Ex: " + ex.getMessage(), ex);
            }
        }
    }

    public void start() {
        if (th == null) {
            start = true;
            th = new Thread(this);
            th.start();
        }
    }

    public void stop() {
        try {
            start = false;
            AddLogMessage msg = new AddLogMessage(MSG_QUIT, null);
            msgQueue.offer(msg);
            th.join();
        } catch (InterruptedException e) {
        }
    }

    private class AddLogMessage {
        private int type;
        private String data;
        public AddLogMessage(int type, String data) {
            this.type = type;
            this.data = data;
        }
    }
}
