package thinypng;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Starter {

	private static Logger log = LoggerFactory.getLogger(Starter.class);
	
	private static int coreNumber = Runtime.getRuntime().availableProcessors();	//返回可用处理器的java虚拟机数量
	
	private List<String> API_key = new CopyOnWriteArrayList<>();	//ArrayList线程安全变体
	
	private static String src;	//源目录
	
	private static String dest;	//目的目录
	
	private static int threshold;	//应该产生线程的数量
	
	private static String apiKeyLoction = "/api_key.properties";		//api的key文件
	
	private static String imgLogLoction = "log.pic";		//已经压缩的图片日志文件
	
	public static void main(String[] args) {
		String src = args[0];
		String dest = args[1];
		threshold = args.length < 3 || args[2] == null || args[2].equals("") ? coreNumber : Integer.parseInt(args[2]);
		try {
            new Starter().compress(src, dest, threshold);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	/**
	 * 开始压缩
	 * @param src
	 * @param dest
	 * @param threshold
	 * @throws Exception
	 */
	private void compress(String src, String dest, int threshold) throws Exception {
		if(!this.checkParam(src,dest)) {
			throw  new IllegalArgumentException("参数不正确！");
		}
		Starter.src = src;		//要压缩的文件地址
		Starter.dest = dest;
		Starter.threshold = threshold <= 0 ? coreNumber : threshold;
        log.info("运行参数：{}, {}, {}", src, dest, threshold);
        //消息队列（要装载压缩图片的队列）
        BlockingQueue<File> messageQueue = new LinkedBlockingQueue<>();
        //生产者
        Provider p = new Provider(messageQueue, new File(src));
        //线程池
        ExecutorService cachePool = Executors.newCachedThreadPool();
        Future<Boolean> future = cachePool.submit(p);	//图片是否加载完
        //阻塞
        while(!future.get()) {
        	future = cachePool.submit(p);
        }
        initComsumer();
        //消费者
        for(int i = 0; i < threshold; i++) {
            Consumer c = new Consumer(messageQueue);
            cachePool.execute(c);
        }
	}

	/**
	 * 加载消费者api_key
	 */
	private void initComsumer() {
		String line = "";
		try(BufferedReader buffReader = new BufferedReader(new InputStreamReader(Starter.class.getResourceAsStream(Starter.apiKeyLoction)))){
			//一行一行的读
			while((line = buffReader.readLine()) != null) {
				log.info("api_key:{}",line);
				API_key.add(line);
			}
		Consumer.setAPIKey(API_key);
		Consumer.setSrcBase(src);
		Consumer.setDestBase(dest);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Consumer initConsumer error - {}",e.getMessage());
		}
	}

	/**
	 * 参数检查
	 * @param src
	 * @param dest
	 * @return
	 */
	private boolean checkParam(String src, String dest) {
		//检查是否为空
		if(src == null || dest == null || "".equals(src) || "".equals(dest)) {
			log.error("参数为空");
			return false;
		}
		//检查源图片是否是jpg或png
		int src_loc = src.lastIndexOf(".");
		String suffix = src.substring(++src_loc);
		if(new File(src).exists()) {
			
			return true;
		} else {
			log.error("src路径不存在");
			return false;
		}
	}
	
}
