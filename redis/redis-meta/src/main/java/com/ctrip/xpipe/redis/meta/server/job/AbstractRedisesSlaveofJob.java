package com.ctrip.xpipe.redis.meta.server.job;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import com.ctrip.xpipe.api.command.Command;
import com.ctrip.xpipe.api.command.CommandFuture;
import com.ctrip.xpipe.api.command.CommandFutureListener;
import com.ctrip.xpipe.api.pool.SimpleKeyedObjectPool;
import com.ctrip.xpipe.api.pool.SimpleObjectPool;
import com.ctrip.xpipe.command.AbstractCommand;
import com.ctrip.xpipe.command.CommandExecutionException;
import com.ctrip.xpipe.command.CommandRetryWrapper;
import com.ctrip.xpipe.command.ParallelCommandChain;
import com.ctrip.xpipe.exception.ExceptionUtils;
import com.ctrip.xpipe.netty.commands.NettyClient;
import com.ctrip.xpipe.pool.XpipeObjectPoolFromKeyed;
import com.ctrip.xpipe.redis.core.entity.RedisMeta;
import com.ctrip.xpipe.redis.core.protocal.error.RedisError;
import com.ctrip.xpipe.retry.RetryDelay;

/**
 * @author wenchao.meng
 *
 * Jul 8, 2016
 */
public abstract class AbstractRedisesSlaveofJob extends AbstractCommand<Void>{
	
	private List<RedisMeta> redises;
	private String masterHost;
	private int masterPort;
	private SimpleKeyedObjectPool<InetSocketAddress, NettyClient> clientPool;
	private int delayBaseMilli = 1000;
	private int retryTimes = 5;
	
	public AbstractRedisesSlaveofJob(List<RedisMeta> slaves, String masterHost, int masterPort, SimpleKeyedObjectPool<InetSocketAddress, NettyClient> clientPool){
		
		this.redises = new LinkedList<>(slaves);
		this.masterHost = masterHost;
		this.masterPort = masterPort;
		this.clientPool = clientPool;
	}

	@Override
	public String getName() {
		return "RedisSlaveMasterChangeJob";
	}

	@Override
	protected void doExecute() throws CommandExecutionException {

		ParallelCommandChain commandChain = new ParallelCommandChain();
		
		for(RedisMeta redisMeta : redises){
			Command<?> backupCommand = createSlaveofCommand(redisMeta, masterHost, masterPort);
			commandChain.add(backupCommand);
		}

		commandChain.execute().addListener(new CommandFutureListener<List<CommandFuture<?>>>() {
			
			@Override
			public void operationComplete(CommandFuture<List<CommandFuture<?>>> commandFuture) throws Exception {
				
				if(commandFuture.isSuccess()){
					future().setSuccess(null);
				}else{
					future().setFailure(commandFuture.cause());
				}
			}
		});;
	}

	private Command<?> createSlaveofCommand(RedisMeta redisMeta, String masterHost, int masterPort) {
		
		SimpleObjectPool<NettyClient> pool = new XpipeObjectPoolFromKeyed<InetSocketAddress, NettyClient>(clientPool, new InetSocketAddress(redisMeta.getIp(), redisMeta.getPort()));
		
		Command<?> command =  createSlaveOfCommand(pool, masterHost, masterPort);
		return CommandRetryWrapper.buildCountRetry(retryTimes, new RetryDelay(delayBaseMilli){
			
			@Override
			public boolean retry(Throwable th) {
				
				Throwable rootCause = ExceptionUtils.getRootCause(th); 
				if(rootCause instanceof RedisError){
					logger.info("[retry][do not retry, because redis error]{}", rootCause.getMessage());
					return false;
				}
				return super.retry(th);
			}
		}, command);
	}

	protected abstract Command<?> createSlaveOfCommand(SimpleObjectPool<NettyClient> clientPool, String masterHost, int masterPort);

	@Override
	protected void doReset(){
		throw new UnsupportedOperationException();
		
	}
	
}
