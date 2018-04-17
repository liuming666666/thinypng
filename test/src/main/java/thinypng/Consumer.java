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
	
	private static Lock lock = new ReentrantLock();		//��
	
	private BlockingQueue<File> messageQueue;		//��Ϣ����
	
	public Consumer(String srcBase) {
		Consumer.srcBase = srcBase;
	}

	@Override
	public void run() {
		//�߳�ռ��lock����
		lock.lock();
		//��ȡ����
		try {
			File file = this.messageQueue.poll(2, TimeUnit.SECONDS);
			if(file == null) {
				log.info("current Consumer - {} - consume faild, messageQueue empty");
				lock.unlock();	//�߳̽���
				return ;
			}
		} catch (InterruptedException e) {
			log.info("",e);
		}
	}

}
