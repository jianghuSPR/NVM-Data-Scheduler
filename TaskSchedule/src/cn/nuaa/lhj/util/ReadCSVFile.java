package cn.nuaa.qjj.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

import cn.nuaa.qjj.domain.Data;
import cn.nuaa.qjj.domain.Disk;
import cn.nuaa.qjj.domain.Job;
import cn.nuaa.qjj.domain.Task;
import cn.nuaa.qjj.main.DataAbout;
import cn.nuaa.qjj.main.MainSchdule;

//CSV��ȡ������װ����
public class ReadCSVFile {
	public static int num = 0;// ��������task��id

	/**
	 * CSV����
	 *
	 */
	public static List<Job> readCsv() throws IOException {
		String srcCSV = "C:/Users/planb/Desktop/data_generator(1)/data_generator/output/jobs2.csv";
		// String targetFile = "F:/test.csv";
		CsvReader reader = new CsvReader(srcCSV, ',', Charset.forName("UTF-8"));
		String[] header = {};
		List<Job> jobList = new ArrayList<Job>();
		while (reader.readRecord()) {
			// ��ͷ��������
			if (reader.getCurrentRecord() == 0) {
				header = reader.getValues();
			}
			String s = reader.getRawRecord();
			// System.out.println(s);
			if (s != null) {
				String[] temp = s.split(",");
				Job job = new Job();
				job.setJobID(Integer.parseInt(temp[1]));
				job.setArriveTime(Integer.parseInt(temp[0]));
				int count = Integer.parseInt(temp[2]);
				// String[] dataId = temp[7].replaceAll("\"",
				// "").split("\\.");//
				// ȥ���ַ������˵�˫����
				List<Task> taskList = new ArrayList<Task>();
				if (count == 1) {
					Task tk = new Task();
					Data dt = new Data();
					tk.setTaskID(ReadCSVFile.num);
					tk.setArriveTime(Integer.parseInt(temp[0]));
					tk.setDeadline(
							2 * Integer.parseInt(temp[3]) + MainSchdule.readDataTime + Integer.parseInt(temp[0]));
					tk.setRequiredData(Integer.parseInt(temp[4]));
					tk.setType(true);
					tk.setPu(MainSchdule.w0);
					tk.setStatus(2);
					tk.setExeTime(Integer.parseInt(temp[3]));
					dt.setInteractive(true);
					dt.setDataID(Integer.parseInt(temp[4]));
					dt.setHotness(0);
					dt.setStatus(false);
					dt.setType(5);

					if (null != Disk.disk) {
						// ���ȼ�������Ƿ��Ѿ�����
						boolean exist = new DataAbout().exitData(Integer.parseInt(temp[4]));
						if (!exist) {
							Disk.disk.add(dt);// ��ʼ��Disk

						}
					}
					taskList.add(tk);
					ReadCSVFile.num++; // ����TaskId�����������ظ���

					job.setInteractive(true);
					job.setDeadline(
							2 * Integer.parseInt(temp[3]) + MainSchdule.readDataTime + Integer.parseInt(temp[0]));
					job.setUtility(MainSchdule.w0);
					job.setExeTime(Integer.parseInt(temp[3]));
				} else {
					String[] dataId = temp[5].split("\\.");
					for (int j = 0; j < count; j++) {
						Task tk = new Task();
						Data dt = new Data();
						tk.setTaskID(ReadCSVFile.num);
						tk.setArriveTime(Integer.parseInt(temp[0]));
						tk.setDeadline(Integer.parseInt(temp[4]) + Integer.parseInt(temp[0]));
						tk.setRequiredData(Integer.parseInt(dataId[j]));
						// ��ʼ��Data���ݣ�ȫ�����Disk
						dt.setDataID(Integer.parseInt(dataId[j]));
						dt.setHotness(0);
						dt.setStatus(false);
						dt.setType(5);
						dt.setInteractive(false);
						tk.setExeTime(Integer.parseInt(temp[3]));
						tk.setType(false);
						tk.setStatus(2);
						tk.setPu(MainSchdule.w1);
						taskList.add(tk);
						if (null != Disk.disk) {
							// ���ȼ�������Ƿ��Ѿ�����
							boolean exist = new DataAbout().exitData(Integer.parseInt(dataId[j]));
							if (!exist) {
								Disk.disk.add(dt);// ��ʼ��Disk
							}
						}
						ReadCSVFile.num++; // ����TaskId�����������ظ���
						job.setInteractive(false);
						job.setDeadline(Integer.parseInt(temp[4]) + Integer.parseInt(temp[1]));
						job.setUtility(MainSchdule.w1);
						job.setExeTime(Integer.parseInt(temp[3]) * count);
					}
				}

				job.setTasks(taskList);

				jobList.add(job);// ��ʼ������job
			}

		}
		reader.close();
		return jobList;
	}

	public static void main(String[] args) {
		try {
			int size = 0;
			List<Job> jobList = readCsv();
			for (Job job : jobList) {
				for (Task ts : job.getTasks()) {
					size++;
				}
			}
			System.out.println("����task  "+size+"��");
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * System.out.println("===================================="); for (Data
		 * dt : Disk.disk) { System.out.println(dt.toString()); }
		 */

		System.out.println("�������ݣ�" + Disk.disk.size());
	}
}