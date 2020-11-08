package com.shichuan;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * project_name: JAVA-001
 * package: com.shichuan
 * description:
 * 实现在 main 函数启动一个新线程，运行一个方法，拿到这个方法的返回值后，退出主线程
 *
 * @author: 史川
 * @Date: 2020/11/8 13:36
 **/
public class HomeWord01 {

	/**
	 * 定义统一的线程池
	 *
	 * @return
	 */
	private ExecutorService initExecutorService() {
		return Executors.newSingleThreadExecutor();
	}

	/**
	 * future
	 */
	@Test
	public void futureDemo1() throws Exception {
		ExecutorService executorService = initExecutorService();
		Future<String> futureResult = executorService.submit(() -> targetMethod());
		String result = futureResult.get();
		System.out.println(result);
	}

	/**
	 * countDownLatch
	 *
	 * @throws Exception
	 */
	@Test
	public void countDownDemo1() throws Exception {
		ExecutorService executorService = initExecutorService();
		CountDownLatch countDownLatch = new CountDownLatch(1);
		ProxyResult proxyResult = new ProxyResult();
		executorService.execute(() -> {
			try {
				String result = targetMethod();
				proxyResult.setResult(result);
			} finally {
				countDownLatch.countDown();
			}
		});
		countDownLatch.await();
		System.out.println(proxyResult.getResult());
		executorService.shutdown();
	}

	/**
	 * sleep
	 *
	 * @throws Exception
	 */
	@Test
	public void sleepDemo1() throws Exception {
		ExecutorService executorService = initExecutorService();
		ProxyResult proxyResult = new ProxyResult();
		executorService.execute(() -> {
			try {
				String result = targetMethod();
				proxyResult.setResult(result);
			} finally {
				proxyResult.setFinish();
			}
		});
		//等待执行结果
		while (!proxyResult.finish()) {
			Thread.sleep(1000L);
		}
		System.out.println(proxyResult.getResult());
		executorService.shutdown();
	}

	/**
	 * semaphore
	 *
	 * @throws Exception
	 */
	@Test
	public void semaphoreDemo1() throws Exception {
		ExecutorService executorService = initExecutorService();
		ProxyResult proxyResult = new ProxyResult();
		Semaphore semaphore = new Semaphore(1);
		executorService.execute(() -> {
			try {
				semaphore.acquire(1);
				String result = targetMethod();
				proxyResult.setResult(result);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				semaphore.release(1);
			}
		});
		//保证线程池的方法已被调用
		Thread.sleep(100L);
		try {
			semaphore.acquire(1);
		} finally {
			semaphore.release(1);
		}
		System.out.println(proxyResult.getResult());
		executorService.shutdown();
	}

//	/**
//	 * cyclicBarrier
//	 *
//	 * @throws Exception
//	 */
//	@Test
//	public void cyclicBarrierDemo1() throws Exception {
//		ExecutorService executorService = initExecutorService();
//		ProxyResult proxyResult = new ProxyResult();
//		CyclicBarrier cyclicBarrier = new CyclicBarrier(1);
//		executorService.execute(() -> {
//			try {
//				String result = targetMethod();
//				proxyResult.setResult(result);
//			} finally {
//				try {
//					cyclicBarrier.await();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		cyclicBarrier.await();
//		System.out.println(proxyResult.getResult());
//		executorService.shutdown();
//	}

	/**
	 * wait notify
	 *
	 * @throws Exception
	 */
	@Test
	public void waitDemo1() throws Exception {
		ExecutorService executorService = initExecutorService();
		ProxyResult proxyResult = new ProxyResult();
		executorService.execute(() -> {
			String result = targetMethod();
			proxyResult.setResult(result);
			synchronized (proxyResult) {
				proxyResult.notify();
			}
		});
		synchronized (proxyResult) {
			proxyResult.wait();
		}
		System.out.println(proxyResult.getResult());
		executorService.shutdown();
	}

	/**
	 * lock
	 *
	 * @throws Exception
	 */
	@Test
	public void lockDemo1() throws Exception {
		ExecutorService executorService = initExecutorService();
		ProxyResult proxyResult = new ProxyResult();
		Lock lock = new ReentrantLock(true);
		executorService.execute(() -> {
			try {
				lock.lock();
				String result = targetMethod();
				proxyResult.setResult(result);
			} finally {
				lock.unlock();
			}
		});
		//让线程逻辑先执行
		Thread.sleep(100L);
		try {
			lock.lock();
		} finally {
			lock.unlock();
		}
		System.out.println(proxyResult.getResult());
		executorService.shutdown();
	}

	/**
	 * 统一定义被调用的目标方法
	 *
	 * @return
	 */
	public String targetMethod() {
		//模拟业务执行阻塞
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "I got return value";
	}

	/**
	 * 代理对象,处理返回值和线程操作结果
	 *
	 * @param
	 */
	private class ProxyResult {

		private AtomicBoolean finishFlag = new AtomicBoolean(Boolean.FALSE);

		private String result;

		public void setFinish() {
			this.finishFlag = new AtomicBoolean(Boolean.TRUE);
		}

		public void setResult(String result) {
			this.result = result;
		}

		public String getResult() {
			return result;
		}

		public boolean finish() {
			return finishFlag.get();
		}
	}
}
