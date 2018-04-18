package thinypng;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图片源,生产者
 * @author 刘明
 *
 */
public class Provider implements Callable<Boolean>{
	
	private static Logger log = LoggerFactory.getLogger(Provider.class);
	
	//消息队列
	private BlockingQueue<File> messageQueue;
	
	//图片源
	private static File src;
	
	public Provider(BlockingQueue<File> messageQueue ,File src) {
		this.setMessageQueue(messageQueue);
		Provider.src = src;
	}
	
	/**
	 * 加载要压缩的文件
	 * @param src
	 * @throws InterruptedException 
	 */
	private void load(File src) throws InterruptedException {
		log.info("Provider load src:{}",src.getAbsolutePath());
		//当要压缩的文件是目录
		if(src.isDirectory()) {
			File[] files = src.listFiles();
			for (File file : files) {
				load(file);
			}
			//Arrays.stream(files).forEach(file -> load(file));
			
		//要压缩的是文件	
		} else if(src.isFile()) {
			//校验文件是否是jpg或png图片
			if(this.validateImg(src)) {
				this.messageQueue.put(src);		//往消息队列添加数据
			}
		}
	}
	
	/**
	 * 校验图片是否为png和jpg格式
	 * @param file
	 * @return
	 */
	private boolean validateImg(File file) {
		int loc = file.getName().lastIndexOf(".");
		String suffix = file.getName().substring(++loc);
		return "png".equals(suffix) || "jpg".equals(suffix);
	}
	
	public static void main(String[] args) {
		
	}

	public BlockingQueue<File> getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(BlockingQueue<File> messageQueue) {
		this.messageQueue = messageQueue;
	}

	public static File getSrc() {
		return src;
	}

	public static void setSrc(File src) {
		Provider.src = src;
	}

	@Override
	public Boolean call() {
		log.info("生产者call函数开始执行，加载图片数据");
		try {
			this.load(Provider.src);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
