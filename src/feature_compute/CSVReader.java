/**
 * PreprocessAndComputeFeatures Project
 * Created by
 *
 */
package feature_compute;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVReader {
	private ArrayList<SimpleAccelData> accelDatas;

	private BufferedReader br = null;

	private String line = "";

	private String cvsSplitBy = ",";

	public CSVReader(String csvFile, int kd) {
		accelDatas = new ArrayList<>();

		try {

			br = new BufferedReader(new FileReader(csvFile));

			
				line = br.readLine();// mode
				line = br.readLine();// timestamp
				line = br.readLine();// time
				line = br.readLine();// header
				line = br.readLine();// test1
				line = br.readLine();//activity
				line = br.readLine();//subject
				line = br.readLine();//first name
				line = br.readLine();//last name
				line = br.readLine();//age
				line = br.readLine();//height
				line = br.readLine();//weight
				line = br.readLine();//gender
				line = br.readLine();//footer
				line = br.readLine();//none
				line = br.readLine();//none
				line = br.readLine();//@data
				

			while ((line = br.readLine()) != null) {

				// use space as separator

				
					String[] values = line.split(cvsSplitBy);

//					 accelDatas.add(new SimpleAccelData(values[0],
//					 Double.parseDouble(values[1]),
//					 Double.parseDouble(values[2]),
//					 Double.parseDouble(values[3])));
					accelDatas.add(new SimpleAccelData(values[0], values[1],
							values[2], values[3]));
				
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ArrayList<SimpleAccelData> getAccelDatas() {
		return accelDatas;
	}

}
