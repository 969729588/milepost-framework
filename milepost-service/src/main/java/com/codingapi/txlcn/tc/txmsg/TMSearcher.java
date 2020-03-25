/*
 * Copyright 2017-2019 CodingApi .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codingapi.txlcn.tc.txmsg;

import com.codingapi.txlcn.common.util.Transactions;
import com.codingapi.txlcn.common.util.id.ModIdProvider;
import com.codingapi.txlcn.tc.config.TxClientConfig;
import com.codingapi.txlcn.txmsg.RpcClientInitializer;
import com.codingapi.txlcn.txmsg.dto.TxManagerHost;
import com.codingapi.txlcn.txmsg.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Date: 19-1-23 下午5:54
 *
 * @author ujued
 */
@Component
public class TMSearcher {

    private static final Logger log = LoggerFactory.getLogger(TMSearcher.class);

    private static RpcClientInitializer RPC_CLIENT_INITIALIZER;

    private static ReliableMessenger RELIABLE_MESSENGER;

    private static volatile CountDownLatch clusterCountLatch;

    private static int knownTMClusterSize = 1;

    @Autowired
    public TMSearcher(RpcClientInitializer rpcClientInitializer, TxClientConfig clientConfig,
                      ReliableMessenger reliableMessenger, ModIdProvider modIdProvider) {
        // 1. util class init
        Transactions.setApplicationIdWhenRunning(modIdProvider.modId());

        // 2. TMSearcher init
        RPC_CLIENT_INITIALIZER = rpcClientInitializer;
        RELIABLE_MESSENGER = reliableMessenger;
        knownTMClusterSize = clientConfig.getManagerAddress().size();
    }

    /**
     * 重新搜寻TM
     */
    public static void search() {
        Objects.requireNonNull(RPC_CLIENT_INITIALIZER);
        //log.info("Searching for more TM...");
        log.info("搜索更多的分布式事务管理端节点...");
        try {
            HashSet<String> cluster = RELIABLE_MESSENGER.queryTMCluster();
            if (cluster.isEmpty()) {
                //log.info("No more TM.");
                log.info("没有更多的分布式事务管理端节点。");
                echoTMClusterSuccessful();
                return;
            }
            clusterCountLatch = new CountDownLatch(cluster.size() - knownTMClusterSize);
            //log.debug("wait connect size is {}", cluster.size() - knownTMClusterSize);
            log.debug("等待连接{}个分布式事务管理端节点。", cluster.size() - knownTMClusterSize);
            RPC_CLIENT_INITIALIZER.init(TxManagerHost.parserList(new ArrayList<>(cluster)), true);
            clusterCountLatch.await(10, TimeUnit.SECONDS);
            echoTMClusterSuccessful();
        } catch (RpcException | InterruptedException e) {
            //throw new IllegalStateException("There is no normal TM.");
            throw new IllegalStateException("没有可用的分布式事务管理端。");
        }
    }

    /**
     * 搜索到一个
     * @return is searched one
     */
    public static boolean searchedOne() {
        if (Objects.nonNull(clusterCountLatch)) {
            if (clusterCountLatch.getCount() == 0) {
                return false;
            }
            clusterCountLatch.countDown();
            return true;
        }
        return false;
    }

    private static void echoTMClusterSuccessful() {
        //log.info("TC[{}] established TM cluster successfully!", Transactions.APPLICATION_ID_WHEN_RUNNING);
        log.info("分布式事务客户端[{}]与分布式事务管理端集群建立连接成功!", Transactions.APPLICATION_ID_WHEN_RUNNING);
        echoTmClusterSize();
    }

    public static void echoTmClusterSize() {
        //log.info("TM cluster's size: {}", RELIABLE_MESSENGER.clusterSize());
        log.info("分布式是管理端集群节点数：{}", RELIABLE_MESSENGER.clusterSize());
    }
}
