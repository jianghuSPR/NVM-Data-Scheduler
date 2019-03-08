package cn.nuaa.qjj.main;

import java.util.ArrayList;
import java.util.List;

import cn.nuaa.qjj.check.TaskFinishCheck;
import cn.nuaa.qjj.check.TaskSheduleCheck;
import cn.nuaa.qjj.domain.Disk;
import cn.nuaa.qjj.domain.Job;
import cn.nuaa.qjj.domain.Task;
import cn.nuaa.qjj.init.DataInit;

public class MainSchdule {
	public static final int readDataTime = 4; // 把数据从Disk读到NVM所需要的时间,也就是单位文件的迁移时间
	// public static final int NVMTime = 1; // 表示在NVM中执行需要的时间
	// public static final int DiskTime = 4; // 表示在Disk中执行需要的时间
	public static final int m = 64; // CPU中solts的个数,默认为64
	public static final int n = 50; // NVM中solts的个数,默认为50
	public static final double p = 0.5; // 表示数据在NVM中的期望
	public static final int H1 = 2; // 表示hotness初始值 hotness = H1+H2*Lable
	public static final int H2 = 2; // 表示被交互式任务调度的时改变hotness值的频率
	public static final int CH = -500; // hotness最低值，默认为-500
	public static final int w0 = 20;
	public static final int a = -2;
	public static final int w1 = 15;
	public static final int w2 = -5;

	public static boolean existMigration = false; // 是否存在数据迁移情况，协同时间戳，共同记录时间
	public static boolean filePipe = false; // 读写通道占用情况，默认为空闲 false
	public static int timeStamp = 0; // 定义时间戳，启动时候默认为0。
	public static int sumJobs = 0; // 记录总共过来的job总数
	public static int timeSlot = 0; // 定义全局唯一的时间片
	public static int acceptJobs = 0; // 记录接受的job总数
	public static int rejectJobs = 0; // 记录拒绝的job总数，可省略
	public static int NVMWriteTimes = 0; // 记录nvm写入放入次数
	public static int dataInNVMTimes = 0; // 用于计算nvm的命中率

	public static List<Job> finishJobs = new ArrayList<Job>(); // 定义完成的所有jobs
	public static List<Task> finishTasks = new ArrayList<Task>();// 表示完成的任务队列
	public static List<Job> waitingJobs = new ArrayList<Job>(); // 定义全局的所有在等待的jobs
	public static List<Task> runningTasks = new ArrayList<Task>(); // 定义全局的所有正在运行的tasks
	public static List<Job> rejectJob = new ArrayList<Job>(); // 定义全局的所有拒绝的job
	public static List<Job> acceptJob = new ArrayList<Job>(); // 定义全局的接收的所有的job
	
	public static void main(String[] args) {
		/*
		 * 1.初始化Disk数据（即Data）； 2.初始化所有Job； 3.并做初始状态下的数据迁移，即Disk-->NVM（n个slots）。
		 */
		List<Job> jobList = new DataInit().dataInput();
		new DataAbout().firstMigration();

		while (true) {
			// 1 有没有任务带来
			List<Job> arriveJobList = new TaskAbout().arriveJobs(jobList);

			// 2 没有直接跳过，如果有，根据策略接收或者拒绝，接受，则加入等待队列
			new TaskAbout().acceptJob(arriveJobList);
			new TaskSheduleCheck().taskSheduleCheck();// cpu空闲的话，进行任务调度，更改状态
			// 3 看是否有文件需要迁移，有则开始迁移
			new DataAbout().canDoMigration();

			MainSchdule.timeSlot++;

			System.out.println("时间片： " + MainSchdule.timeSlot);

			new TaskFinishCheck().taskFinishCheck();// 有没有任务完成，有的话，移除,更改状态
			new DataAbout().changeHotness();// 时间推移必须更改hotness的值,更改状态

			System.out.println(MainSchdule.timeSlot + "时刻，正在运行  " + runningTasks.size() + " 个任务");
			if (MainSchdule.timeSlot > 3200) {
				break;
			}

		}

		/*
		 * for (Job job : finishJobs) { if(job.getJobID() == 164){
		 * System.out.println(job.toString()); }
		 * 
		 * }
		 */
		// 所有到来的任务全部完成，计算最终结果

		System.out.println("拒绝的job有：    ");
		for (Job job : rejectJob) {
			System.out.println(job.toString());
		}

		System.out.println("本次实验任务接受率为： " + new DataStatistics().getAcceptTate());
		System.out.print("本次实验NVM写入的次数为： " + new DataStatistics().getNVMWriteTimes());
		System.out.println("    共有数据： " + (Disk.disk.size() + MainSchdule.n));
		System.out.println("本次实验NVM的命中率为： " + new DataStatistics().NVMHitRate());
		System.out.println("本次实验的总Utility为： " + new DataStatistics().getUtility());

	}

}
