//cutFile_FDSs_V2_44F

package feature_compute;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import feature_compute.PreprocessAndComputeFeatures;

public class PreprocessAndComputeFeatures {

	public String DATA_PATH = "E:\\RawData\\RawData_Train2_3"; // Ä�áº§u vÃ o cuttingFile
	public String DATA_OUTPUT_PATH = "E:\\RawData\\cutTrain2.3";

	private static String RAW_ACCEL = "FULL";
	private static String BEHAVIOR = "V";
	private static final String FILTER_VEHICLE = "Moto";
	private static final String HEADER_FILE = "Accelerometer"; // Ã„ï¿½ang thiÃ¡ÂºÂ¿u file cÃƒÂ³ tÃ¡Â»Â« khÃƒÂ³a
	private int roundFFT = 6;
	public int modelKind = 0;

	public static final int READING_PER_SECOND = 100;// Táº§n sá»‘ 50Hz, cÃ³ thá»ƒ thay Ä‘á»•i Ä‘áº¿n 200Hz
	public static final int TIME_HEADER_AND_FOOTER_FOR_STOP = 1;
	private static final int TIME_HEADER_AND_FOOTER_FOR_MOVING = 1;
	private static final int TIME_HEADER_AND_FOOTER_FOR_ABNORMAL_ACTIVITY = 1;
	private static final String MOVING = "Moving";
	private static float OVERLAPPING; // 0.25f; // 50%
	private static final int WINDOWN_X = 1;
	private static final int WINDOWN_Y = 2;
	private static final int WINDOWN_Z = 3;
	private static final int TIMESTAMP = 4;
	private static final double ALPHA = 0.1d;

	public ArrayList<FeaturesVector> HARDataset;
	public ArrayList<ArrayList<Double>> InstanceFeatureValueModel2;

	// Raw data
	private ArrayList<SimpleAccelData> rawAccelDatas;

	private int windownLenght;
	private String vhkind;
	public int studentdata = 0;

	private File dataFoler;
	private File dataFile;
	private FileWriter fileWriter;

	public String RawData() {

		return this.RAW_ACCEL;

	}

	public String vehicleKind() {

		return this.FILTER_VEHICLE;
	}
	// Ä‘oáº¡n code trong hÃ m PreprocessAndComputeFeatures lÃ  Ä‘á»ƒ Ä‘á»�c file csv cÃ³
	// tÃªn:Accelerometer_Motor_S_, vÃ 
	// 04_05_2017_16_34_UserID1_ngan_Moto_Stop_FULL_TRANS_ACCE_DATA.csv
	// vÃ¬ váº­y khi tÃªn file khÃ´ng cÃ³ Ä‘á»‹nh dáº¡ng nhÆ° 2 kiá»ƒu nÃ y chÆ°Æ¡ng trÃ¬nh cháº¡y bá»‹
	// lá»—i
	// tÃªn BSC_gyro_1_2 lÃ  tÃªn sai. Muá»‘n khÃ´ng bá»‹ lá»—i cÃ³ thá»ƒ Ä‘á»•i tÃªn file
	// BSC_gyro_1_2 thÃ nh 03_09_2020_13_57_User_tran_Moto_Stop_FULL_TRANS_ACCE_DATA
	// hoáº·c dÃ¹ng Ä‘oáº¡n code dÆ°á»›i Ä‘Ã¢y

	/*
	 * Ä‘oáº¡n code nÃ y sáº½ xá»­ lÃ½ cÃ¡c file cÃ³ Ä‘á»‹nh dáº¡ng
	 * vehicle_status_number1_number2.csv vÃ­ dá»¥ BSC_gyro_1_2.csv
	 */
	public static ArrayList<String> process_file_name(String file_name) {
		ArrayList<String> name_part = new ArrayList<String>(); // khai bÃƒÂ¡o mÃ¡ÂºÂ£ng chÃ¡Â»Â©a output tÃƒÂªn file theo kiÃ¡Â»Æ’u
																// name_part = [vehicle, status]
		int first_of__ = file_name.indexOf("_"); // lÃ¡ÂºÂ¥y ra vÃ¡Â»â€¹ trÃƒÂ­ _ Ã„â€˜Ã¡ÂºÂ§u tiÃƒÂªn trong vd trÃƒÂªn lÃƒÂ  3
		System.out.println(file_name.indexOf("_", first_of__ + 1));
//		System.out.println(first_of__);		
		String status = file_name.substring(0, first_of__);// lÃ¡ÂºÂ¥y trÃ¡ÂºÂ¡ng thÃƒÂ¡i, vd: BSC
		String sensor = file_name.substring(first_of__ + 1, file_name.indexOf("_", first_of__ + 1)); // lÃ¡ÂºÂ¥y sensor ,
																										// vd: gyro
		String[] words = file_name.split("_");
		String sub = words[2];
		name_part.add(sensor);
		name_part.add(status);
		name_part.add(sub);
		return name_part;
	}

	// ********************************
	// Ä�oáº¡n chÆ°Æ¡ng trÃ¬nh Ä‘á»�c dá»¯ liá»‡u training cÃ³ tÃªn file dáº¡ng
	// Accelerometer_Motor_S_
	// ********************************
	// public PreprocessAndComputeFeatures(int windownLenght, String vhkind){

	public PreprocessAndComputeFeatures(int windownLenght, String vhkind, float ovl, String dataKind,
			String sensor_name, String data_path, String output_path) {

		this.RAW_ACCEL = dataKind;
		this.windownLenght = (windownLenght);
		this.vhkind = (vhkind);
		this.OVERLAPPING = ovl;
		this.DATA_PATH = data_path;
		this.DATA_OUTPUT_PATH = output_path;

		// this.windowLenght = dataProcessing.wdsite;
		HARDataset = new ArrayList<>();
		dataFoler = new File(DATA_PATH);

		if (!dataFoler.exists()) {
			System.out.println("Does not existed file");
			return;
		}

		File[] fileInFolder = dataFoler.listFiles();

		System.out.println(fileInFolder.length + "");

		for (int i = 0; i < fileInFolder.length; i++) {
			String fileName = fileInFolder[i].getName();
			String headerfile = fileName.substring(2, fileName.indexOf("_"));
			ArrayList<String> name_part = process_file_name(fileName);
			String status = name_part.get(1);
			String sub = name_part.get(2);
			this.DATA_OUTPUT_PATH = output_path + "\\" + sensor_name + "_" + ovl;

			if (fileName.indexOf(sensor_name) != -1 && Integer.parseInt(sub) < 68) { // so luong sub muon dung
//			if (fileName.indexOf(sensor_name) != -1) { // so luong sub muon dung
				System.out.println(fileName);
				rawAccelDatas = getAccelDatasFromCSV(fileInFolder[i], 0); // Ä‘á»�c vÃ  láº¥y dá»¯ liá»‡u file Ä‘áº§u vÃ o
//				computeFeatures(rawAccelDatas, this.vhkind, status); // xá»­ lÃ½ dá»¯ liá»‡u táº¡o ra cÃ¡c Ä‘áº·c trÆ°ng má»›i
				computeFeatures_A_Nhac(rawAccelDatas, this.vhkind, status);
			}
		}
	}

