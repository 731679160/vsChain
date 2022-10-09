package tools;

import server.SearchOutput;
import server.ToDOTreeNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class WriteVO {
    public static long writeVOToLocal(String vo) {
        try {
            File writeName = new File("./src/vo.txt");
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(vo);
            }
            return writeName.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String voToStr(SearchOutput res) {
        StringBuffer str = new StringBuffer();
        List<Integer> result = res.getResult();
        List<ToDOTreeNode> voToDO = res.getVOToDO();
        str.append(result.toString());
        str.append("\n");
        if (voToDO != null) {
            for (int i = 0; i < voToDO.size(); i++) {
                str.append(treeToStr(res.getVOToDO().get(i)));
                str.append("\n");
            }
        }
        return str.toString();
    }

    public static String treeToStr(ToDOTreeNode node) {
        if (node == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<");
        stringBuffer.append(node.id + ",");
        stringBuffer.append(node.hashId);
        stringBuffer.append(",");
        stringBuffer.append(treeToStr(node.left));
        stringBuffer.append(",");
        stringBuffer.append(treeToStr(node.right));
        stringBuffer.append(">");
        return stringBuffer.toString();
    }
}
