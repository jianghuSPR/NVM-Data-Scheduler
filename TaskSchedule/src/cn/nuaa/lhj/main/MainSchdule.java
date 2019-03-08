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
	public static final int readDataTime = 4; // �����ݴ�Disk����NVM����Ҫ��ʱ��,Ҳ���ǵ�λ�ļ���Ǩ��ʱ��
	// public static final int NVMTime = 1; // ��ʾ��NVM��ִ����Ҫ��ʱ��
	// public static final int DiskTime = 4; // ��ʾ��Disk��ִ����Ҫ��ʱ��
	public static final int m = 64; // CPU��solts�ĸ���,Ĭ��Ϊ64
	public static final int n = 50; // NVM��solts�ĸ���,Ĭ��Ϊ50
	public static final double p = 0.5; // ��ʾ������NVM�е�����
	public static final int H1 = 2; // ��ʾhotness��ʼֵ hotness = H1+H2*Lable
	public static final int H2 = 2; // ��ʾ������ʽ������ȵ�ʱ�ı�hotnessֵ��Ƶ��
	public static final int CH = -500; // hotness���ֵ��Ĭ��Ϊ-500
	public static final int w0 = 20;
	public static final int a = -2;
	public static final int w1 = 15;
	public static final int w2 = -5;

	public static boolean existMigration = false; // �Ƿ��������Ǩ�������Эͬʱ�������ͬ��¼ʱ��
	public static boolean filePipe = false; // ��дͨ��ռ�������Ĭ��Ϊ���� false
	public static int timeStamp = 0; // ����ʱ���������ʱ��Ĭ��Ϊ0��
	public static int sumJobs = 0; // ��¼�ܹ�������job����
	public static int timeSlot = 0; // ����ȫ��Ψһ��ʱ��Ƭ
	public static int acceptJobs = 0; // ��¼���ܵ�job����
	public static int rejectJobs = 0; // ��¼�ܾ���job��������ʡ��
	public static int NVMWriteTimes = 0; // ��¼nvmд��������
	public static int dataInNVMTimes = 0; // ���ڼ���nvm��������

	public static List<Job> finishJobs = new ArrayList<Job>(); // ������ɵ�����jobs
	public static List<Task> finishTasks = new ArrayList<Task>();// ��ʾ��ɵ��������
	public static List<Job> waitingJobs = new ArrayList<Job>(); // ����ȫ�ֵ������ڵȴ���jobs
	public static List<Task> runningTasks = new ArrayList<Task>(); // ����ȫ�ֵ������������е�tasks
	public static List<Job> rejectJob = new ArrayList<Job>(); // ����ȫ�ֵ����оܾ���job
	public static List<Job> acceptJob = new ArrayList<Job>(); // ����ȫ�ֵĽ��յ����е�job
	
	public static void main(String[] args) {
		/*
		 * 1.��ʼ��Disk���ݣ���Data���� 2.��ʼ������Job�� 3.������ʼ״̬�µ�����Ǩ�ƣ���Disk-->NVM��n��slots����
		 */
		List<Job> jobList = new DataInit().dataInput();
		new DataAbout().firstMigration();

		while (true) {
			// 1 ��û���������
			List<Job> arriveJobList = new TaskAbout().arriveJobs(jobList);

			// 2 û��ֱ������������У����ݲ��Խ��ջ��߾ܾ������ܣ������ȴ�����
			new TaskAbout().acceptJob(arriveJobList);
			new TaskSheduleCheck().taskSheduleCheck();// cpu���еĻ�������������ȣ�����״̬
			// 3 ���Ƿ����ļ���ҪǨ�ƣ�����ʼǨ��
			new DataAbout().canDoMigration();

			MainSchdule.timeSlot++;

			System.out.println("ʱ��Ƭ�� " + MainSchdule.timeSlot);

			new TaskFinishCheck().taskFinishCheck();// ��û��������ɣ��еĻ����Ƴ�,����״̬
			new DataAbout().changeHotness();// ʱ�����Ʊ������hotness��ֵ,����״̬

			System.out.println(MainSchdule.timeSlot + "ʱ�̣���������  " + runningTasks.size() + " ������");
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
		// ���е���������ȫ����ɣ��������ս��

		System.out.println("�ܾ���job�У�    ");
		for (Job job : rejectJob) {
			System.out.println(job.toString());
		}

		System.out.println("����ʵ�����������Ϊ�� " + new DataStatistics().getAcceptTate());
		System.out.print("����ʵ��NVMд��Ĵ���Ϊ�� " + new DataStatistics().getNVMWriteTimes());
		System.out.println("    �������ݣ� " + (Disk.disk.size() + MainSchdule.n));
		System.out.println("����ʵ��NVM��������Ϊ�� " + new DataStatistics().NVMHitRate());
		System.out.println("����ʵ�����UtilityΪ�� " + new DataStatistics().getUtility());

	}

}