	// overloading
	public PreprocessAndComputeFeatures(int[] wdl, String vhkind, float[] ovl, String[] st, String dataKind,
			String sensor_name, String data_path, String output_path) {
		System.out.println("run run run");
		// float [] Rate = {0.25f,0.5f,0.75f};
		// this.windownLenght = (windownLenght);
		this.RAW_ACCEL = dataKind;
		this.vhkind = (vhkind);
		// this.OVERLAPPING = ovl;

		// this.windowLenght = dataProcessing.wdsite;
		HARDataset = new ArrayList<>();

		dataFoler = new File(DATA_PATH);
		if (!dataFoler.exists()) {
			System.out.println("Does not existed file");
			return;
		}

		File[] fileInFolder = dataFoler.listFiles();
		System.out.println(fileInFolder.length + "");

		for (int i = 0; i < fileInFolder.length; i++) {
			String fileName = fileInFolder[i].getName();
			String headerfile = fileName.substring(0, fileName.indexOf("_"));
			if (headerfile.equals(HEADER_FILE)) {// get data from student collector
				this.studentdata = 1;
				int space = fileName.indexOf("_");
				int space2 = fileName.indexOf("_", space + 1);
				String vehicle = fileName.substring(space + 1, space2);
				String status = fileName.substring(space2 + 1, fileName.indexOf("_", space2 + 1));
				rawAccelDatas = getAccelDatasFromCSV(fileInFolder[i], studentdata);

				// rawAccelDatas = preprocessData(rawAccelDatas, status);
				computeFeatures(rawAccelDatas, vehicle, status);

			} else {
				int space = fileName.indexOf("_");
				int space2 = fileName.indexOf("_", space + 1);
				int space3 = fileName.indexOf("_", space2 + 1);
				int space4 = fileName.indexOf("_", space3 + 1);
				int space5 = fileName.indexOf("_", space4 + 1);
				int space6 = fileName.indexOf("_", space5 + 1);
				int space7 = fileName.indexOf("_", space6 + 1);
				int space8 = fileName.indexOf("_", space7 + 1);
				int space9 = fileName.indexOf("_", space8 + 1);
				int space10 = fileName.indexOf("_", space9 + 1);
//in 02062021 thÃªm má»›i				
				int space11 = fileName.indexOf("_", space10 + 1);
				int space12 = fileName.indexOf("_", space11 + 1);
//out 02062021 thÃªm má»›i					
				// int space11 = fileName.indexOf("_", space10 + 1);
				// int space11= fileName.in

				// String sensor = fileName.substring(0, fileName.indexOf("_"));
				// String sensor = fileName.substring(0, fileName.indexOf("_"));
				// Get the kind of sensor: Full or Root
				String sensor = fileName.substring(fileName.indexOf("_", space10 + 1) + 1,
						fileName.indexOf("_", space11 + 1));

				// Get vehicle kind to filter
				String vehicleKind = fileName.substring(fileName.indexOf("_", space6 + 1) + 1,
						fileName.indexOf("_", space7 + 1));
				// Get Activity
				String activityKind = fileName.substring(fileName.indexOf("_", space7 + 1) + 1,
						fileName.indexOf("_", space8 + 1));

				// if (sensor.equals(RAW_ACCEL) )
				// Filter activity, sensor, vehicleKind
				// if (sensor.equals(RAW_ACCEL) && vehicleKind.equals(FILTER_VEHICLE) ){
				// All activity
				boolean filterActivity = (activityKind.equals("S") || activityKind.equals("Stop")
						|| activityKind.equals("Mov") || activityKind.equals("M") || activityKind.equals("Acc")
						|| activityKind.equals("A") || activityKind.equals("Dec") || activityKind.equals("D")
						|| activityKind.equals("L") || activityKind.equals("R") || activityKind.equals(BEHAVIOR));

				boolean filterSMALR = ((sensor.equals(RAW_ACCEL) && vehicleKind.equals(vhkind))
						&& (filterActivity == true)); // depend on vhkind to compute

				// boolean filterActivity =
				// (activityKind.equals("Acc")||activityKind.equals("A")||activityKind.equals("Dec")||activityKind.equals("D")||activityKind.equals("L")||activityKind.equals("R"));
				if ((filterSMALR == true)) {
					// String vehicle = fileName.substring(space + 1,space2);
					String vehicle = fileName.substring(fileName.indexOf("_", space6 + 1) + 1,
							fileName.indexOf("_", space7 + 1));

					// String status = fileName.substring(space2 + 1, fileName.indexOf("_", space2 +
					// 1));
					String status = fileName.substring(fileName.indexOf("_", space7 + 1) + 1,
							fileName.indexOf("_", space8 + 1));
					// change label Mov -> M

					if (status.equals("Mov")) {

						status = "M";
					}
					// Changing label Stop -> S
					if (status.equals("Acc")) {
						status = "M";
					}
					//
					if (status.equals("Dec")) {
						status = "M";
					}
					if (status.equals("V")) { // chuyen trang thai V thanh M
						status = "M";
					}
					//
					if (status.equals("D")) {
						status = "M";
					}
					//
					if (status.equals("A")) {
						status = "M";
					}
					//
					if (status.equals("Stop")) {
						status = "S";

					}
					// Return window length and overlapping by per status
					for (int n = 0; n < 4; n++) {
						if (status.equals(st[n])) {
							this.windownLenght = wdl[n];
							PreprocessAndComputeFeatures.OVERLAPPING = ovl[n];
						}
					}

					//
					rawAccelDatas = getAccelDatasFromCSV(fileInFolder[i], 0);

					// rawAccelDatas = preprocessData(rawAccelDatas, status);
					computeFeatures(rawAccelDatas, vehicle, status);
				} // end else
			} // end for
		}

	}

	/**
	 *
	 * @param rawAccelDatas data in a windown
	 * @param begin         collect data from @param begin
	 * @param windownLenght number of samples
	 * @return
	 */
	private ArrayList<SimpleAccelData> getRawDataWindown(ArrayList<SimpleAccelData> rawAccelDatas, int begin,
			int windownLenght) {
		ArrayList<SimpleAccelData> datas = new ArrayList<>();
		for (int i = 0; i < windownLenght; i++) {
			datas.add(rawAccelDatas.get(begin + i));
		}
		return datas;
	}

	public void outputFile(String fileName, int modelKind) {
		this.modelKind = (modelKind);
		File mFile = new File(DATA_OUTPUT_PATH);
		if (!mFile.exists()) {
			mFile.mkdirs();
		}
		dataFile = new File(DATA_OUTPUT_PATH, fileName);
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Created new file and writing

			fileWriter = null;

			try {
				fileWriter = new FileWriter(dataFile);

				//
				int size = HARDataset.size();

				// ======= Ä�áº£o vá»‹ trÃ­ cÃ¡c modelKind

				// ===== modelKind == 0 ======
				if (modelKind == 0) { // T1 (Ä‘Ãºng vÃ  Ä‘á»§ 20 feature (20210313))

					writeLine(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								f.getStatus());
					}
					// ===== End modelKind == 0 ======

					// ===== modelKind == 1 ======
				} else if (modelKind == 1) { // F1 = 4 feature
					writeLine_model1(
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model1(
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",

								f.getStatus());
					}
					// ===== End modelKind == 1 ======

					// ===== modelKind == 2 ======
				} else if (modelKind == 2) { // H1 = 3 feature => chi co 03 Hjorth

					writeLine_model2(
							// H1
							"Activity", "Complexity", "Mobility", "Status");

					for (int i = 0; i < size; i++) { // day du

						FeaturesVector f = HARDataset.get(i);
						writeLine_model2(
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",

								f.getStatus());
					}
					// ===== End modelKind == 2 ======

					// ===== modelKind == 3 ======
				} else if (modelKind == 3) { // TF1 = 24 feature =>
					writeLine_model3(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model3(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",

								f.getStatus());

					}
					// ===== End modelKind == 3 ======

					// ===== modelKind == 4 ======
				} else if (modelKind == 4) { // TH1 = 23 feature => Time + frequency

					writeLine_model4(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// H1
							"Activity", "Complexity", "Mobility", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);
						writeLine_model4(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",

								f.getStatus());
					}
					// ===== End modelKind == 4 ======

					// ===== modelKind == 5 ======
				} else if (modelKind == 5) { // TFH1 = 27 feature => Time + Frequency + Hjorth
					writeLine_model5(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy",
							// H1
							"Activity", "Complexity", "Mobility", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);
						writeLine_model5(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",

								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",

								f.getStatus());
					}
					// ===== End modelKind == 5 ======

