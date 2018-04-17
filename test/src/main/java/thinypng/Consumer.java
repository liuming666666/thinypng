package thinypng;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer implements Runnable {

	private static Logger log = LoggerFactory.getLogger(Consumer.class);
	
	private static String srcBase;
	
	private static String destBase;
	
	private static Lock lock = new ReentrantLock();		//锁
	
	private BlockingQueue<File> messageQueue;		//消息队列
	
	public Consumer(String srcBase) {
		Consumer.srcBase = srcBase;
	}

	@Override
	public void run() {
		//线程占有lock对象
		lock.lock();
		//获取数据
		try {
			File file = this.messageQueue.poll(2, TimeUnit.SECONDS);
			if(file == null) {
				log.info("current Consumer - {} - consume faild, messageQueue empty");
				lock.unlock();	//线程解锁
				return ;
			}
		} catch (InterruptedException e) {
			log.info("",e);
		}
	}

}
