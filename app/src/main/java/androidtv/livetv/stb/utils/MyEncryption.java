package androidtv.livetv.stb.utils;

import android.util.Base64;

import java.math.BigInteger;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

public class MyEncryption {
	private final String KEY = "cYP3yTFTM2g1biPvz69ZdLGhuHn59sZrCrie2UqCYB7FH1nRa0U5PhvMAloKdNpQVVJcqfli6Ityu01R";
	
	public String encrypt(byte[] data, byte[] ivs) {
	    
		byte[] key = this.KEY.getBytes();
	    
		try {
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        
	        byte[] finalKey = new byte[16];
	        int len = key.length > 16 ? 16 : key.length;
	        System.arraycopy(key, 0, finalKey, 0, len);
	        
	        SecretKeySpec secretKeySpec = new SecretKeySpec(finalKey, "AES");
	        
	        byte[] finalIvs = new byte[16];
	        len = ivs.length > 16 ? 16 : ivs.length;
	        System.arraycopy(ivs, 0, finalIvs, 0, len);
	        
	        IvParameterSpec ivps = new IvParameterSpec(finalIvs);
	        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivps);
	        
	        return Base64.encodeToString( cipher.doFinal(data), Base64.DEFAULT);
	        
	    } catch (Exception e) {
			Timber.wtf(e);
	    }

	    return null;
	}

	public byte[] decrypt(byte[] data, byte[] ivs) {
	    
		byte[] key = this.KEY.getBytes();
		
		try {
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        
	        byte[] finalKey = new byte[16];
	        int len = key.length > 16 ? 16 : key.length;
	        System.arraycopy(key, 0, finalKey, 0, len);
	        
	        SecretKeySpec secretKeySpec = new SecretKeySpec(finalKey, "AES");
	        
	        byte[] finalIvs = new byte[16];
	        len = ivs.length > 16 ? 16 : ivs.length;
	        System.arraycopy(ivs, 0, finalIvs, 0, len);
	        
	        IvParameterSpec ivps = new IvParameterSpec(finalIvs);
	        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivps);
	        
	        return cipher.doFinal(data);
	    } catch (Exception e) {
	        
	    }
		
		return "".getBytes();
	}
	
	public String getRandom(int length ) {
		//return new BigInteger( 130, new SecureRandom() ).toString(length);
		final String bucket = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ@#$%&*_";
		Random rnd = new Random();

		StringBuilder sb = new StringBuilder( length );
		for( int i = 0; i < length; i++ ) 
			sb.append( bucket.charAt( rnd.nextInt(bucket.length()) ) );
	   
		return sb.toString();
	}

	public String getEncryptedToken(String token ) {
		String ivs = this.getRandom(10);
		token = this.encrypt( token.getBytes(), ivs.getBytes() );

		token = ivs + token;

		return token.replace("\n","").trim();
	}
	
	public String getDecryptedToken(String token ) {
		String ivs = token.substring(0, 10);
		token = token.substring(10);
		
		byte[] tokenBytes = Base64.decode(token, Base64.DEFAULT );
		
		return new String( this.decrypt(tokenBytes, ivs.getBytes() ) );
	}
	
	public String getEncryptedTokenInHex(String token ) {
		String ivs = this.getRandom(10);
		token = this.encrypt( token.getBytes(), ivs.getBytes() );
		
		token = ivs + token;
		
		return this.bytArrayToHex( token.getBytes() );
	}
	
	public String bytArrayToHex(byte[] bytes) {
	   StringBuilder sb = new StringBuilder();
	   for(byte b: bytes)
	      sb.append(String.format("%02x", b&0xff));
	   
	   return sb.toString();
	}
	
	public byte[] hexToByteArray( String hex ) {
		byte[] bytes =  new BigInteger( hex, 16).toByteArray();
		
		return bytes;
	}
}