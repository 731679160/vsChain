package dataOwner;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadDataToInvertedIndex {
    public static List<InvertedIndex> readGeneratedData(String path) throws Exception{
        List<InvertedIndex> allIndex = new ArrayList<InvertedIndex>();
        File file = new File(path);
        if(file.isFile()&&file.exists()){
            InputStreamReader fla = new InputStreamReader(new FileInputStream(file));
            BufferedReader scr = new BufferedReader(fla);
            String str = null;
            while((str = scr.readLine()) != null){
                String[] data = str.split(" ");
                List<Integer> ids = new ArrayList<Integer>();
                for(int i = 1;i < data.length;i++){
                    ids.add(Integer.valueOf(data[i]));
                }
                allIndex.add(new InvertedIndex(Integer.valueOf(data[0]),ids));
            }
            scr.close();
            fla.close();
        }
        return allIndex;
    }

}