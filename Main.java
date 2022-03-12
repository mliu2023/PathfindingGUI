import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.lang.InterruptedException;
class Pair{
  public int a;
  public int b;
  public Pair(){
    a = 0;
    b = 0;
  }
  public Pair(int a, int b){
    this.a = a;
    this.b = b;
  }
  public Pair clone(){
    Pair p = new Pair();
    p.a = a;
    p.b = b;
    return p;
  }
  public boolean equals(Pair other){
    return this.a == other.a && this.b == other.b;
  }
}
public class Main extends JFrame{
  private JPanel buttonPanel;
  private JButton[][] buttonArr;
  private JLabel instructions, noPath;
  private JButton submit, clearAll, clearPath, placeBomb;
  private ButtonListener listener1;
  private ImageIcon goal, cree, black, yellow, bomb;
  private static Container cp;
  private static JButton darkMode;

  private boolean[][] visited;
  private boolean isBomb = false;
  private ArrayList<Pair> bombArr = new ArrayList<Pair>();
  private int height = 20, width = 20;

  public Main(){
    super("PathFinding");
    cp = getContentPane();
    cp.setLayout(new FlowLayout());
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    // buttons
    listener1 = new ButtonListener();
    submit = new JButton("Run");
    submit.addActionListener(listener1);
    clearAll = new JButton("Clear all");
    clearAll.addActionListener(listener1);
    clearPath = new JButton("Clear path");
    clearPath.addActionListener(listener1);
    placeBomb = new JButton("Toggle bomb placing");
    placeBomb.setActionCommand("Place bomb");
    placeBomb.addActionListener(listener1);
    darkMode = new JButton("Dark mode");
    darkMode.addActionListener(listener1);

    // images
    goal = new ImageIcon("goal.png");
    goal = new ImageIcon(goal.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
    cree = new ImageIcon("cree.png");
    cree = new ImageIcon(cree.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
    black = new ImageIcon("black.jpeg");
    black = new ImageIcon(black.getImage().getScaledInstance(30, 30, Image.SCALE_FAST));
    yellow = new ImageIcon("yellow.jpeg");
    yellow = new ImageIcon(yellow.getImage().getScaledInstance(30, 30, Image.SCALE_FAST));
    bomb = new ImageIcon("bomb.png");
    bomb = new ImageIcon(bomb.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

    // setting up grid of buttons
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(height, width));
    buttonArr = new JButton[height][width];
    for(int i = 0; i < height; i++){
      for(int j = 0; j < width; j++){
        buttonArr[i][j] = new JButton();
        buttonArr[i][j].setPreferredSize(new Dimension(30, 30));
        if(i == height-1 && j == width-1){
          buttonArr[i][j].setIcon(goal);
        }
        else if(i == 0 && j == 0){
          buttonArr[i][j].setIcon(cree);
        }
        buttonArr[i][j].addActionListener(listener1);
        buttonArr[i][j].setActionCommand(Integer.toString(height*i+j));
        buttonArr[i][j].setOpaque(true);
        buttonPanel.add(buttonArr[i][j], height*i+j);
      }
    }

    // labels
    instructions = new JLabel("Press a button to make it a wall.");
    noPath = new JLabel("No path found.");
    noPath.setVisible(false);

    bombArr.add(new Pair(0, 0));
    visited = new boolean[height][width];

    cp.add(darkMode);
    cp.add(instructions);
    cp.add(submit);
    cp.add(clearAll);
    cp.add(clearPath);
    cp.add(placeBomb);
    cp.add(buttonPanel);
    cp.add(noPath);

    setSize(700, 700);
    setVisible(true);
  }
    public static void main(String[] args){
      Main app = new Main();
    }

    private class ButtonListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
        // dark mode
        if(e.getActionCommand().equals("Dark mode")){
          Main.cp.setBackground(Color.GRAY);
          Main.darkMode.setText("Light mode");
        }
        // light mode
        else if(e.getActionCommand().equals("Light mode")){
          Main.cp.setBackground(Color.WHITE);
          Main.darkMode.setText("Dark mode");
        }
        // runs a BFS and finds a shortest path, if there is one
        else if(e.getActionCommand().equals("Run")){
          bombArr.add(new Pair(height-1, width-1));
          for(int i = 1; i < bombArr.size(); i++){
            Pair currPair = bombArr.get(i);
            Pair prevPair = bombArr.get(i-1);
            ArrayList<Pair> arr = bfs(prevPair.a, prevPair.b, currPair.a, currPair.b);
            if(arr != null){
              for(Pair p : arr){
                if(p.a != prevPair.a || p.b != prevPair.b){
                  buttonArr[p.a][p.b].setIcon(yellow);
                }
              }
              noPath.setVisible(false);
            }
            else{
              noPath.setVisible(true);
              break;
            }
            reset();
          }
          bombArr.remove(bombArr.size()-1);
        }
        // clears the grid
        else if(e.getActionCommand().equals("Clear all")){
          clearAll();
        }
        else if(e.getActionCommand().equals("Clear path")){
          resetPath();
        }
        else if(e.getActionCommand().equals("Place bomb")){
          isBomb = !isBomb;
        }
        else{
          int i = Integer.parseInt(e.getActionCommand())/height;
          int j = Integer.parseInt(e.getActionCommand()) - i*height;
          if(isBomb){
            bombArr.add(new Pair(i, j));
            buttonArr[i][j].setIcon(bomb);
          }
          else{
            if(buttonArr[i][j].getIcon() == null || !buttonArr[i][j].getIcon().equals(black)){
              buttonArr[i][j].setIcon(black);
              visited[i][j] = true;
              bombArr.remove(new Pair(i, j));
            }
            else{
              buttonArr[i][j].setIcon(null);
              visited[i][j] = false;
            }
          }
        }
      }
      public ArrayList<Pair> bfs(int r, int c, int goalR, int goalC){
        //reset();
        Queue<ArrayList<Pair>> q = new LinkedList<ArrayList<Pair>>();
        ArrayList<Pair> arr = new ArrayList<Pair>();
        arr.add(new Pair(r, c));
        q.offer(arr);
        visited[r][c] = true;
        while(q.peek() != null){
          ArrayList<Pair> currArr = q.poll();
          int currR = currArr.get(currArr.size()-1).a;
          int currC = currArr.get(currArr.size()-1).b;
          int[] dX = {-1, 1, 0, 0};
          int[] dY = {0, 0, -1, 1};
          for(int i = 0; i < 4; i++){
            int dR = currR + dY[i];
            int dC = currC + dX[i];
            if(dR >= 0 && dR < height && dC >= 0 && dC < width){
              if(!visited[dR][dC]){
                if(dR == goalR && dC == goalC){
                  return currArr;
                }
                ArrayList<Pair> pushArr = new ArrayList<Pair>();
                for(Pair p : currArr){
                  pushArr.add(p.clone());
                }
                pushArr.add(new Pair(dR, dC));
                q.offer(pushArr);
                visited[dR][dC] = true;
              }
            }
          }
        }
        return null;
      }
      // clears the grid
      public void clearAll(){
        bombArr.clear();
        for(int i = 0; i < height; i++){
          for(int j = 0; j < width; j++){
            if(i+j != 0 && i+j != height+width-2){
              buttonArr[i][j].setIcon(null);
              visited[i][j] = false;
            }
          }
        }
      }
      // reset after bfs finds a bomb
      public void reset(){
        for(int i = 0; i < height; i++){
          for(int j = 0; j < width; j++){
            if(buttonArr[i][j].getIcon() == null){
              visited[i][j] = false;
            }
            else if(!buttonArr[i][j].getIcon().equals(black)){
              visited[i][j] = false;
            }
          }
        }
      }
      // resets the path
      public void resetPath(){
        for(int i = 0; i < height; i++){
          for(int j = 0; j < width; j++){
            if(buttonArr[i][j].getIcon() == null){
              visited[i][j] = false;
            }
            else if(buttonArr[i][j].getIcon().equals(yellow)){
              buttonArr[i][j].setIcon(null);
              visited[i][j] = false;
            }
          }
        }
      }
    }
}
