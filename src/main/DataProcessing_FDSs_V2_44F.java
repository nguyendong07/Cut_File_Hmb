package main;

import feature_compute.PreprocessAndComputeFeatures;
import java.io.File;

public class DataProcessing_FDSs_V2_44F {

	public static String[] VH = { "Walk" };
	public static String[] Sensors = { "acc", "ori", "gyro" };
//	public static float[] Rate = {0.1f, 0.2f, 0.30f, 0.40f, 0.50f, 0.60f, 0.70f, 0.80f, 0.90f};
	public static float[] Rate = {0.50f};
	public static String rawData = "FULL";

	//public static String DATA_PATH = "D:\\Data\\NCS20212024\\FallDetectionSystem\\Datasets\\UpFall\\UP_Fall_Dataset_Accelerometer_Pocket\\File_csv";
	public static String DATA_PATH = "C:\\Users\\dongnv\\Desktop\\CSV";

	public static String DATA_PATH_CUT = "";
	private static File dataFolerStatus;

	
	private static String DATA_OUTPUT_PATH = "C:\\Users\\dongnv\\Desktop\\OUTPUT_62Features";

	public static void creatingFolder() throws Exception {
		File mFile = new File(DATA_OUTPUT_PATH);
		File[] listFiles = mFile.listFiles();
		for (File file : listFiles) {
			System.out.println("Deleting " + file.getName());
			file.delete();
		}		
		new File(DATA_OUTPUT_PATH + "\\acc_0.5").mkdir();
	}
	public static void cuttingFile() throws Exception {
		creatingFolder();
		dataFolerStatus = new File(DATA_PATH);
		if (!dataFolerStatus.exists()) {
			System.out.println("Does not existed file");
			return;
		}
		File[] fileInFolderStatus = dataFolerStatus.listFiles();

		// select folder file csv ----- CSI, CSO, BSC, ....
		for (int statusNumber = 0; statusNumber < fileInFolderStatus.length; statusNumber++) {
			DATA_PATH_CUT = DATA_PATH + "\\" + fileInFolderStatus[statusNumber].getName();
			for (int r = 0; r < Rate.length; r++) { // { 0.1f, 0.10f, 0.20f, 0.30f, 0.40f, 0.50f, 0.60f, 0.70f, 0.80f, 0.90f };
				for (int j = 0; j < 1; j++) { // Sensors "acc", "ori", "gyro"
					for (int i = 1; i <= 6; i++) { // FFTlength[] = { 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384 } windownLenght
																//	   0   1   2    3    4   5     6     7     8     9     10
						System.out.println(fileInFolderStatus[statusNumber].getName() + "_" + Sensors[j] + "_" + Rate[r]);
						System.out.println("  =>  windownLenght = " + r);
						PreprocessAndComputeFeatures computer = new PreprocessAndComputeFeatures(i, VH[0], Rate[r],
								rawData, Sensors[j], DATA_PATH_CUT, DATA_OUTPUT_PATH);// Depend Vehicle
						String opf12 = Sensors[j] + "_" + i + "_" + Rate[r] + "_model_" + 13 + "_UpFall_62F.csv";
						computer.outputFile(opf12, 13);

					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		cuttingFile();
		System.out.println("cut xong!");
	}
}
