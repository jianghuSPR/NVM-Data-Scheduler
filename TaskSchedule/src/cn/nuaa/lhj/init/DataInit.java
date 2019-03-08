package cn.nuaa.qjj.init;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.nuaa.qjj.domain.Job;
import cn.nuaa.qjj.util.ReadCSVFile;

public class DataInit {
	// 初始化磁盘数据,并做初始状态下的数据迁移，并读出返回所有Job
	public List<Job> dataInput() {
		List<Job> jobList = new ArrayList<Job>();
		// 读文件，把所有的数据初始化；
		try {
			jobList = ReadCSVFile.readCsv();
			/*
			 * for (Job job : jobList) { System.out.println(job.toString());
			 * System.out.println(); }
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jobList;
	}
}
