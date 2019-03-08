package cn.nuaa.qjj.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.nuaa.qjj.domain.Job;
import cn.nuaa.qjj.domain.Task;

public class TaskAbout {
	// 读取指定时间片到来的所有Jobs
	public List<Job> arriveJobs(List<Job> jobList) {
		List<Job> tempJobs = new ArrayList<Job>();
		for (Job job : jobList) {
			if (job.getArriveTime() == MainSchdule.timeSlot) {
				System.out.println("有任务到达：" + job.toString());
				tempJobs.add(job);
				for (Task task : job.getTasks()) {
					new DataAbout().changeHotnessBecauseUse(task); // 更改到达任务需要的数据的hotness，很重要！！
				}
			}
		}
		return tempJobs;
	}

	// 判断是否接收job
	public boolean acceptJob(List<Job> jobList) {
		if (null == jobList) {
			return false;
		} else {
			for (Job job : jobList) {
				if (receiveJob(job)) {
					for (Task task : job.getTasks()) {
						task.setStatus(0);// 首先更改task的状态为等待
					}
					MainSchdule.waitingJobs.add(job); // 如果接收，直接加入等待队列中
					MainSchdule.acceptJobs++;
					System.out.println("接受job：" + job.toString());
					// 更改需要的数据类型，很重要
					new DataAbout().changeTypeWhenEnterWaiting(job);
				} else {
					MainSchdule.rejectJob.add(job);
					System.out.println("拒绝了job：" + job.toString());
				}
			}
			return true;
		}
	}

	// 通过传入Job，返回接受还是拒绝的方法，即接收拒绝策略
	public boolean receiveJob(Job job) {
		boolean flag = false;
		int latSchTime = getLatestTime(job);
		if (acceptTask(latSchTime)) {
			flag = true;
		}
		return flag;
	}

	// 接受或拒绝任务策略方法
	public boolean acceptTask(int latSchTime) {
		if (latSchTime < MainSchdule.timeSlot) {
			return false;
		} else {
			boolean flag = false;
			int numNVM = 0;
			int numDisk = 0;
			int load = 0;
			for (Task task : MainSchdule.runningTasks) {
				int dataID = task.getRequiredData();
				boolean inNVM = new DataAbout().dataInNVM(dataID);
				if (inNVM) {
					numNVM++;
				} else {
					numDisk++;
				}
			}
			for (Job job : MainSchdule.waitingJobs) {
				int time = getLatestTime(job); // 获得等待队列任务的最晚调度时间
				// 只有当等待队列中的任务的最晚调度时间小于判断任务的最晚调度时间时，才算入结果
				if (time < latSchTime) {
					List<Task> tasks = job.getTasks();
					for (Task task : tasks) {
						int dataID = task.getRequiredData();
						boolean inNVM = new DataAbout().dataInNVM(dataID);
						if (inNVM) {
							numNVM++;
							load += task.getExeTime();
						} else {
							load += task.getExeTime() + MainSchdule.readDataTime;
							numDisk++;
						}
					}
				}

			}
			/*
			 * // p表示数据在NVM之中的期望 double x = numNVM * MainSchdule.p *
			 * (MainSchdule.NVMTime + numDisk * (1 - MainSchdule.p) *
			 * MainSchdule.DiskTime)/MainSchdule.m; if (Math.ceil(x) <
			 * latSchTime) { flag = true; }
			 */
			if (Math.ceil(load / MainSchdule.m) < latSchTime) {
				flag = true;
			}
			return flag;
		}
	}

	// 传入Job，获得它的最晚调度时间
	public int getLatestTime(Job job) {
		if (job.isInteractive()) {
			// 如果是交互式任务
			Task tempTask = job.getTasks().get(0);
			int dataID = tempTask.getRequiredData();
			boolean inNVM = new DataAbout().dataInNVM(dataID);
			int sumExeTime;
			if (inNVM) {
				sumExeTime = job.getExeTime();
			} else {
				sumExeTime = job.getExeTime() + MainSchdule.readDataTime;
			}
			int latSchTime = job.getDeadline() - sumExeTime; // 最晚调度时间=截止时间-总执行时间
			return latSchTime;
		} else {
			// 如果是批处理任务
			int notInNVM = 0;
			for (Task task : job.getTasks()) {
				int id = task.getRequiredData();
				int time = task.getExeTime();
				boolean inNVM = new DataAbout().dataInNVM(id);
				if (!inNVM) {
					notInNVM++;
				}
			}
			int sumExeTime = notInNVM * MainSchdule.readDataTime + job.getTasks().size() * job.getExeTime();// 任务总的执行时间
			int latSchTime = job.getDeadline() - sumExeTime / 20;
			return latSchTime;
		}
	}

	// 传入Task，返回它的执行时间
	public int getSumExeTime(Task ts) {
		int dataID = ts.getRequiredData();
		int sumExe = ts.getExeTime();
		boolean inNVM = new DataAbout().dataInNVM(dataID);
		if (!inNVM) {
			sumExe += MainSchdule.readDataTime;
		}
		return sumExe;
	}

	// 根据task需要的数据在不在nvm进行排序
	public List<Task> sortTaskByDataInNVM(List<Task> listTasks) {
		List<Task> list = listTasks;
		List<Task> temp = new ArrayList<Task>();
		Iterator<Task> it = list.iterator();
		while (it.hasNext()) {
			Task task = it.next();
			int dataId = task.getRequiredData();
			if (new DataAbout().dataInNVM(dataId)) {
				temp.add(task);
				it.remove();
			}
		}
		for (Task tk : list) {
			temp.add(tk);
		}
		return temp;
	}

	public int getNumTask(Job job) {
		int sum = 1;
		for (Job jo : MainSchdule.acceptJob) {
			if (job.getJobID() == jo.getJobID()) {
				sum = jo.getTasks().size();
				break;
			}
		}
		return sum;
	}

}
