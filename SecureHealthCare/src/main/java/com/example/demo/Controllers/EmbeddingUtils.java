package com.example.demo.Controllers;




public class EmbeddingUtils {

	public static byte[] floatArrayToByteArray(float[] floats) {
        byte[] bytes = new byte[floats.length * 4];
        for (int i = 0; i < floats.length; i++) {
            int bits = Float.floatToIntBits(floats[i]);
            bytes[i * 4] = (byte) (bits & 0xff);
            bytes[i * 4 + 1] = (byte) ((bits >> 8) & 0xff);
            bytes[i * 4 + 2] = (byte) ((bits >> 16) & 0xff);
            bytes[i * 4 + 3] = (byte) ((bits >> 24) & 0xff);
        }
        return bytes;
    }
    
    public static float[] byteArrayToFloatArray(byte[] bytes) {
        if (bytes == null || bytes.length % 4 != 0) {
            System.err.println("Invalid byte array for float conversion");
            return null;
        }
        
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            int bits = (bytes[i * 4] & 0xff) |
                      ((bytes[i * 4 + 1] & 0xff) << 8) |
                      ((bytes[i * 4 + 2] & 0xff) << 16) |
                      ((bytes[i * 4 + 3] & 0xff) << 24);
            floats[i] = Float.intBitsToFloat(bits);
        }
        return floats;
    }
    
    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            System.err.println("Invalid vectors for cosine similarity calculation");
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator == 0.0) {
            return 0.0;
        }
        
        return dotProduct / denominator;
    }
}
