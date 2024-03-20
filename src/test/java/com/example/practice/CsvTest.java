package com.example.practice;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.practice.ToolChen.printData;

public class CsvTest {

  private static final String PATH = "data.csv";

  public static void main(String[] args) {
    Map<Integer, String> voteMap = readCsv();
    printData(readCsv().keySet().stream().toList(), "=============  已投票统计  =============", "已投票: ");
  }

  public static Map<Integer, String> readCsv() {
    Map<Integer, String> map = new HashMap<>();
    List<Integer> list = new ArrayList<>();
    try (Reader reader = new FileReader(new ClassPathResource(PATH).getFile().toPath().toString()); CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
      for (CSVRecord csvRecord : csvParser) {
        String houseNo = csvRecord.get("houseNo"); // 获取"houseNo"列的值
        String vote = csvRecord.get("vote"); // 获取"vote"列的值
        map.put(Integer.parseInt(houseNo), vote);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return map;
  }
}
