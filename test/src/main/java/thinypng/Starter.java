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
	
	private static int coreNumber = Runtime.getRuntime().availableProcessors();	//���ؿ��ô�������java���������
	
	private List<String> API_key = new CopyOnWriteArrayList<>();	//ArrayList�̰߳�ȫ����
	
	private static String src;	//ԴĿ¼
	
	private static String dest;	//Ŀ��Ŀ¼
	
	private static int threshold;	//Ӧ�ò����̵߳�����
	
	private static String apiKeyLoction = "/api_key.properties";		//api��key�ļ�
	
	private static String imgLogLoction = "log.pic";		//�Ѿ�ѹ����ͼƬ��־�ļ�
	
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
	 * ��ʼѹ��
	 * @param src
	 * @param dest
	 * @param threshold
	 * @throws Exception
	 */
	private void compress(String src, String dest, int threshold) throws Exception {
		if(!this.checkParam(src,dest)) {
			throw  new IllegalArgumentException("��������ȷ��");
		}
		Starter.src = src;		//Ҫѹ�����ļ���ַ
		Starter.dest = dest;
		Starter.threshold = threshold <= 0 ? coreNumber : threshold;
        log.info("���в�����{}, {}, {}", src, dest, threshold);
        //��Ϣ���У�Ҫװ��ѹ��ͼƬ�Ķ��У�
        BlockingQueue<File> messageQueue = new LinkedBlockingQueue<>();
        //������
        Provider p = new Provider(messageQueue, new File(src));
        //�̳߳�
        ExecutorService cachePool = Executors.newCachedThreadPool();
        Future<Boolean> future = cachePool.submit(p);	//ͼƬ�Ƿ������
        //����
        while(!future.get()) {
        	future = cachePool.submit(p);
        }
        initComsumer();
        //������
        for(int i = 0; i < threshold; i++) {
            Consumer c = new Consumer(messageQueue);
            cachePool.execute(c);
        }
	}

	/**
	 * ����������api_key
	 */
	private void initComsumer() {
		String line = "";
		try(BufferedReader buffReader = new BufferedReader(new InputStreamReader(Starter.class.getResourceAsStream(Starter.apiKeyLoction)))){
			//һ��һ�еĶ�
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
	 * �������
	 * @param src
	 * @param dest
	 * @return
	 */
	private boolean checkParam(String src, String dest) {
		//����Ƿ�Ϊ��
		if(src == null || dest == null || "".equals(src) || "".equals(dest)) {
			log.error("����Ϊ��");
			return false;
		}
		//���ԴͼƬ�Ƿ���jpg��png
		int src_loc = src.lastIndexOf(".");
		String suffix = src.substring(++src_loc);
		if(new File(src).exists()) {
			
			return true;
		} else {
			log.error("src·��������");
			return false;
		}
	}
	
}
