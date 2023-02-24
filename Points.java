import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashMap;
import java.io.*;
import java.util.Scanner;

/**
 * Class containing full implementation of solution
 * 
 * Class points contains methods to filter and print data, as well as main method
 * 
 * @author Arun Sivarajah
 *
 */
public class Points {
  private PriorityQueue<Transaction> add; //pq to order points added by date
  private ArrayList<Transaction> subtract; //arraylist to store points to deduct
  private ArrayList<Transaction> used; //arraylist to store any points that get depleted
  private int addCnt; //tracker to tally total points to add
  private int subCnt; //tracker to tally total points to subtract
  
  /**
   * Helper class to define a transaction Object
   * 
   * @author arung
   *
   */
  class Transaction{
    private String name; //name of compnay
    private int points; //points (+ or -) associated with transaction
    private LocalDateTime date; //time of transaction
    
    /**
     * Contstructor for Transaction class
     * 
     * @param points
     * @param date
     * @param name
     */
    public Transaction(int points, String date, String name) {
      this.points = points;
      //converts date from iso 8601 format string to LocalDateTime object
      this.date = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
      this.name = name;
    }
    
    /**
     * points getter
     * 
     * @return int points
     */
    public int getPoints() {
      return points;
    }
    
    /**
     * date getter
     * 
     * @return LocalDateTime
     */
    public LocalDateTime getDate() {
      return date;
    }
    
    /**
     * set points value
     * 
     * @param p points to date
     */
    public void setPoints(int p) {
      points = p;
    }
    
    /**
     * Name getter
     * 
     * @return String name
     */
    public String getName() {
      return name;
    }
    
    /**
     * Format for printing a Transaction object
     */
    @Override
    public String toString() {
      return name + " " + points + " " + date;
    }
  }
  
  /**
   * Constructor for points class
   */
  public Points() {
    add = new PriorityQueue<>(Comparator.comparing(Transaction::getDate));
    subtract = new ArrayList<Transaction>();
    used = new ArrayList<Transaction>();
    addCnt = 0;
    subCnt = 0;
  }
  
  /**
   * Add a "plus points" transaction to the priority queue
   * Increment plus points counter
   * 
   * @param points
   * @param date
   * @param name
   */
  public void addTransaction(int points, String date, String name) {
    addCnt += points;
    add.offer(new Transaction(points, date, name));
  }
  
  /**
   * add a point deduction transaction to the array list
   * increment subtraction points counter
   * 
   * @param points
   * @param date
   * @param name
   */
  public void subTransaction(int points, String date, String name) {
    subCnt -= points;
    subtract.add(new Transaction(points, date, name));
  }
  
  /**
   * The filter method is called after all transactions are added to to the appropriate
   * lists. It goes through the subtract points arraylist and subtracts the points from the 
   * add points priority queue based on date.
   */
  public void filter() {
    //iterate through transactions
    for(Transaction t: subtract) {
      int value = t.getPoints();
      //while there are still points to be subtracted
      while (value != 0) {
        Transaction deduct = add.peek();
        //if the points to deduct are more than the oldest transaction:
        if (deduct.getPoints() < Math.abs(value)) {
          //Remove as many points as possible and put transaction in the used list
          value += deduct.getPoints();
          deduct.setPoints(0);
          used.add(add.poll());
        }
        //else deduct all points from most recent transaction
        else {
          deduct.setPoints(deduct.getPoints() + value);
          value = 0;
        }
      }
    }
  }
  
  /**
   * Overrides default object two string to print the remaining points left in required
   * format
   */
  @Override
  public String toString() {
    String output = "";
    //Hashmap to keep track of what companies are already accounted for
    HashMap<String, Integer> pointsTable = new HashMap<>();
    //iterate through priority queue
    for (Transaction t: add) {
      //if company already exists in map then increment the points of the company
      if (pointsTable.containsKey(t.getName())) {
        pointsTable.put(t.getName(), pointsTable.get(t.getName()) + t.getPoints());
      }
      //else put company in map
      else {
        pointsTable.put(t.getName(), t.getPoints());
      }
    }
    //iterate through used list to add any absent companies with zero points to map
    for (Transaction u: used) {
      if (!pointsTable.containsKey(u.getName())) {
        pointsTable.put(u.getName(), u.getPoints());
      }
    }
    
    StringBuilder sb = new StringBuilder("{");
    for (HashMap.Entry<String, Integer> entry : pointsTable.entrySet()) {
        String name = entry.getKey();
        int points = entry.getValue();
        sb.append(name).append(": ").append(points).append(", ");
    }
    if (used.size() > 0) {
        sb.setLength(sb.length() - 2); // remove trailing comma and space
    }
    sb.append("}");
    return sb.toString();
  }
  
  /**
   * Main method for loading, adding and calculating points
   * 
   * @param args
   */
  public static void main(String[] args) {
    //user input points to deduct
    int deduct = -1 * Integer.parseInt(args[0]);
    //user input filename
    String fileName = args[1];
    Points p = new Points();
    try {
      Scanner sc = new Scanner(new File(fileName));
      sc.nextLine();
      while(sc.hasNextLine()) {
        //extract name points and date information from each csv line
        String line = sc.nextLine();
        String[] parts = line.split(",");
        String name = parts[0];
        int points = Integer.parseInt(parts[1]);
        String date = parts[2].replaceAll("^\"|\"$", "");
        //put new transaction in either add or subtract
        if (points < 0) {
          p.subTransaction(points, date, name);
        }
        else {
         p.addTransaction(points, date, name);
        }
      }
      //add user defined subtraction transaction
      p.subTransaction(deduct, LocalDateTime.now().toString(), "User");
      //Check if positive transactions were added
      if (p.add.size() == 0) {
        System.out.println("No available point balance");
        return;
      }
      //ensure user does not get negative points
      if (p.addCnt < p.subCnt) {
        System.out.println("Not enough points left");
        return;
      }
      //run filter operation
      p.filter();
      System.out.println(p);
    } 
    catch (FileNotFoundException e) {
      System.out.println("File does not exist");
      e.printStackTrace();
      return;
    }
  }
}
