package cn.moyada.sharingan.monitor.api;

import cn.moyada.sharingan.monitor.api.entity.Invocation;

/**
 * @author xueyikang
 * @since 1.0
 **/
public class TestMonitor implements Monitor {

    @Override
    public void listener(Invocation invocation) {
        System.out.println(invocation.toString());
    }
}