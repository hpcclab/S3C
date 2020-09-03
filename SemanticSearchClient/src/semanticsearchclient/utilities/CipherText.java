package semanticsearchclient.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CipherText {

	public void encrypt(String key, String input, String output) throws Throwable {
		encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, input, output);
	}

	public void decrypt(String key, String input, String output) throws Throwable {
		encryptOrDecrypt(key, Cipher.DECRYPT_MODE, input, output);
	}

	private void encryptOrDecrypt(String key, int mode, String input, String output) throws Throwable {
		

		File file = new File(input);
		FileInputStream is = new FileInputStream(file);
		file = new File(output);
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		
		FileOutputStream os = new FileOutputStream(file);

		DESKeySpec dks = new DESKeySpec(key.getBytes());
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		SecretKey desKey = skf.generateSecret(dks);
		Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE

		if (mode == Cipher.ENCRYPT_MODE) {
			cipher.init(Cipher.ENCRYPT_MODE, desKey);
			CipherInputStream cis = new CipherInputStream(is, cipher);
			doCopy(cis, os);
		} else if (mode == Cipher.DECRYPT_MODE) {
			cipher.init(Cipher.DECRYPT_MODE, desKey);
			CipherOutputStream cos = new CipherOutputStream(os, cipher);
			doCopy(is, cos);
		}
	}

	public static void doCopy(InputStream is, OutputStream os) throws IOException {
		byte[] bytes = new byte[64];
		int numBytes;
		while ((numBytes = is.read(bytes)) != -1) {
			os.write(bytes, 0, numBytes);
		}
		os.flush();
		os.close();
		is.close();
	}

        /**
         * Hash File Contents.
         * Goes through a file line by line, hashing each line as a string.
         * Then writes each string to another keyfile of the same name in the
         * temporary directory where the encrypted files are being held.
         * @param filePath Absolute path of the file
         * @param fileName Name of the file
         * @throws IOException 
         */
        public void HashFileContents(String filePath, String fileName) throws IOException{
            //Prep to read the file
            File file = new File(filePath);
            FileInputStream is = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
            //Each line in the file is a key phrase we're looking to hash
            List<String> hashedPhrases = new ArrayList<String>();
            String keyPhrase;
            while ((keyPhrase = br.readLine()) != null) {
                Integer hash = keyPhrase.toLowerCase().hashCode();
                hashedPhrases.add(hash.toString());
            }
            
            //Now hashedPhrases has a collection of strings representing the hashed keyphrases
            String outputFile = Constants.tempLocation + File.separator
                    + fileName; //Where we're putting the new file
            Path outputPath = Paths.get(outputFile);
            //Actuall write to the file
            Files.write(outputPath, hashedPhrases, Charset.forName("UTF-8"));
            
        }
}