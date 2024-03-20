package com.example.practice;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.practice.CsvTest.readCsv;

public class ToolChen {

  public static void main(String[] args) {
    printData(getVoteList(), "=============  已投票统计  =============", "已投票: ");
    printData(householdsWithoutVotes(), "=============  未投票统计  =============", "未投票: ");
    System.out.println("=============  统计信息  =============");
    calculateNumberOfVotingHouseholds();
  }

  public static void calculateNumberOfVotingHouseholds() {
    List<Integer> votes = getVoteList();
    List<Integer> totalHouses = generateNumber();
    //计算投票户数
    int minimumVotingHouse = (int) Math.ceil(totalHouses.size() / 3.0 * 2.0);
    System.out.println("总户数: " + totalHouses.size() + " ,已投票数: " + votes.size() + ", 最低生效的投票数: " + minimumVotingHouse);

    //计算投票面积
    double voteArea = calculateArea(votes);
    double totalArea = calculateArea(totalHouses);
    double minimumArea = totalArea / 3.0 * 2.0;
    System.out.println("楼栋总面积为: " + totalArea + " ,已投票面积为: " + voteArea + " ,最低生效面积为: " + minimumArea);
  }

  public static List<Integer> householdsWithoutVotes() {
    List<Integer> votes = getVoteList();
    List<Integer> allHouseholds = generateNumber();
    List<Integer> missingVotes = allHouseholds.stream().filter(household -> !votes.contains(household)).collect(Collectors.toList());
    return missingVotes;
  }

  public static List<Integer> getVoteList() {
    Map<Integer, String> voteMap = readCsv();
    return voteMap.keySet().stream().toList();
  }

  public static List<Integer> generateNumber() {
    List<Integer> houseNumbersList = new ArrayList<>();
    for (int floor = 1; floor <= 30; floor++) {
      for (int i = 1; i <= 4; i++) {
        String houseNumber = String.format("%d%02d", floor, i);
        houseNumbersList.add(Integer.parseInt(houseNumber));
      }
    }
    houseNumbersList.add(3102);
    houseNumbersList.add(3103);
    return houseNumbersList;
  }

  public static void printData(List<Integer> votes, String header, String message) {
    System.out.println(header);
    System.out.println("总计: "+votes.size());
    Map<Integer, List<Integer>> resultMap = spiltDataByNumber(votes);
    resultMap.forEach((key, list) -> {
      Collections.sort(list);// 对列表进行排序
      System.out.println("户号: " + key + "总数: " + list.size() + ", " + message + list);
    });
    System.out.println();
  }

  public static Map<Integer, List<Integer>> spiltDataByNumber(List<Integer> votes) {
    Map<Integer, List<Integer>> resultMap = new HashMap<>();

    votes.forEach(num -> {
      int key = num % 10; // 获取尾数
      resultMap.computeIfAbsent(key, k -> new ArrayList<>()).add(num);
    });

    return resultMap;
  }

  public static double calculateArea(List<Integer> votes) {
    Map<Integer, List<Integer>> resultMap = spiltDataByNumber(votes);
    double area1 = resultMap.containsKey(1) ? resultMap.get(1).size() * 106.04 : 0;
    double area2 = resultMap.containsKey(2) ? resultMap.get(2).size() * 92.23 : 0;
    double area3 = resultMap.containsKey(3) ? resultMap.get(3).size() * 92.23 : 0;
    double area4 = resultMap.containsKey(4) ? resultMap.get(4).size() * 89.04 : 0;
    return area1 + area2 + area3 + area4;
  }
}
