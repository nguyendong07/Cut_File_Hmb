package feature_compute;

import java.util.Arrays;
public class slidewidow{

    public static int Window = 4;
	public static double[] X = {1,2,3,4,5,6,7,8,9,10,11,12};
    public static double rate = 0.5;
	public slidewidow() {
       this.Window = Window;
       this.rate = rate;
    }
     public static void main(String []args){
        Slide();
     }
		/*
		 * public static double CopyRange(double[] x, int a, int b) {
		 * 
		 * for (int i = a ; i <= b ; i++) {
		 * 
		 * } }
		 */
     
      public static void Slide() {
        double[] X1 = {1,2,3,4,5,6,7,8,9,10,11,12};
    	
    	 double[] CopyX = Arrays.copyOfRange(X1, 1, 2) ;
    	System.out.print(CopyX); 	
    	
    }
}