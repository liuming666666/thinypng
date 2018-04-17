import java.io.File;
import java.io.IOException;

import com.tinify.Source;
import com.tinify.Tinify;

/**
 * java�ӿڲ���tinyPNG
 * @author juyi1012
 *
 */
public class TinyPNGUtil {
	
	public static void main(String[] args) {
		//����api key
		Tinify.setKey("BdCINnWUN7xCEqixltgcM25OSwkGq93v");
		File f = new File("C:/Users/juyi1012/Desktop/tinyPNG");
		Source source = null;
		if(f.isDirectory()) {
			//�ļ���
			File[] fs = f.listFiles();
			for (File file : fs) {
				//����ͼƬ�Ƿ����Ҫ��
				if(validatePic(file)) {
					System.out.println(file.getAbsolutePath());
					try {
						source = Tinify.fromFile(file.getAbsolutePath());
						source.toFile("C:/Users/juyi1012/Desktop/tinyPNG1/" + file.getName());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		} else {
			//�ļ�
			try {
				source = Tinify.fromFile(f.getAbsolutePath());
				source.toFile("C:/Users/juyi1012/Desktop/tinyPNG1/"+f.getName());
			} catch (IOException e) {
				System.out.println("ͼƬѹ���쳣");
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * У��ͼƬ(jpg��png)
	 * @return
	 */
	public static boolean validatePic(File file) {
		int pos = file.getAbsolutePath().lastIndexOf(".");
		String suffix = file.getAbsolutePath().substring(++pos);
		return suffix.equals("jpg") || suffix.equals("png");
	}
	
	
	
}