					// ===== modelKind == 6 ======
				} else if (modelKind == 6) { // T2 = 34 feature => Chi thuoc tinh trong mien thoi gian
					writeLine_model6(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
							"xPAR", "yPAR", "zPAR", "xSMA", "ySMA", "zSMA", "SMA", "SMVD", "meanPhi", "meanTheta",
							"varPhi", "varTheta", "igPhi", "igTheta", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model6(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",

								f.getStatus());

					}
					// ===== End modelKind == 6 ======

					// ===== modelKind == 7 ======
				} else if (modelKind == 7) {// F2 = 7 feature
					writeLine_model7(
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy",
							// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
							"xFFTEntropy", "yFFTEntropy", "zFFTEntropy", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model7(
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",

								f.getStatus());
					}
					// ===== End modelKind == 7 ======

					// ===== modelKind == 8 ======
				} else if (modelKind == 8) { // H2 = 18 FEATURR => Chi 18 tham so Hjorth
					writeLine_model8(
							// H1
							"Activity", "Complexity", "Mobility",
							// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
							"xActivity", "yActivity", "zActivity", "pActivity", "tActivity", "xMobility", "yMobility",
							"zMobility", "pMobility", "tMobility", "xComplexity", "yComplexity", "zComplexity",
							"pComplexity", "tComplexity", "Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model8(
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}
					// ===== End modelKind == 8 ======

					// ===== modelKind == 9 ======
				} else if (modelKind == 9) { // TF2 = 41 feature
					writeLine_model9(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
							"xPAR", "yPAR", "zPAR", "xSMA", "ySMA", "zSMA", "SMA", "SMVD", "meanPhi", "meanTheta",
							"varPhi", "varTheta", "igPhi", "igTheta",
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy",
							// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
							"xFFTEntropy", "yFFTEntropy", "zFFTEntropy", "Status");

					for (int i = 0; i < size; i++) {

						FeaturesVector f = HARDataset.get(i);

						writeLine_model9(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",

								f.getStatus());
					}
					// ===== End modelKind == 9 ======

					// ===== modelKind == 10 ======
				} else if (modelKind == 10) { // TH2 = 52 feature
					writeLine_model10(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
							"xPAR", "yPAR", "zPAR", "xSMA", "ySMA", "zSMA", "SMA", "SMVD", "meanPhi", "meanTheta",
							"varPhi", "varTheta", "igPhi", "igTheta",
							// H1
							"Activity", "Complexity", "Mobility",
							// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//							"xActivity", "yActivity", "zActivity", "pActivity", "tActivity", "xMobility", "yMobility",
//							"zMobility", "pMobility", "tMobility", "xComplexity", "yComplexity", "zComplexity",
//							"pComplexity", "tComplexity", 
							"Status");

					for (int i = 0; i < size; i++) {

						FeaturesVector f = HARDataset.get(i);

						writeLine_model10(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}
					// ===== End modelKind == 10 ======

					// ===== modelKind == 11 ======
				} else if (modelKind == 11) { // TFH2 = 59 feature
					writeLine_model11(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
							"xPAR", "yPAR", "zPAR", "xSMA", "ySMA", "zSMA", "SMA", "SMVD", "meanPhi", "meanTheta",
							"varPhi", "varTheta", "igPhi", "igTheta",
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy",
							// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
							"xFFTEntropy", "yFFTEntropy", "zFFTEntropy",
							// H1
							"Activity", "Complexity", "Mobility",
							// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//							"xActivity", "yActivity", "zActivity", "pActivity", "tActivity", "xMobility", "yMobility",
//							"zMobility", "pMobility", "tMobility", "xComplexity", "yComplexity", "zComplexity",
//							"pComplexity", "tComplexity", 
							"Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model11(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}
					// ===== End modelKind == 11 ======

				}
				
				
				else if (modelKind == 12) { // TFH2 = 59 feature
					writeLine_model12(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
							"xPAR", "yPAR", "zPAR", "xSMA", "ySMA", "zSMA", "SMA", "SMVD", "meanPhi", "meanTheta",
							"varPhi", "varTheta", "igPhi", "igTheta",
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy",
							// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
							"xFFTEntropy", "yFFTEntropy", "zFFTEntropy",
							// H1
							"Activity", "Complexity", "Mobility",
							
							//update new features
							"avaLamdaOren","varLamdaOren",
							"avaPhiOren", "varPhiOren",
							// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//							"xActivity", "yActivity", "zActivity", "pActivity", "tActivity", "xMobility", "yMobility",
//							"zMobility", "pMobility", "tMobility", "xComplexity", "yComplexity", "zComplexity",
//							"pComplexity", "tComplexity", 
							"Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model12(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								
								//update new features
								f.getAvaLamdaOren() + "", f.getVarLamdaOren() + "",
								
								f.getAvaPhiOren() + "", f.getVarPhiOren() + "",
								
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}
					// ===== End modelKind == 12 ======

				}
				
				else if (modelKind == 13) { // bo sung feature wavelet
					writeLine_model13(
							// T1
							"ARA", "MeanX", "MeanY", "MeanZ", "MeanXYZ", "VarX", "VarY", "VarZ", "DiffX", "DiffY",
							"DiffZ", "sdX", "sdY", "sdZ", "CovarianceXY", "CovarianceYZ", "CovarianceZX",
							"ZeroCrossingX", "ZeroCrossingY", "ZeroCrossingZ",
							// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
							"xPAR", "yPAR", "zPAR", "xSMA", "ySMA", "zSMA", "SMA", "SMVD", "meanPhi", "meanTheta",
							"varPhi", "varTheta", "igPhi", "igTheta",
							// F1
							"Energy", "XFFTEnergy", "yFFTEnergy", "zFFTEnergy",
							// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
							"xFFTEntropy", "yFFTEntropy", "zFFTEntropy",
							// H1
							"Activity", "Complexity", "Mobility",
							
							//update wavelet features
							
							//for X
							"MavWaveletXLevelOneA","MavWaveletXLevelOneD",
							"AvpWaveletXLevelOneA", "AvpWaveletXLevelOneD",
							"SkWaveletXLevelOneA","SkWaveletXLevelOneD",
							
							//for Y
							"MavWaveletYLevelOneA","MavWaveletYLevelOneD",
							"AvpWaveletYLevelOneA", "AvpWaveletYLevelOneD",
							"SkWaveletYLevelOneA","SkWaveletYLevelOneD",
							
							//for Z
							"MavWaveletZLevelOneA","MavWaveletZLevelOneD",
							"AvpWaveletZLevelOneA", "AvpWaveletZLevelOneD",
							"SkWaveletZLevelOneA","SkWaveletZLevelOneD",
							
							// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//							"xActivity", "yActivity", "zActivity", "pActivity", "tActivity", "xMobility", "yMobility",
//							"zMobility", "pMobility", "tMobility", "xComplexity", "yComplexity", "zComplexity",
//							"pComplexity", "tComplexity", 
							"Status");

					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model13(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								
								//update wavelet features
								f.getMavWaveletXLevelOneA() + "", f.getMavWaveletXLevelOneD() + "",
								
								f.getAvpWaveletXLevelOneA() + "", f.getAvpWaveletXLevelOneD() + "",
								
								f.getSkWaveletXLevelOneA() + "", f.getSkWaveletXLevelOneD() + "",
								
								
								f.getMavWaveletYLevelOneA() + "", f.getMavWaveletYLevelOneD() + "",
								
								f.getAvpWaveletYLevelOneA() + "", f.getAvpWaveletYLevelOneD() + "",
								
								f.getSkWaveletYLevelOneA() + "", f.getSkWaveletYLevelOneD() + "",
								
								
								f.getMavWaveletZLevelOneA() + "", f.getMavWaveletZLevelOneD() + "",
								
								f.getAvpWaveletZLevelOneA() + "", f.getAvpWaveletZLevelOneD() + "",
								
								f.getSkWaveletZLevelOneA() + "", f.getSkWaveletZLevelOneD() + "",
								
								
								
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}
					// ===== End modelKind == 12 ======

				}

				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			/**
			 * ****************************
			 */
		} else {

			try {

				fileWriter = new FileWriter(dataFile, true); // append data

				//
				int size = HARDataset.size();

				// ===== modelKind == 0 ======
				if (modelKind == 0) {// T1 = 20 feature (chi co 20 time domain)
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);
						writeLine(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								f.getStatus());
					}
					// ===== modelKind == 1 ======
				} else if (modelKind == 1) { // F1 = 4 feature
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model1(
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",

								f.getStatus());
					}

					// ===== modelKind == 2 ======
				} else if (modelKind == 2) { // H1 = 3 feature (// chi co 03 Hjorth
					for (int i = 0; i < size; i++) { // day du
						FeaturesVector f = HARDataset.get(i);

						writeLine_model2(
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",

								f.getStatus());
					}

					// ===== modelKind == 3 ======
				} else if (modelKind == 3) { // TF1 = 20 feature (without Hijorth feature)
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model3(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",

								f.getStatus());
					}

					// ===== modelKind == 4 ======
				} else if (modelKind == 4) { // TH1 = 23 feature ( // 23 Time + frequency
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model4(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",

								f.getStatus());
					}

					// ===== modelKind == 5 ======
				} else if (modelKind == 5) { // TFH1 = 27 feature => Time + Frequency + Hjorth
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);
						writeLine_model5(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",

								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",

								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",

								f.getStatus());
					}
					// ===== modelKind == 6 ======
					// ===== modelKind == 6 ======
				} else if (modelKind == 6) { // T2 = 34 feature => Chi thuoc tinh trong mien thoi gian
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model6(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",

								f.getStatus());
					}

					// ===== modelKind == 7 ======
				} else if (modelKind == 7) {
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model7(
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",

								f.getStatus());
					}

					// ===== modelKind == 8 ======
				} else if (modelKind == 8) {
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model8(
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}
					// ===== modelKind == 9 ======
				} else if (modelKind == 9) {
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model9(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",

								f.getStatus());
					}

					// ===== modelKind == 10 ======
				} else if (modelKind == 10) {
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model10(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}

					// ===== modelKind == 11 ======
				} else if (modelKind == 11) {
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);

						writeLine_model11(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}

				}
				
				
