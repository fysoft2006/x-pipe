package com.ctrip.xpipe.redis.meta.server.job;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.unidal.tuple.Pair;

import com.ctrip.xpipe.api.pool.SimpleKeyedObjectPool;
import com.ctrip.xpipe.netty.commands.NettyClient;
import com.ctrip.xpipe.pool.XpipeNettyClientObjectPool;
import com.ctrip.xpipe.redis.core.entity.RedisMeta;
import com.ctrip.xpipe.redis.meta.server.AbstractMetaServerTest;
import com.ctrip.xpipe.utils.IpUtils;

/**
 * @author wenchao.meng
 *
 *         Oct 28, 2016
 */
public class SlaveofJobTest extends AbstractMetaServerTest {

	private String[] redises = new String[] { "127.0.0.1:6379", "127.0.0.1:6479" };

	@Test
	public void test() throws Exception {

		SimpleKeyedObjectPool<InetSocketAddress, NettyClient> clientPool = getKeyedObjectPool();

		List<RedisMeta> slaves = getRedisSlaves(redises);

		SlaveofJob slaveofJob = new SlaveofJob(slaves, "10.2.58.242", 6379, clientPool);
		slaveofJob.execute().get();

	}

	private SimpleKeyedObjectPool<InetSocketAddress, NettyClient> getKeyedObjectPool() throws Exception {
		
		XpipeNettyClientObjectPool pool = new XpipeNettyClientObjectPool();
		
		pool.initialize();
		pool.start();
		add(pool);
		return pool;
	}

	private List<RedisMeta> getRedisSlaves(String[] redises) {

		List<RedisMeta> slaves = new LinkedList<>();
		for (String redis : redises) {

			Pair<String, Integer> addr = IpUtils.parseSingleAsPair(redis);
			RedisMeta redisMeta = new RedisMeta();
			redisMeta.setIp(addr.getKey());
			redisMeta.setPort(addr.getValue());

			slaves.add(redisMeta);
		}

		return slaves;
	}

}
