package lsm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Datas {

	public static void main(String[] args) {
		
		List<String> list = getKeys();
		list.forEach(e -> {
			System.out.println(e);
		});
		System.out.println(list.size());
	}
	
	public static List<String> getKeys(){
		List<String> list = new ArrayList<>();
		try(FileReader fr = new FileReader(new File("/home/bird/eclipse-workspace/distributed-kv/example/src/lsm/test-data"));
				BufferedReader br = new BufferedReader(fr)){
			String line = null;
			while((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static List<String> getValues(){
		List<String> list = new ArrayList<>();
		try(FileReader fr = new FileReader(new File("/home/bird/eclipse-workspace/distributed-kv/example/src/lsm/test-data-value"));
				BufferedReader br = new BufferedReader(fr)){
			String line = null;
			while((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

}