//				update new features
//				 ===== modelKind == 12 ======
			 else if (modelKind == 12) {
				for (int i = 0; i < size; i++) {
					FeaturesVector f = HARDataset.get(i);
					//f.getVarLamdaOren() + "", f.getAvaPhiOren() + "" + f.getVarPhiOren() + "",
					writeLine_model12(
							// T1
							f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
							f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
							f.getVarianceZ() + "",
							// diff = Distribution ???????????
							f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

							f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
							f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
							f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
							f.getZeroCrossingRateZ() + "",
							// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
							f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
							f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
							f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
							f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
							// F1
							f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
							f.getFftEnergyZ() + "",
							// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
							f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",
							// H1
							f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
							f.getAvaLamdaOren() + "", f.getVarLamdaOren()+"", f.getAvaPhiOren() + "", + f.getVarPhiOren() + "",
							// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//							f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//							f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//							f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//							f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//							f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

							f.getStatus());
				}

			}
				
				
			 else if (modelKind == 13) {
					for (int i = 0; i < size; i++) {
						FeaturesVector f = HARDataset.get(i);
						//f.getVarLamdaOren() + "", f.getAvaPhiOren() + "" + f.getVarPhiOren() + "",
						writeLine_model13(
								// T1
								f.getAverageResultantAcceleration() + "", f.getMeanX() + "", f.getMeanY() + "",
								f.getMeanZ() + "", f.getMeanXYZ() + "", f.getVarianceX() + "", f.getVarianceY() + "",
								f.getVarianceZ() + "",
								// diff = Distribution ???????????
								f.getDistributionX() + "", f.getDistributionY() + "", f.getDistributionZ() + "",

								f.getStandardDeviationX() + "", f.getStandardDeviationY() + "",
								f.getStandardDeviationZ() + "", f.getCovarianceXY() + "", f.getCovarianceYZ() + "",
								f.getCovarianceZX() + "", f.getZeroCrossingRateX() + "", f.getZeroCrossingRateY() + "",
								f.getZeroCrossingRateZ() + "",
								// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
								f.getParX() + "", f.getParY() + "", f.getParZ() + "", f.getsmaX() + "",
								f.getsmaY() + "", f.getsmaZ() + "", f.getsma() + "", f.getdsvm() + "",
								f.getMeanPhi() + "", f.getMeanTheta() + "", f.getvariancePhi() + "",
								f.getvarianceTheta() + "", f.getigPHI() + "", f.getigTHETA() + "",
								// F1
								f.getEnergy() + "", f.getFftEnergyX() + "", f.getFftEnergyY() + "",
								f.getFftEnergyZ() + "",
								// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
								f.getFftEntropyX() + "", f.getFftEntropyY() + "", f.getFftEntropyZ() + "",
								// H1
								f.getActivity() + "", f.getComplexity() + "", f.getMobility() + "",
								
								//bo sung feature wavelet
								f.getMavWaveletXLevelOneA() + "", f.getMavWaveletXLevelOneD()+"", 
								
								f.getAvpWaveletXLevelOneA() + "", + f.getAvpWaveletXLevelOneD() + "",
								
								f.getSkWaveletXLevelOneA() + "", + f.getSkWaveletXLevelOneD() + "",
								
								
								f.getMavWaveletYLevelOneA() + "", f.getMavWaveletYLevelOneD()+"", 
								
								f.getAvpWaveletYLevelOneA() + "", + f.getAvpWaveletYLevelOneD() + "",
								
								f.getSkWaveletYLevelOneA() + "", + f.getSkWaveletYLevelOneD() + "",
								
								
								f.getMavWaveletZLevelOneA() + "", f.getMavWaveletZLevelOneD()+"", 
								
								f.getAvpWaveletZLevelOneA() + "", + f.getAvpWaveletZLevelOneD() + "",
								
								f.getSkWaveletZLevelOneA() + "", + f.getSkWaveletZLevelOneD() + "",
								
								
								// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//								f.getActivityX() + "", f.getActivityY() + "", f.getActivityZ() + "",
//								f.getActivityPhi() + "", f.getActivityTheta() + "", f.getMobilityX() + "",
//								f.getMobilityY() + "", f.getMobilityZ() + "", f.getMobilityPhi() + "",
//								f.getMobilityTheta() + "", f.getComplexityX() + "", f.getComplexityY() + "",
//								f.getComplexityZ() + "", f.getComplexityPhi() + "", f.getComplexityTheta() + "",

								f.getStatus());
					}

				}


				fileWriter.flush();
				fileWriter.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}

	// == Model0 == // T1 = 20 feature (model8 => model0)
	private void writeLine(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ, String status)

			throws IOException {
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}
	// == End model0 ==

	// == Model1 == // F1 = 4 feature (Táº¡o má»›i báº±ng cÃ¡ch copy model3 => model1)
	private void writeLine_model1(
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy, String status)

			throws IOException { // Writing no Hijorth feature
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}
	// == End model1 ==

	// == Model2 == // H1 = 3 feature (model9 => model2)
	private void writeLine_model2(
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility, String status)

			throws IOException {
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}
	// == End model2 ==

	// == Model3 == // TF1 = 24 feature (model1 => model3)
	private void writeLine_model3(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy, String status)

			throws IOException { // Writing no Hijorth feature
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");

	}
	// == End model3 ==

	// == Model4 == // TH1 = 23 feature (model7 => model4, Bá»›t feature 24 xuá»‘ng cÃ²n
	// 23)
	private void writeLine_model4(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility, String status)

			throws IOException {
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}

	// == End model4 ==

	// == Model5 == // TFH1 = 27 feature (model6 => model5)
	private void writeLine_model5(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy,
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility, String status)

			throws IOException {
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}

	// == End model5 ==

	// == Model6 == // T2 = 34 feature (model3 => model6)
	private void writeLine_model6(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
			String xPAR, String yPAR, String zPAR, String xSMA, String ySMA, String zSMA, String SMA, String DSVM,
			String meanPhi, String meanTheta, String varPhi, String varTheta, String igPhi, String igTheta,
			String status)

			throws IOException { // Writing 36 feature
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
		fileWriter.append(xPAR);
		fileWriter.append(",");
		fileWriter.append(yPAR);
		fileWriter.append(",");
		fileWriter.append(zPAR);
		fileWriter.append(",");
		fileWriter.append(xSMA);
		fileWriter.append(",");
		fileWriter.append(ySMA);
		fileWriter.append(",");
		fileWriter.append(zSMA);
		fileWriter.append(",");
		fileWriter.append(SMA);
		fileWriter.append(",");
		fileWriter.append(DSVM);
		fileWriter.append(",");
		fileWriter.append(meanPhi);
		fileWriter.append(",");
		fileWriter.append(meanTheta);
		fileWriter.append(",");
		fileWriter.append(varPhi);
		fileWriter.append(",");
		fileWriter.append(varTheta);
		fileWriter.append(",");
		fileWriter.append(igPhi);
		fileWriter.append(",");
		fileWriter.append(igTheta);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}

	// == End model6 ==

	// == Model7 == // F2 = 7 feature (Táº¡o má»›i báº±ng cÃ¡ch copy model9 => model7)
	private void writeLine_model7(
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy,
			// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
			String xFFTEntropy, String yFFTEntropy, String zFFTEntropy, String status)

			throws IOException { // Writing 36 feature
		// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
		fileWriter.append(xFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(yFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(zFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}
	// == End model7 ==

	// == Model8 == // H2 = 18 feature (model5 => model8)
	private void writeLine_model8(
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility,
			// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
			String xActivity, String yActivity, String zActivity, String pActivity, String tActivity, String xMobility,
			String yMobility, String zMobility, String pMobility, String tMobility, String xComplexity,
			String yComplexity, String zComplexity, String pComplexity, String tComplexity, String status)

			throws IOException { //
		// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
		fileWriter.append(xActivity);
		fileWriter.append(",");
		fileWriter.append(yActivity);
		fileWriter.append(",");
		fileWriter.append(zActivity);
		fileWriter.append(",");
		fileWriter.append(pActivity);
		fileWriter.append(",");
		fileWriter.append(tActivity);
		fileWriter.append(",");

		fileWriter.append(xMobility);
		fileWriter.append(",");
		fileWriter.append(yMobility);
		fileWriter.append(",");
		fileWriter.append(zMobility);
		fileWriter.append(",");
		fileWriter.append(pMobility);
		fileWriter.append(",");
		fileWriter.append(tMobility);
		fileWriter.append(",");

		fileWriter.append(xComplexity);
		fileWriter.append(",");
		fileWriter.append(yComplexity);
		fileWriter.append(",");
		fileWriter.append(zComplexity);
		fileWriter.append(",");
		fileWriter.append(pComplexity);
		fileWriter.append(",");
		fileWriter.append(tComplexity);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}
	// == End model8 ==

	// == Model9 == // TF2 = 42 feature (model4 => model9)
	private void writeLine_model9(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
			String xPAR, String yPAR, String zPAR, String xSMA, String ySMA, String zSMA, String SMA, String DSVM,
			String meanPhi, String meanTheta, String varPhi, String varTheta, String igPhi, String igTheta,
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy,
			// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
			String xFFTEntropy, String yFFTEntropy, String zFFTEntropy, String status)

			throws IOException { // Writing 42 feature
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
		fileWriter.append(xPAR);
		fileWriter.append(",");
		fileWriter.append(yPAR);
		fileWriter.append(",");
		fileWriter.append(zPAR);
		fileWriter.append(",");
		fileWriter.append(xSMA);
		fileWriter.append(",");
		fileWriter.append(ySMA);
		fileWriter.append(",");
		fileWriter.append(zSMA);
		fileWriter.append(",");
		fileWriter.append(SMA);
		fileWriter.append(",");
		fileWriter.append(DSVM);
		fileWriter.append(",");
		fileWriter.append(meanPhi);
		fileWriter.append(",");
		fileWriter.append(meanTheta);
		fileWriter.append(",");
		fileWriter.append(varPhi);
		fileWriter.append(",");
		fileWriter.append(varTheta);
		fileWriter.append(",");
		fileWriter.append(igPhi);
		fileWriter.append(",");
		fileWriter.append(igTheta);
		fileWriter.append(",");
		// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
		fileWriter.append(xFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(yFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(zFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");

	}
	// == End model9 ==

	// == Model10 == // TH2 = 52 feature (model0 => model10, Bá»” SUNG THÃŠM FEATURE Ä�á»‚
	// Ä�á»¦ 52)
	private void writeLine_model10(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
			String xPAR, String yPAR, String zPAR, String xSMA, String ySMA, String zSMA, String SMA, String DSVM,
			String meanPhi, String meanTheta, String varPhi, String varTheta, String igPhi, String igTheta,
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility,
			// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//			String xActivity, String yActivity, String zActivity, String pActivity, String tActivity, String xMobility,
//			String yMobility, String zMobility, String pMobility, String tMobility, String xComplexity,
//			String yComplexity, String zComplexity, String pComplexity, String tComplexity, 
			
			String status)

			throws IOException { // Writing 42 feature
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
		fileWriter.append(xPAR);
		fileWriter.append(",");
		fileWriter.append(yPAR);
		fileWriter.append(",");
		fileWriter.append(zPAR);
		fileWriter.append(",");
		fileWriter.append(xSMA);
		fileWriter.append(",");
		fileWriter.append(ySMA);
		fileWriter.append(",");
		fileWriter.append(zSMA);
		fileWriter.append(",");
		fileWriter.append(SMA);
		fileWriter.append(",");
		fileWriter.append(DSVM);
		fileWriter.append(",");
		fileWriter.append(meanPhi);
		fileWriter.append(",");
		fileWriter.append(meanTheta);
		fileWriter.append(",");
		fileWriter.append(varPhi);
		fileWriter.append(",");
		fileWriter.append(varTheta);
		fileWriter.append(",");
		fileWriter.append(igPhi);
		fileWriter.append(",");
		fileWriter.append(igTheta);
		fileWriter.append(",");
		// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//		fileWriter.append(xActivity);
//		fileWriter.append(",");
//		fileWriter.append(yActivity);
//		fileWriter.append(",");
//		fileWriter.append(zActivity);
//		fileWriter.append(",");
//		fileWriter.append(pActivity);
//		fileWriter.append(",");
//		fileWriter.append(tActivity);
//		fileWriter.append(",");
//
//		fileWriter.append(xMobility);
//		fileWriter.append(",");
//		fileWriter.append(yMobility);
//		fileWriter.append(",");
//		fileWriter.append(zMobility);
//		fileWriter.append(",");
//		fileWriter.append(pMobility);
//		fileWriter.append(",");
//		fileWriter.append(tMobility);
//		fileWriter.append(",");
//
//		fileWriter.append(xComplexity);
//		fileWriter.append(",");
//		fileWriter.append(yComplexity);
//		fileWriter.append(",");
//		fileWriter.append(zComplexity);
//		fileWriter.append(",");
//		fileWriter.append(pComplexity);
//		fileWriter.append(",");
//		fileWriter.append(tComplexity);
//		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");
	}
	// == End model10 ==

	// == Model11 == // TFH2 = 59 feature (model2 => model11)
	private void writeLine_model11(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
			String xPAR, String yPAR, String zPAR, String xSMA, String ySMA, String zSMA, String SMA, String DSVM,
			String meanPhi, String meanTheta, String varPhi, String varTheta, String igPhi, String igTheta,
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy,
			// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
			String xFFTEntropy, String yFFTEntropy, String zFFTEntropy,
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility,
			
			// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//			String xActivity, String yActivity, String zActivity, String pActivity, String tActivity, String xMobility,
//			String yMobility, String zMobility, String pMobility, String tMobility, String xComplexity,
//			String yComplexity, String zComplexity, String pComplexity, String tComplexity, 
			
			String status)

			throws IOException {
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
		fileWriter.append(xPAR);
		fileWriter.append(",");
		fileWriter.append(yPAR);
		fileWriter.append(",");
		fileWriter.append(zPAR);
		fileWriter.append(",");
		fileWriter.append(xSMA);
		fileWriter.append(",");
		fileWriter.append(ySMA);
		fileWriter.append(",");
		fileWriter.append(zSMA);
		fileWriter.append(",");
		fileWriter.append(SMA);
		fileWriter.append(",");
		fileWriter.append(DSVM);
		fileWriter.append(",");
		fileWriter.append(meanPhi);
		fileWriter.append(",");
		fileWriter.append(meanTheta);
		fileWriter.append(",");
		fileWriter.append(varPhi);
		fileWriter.append(",");
		fileWriter.append(varTheta);
		fileWriter.append(",");
		fileWriter.append(igPhi);
		fileWriter.append(",");
		fileWriter.append(igTheta);
		fileWriter.append(",");
		// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
		fileWriter.append(xFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(yFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(zFFTEntropy);
		fileWriter.append(",");
		// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//		fileWriter.append(xActivity);
//		fileWriter.append(",");
//		fileWriter.append(yActivity);
//		fileWriter.append(",");
//		fileWriter.append(zActivity);
//		fileWriter.append(",");
//		fileWriter.append(pActivity);
//		fileWriter.append(",");
//		fileWriter.append(tActivity);
//		fileWriter.append(",");
//
//		fileWriter.append(xMobility);
//		fileWriter.append(",");
//		fileWriter.append(yMobility);
//		fileWriter.append(",");
//		fileWriter.append(zMobility);
//		fileWriter.append(",");
//		fileWriter.append(pMobility);
//		fileWriter.append(",");
//		fileWriter.append(tMobility);
//		fileWriter.append(",");
//
//		fileWriter.append(xComplexity);
//		fileWriter.append(",");
//		fileWriter.append(yComplexity);
//		fileWriter.append(",");
//		fileWriter.append(zComplexity);
//		fileWriter.append(",");
//		fileWriter.append(pComplexity);
//		fileWriter.append(",");
//		fileWriter.append(tComplexity);
//		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");

	}
	
	//String varLamdaOren, String avaPhiOren, String varPhiOren,
	
	private void writeLine_model12(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
			String xPAR, String yPAR, String zPAR, String xSMA, String ySMA, String zSMA, String SMA, String DSVM,
			String meanPhi, String meanTheta, String varPhi, String varTheta, String igPhi, String igTheta,
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy,
			// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
			String xFFTEntropy, String yFFTEntropy, String zFFTEntropy,
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility,
			
			String avaLamdaOren, String varLamdaOren, String avaPhiOren, String varPhiOren,
			
			// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//			String xActivity, String yActivity, String zActivity, String pActivity, String tActivity, String xMobility,
//			String yMobility, String zMobility, String pMobility, String tMobility, String xComplexity,
//			String yComplexity, String zComplexity, String pComplexity, String tComplexity, 
			
			String status)

			throws IOException {
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
		fileWriter.append(xPAR);
		fileWriter.append(",");
		fileWriter.append(yPAR);
		fileWriter.append(",");
		fileWriter.append(zPAR);
		fileWriter.append(",");
		fileWriter.append(xSMA);
		fileWriter.append(",");
		fileWriter.append(ySMA);
		fileWriter.append(",");
		fileWriter.append(zSMA);
		fileWriter.append(",");
		fileWriter.append(SMA);
		fileWriter.append(",");
		fileWriter.append(DSVM);
		fileWriter.append(",");
		fileWriter.append(meanPhi);
		fileWriter.append(",");
		fileWriter.append(meanTheta);
		fileWriter.append(",");
		fileWriter.append(varPhi);
		fileWriter.append(",");
		fileWriter.append(varTheta);
		fileWriter.append(",");
		fileWriter.append(igPhi);
		fileWriter.append(",");
		fileWriter.append(igTheta);
		fileWriter.append(",");
		// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
		fileWriter.append(xFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(yFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(zFFTEntropy);
		fileWriter.append(",");
		// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		
		fileWriter.append(avaLamdaOren);
		fileWriter.append(",");
		fileWriter.append(varLamdaOren);
		fileWriter.append(",");
		fileWriter.append(avaPhiOren);
		fileWriter.append(",");
		fileWriter.append(varPhiOren);
		fileWriter.append(",");
		// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//		fileWriter.append(xActivity);
//		fileWriter.append(",");
//		fileWriter.append(yActivity);
//		fileWriter.append(",");
//		fileWriter.append(zActivity);
//		fileWriter.append(",");
//		fileWriter.append(pActivity);
//		fileWriter.append(",");
//		fileWriter.append(tActivity);
//		fileWriter.append(",");
//
//		fileWriter.append(xMobility);
//		fileWriter.append(",");
//		fileWriter.append(yMobility);
//		fileWriter.append(",");
//		fileWriter.append(zMobility);
//		fileWriter.append(",");
//		fileWriter.append(pMobility);
//		fileWriter.append(",");
//		fileWriter.append(tMobility);
//		fileWriter.append(",");
//
//		fileWriter.append(xComplexity);
//		fileWriter.append(",");
//		fileWriter.append(yComplexity);
//		fileWriter.append(",");
//		fileWriter.append(zComplexity);
//		fileWriter.append(",");
//		fileWriter.append(pComplexity);
//		fileWriter.append(",");
//		fileWriter.append(tComplexity);
//		fileWriter.append(",");
		fileWriter.append(status);
		fileWriter.append("\n");

	}
	// == End model11 ==
	
	
	
	private void writeLine_model13(
			// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String ara, String meanX, String meanY, String meanZ, String meanXYZ, String varX, String varY, String varZ,
			String diffX, String diffY, String diffZ, String sdX, String sdY, String sdZ, String covXY, String covYZ,
			String covZX, String zrcX, String zrcY, String zrcZ,
			// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
			String xPAR, String yPAR, String zPAR, String xSMA, String ySMA, String zSMA, String SMA, String DSVM,
			String meanPhi, String meanTheta, String varPhi, String varTheta, String igPhi, String igTheta,
			// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String energy, String xFFTEnergy, String yFFTEnergy, String zFFTEnergy,
			// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
			String xFFTEntropy, String yFFTEntropy, String zFFTEntropy,
			// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
			String activity, String complexity, String mobility,
			
			String MavWaveletXLevelOneA, String MavWaveletXLevelOneD, 
			String AvpWaveletXLevelOneA, String AvpWaveletXLevelOneD,
			String SkWaveletXLevelOneA, String SkWaveletXLevelOneD,
			
			String MavWaveletYLevelOneA, String MavWaveletYLevelOneD, 
			String AvpWaveletYLevelOneA, String AvpWaveletYLevelOneD,
			String SkWaveletYLevelOneA, String SkWaveletYLevelOneD,
			
			
			String MavWaveletZLevelOneA, String MavWaveletZLevelOneD, 
			String AvpWaveletZLevelOneA, String AvpWaveletZLevelOneD,
			String SkWaveletZLevelOneA, String SkWaveletZLevelOneD,
			
			
			
			// Bá»• sung thÃªm 15 feature (H1 + 15 = H2)
//			String xActivity, String yActivity, String zActivity, String pActivity, String tActivity, String xMobility,
//			String yMobility, String zMobility, String pMobility, String tMobility, String xComplexity,
//			String yComplexity, String zComplexity, String pComplexity, String tComplexity, 
			
			String status)

			throws IOException {
		// T1 = 20 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(ara);
		fileWriter.append(",");
		fileWriter.append(meanX);
		fileWriter.append(",");
		fileWriter.append(meanY);
		fileWriter.append(",");
		fileWriter.append(meanZ);
		fileWriter.append(",");
		fileWriter.append(meanXYZ);
		fileWriter.append(",");
		fileWriter.append(varX);
		fileWriter.append(",");
		fileWriter.append(varY);
		fileWriter.append(",");
		fileWriter.append(varZ);
		fileWriter.append(",");
		fileWriter.append(diffX);
		fileWriter.append(",");
		fileWriter.append(diffY);
		fileWriter.append(",");
		fileWriter.append(diffZ);
		fileWriter.append(",");
		fileWriter.append(sdX);
		fileWriter.append(",");
		fileWriter.append(sdY);
		fileWriter.append(",");
		fileWriter.append(sdZ);
		fileWriter.append(",");
		fileWriter.append(covXY);
		fileWriter.append(",");
		fileWriter.append(covYZ);
		fileWriter.append(",");
		fileWriter.append(covZX);
		fileWriter.append(",");
		fileWriter.append(zrcX);
		fileWriter.append(",");
		fileWriter.append(zrcY);
		fileWriter.append(",");
		fileWriter.append(zrcZ);
		fileWriter.append(",");
		// Bá»• sung thÃªm 14 feature (T1 + 14 = T2)
		fileWriter.append(xPAR);
		fileWriter.append(",");
		fileWriter.append(yPAR);
		fileWriter.append(",");
		fileWriter.append(zPAR);
		fileWriter.append(",");
		fileWriter.append(xSMA);
		fileWriter.append(",");
		fileWriter.append(ySMA);
		fileWriter.append(",");
		fileWriter.append(zSMA);
		fileWriter.append(",");
		fileWriter.append(SMA);
		fileWriter.append(",");
		fileWriter.append(DSVM);
		fileWriter.append(",");
		fileWriter.append(meanPhi);
		fileWriter.append(",");
		fileWriter.append(meanTheta);
		fileWriter.append(",");
		fileWriter.append(varPhi);
		fileWriter.append(",");
		fileWriter.append(varTheta);
		fileWriter.append(",");
		fileWriter.append(igPhi);
		fileWriter.append(",");
		fileWriter.append(igTheta);
		fileWriter.append(",");
		// F1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(energy);
		fileWriter.append(",");
		fileWriter.append(xFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(yFFTEnergy);
		fileWriter.append(",");
		fileWriter.append(zFFTEnergy);
		fileWriter.append(",");
		// Bá»• sung thÃªm 3 feature (F1 + 3 = F2)
		fileWriter.append(xFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(yFFTEntropy);
		fileWriter.append(",");
		fileWriter.append(zFFTEntropy);
		fileWriter.append(",");
		// H1 = 4 feature (Ä‘Ã£ dÃ² Ä‘Ãºng - 13/03/2021)
		fileWriter.append(activity);
		fileWriter.append(",");
		fileWriter.append(complexity);
		fileWriter.append(",");
		fileWriter.append(mobility);
		fileWriter.append(",");
		
		fileWriter.append(MavWaveletXLevelOneA);
		fileWriter.append(",");
		fileWriter.append(MavWaveletXLevelOneD);
		fileWriter.append(",");
		fileWriter.append(AvpWaveletXLevelOneA);
		fileWriter.append(",");
		fileWriter.append(AvpWaveletXLevelOneD);
		fileWriter.append(",");
		fileWriter.append(SkWaveletXLevelOneA);
		fileWriter.append(",");
		fileWriter.append(SkWaveletXLevelOneD);
		fileWriter.append(",");
		
		
		fileWriter.append(MavWaveletYLevelOneA);
		fileWriter.append(",");
		fileWriter.append(MavWaveletYLevelOneD);
		fileWriter.append(",");
		fileWriter.append(AvpWaveletYLevelOneA);
		fileWriter.append(",");
		fileWriter.append(AvpWaveletYLevelOneD);
		fileWriter.append(",");
		fileWriter.append(SkWaveletYLevelOneA);
		fileWriter.append(",");
		fileWriter.append(SkWaveletYLevelOneD);
		fileWriter.append(",");
		
		
		fileWriter.append(MavWaveletZLevelOneA);
		fileWriter.append(",");
		fileWriter.append(MavWaveletZLevelOneD);
		fileWriter.append(",");
		fileWriter.append(AvpWaveletZLevelOneA);
		fileWriter.append(",");
		fileWriter.append(AvpWaveletZLevelOneD);
		fileWriter.append(",");
		fileWriter.append(SkWaveletZLevelOneA);
		fileWriter.append(",");
		fileWriter.append(SkWaveletZLevelOneD);
		fileWriter.append(",");
		
		fileWriter.append(status);
		fileWriter.append("\n");

	}
	
	
	
	// == End model11 ==
	ArrayList<Double> getInstance2Fastvector(double ara, double meanX, double meanY, double meanZ, double meanXYZ,
			double varX, double varY, double varZ, double diffX, double diffY, double diffZ, double sdX, double sdY,
			double sdZ, double covXY, double covYZ, double covZX, double zrcX, double zrcY, double zrcZ,
			double activity, double complexity, double mobility, double energy, double xFFTEnergy, double yFFTEnergy,
			double zFFTEnergy, double xFFTEntropy, double yFFTEntropy, double zFFTEntropy, double xPAR, double yPAR,
			double zPAR, double xSMA, double ySMA, double zSMA, double SMA, double DSVM, 
			
//			double xActivity, double yActivity, double zActivity, double pActivity, double tActivity, 
//			double xMobility, double yMobility, double zMobility, double pMobility, double tMobility, 
//			double xComplexity, double yComplexity, double zComplexity, double pComplexity, double tComplexity, 
			
			double meanPhi, double meanTheta, double varPhi, double varTheta, double igPhi, double igTheta) {

		ArrayList<Double> f = null;

		f.add(ara);
		f.add(meanX);
		f.add(meanY);
		f.add(meanZ);
		f.add(meanXYZ);
		f.add(varX);
		f.add(varY);
		f.add(varZ);
		f.add(diffX);
		f.add(diffY);
		f.add(diffZ);
		f.add(sdX);
		f.add(sdY);
		f.add(sdZ);
		f.add(covXY);
		f.add(covYZ);
		f.add(covZX);
		f.add(zrcX);
		f.add(zrcY);
		f.add(zrcZ);
		f.add(activity);
		f.add(complexity);
		f.add(mobility);
		f.add(energy);
		f.add(xFFTEnergy);
		f.add(yFFTEnergy);
		f.add(zFFTEnergy);
		f.add(xFFTEntropy);
		f.add(yFFTEntropy);
		f.add(zFFTEntropy);
		f.add(xPAR);
		f.add(yPAR);
		f.add(zPAR);
		f.add(xSMA);
		f.add(ySMA);
		f.add(zSMA);
		f.add(SMA);
		f.add(DSVM);
		
//		f.add(xActivity);
//		f.add(yActivity);
//		f.add(zActivity);
//		f.add(pActivity);
//		f.add(tActivity);
//		f.add(xMobility);
//		f.add(yMobility);
//		f.add(zMobility);
//		f.add(pMobility);
//		f.add(tMobility);
//		f.add(xComplexity);
//		f.add(yComplexity);
//		f.add(zComplexity);
//		f.add(pComplexity);
//		f.add(tComplexity);
		
		f.add(meanPhi);
		f.add(meanTheta);
		f.add(varPhi);
		f.add(varTheta);
		f.add(igPhi);
		f.add(igTheta);

		return f;

	}

	// ------------------------------------------------------------------------------------------------------

	private void computeFeatures(ArrayList<SimpleAccelData> accelDatas, String vehicle, String status) {
		int length = accelDatas.size();

		// int readingOfAWindown = windownLenght * READING_PER_SECOND + 56;
		int readingOfAWindown = windownLenght * READING_PER_SECOND;
		;

		float nWindows = 0; // number of windows
		if (OVERLAPPING >= 0.5) {
			nWindows = (length / (readingOfAWindown * OVERLAPPING)) - 1; // number of windows
		} else if (OVERLAPPING < 0.5 & OVERLAPPING != 0) {
			nWindows = (length / (readingOfAWindown * (1 - OVERLAPPING))) - 1; // number of windows

//Luyá»‡n thÃªm Ä‘á»ƒ cut file á»Ÿ overlap = 0%			
		} else // OVERLAPPING =0 luyen them
		{
			nWindows = (length / (readingOfAWindown * (1 - OVERLAPPING))); // number of windows
		}

//Them moi (20210605)
		if (nWindows < 1) {
			nWindows = 1;
			readingOfAWindown = accelDatas.size();
		}

		double[] endSignal = new double[] { 0d, 0d, 0d };
		double[] windownX;
		double[] windownY;
		double[] windownZ;
		double[] timestamp;
		PowerFeatures powerFeatures = new PowerFeatures(); /// TÃ¡ÂºÂ¡o ra thuÃ¡Â»â„¢c tÃƒÂ­nh cÃ¡Â»Â§a sÃ¡Â»Â¯ liÃ¡Â»â€¡u
		ArrayList<SimpleAccelData> windowData;

		FeaturesStatistic fStatistic;

// Them moi trÆ°Æ¡ng hop co 1 cua so do nwindow <1

		for (int i = 0; i <= nWindows - 1; i++) {

			windownX = getWindow(WINDOWN_X, accelDatas, (int) ((i * readingOfAWindown) * (1 - OVERLAPPING)),
					readingOfAWindown);

			System.out.println((int) ((i * readingOfAWindown) * (1 - OVERLAPPING)) + " , "
					+ (int) (((i * readingOfAWindown) * (1 - OVERLAPPING)) + readingOfAWindown));

			windownY = getWindow(WINDOWN_Y, accelDatas, (int) ((i * readingOfAWindown) * (1 - OVERLAPPING)),
					readingOfAWindown);
			windownZ = getWindow(WINDOWN_Z, accelDatas, (int) ((i * readingOfAWindown) * (1 - OVERLAPPING)),
					readingOfAWindown);
			timestamp = getWindow(TIMESTAMP, accelDatas, (int) ((i * readingOfAWindown) * (1 - OVERLAPPING)),
					readingOfAWindown);

			// High Filter
			windownX = filter(windownX);
			windownY = filter(windownY);
			windownZ = filter(windownZ);

			// Low filter
			windownX = lowFilter(windownX, i, endSignal[0]);
			windownY = lowFilter(windownY, i, endSignal[1]);
			windownZ = lowFilter(windownZ, i, endSignal[2]);

			if (i < nWindows - 2) {
				endSignal[0] = windownX[0];
				endSignal[1] = windownY[0];
				endSignal[2] = windownZ[0];
			}

			// Power Features
			windowData = getRawDataWindown(rawAccelDatas, i, windownLenght);
			powerFeatures.setAcc(windowData);
			powerFeatures.analysisRMS();

			// A features vector include var, mean, sd, distribution(max - min),
			// covariance(cov(x,y), cov(y,z), cov(z,x),
			// zero-crossing-rate(zrcX, zrcY, zrcZ), energy, entropy, activity, mobility,
			// complexity.
			fStatistic = new FeaturesStatistic(windownX, windownY, windownZ, windownLenght, timestamp,
					READING_PER_SECOND);
			FeaturesVector fV = new FeaturesVector();
			fV.setActivity(powerFeatures.getActivity());
			fV.setComplexity(powerFeatures.getComplexity());
			fV.setEnergy(powerFeatures.getEnergy());
			fV.setMobility(powerFeatures.getMobility());
			fV.setVehicle(vehicle);
			fV.setStatus(status);
			fV.setAverageResultantAcceleration(fStatistic.getWindownARA());
			fV.setVarianceX(fStatistic.getVarianceX());
			fV.setVarianceY(fStatistic.getVarianceY());
			fV.setVarianceZ(fStatistic.getVarianceZ());
			fV.setDistributionX(fStatistic.getDistributionX());
			fV.setDistributionY(fStatistic.getDistributionY());
			fV.setDistributionZ(fStatistic.getDistributionZ());
			fV.setMeanX(fStatistic.getMeanX());
			fV.setMeanY(fStatistic.getMeanY());
			fV.setMeanZ(fStatistic.getMeanZ());
			fV.setMeanXYZ(fStatistic.getMeanXYZ());
			fV.setStandardDeviationX(fStatistic.getStandardDeviationX());
			fV.setStandardDeviationY(fStatistic.getStandardDeviationY());
			fV.setStandardDeviationZ(fStatistic.getStandardDeviationZ());
			fV.setCovarianceXY(fStatistic.getCovarianceXY());
			fV.setCovarianceYZ(fStatistic.getCovarianceYZ());
			fV.setCovarianceZX(fStatistic.getCovarianceZX());
			fV.setZeroCrossingRateX(fStatistic.getZeroCrossRateX());
			fV.setZeroCrossingRateY(fStatistic.getZeroCrossRateY());
			fV.setZeroCrossingRateZ(fStatistic.getZeroCrossRateZ());
			fV.setFftEnergyX(fStatistic.getXFFTEnergy());
			fV.setFftEnergyY(fStatistic.getYFFTEnergy());
			fV.setFftEnergyZ(fStatistic.getZFFTEnergy());
			fV.setFftEntropyX(fStatistic.getXFFTEntropy());
			fV.setFftEntropyY(fStatistic.getYFFTEntropy());
			fV.setFftEntropyZ(fStatistic.getZFFTEntropy());
			// add new 8 feature Nhacld
			fV.setParX(fStatistic.getPARX());
			fV.setParY(fStatistic.getPARY());
			fV.setParZ(fStatistic.getPARZ());
			// sma
			fV.setsmaX(fStatistic.getSMAX());
			fV.setsmaY(fStatistic.getSMAY());
			fV.setsmaZ(fStatistic.getSMAZ());
			fV.setsmaX(fStatistic.getSMA());
			// dsvm
			fV.setdsvm(fStatistic.getDSVM());
			
			// Activity 5 feature: x,y,z,phi, theta
//			fV.setActivityX(fStatistic.getACTIVITY("X"));
//			fV.setActivityY(fStatistic.getACTIVITY("Y"));
//			fV.setActivityZ(fStatistic.getACTIVITY("Z"));
//			fV.setActivityPhi(fStatistic.getACTIVITY("P"));
//			fV.setActivityTheta(fStatistic.getACTIVITY("T"));
//			//
//			fV.setMobilityX(fStatistic.getMOBILITY("X"));
//			fV.setMobilityY(fStatistic.getMOBILITY("Y"));
//			fV.setMobilityZ(fStatistic.getMOBILITY("Z"));
//			fV.setMobilityPhi(fStatistic.getMOBILITY("P"));
//			fV.setMobilityTheta(fStatistic.getMOBILITY("T"));
//			//
//			fV.setComplexityX(fStatistic.getCOMPLEXITY("X"));
//			fV.setComplexityY(fStatistic.getCOMPLEXITY("Y"));
//			fV.setComplexityZ(fStatistic.getCOMPLEXITY("Z"));
//			fV.setComplexityPhi(fStatistic.getCOMPLEXITY("P"));
//			fV.setComplexityTheta(fStatistic.getCOMPLEXITY("T"));
			
			fV.setMeanPhi(fStatistic.getMeanPhi());
			fV.setMeanTheta(fStatistic.getMeanTheta());
			fV.setvariancePhi(fStatistic.getVariancePhi());
			fV.setvarianceTheta(fStatistic.getVarianceTheta());
			fV.setigPHI(fStatistic.getIgPhi());
			fV.setigTHETA(fStatistic.getIgTheta());
			
			//update new features
			fV.setavaLamdaOren(fStatistic.GetAvaLamdaOren());
			fV.setvarLamdaOren(fStatistic.GetVarLamdaOren());
			fV.setavaPhiOren(fStatistic.GetAvaPhiOren());
			fV.setvarPhiOren(fStatistic.GetVarPhiOren());
			
			//bo sung feature wavelet
			
			fV.setMavWaveletXLevelOneA(fStatistic.GetMavWaveletXLevelOneA());
			fV.setMavWaveletXLevelOneD(fStatistic.GetMavWaveletXLevelOneD());
			fV.setAvpWaveletXLevelOneA(fStatistic.GetAvpWaveletXLevelOneA());
			fV.setAvpWaveletXLevelOneD(fStatistic.GetAvpWaveletXLevelOneD());
			fV.setSkWaveletXLevelOneA(fStatistic.GetSkWaveletXLevelOneA());
			fV.setSkWaveletXLevelOneD(fStatistic.GetSkWaveletXLevelOneD());
			
			
			fV.setMavWaveletYLevelOneA(fStatistic.GetMavWaveletYLevelOneA());
			fV.setMavWaveletYLevelOneD(fStatistic.GetMavWaveletYLevelOneD());
			fV.setAvpWaveletYLevelOneA(fStatistic.GetAvpWaveletYLevelOneA());
			fV.setAvpWaveletYLevelOneD(fStatistic.GetAvpWaveletYLevelOneD());
			fV.setSkWaveletYLevelOneA(fStatistic.GetSkWaveletYLevelOneA());
			fV.setSkWaveletYLevelOneD(fStatistic.GetSkWaveletYLevelOneD());
			
			fV.setMavWaveletZLevelOneA(fStatistic.GetMavWaveletZLevelOneA());
			fV.setMavWaveletZLevelOneD(fStatistic.GetMavWaveletZLevelOneD());
			fV.setAvpWaveletZLevelOneA(fStatistic.GetAvpWaveletZLevelOneA());
			fV.setAvpWaveletZLevelOneD(fStatistic.GetAvpWaveletZLevelOneD());
			fV.setSkWaveletZLevelOneA(fStatistic.GetSkWaveletZLevelOneA());
			fV.setSkWaveletZLevelOneD(fStatistic.GetSkWaveletZLevelOneD());
			
			//AvpWaveletXLevelOneA

			HARDataset.add(fV);
		}
	}

	private void computeFeatures_A_Nhac(ArrayList<SimpleAccelData> accelDatas, String vehicle, String status) {
		int length = accelDatas.size();
		int readingOfAWindown = 0;
		int FFTlength[] = { 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384 };

		readingOfAWindown = FFTlength[windownLenght];

		double[] endSignal = new double[] { 0d, 0d, 0d };
		double[] windownX;
		double[] windownY;
		double[] windownZ;
		double[] timestamp;
		PowerFeatures powerFeatures = new PowerFeatures();
		ArrayList<SimpleAccelData> windowData;

		FeaturesStatistic fStatistic;

		int i = 0, start = 0, stop = 0;
		int overlapvalue = (int) ((readingOfAWindown) * (1 - OVERLAPPING));
		// stop=readingOfAWindown;
		int lengs = stop - start;

		while (stop < length) {
			if (i != 0)	start += overlapvalue;
			stop = start + readingOfAWindown;
			lengs = stop - start;
			if (stop > length) 
				{
				lengs = length - start;
				}

			System.out.println("i = " + i + "  start = " + start + "  stop = " + stop + "  length " + length + 
					"  lengs = OVERLAPPING = " + lengs + " readingOfAWindown " + readingOfAWindown + "  windownLenght = " + windownLenght);
			
			windownX = getWindow(WINDOWN_X, accelDatas, start, lengs);
			windownY = getWindow(WINDOWN_Y, accelDatas, start, lengs);
			windownZ = getWindow(WINDOWN_Z, accelDatas, start, lengs);
			timestamp = getWindow(TIMESTAMP, accelDatas, start, lengs);

			// High Filter
			windownX = filter(windownX);
			windownY = filter(windownY);
			windownZ = filter(windownZ);

			// Low filter
			windownX = lowFilter(windownX, i, endSignal[0]);
			windownY = lowFilter(windownY, i, endSignal[1]);
			windownZ = lowFilter(windownZ, i, endSignal[2]);

			if ((start + overlapvalue + readingOfAWindown) < length) {
				endSignal[0] = windownX[0];
				endSignal[1] = windownY[0];
				endSignal[2] = windownZ[0];
			}
			/*
			 * if (i < nWindows - 2) { endSignal[0] = windownX[0]; endSignal[1] =
			 * windownY[0]; endSignal[2] = windownZ[0]; }
			 */

			// Power Features
			// windowData = getRawDataWindown(rawAccelDatas, i, windownLenght);
			windowData = getRawDataWindown(rawAccelDatas, i, lengs);
			powerFeatures.setAcc(windowData);
			powerFeatures.analysisRMS();

			// A features vector include var, mean, sd, distribution(max - min),
			// covariance(cov(x,y), cov(y,z), cov(z,x),
			// zero-crossing-rate(zrcX, zrcY, zrcZ), energy, entropy, activity, mobility,
			// complexity.
			fStatistic = new FeaturesStatistic(windownX, windownY, windownZ, windownLenght, timestamp,
					READING_PER_SECOND);
			FeaturesVector fV = new FeaturesVector();
			fV.setActivity(powerFeatures.getActivity());
			fV.setComplexity(powerFeatures.getComplexity());
			fV.setEnergy(powerFeatures.getEnergy());
			fV.setMobility(powerFeatures.getMobility());
			fV.setVehicle(vehicle);
			fV.setStatus(status);
			fV.setAverageResultantAcceleration(fStatistic.getWindownARA());
			fV.setVarianceX(fStatistic.getVarianceX());
			fV.setVarianceY(fStatistic.getVarianceY());
			fV.setVarianceZ(fStatistic.getVarianceZ());
			fV.setDistributionX(fStatistic.getDistributionX());
			fV.setDistributionY(fStatistic.getDistributionY());
			fV.setDistributionZ(fStatistic.getDistributionZ());
			fV.setMeanX(fStatistic.getMeanX());
			fV.setMeanY(fStatistic.getMeanY());
			fV.setMeanZ(fStatistic.getMeanZ());
			fV.setMeanXYZ(fStatistic.getMeanXYZ());
			fV.setStandardDeviationX(fStatistic.getStandardDeviationX());
			fV.setStandardDeviationY(fStatistic.getStandardDeviationY());
			fV.setStandardDeviationZ(fStatistic.getStandardDeviationZ());
			fV.setCovarianceXY(fStatistic.getCovarianceXY());
			fV.setCovarianceYZ(fStatistic.getCovarianceYZ());
			fV.setCovarianceZX(fStatistic.getCovarianceZX());
			fV.setZeroCrossingRateX(fStatistic.getZeroCrossRateX());
			fV.setZeroCrossingRateY(fStatistic.getZeroCrossRateY());
			fV.setZeroCrossingRateZ(fStatistic.getZeroCrossRateZ());
			fV.setFftEnergyX(fStatistic.getXFFTEnergy());
			fV.setFftEnergyY(fStatistic.getYFFTEnergy());
			fV.setFftEnergyZ(fStatistic.getZFFTEnergy());
			fV.setFftEntropyX(fStatistic.getXFFTEntropy());
			fV.setFftEntropyY(fStatistic.getYFFTEntropy());
			fV.setFftEntropyZ(fStatistic.getZFFTEntropy());
			// add new 8 feature Nhacld
			fV.setParX(fStatistic.getPARX());
			fV.setParY(fStatistic.getPARY());
			fV.setParZ(fStatistic.getPARZ());
			// sma
			fV.setsmaX(fStatistic.getSMAX());
			fV.setsmaY(fStatistic.getSMAY());
			fV.setsmaZ(fStatistic.getSMAZ());
			fV.setsma(fStatistic.getSMA());
			// dsvm
			fV.setdsvm(fStatistic.getDSVM());
			// Activity 5 feature: x,y,z,phi, theta
//			fV.setActivityX(fStatistic.getACTIVITY("X"));
//			fV.setActivityY(fStatistic.getACTIVITY("Y"));
//			fV.setActivityZ(fStatistic.getACTIVITY("Z"));
//			fV.setActivityPhi(fStatistic.getACTIVITY("P"));
//			fV.setActivityTheta(fStatistic.getACTIVITY("T"));
//			//
//			fV.setMobilityX(fStatistic.getMOBILITY("X"));
//			fV.setMobilityY(fStatistic.getMOBILITY("Y"));
//			fV.setMobilityZ(fStatistic.getMOBILITY("Z"));
//			fV.setMobilityPhi(fStatistic.getMOBILITY("P"));
//			fV.setMobilityTheta(fStatistic.getMOBILITY("T"));
//			//
//			fV.setComplexityX(fStatistic.getCOMPLEXITY("X"));
//			fV.setComplexityY(fStatistic.getCOMPLEXITY("Y"));
//			fV.setComplexityZ(fStatistic.getCOMPLEXITY("Z"));
//			fV.setComplexityPhi(fStatistic.getCOMPLEXITY("P"));
//			fV.setComplexityTheta(fStatistic.getCOMPLEXITY("T"));
			
			fV.setMeanPhi(fStatistic.getMeanPhi());
			fV.setMeanTheta(fStatistic.getMeanTheta());
			fV.setvariancePhi(fStatistic.getVariancePhi());
			fV.setvarianceTheta(fStatistic.getVarianceTheta());
			fV.setigPHI(fStatistic.getIgPhi());
			fV.setigTHETA(fStatistic.getIgTheta());
			
			//update new features
			fV.setavaLamdaOren(fStatistic.GetAvaLamdaOren());
			fV.setvarLamdaOren(fStatistic.GetVarLamdaOren());
			fV.setavaPhiOren(fStatistic.GetAvaPhiOren());
			fV.setvarPhiOren(fStatistic.GetVarPhiOren());
			
			//bo sung feature wavelet
			
			fV.setMavWaveletXLevelOneA(fStatistic.GetMavWaveletXLevelOneA());
			fV.setMavWaveletXLevelOneD(fStatistic.GetMavWaveletXLevelOneD());
			fV.setAvpWaveletXLevelOneA(fStatistic.GetAvpWaveletXLevelOneA());
			fV.setAvpWaveletXLevelOneD(fStatistic.GetAvpWaveletXLevelOneD());
			fV.setSkWaveletXLevelOneA(fStatistic.GetSkWaveletXLevelOneA());
			fV.setSkWaveletXLevelOneD(fStatistic.GetSkWaveletXLevelOneD());
			
			fV.setMavWaveletYLevelOneA(fStatistic.GetMavWaveletYLevelOneA());
			fV.setMavWaveletYLevelOneD(fStatistic.GetMavWaveletYLevelOneD());
			fV.setAvpWaveletYLevelOneA(fStatistic.GetAvpWaveletYLevelOneA());
			fV.setAvpWaveletYLevelOneD(fStatistic.GetAvpWaveletYLevelOneD());
			fV.setSkWaveletYLevelOneA(fStatistic.GetSkWaveletYLevelOneA());
			fV.setSkWaveletYLevelOneD(fStatistic.GetSkWaveletYLevelOneD());
			
			fV.setMavWaveletZLevelOneA(fStatistic.GetMavWaveletZLevelOneA());
			fV.setMavWaveletZLevelOneD(fStatistic.GetMavWaveletZLevelOneD());
			fV.setAvpWaveletZLevelOneA(fStatistic.GetAvpWaveletZLevelOneA());
			fV.setAvpWaveletZLevelOneD(fStatistic.GetAvpWaveletZLevelOneD());
			fV.setSkWaveletZLevelOneA(fStatistic.GetSkWaveletZLevelOneA());
			fV.setSkWaveletZLevelOneD(fStatistic.GetSkWaveletZLevelOneD());
			
			HARDataset.add(fV);
			// tang gia tri vong lap while (tang cua so)
			i++;
		}
	}

	private double[] lowFilter(double[] windownX, int index, double endSignal) {
		int length = windownX.length;
		double[] windown = new double[length];
		if (index == 0) {
			windown[0] = windownX[0];
		} else {
			windown[0] = endSignal;
		}

		for (int i = 1; i < length; i++) {
			windown[i] = lowFilters(windownX[i - 1], windownX[i]);
		}
		return windown;
	}

	private double lowFilters(double signalOutput, double signalInput) {// loc thong thap, 2.3
		return signalOutput + ALPHA * (signalInput - signalOutput);
	}

	private double[] getWindow(int type, ArrayList<SimpleAccelData> accelDatas, int fromIndex, int length) {
		double[] windown = new double[length];
		switch (type) {
		case WINDOWN_X:
			for (int i = 0; i < length - 1; i++) {
				windown[i] = accelDatas.get(fromIndex + i).getX();
			}
			break;
		case WINDOWN_Y:
			for (int i = 0; i < length - 1; i++) {
				windown[i] = accelDatas.get(fromIndex + i).getY();
			}
			break;
		case WINDOWN_Z:
			for (int i = 0; i < length - 1; i++) {
				windown[i] = accelDatas.get(fromIndex + i).getZ();
			}
			break;
		case TIMESTAMP:
			for (int i = 0; i < length - 1; i++) {
				windown[i] = Double.valueOf(accelDatas.get(fromIndex + i).getTimestamp());
			}
			break;
		default:
			break;
		}
		return windown;
	}

	private ArrayList<SimpleAccelData> preprocessData(ArrayList<SimpleAccelData> accelDatas, String status) {
		int redundantLength = 0;

		if ((accelDatas.size() > 1000) & (status.equals("Mov") || status.equals("M"))) {
			redundantLength = TIME_HEADER_AND_FOOTER_FOR_MOVING * READING_PER_SECOND;
		} else if ((accelDatas.size() > 2000)) {
			redundantLength = TIME_HEADER_AND_FOOTER_FOR_STOP * READING_PER_SECOND;
		}

		if (accelDatas.size() < 10) {
			System.out.println("The data file is small: " + redundantLength);
		} else // Remove header and footer of data
		if (accelDatas.size() > 2 * redundantLength) {

			for (int i = 0; i < redundantLength; i++) {
				accelDatas.remove(0);// remove header

				accelDatas.remove(accelDatas.size() - 1);// remove footer
			}
		} else {
			for (int i = 0; i < accelDatas.size(); i++) {
				accelDatas.remove(0);// remove header
				accelDatas.remove(accelDatas.size() - 1);// remove footer
			}
		}

		return accelDatas;
	}

	private ArrayList<SimpleAccelData> getAccelDatasFromCSV(File file, int kd) { // kd==1 is get data of student
		CSVReader csvReader = new CSVReader(file.getPath(), kd);
		return csvReader.getAccelDatas();
	}

	private double[] filter(double[] arms) {
		// TODO Auto-generated method stub

		double a1, a2, b0, b1, b2 = 0;
		b1 = 1;
		b0 = 0;
		b2 = 1;
		a1 = -1.56f;
		a2 = 0.6f;
		int len = arms.length;
		double[] rs = new double[len];

		if (len > 2) {
			rs[0] = arms[0];

			rs[1] = arms[1];
		}

		for (int i = 2; i < len; i++) {
			double ar = 0;
			ar = b0 * arms[i] + b1 * arms[i - 1] + b2 * arms[i - 2] - a1 * rs[i - 1] - a2 * rs[i - 2];
			rs[i] = ar;
		}

		return rs;
	}

	public int getwdlenght() {

		return this.windownLenght;
	}
}
