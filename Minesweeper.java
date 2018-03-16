import javafx.application.Application;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;
import java.util.function.Consumer;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
/*
 * @author ronit
 */
public class Minesweeper extends Application {  
    int noOfRows;
    int noOfCols;
    int mines;
    int flagsUsed;
    Stage s;
    boolean firstClick;
    Cell[][] cells;
    Button[][] buttons;
    private void settingValues(){
        List<String> choices = new ArrayList<>();
        choices.add("9*9   10 Mines");
        choices.add("16*16 40 Mines");
        choices.add("30*16 99 Mines");
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("9*9 10 Mines", choices);
        dialog.setHeaderText("Welcome to minesweeper");
        dialog.setTitle("Set diificulty ");
        dialog.setContentText("Choose difficulty setting:");
        
        Optional<String> result=dialog.showAndWait();
        if(result.isPresent()==false)
            System.exit(0);
        result.ifPresent(new Consumer<String>() {
            @Override
            public void accept(String letter) {
                if(letter.equalsIgnoreCase("9*9   10 Mines")){
                    noOfRows=9;
                    noOfCols=9;
                    mines=10;
                }
                else if(letter.equalsIgnoreCase("16*16 40 Mines")){
                    noOfRows=16;
                    noOfCols=16;
                    mines=40;
                }
                else if(letter.equalsIgnoreCase("30*16 99 Mines")){
                    noOfRows=16;
                    noOfCols=30;
                    mines=99;
                }
            }
        });
    }
    @Override
    public void start(Stage primaryStage){
        s=primaryStage;
        initialize(primaryStage);
    }
    private void flagsMoreThanMines(){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Putting more flags ");
        alert.setHeaderText("Ther are only "+mines+" mines . So you can't put more than "+mines+" flags ");
        alert.setContentText("Consider removing some falgs ");
        alert.showAndWait();
    }
    private void initialize(Stage primaryStage){
        noOfRows=9; //these are the default values 
        noOfCols=9;
        mines=10;      
        settingValues();
        firstClick=true;
        cells=new Cell[noOfRows][noOfCols];
        buttons=new Button[noOfRows][noOfCols];
        primaryStage.setTitle("Minewseeper");
        GridPane grid=new GridPane();
        for(int row=0;row<=noOfRows-1;row++){
            for(int col=0;col<=noOfCols-1;col++){
                buttons[row][col]=new Button();
                String location=this.getClass().getResource("buttonStyling.css").toExternalForm();
                buttons[row][col].getStylesheets().add(location);
                buttons[row][col].getStyleClass().add("cells");
                cells[row][col]=new Cell();
                cells[row][col].col=col;
                cells[row][col].row=row;
                buttons[row][col].setOnMousePressed(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent event) {
                        double x=event.getSceneX();
                        double y=event.getSceneY();
                        int row=(int)y/30;
                        int col=(int)x/30;
                        if(event.getButton().toString().equalsIgnoreCase("primary")){
                            if(firstClick)
                                placeBombs(cells[row][col]);
                            firstClick=false;
                            cells[row][col].revealed=true;
                            cellClicked(row,col);
                        }
                        else if(event.getButton().toString().equalsIgnoreCase("secondary")){
                            cod:if(cells[row][col].flagged==false&&cells[row][col].revealed==false){
                                if(flagsUsed==mines){
                                    flagsMoreThanMines();
                                    break cod;
                                }
                            cells[row][col].flagged=true;
                            flagsUsed++;
                            refreshGUI();
                            }
                            else{
                                cells[row][col].flagged=false;
                                cells[row][col].revealed=false;
                                if(buttons[row][col].getText().equals("F"))
                                buttons[row][col].setText("");
                                flagsUsed--;
                                refreshGUI();
                            }
                        }
                    }
                });
                buttons[row][col].setPrefSize(30,30);
                grid.add(buttons[row][col], col, row);
            }
        }
        Scene scene=new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();        
    }
    public static void main(String[] args) {
        launch(args);
    }
    private void winLosePrompt(boolean winOrLose){
        String val;
        if(winOrLose)
            val="You won";
        else 
            val="You lose";
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(null);
        alert.setHeaderText(val);
        alert.setContentText("Would you like to replay ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK)
            initialize(s); //restarting
         else 
            System.exit(0);
    }
    private class Cell{
        boolean hasBomb;
        boolean flagged;
        /*
         * True If it's checked for bombs , this variable is necessary , otherwise it
         * may trigger stackoverflow error 
         */
        boolean checked=false; 
        boolean revealed=false; //it's not clicked yet 
        int noOfBommbNear=0; 
        int row;
        int col;
    }
    private void placeBombs(Cell c){
        Random rand=new Random();
        int placed=0;
        while(placed<mines){
        int row=rand.nextInt(noOfRows);
        int col=rand.nextInt(noOfCols);
        if(row==c.row&&col==c.col)
            continue;
        else if(cells[row][col].hasBomb)
            continue;
        else{
            cells[row][col].hasBomb=true;
            placed++;
        }
        }
    }
    private void refreshGUI(){
        for(int r=0;r<=noOfRows-1;r++){
            for(int c=0;c<=noOfCols-1;c++){
                if(cells[r][c].revealed&&cells[r][c].noOfBommbNear==0){
                    buttons[r][c].getStyleClass().add("clicked_cells");
                    if(cells[r][c].flagged){ //to remove flags 
                    buttons[r][c].setText(""); 
                    flagsUsed--;
                    }
                }
                else if(cells[r][c].revealed&&cells[r][c].flagged==false){
                    buttons[r][c].getStyleClass().add("clicked_cells");
                    buttons[r][c].setText(Integer.toString(cells[r][c].noOfBommbNear));
                }
                if(cells[r][c].flagged&&cells[r][c].revealed==false)
                    buttons[r][c].setText("F");
            }
        }
    }
    private boolean hasWon(){
        boolean result=false;
        int nonBombCell=0;
        for(int r=0;r<=noOfRows-1;r++){
            for(int c=0;c<=noOfCols-1;c++){
                if(cells[r][c].revealed&&cells[r][c].hasBomb==false)
                    nonBombCell++;
            }
        }
        if(nonBombCell==(noOfRows*noOfCols)-mines)
            result=true;
        return result;
    }
    private void cellClicked(int r,int c){
        if(cells[r][c].hasBomb && cells[r][c].flagged==false){
            cells[r][c].revealed=true;
            refreshGUI();
            winLosePrompt(false);
        }
        else if(cells[r][c].flagged&&cells[r][c].revealed);
        else{
            checkSurrounding(r,c);
            if(hasWon())
                winLosePrompt(true);
            refreshGUI();
        }
    }
    private void checkSurrounding(int r,int c){
      int bombs=0;
        for(int cRow=r-1;cRow<=r+1;cRow++){ //looping through all the neighbouring cells 
            for(int cCol=c-1;cCol<=c+1;cCol++){
                try{
                    if(cells[cRow][cCol].hasBomb)
                        bombs++;
                  }
                catch(IndexOutOfBoundsException e){ //sometimes values can be negative or more than the number 
                  continue;  //of rows in that situation it just skips  
                }
            }
        }
        cells[r][c].checked=true;
        if(bombs==0){
            cells[r][c].revealed=true;
            for(int cRow=r-1;cRow<=r+1;cRow++){  
               for(int cCol=c-1;cCol<=c+1;cCol++){
                    if((cRow>=0&&cCol>=0) && (cRow<=noOfRows-1&&cCol<=noOfCols-1)){ //checking if they are negative or 
                     if(cells[cRow][cCol].checked==false){ //more than the range 
                        checkSurrounding(cRow,cCol);
                     }
                     else{
                         continue;   
                    }
                  }                  
               }
           }
        }
        else{
            cells[r][c].noOfBommbNear=bombs;
            cells[r][c].revealed=true;
        }
    }
    }
