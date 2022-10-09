package tools;


import server.KeywordTreeNode;
import server.UpdData;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class StringXor {
    //加密异或  s1是大整数，s2是短10进制符串 + ("a" or "d")，输出10进制字符串
    public static String xor(String s1, String id){
        byte[] bytes1 = s1.getBytes(StandardCharsets.UTF_8);
        byte[] byteId = id.getBytes(StandardCharsets.UTF_8);
        xorTool(bytes1, byteId);
        return new String(byteId);
    }

    public static KeywordTreeNode deXor(String s1, String v){
        String[] data = v.split(",");
        byte[] bytes1 = s1.getBytes(StandardCharsets.UTF_8);
        byte[] byteId = data[0].getBytes(StandardCharsets.UTF_8);
        xorTool(bytes1, byteId);
        int id = Integer.parseInt(new String(byteId));
        KeywordTreeNode node;
        if (data.length != 4) {
            node = new KeywordTreeNode(id ,data[1], "", "");
        } else {
            node = new KeywordTreeNode(id ,data[1], data[2], data[3]);
        }
        return node;
    }

    private static void xorTool(byte[] s1, byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= s1[i % s1.length];
        }
    }

    //加密异或  s1是长16进制字符串，s2是短10进制符串 + ("a" or "d")，输出10进制字符串
    public static String updXor(String s1, String id, String op){
        byte[] bytes1 = s1.getBytes(StandardCharsets.UTF_8);
        byte[] byteId = id.getBytes(StandardCharsets.UTF_8);
        byte[] bytesOp = op.getBytes(StandardCharsets.UTF_8);
        xorTool(bytes1, byteId);
        xorTool(bytes1, bytesOp);
        return new String(byteId) + "," + new String(bytesOp);
    }

    //解密异或
    public static UpdData updDeXor(String s1, String s2){
        byte[] bytes1 = s1.getBytes(StandardCharsets.UTF_8);
        String[] split = s2.split(",");
        byte[] byteId = split[0].getBytes(StandardCharsets.UTF_8);
        byte[] bytesOp = split[1].getBytes(StandardCharsets.UTF_8);
        xorTool(bytes1, byteId);
        xorTool(bytes1, bytesOp);
        return new UpdData(Integer.parseInt(new String(byteId)), split[2], new String(bytesOp));
    }

//    @Test
//    public void test() {
//        String s1 = SHA.HASHDataToString("1");
//        String id = "1";
//        String hash = SHA.HASHDataToString("2");
//        String enc = xor(s1, id, hash);
//
//        long startTime1=System.nanoTime();
//        String idAndHash = deXor(s1, enc);
//        long endTime1=System.nanoTime();
//        System.out.println(endTime1 - startTime1);
//        long startTime2=System.nanoTime();
//        SHA.HASHDataToString(hash + id);
//        long endTime2=System.nanoTime();
//        System.out.println(endTime2 - startTime2);
//
//        String[] split = idAndHash.split(",");
//        int theId = Integer.parseInt(split[0]);
//        String theHash = split[1];
//    }


}
