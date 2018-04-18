package thinypng;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinify.Options;
import com.tinify.Source;
import com.tinify.Tinify;

public class Consumer implements Runnable {

	private static Logger log = LoggerFactory.getLogger(Consumer.class);
	
	private static String srcBase;	//源目录
	
	private static String destBase;	//目的目录
	
	private static File destFile;	//目的文件
	
	private static Lock lock = new ReentrantLock();		//锁
	
	private BlockingQueue<File> messageQueue;		//消息队列
	
    private BlockingQueue<String> writeQueue;		//
	
	private static List<String> APIKey = new CopyOnWriteArrayList<>();
	
	public Consumer(BlockingQueue<File> messageQueue ) {
		super();
		this.messageQueue = messageQueue;
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
			//装载文件
			destFile = new File(destBase + File.separator +file.getName());
			lock.unlock();	//解锁
			//开始压缩图片
			doCompress(file,destFile);
		} catch (InterruptedException e) {
			log.info("",e);
		}
	}

	/**
	 * 图片压缩
	 * @param file
	 * @param destFile
	 */
	private void doCompress(File srcFile, File destFile) {
		System.out.println(destFile.getAbsolutePath());
		log.info("Current Comsumer - {} - Comsumer doCompress src:{}",Thread.currentThread().getName(),srcFile.getAbsolutePath());
		Tinify.setKey(APIKey.get(Thread.currentThread().getName().hashCode() % APIKey.size()));
		try {
			Source source = Tinify.fromFile(srcFile.getAbsolutePath());
			BufferedImage bufferedImage = ImageIO.read(srcFile);
			if(bufferedImage.getWidth() > 800) {
				Options options = new Options().with("method", "scale").with("width", 800);
				Source reset = source.resize(options);
				reset.toFile(destFile.getAbsolutePath());
				return ;
			}
			source.toFile(destFile.getAbsolutePath());
			
			
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Current Consumer - {} - Consumer doCompress exception error:{}, src.path:{}", Thread.currentThread().getName(), e.getMessage(), srcFile.getAbsolutePath());
			
		}
	}

	public static List<String> getAPIKey() {
		return APIKey;
	}

	public static void setAPIKey(List<String> aPIKey) {
		APIKey = aPIKey;
	}

	public static String getSrcBase() {
		return srcBase;
	}

	public static void setSrcBase(String srcBase) {
		Consumer.srcBase = srcBase;
	}

	public static String getDestBase() {
		return destBase;
	}

	public static void setDestBase(String destBase) {
		Consumer.destBase = destBase;
	}
	
}
