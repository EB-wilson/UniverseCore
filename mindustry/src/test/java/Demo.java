import java.util.Arrays;

public class Demo{
  public static void main(String[] args) throws NoSuchFieldException{
    System.out.println(Arrays.toString(findSum(12, 1, 8, 3, 5, 4, 6, 10)));
  }

  public static int[] findSum(int target, int... ints){
    int[] res = new int[2];

    int counter = 0;
    for(int i = 0; i < ints.length; i++){
      for(int j = i; j < ints.length; j++){
        counter++;
        /*if(i == j) continue;
        if(ints[i] + ints[j] == target){
          res[0] = i;
          res[1] = j;
          return res;
        }*/

      }
    }

    System.out.println("n: " + ints.length);
    System.out.println("nlogn: " + ints.length*Math.log(ints.length));
    System.out.println(counter);

    return null;
  }
}
