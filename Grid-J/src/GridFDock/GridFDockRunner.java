/*
 * MolGridCal
 * Copyright MolGridCal Team
 * http://molgridcal.codeplex.com
 * Some codes use the JPPF API.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package GridFDock;

import java.io.*;
import java.util.List;
import java.text.*;
import java.util.*;
import org.apache.commons.net.ftp.FTPFile;
import org.jppf.client.*;
import org.jppf.node.protocol.DataProvider;
import org.jppf.node.protocol.MemoryMapDataProvider;
import org.jppf.node.protocol.Task;
import org.jppf.utils.Operator;

public class GridFDockRunner {
	private static JPPFClient jppfClient = null;
	BufferedReader rd = null;
	String fltmp;
	FTPFile[] fileList = null;
	File fl;
	String tempStr = null;
	String userdir;

	String ip;
	int port;
	// The max tasks according to the memory.
	int maxNodes = 5000;
	String user;
	String passwd;
	String downdir;
	String uploaddir;
	String command;
	String commandDir;
	String program;
	String sign;
	String[] Arraytmp;
	String[] temArray = new String[10];
	int countEnd, countStart, tmpCount;
	public static Ordinary cn = new Ordinary();
	public static DataDistribute dt = new DataDistribute();
	DecimalFormat df = new DecimalFormat("0.%");

	String inputParameter = "Vinaparameter.mgc";

	public static void main(String[] args) {
		SimpleDateFormat sdf = null;
		String executeTime;
		try {
			System.out.println("*********** Work Started **********");
			System.out.println("###################################################################" + "\n"
					+ "# If you used MolGridCal in your work, please cite:               #" + "\n"
					+ "#                                                                 #" + "\n"
					+ "# Qifeng Bai, PLoS One. 2014 Sep 17;9(9):e107837.                 #" + "\n"
					+ "# Download site: https://github.com/MolGridCal/MolGridCal         #" + "\n"
					+ "# E-mail (molaical@yeah.net)                                      #" + "\n"
					+ "# Blog website (http://molgridcal.blog.163.com).                  #" + "\n"
					+ "###################################################################");
			// Create the client.
			jppfClient = new JPPFClient();
			// create a runner instance.
			GridFDockRunner runner = new GridFDockRunner();
			// Create a job
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			executeTime = sdf.format(new java.util.Date());
			System.out.println("Executed time is on: " + executeTime);
			runner.executeBlockingTask(jppfClient);
			// JPPFJob job = runner.buildJob();
			// execute a blocking job
			// runner.executeBlockingJob(job);
		} catch (Exception e) {
			System.out.println("get bad results******!");
			e.printStackTrace();
		} finally {
			if (jppfClient != null)
				jppfClient.close();
		}

		executeTime = sdf.format(new java.util.Date());
		System.out.println("The work finished time is on: " + executeTime);
		System.gc();
	}

	public JPPFJob buildJob(final String jobName) throws Exception {

		// Read the parameters from the file.
		try {
			fl = new File(inputParameter);
		} catch (Exception e) {
			System.out.println("The parameter of " + inputParameter + " can not be found, please create it!");
		}

		try {
			rd = new BufferedReader(new FileReader(fl));
			userdir = System.getProperty("user.dir");

			while ((tempStr = rd.readLine()) != null) {
				if (tempStr.length() == 0) {
				} else {

					temArray = tempStr.split("\\s+");
					// Replace "\" to  "/".
					if (temArray[1].contains("\\")) {
						temArray[1].replaceAll("\\\\", "/");
					}

					if (temArray[0].equalsIgnoreCase("Ipaddress")) {
						ip = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("IpPort")) {
						port = Integer.parseInt(temArray[1]);
					} else if (temArray[0].equalsIgnoreCase("User")) {
						user = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Password")) {
						passwd = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Downloaddir")) {
						downdir = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("UploadDir")) {
						uploaddir = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Program")) {
						program = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Command")) {
						command = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Token")) {
						sign = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("MaxNodes")) {
						maxNodes = Integer.parseInt(temArray[1]);
					} else if (temArray[0].equalsIgnoreCase("commandDir")) {
						if (tempStr.contains("Program Files")) {
							commandDir = temArray[1] + " " + temArray[2];
						} else if (tempStr.contains("Program Files (x86)")) {
							commandDir = temArray[1] + " " + temArray[2] + " " + temArray[3];
						} else {
							commandDir = temArray[1];
						}
					} else if (temArray[0].startsWith("#")) {

					} else {
						System.out.println("***********: " + tempStr);
						System.out.println("Configure the wrong parameters in " + inputParameter + ", please "
								+ "check \"IpPort\", \"User\", \"Password\", \"Downloaddir\", "
								+ "\"UploadDir\", \"Program\", \"Command\" and comments \"#\" so on,"
								+ " and add it according manual!");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception happen when read the " + inputParameter + ", please check the parameters!");
		} finally {
			if (rd != null) {
				rd.close();
			}
		}
		if (ip == null) {
			System.out.println("The FTP ip address should be setting, please check paramter file!");
		} else if (port == 0) {
			System.out.println("The FTP ip port should be setting, please check paramter file!");
		} else if (user == null) {
			System.out.println("The FTP user should be setting, please check paramter file!");
		} else if (passwd == null) {
			System.out.println("The FTP password should be setting, please check paramter file!");
		} else if (downdir == null) {
			System.out.println("The FTP download directory should be setting, please check paramter file!");
		} else if (uploaddir == null) {
			System.out.println("The FTP upload directory should be setting, please check paramter file!");
		} else if (program == null) {
			System.out.println(
					"The run program should be setting such as Autodock_vina etc., please check paramter file!");
		} else if (command == null) {
			System.out.println("The program command should be setting such as \"vina\", please check paramter file!");
		} else if (sign == null) {
			System.out.println("The token should be for delete no use files, please check paramter file!");
		} else {
			System.out.println("Every parameter is ok! Now preparing for work!");
		}

		try {
			dt.connect(ip, port, user, passwd);
			if (dt.isconnect() == true) {
				System.out.println("The ftp had connected!");
			} else {
				System.out.println("The ftp can not connect! Please check FTP!");
			}
		} catch (IOException e3) {
			System.out.println("FTP connect err in the server!");
		}

		try {
			fileList = dt.ftpClient.listFiles(downdir);
		} catch (IOException e) {
			System.out.println("The change working directory is wrong!");
			e.printStackTrace();
		}

		DataProvider dp = new MemoryMapDataProvider();

		JPPFJob job = new JPPFJob();
		job.setDataProvider(dp);

		if (program.equalsIgnoreCase("Autodock_Vina")) {
			for (int i = countStart; i < countEnd; i++) {
				fltmp = fileList[i].getName();
				//System.out.println("$$$$$$$$$$$$$$$$$$^^^^^^^^:  " + fltmp);
				final Task<?> task = job.add(
						new GridFVinaTask(ip, port, user, passwd, downdir, uploaddir, program, command, fltmp, sign));
				task.setId(jobName + " - MolGridCal task");
				System.gc();
			}
			dt.logout();
			dt.disconnect();
		} else if (program.equalsIgnoreCase("Autodock4")) {
			for (int i = countStart; i < countEnd; i++) {
				Arraytmp = fileList[i].getName().split("\\.");

				if (fileList[i].getName().contains("pdbqt")) {
					fltmp = Arraytmp[0];
					final Task<?> task = job.add(new GridFAutodock4Task(ip, port, user, passwd, downdir, uploaddir,
							program, command, fltmp, sign));
					task.setId(jobName + " - MolGridCal task");
					System.gc();
				}
			}
			dt.logout();
			dt.disconnect();
		} else if (program.equalsIgnoreCase("Ledock")) {
			System.out.println("MolGridCal only support the linux version of Ledock.");
			for (int i = countStart; i < countEnd; i++) {
				fltmp = fileList[i].getName();
				final Task<?> task = job.add(
						new GridFLeDockTask(ip, port, user, passwd, downdir, uploaddir, program, command, fltmp, sign));
				task.setId(jobName + " - MolGridCal task");
				System.gc();
			}
			dt.logout();
			dt.disconnect();
		} else if (program.equalsIgnoreCase("UCSFDock")) {
			System.out.println("MolGridCal only support the linux version of UCSF Dock.");
			for (int i = countStart; i < countEnd; i++) {
				fltmp = fileList[i].getName();
				final Task<?> task = job.add(new GridFUCSFDOCKTask(ip, port, user, passwd, downdir, uploaddir, program,
						command, fltmp, sign, commandDir));
				task.setId(jobName + " - MolGridCal task");
				System.gc();
			}
			dt.logout();
			dt.disconnect();
		} else if (program.equalsIgnoreCase("Schrodinger")) {
			for (int i = countStart; i < countEnd; i++) {
				fltmp = fileList[i].getName();
				final Task<?> task = job.add(new GridFSchrodingerTask(ip, port, user, passwd, downdir, uploaddir,
						program, command, fltmp, sign, commandDir));
				task.setId(jobName + " - MolGridCal task");
				System.gc();
			}
			dt.logout();
			dt.disconnect();
		}
		System.gc();
		return job;
	}

	// Blocking mode
	public void executeBlockingTask(final JPPFClient jppfClient) throws Exception {
		/** This part send the 8000 results to nodes. It is likely m*n **/
		// Read the parameters from the file.
		try {
			fl = new File(inputParameter);
		} catch (Exception e) {
			System.out.println("The parameter of " + inputParameter + " can not be found, please create it!");
		}

		try {
			rd = new BufferedReader(new FileReader(fl));
			userdir = System.getProperty("user.dir");

			while ((tempStr = rd.readLine()) != null) {
				if (tempStr.length() == 0) {
				} else {

					temArray = tempStr.split("\\s+");
					if (temArray[0].equalsIgnoreCase("Ipaddress")) {
						ip = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("IpPort")) {
						port = Integer.parseInt(temArray[1]);
					} else if (temArray[0].equalsIgnoreCase("User")) {
						user = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Password")) {
						passwd = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Downloaddir")) {
						downdir = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("UploadDir")) {
						uploaddir = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Program")) {
						program = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Command")) {
						command = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("Token")) {
						sign = temArray[1];
					} else if (temArray[0].equalsIgnoreCase("MaxNodes")) {
						maxNodes = Integer.parseInt(temArray[1]);
					} else if (temArray[0].equalsIgnoreCase("commandDir")) {
						if (tempStr.contains("Program Files")) {
							commandDir = temArray[1] + " " + temArray[2];
						} else if (tempStr.contains("Program Files (x86)")) {
							commandDir = temArray[1] + " " + temArray[2] + " " + temArray[3];
						} else {
							commandDir = temArray[1];
						}
					} else if (temArray[0].startsWith("#")) {
					} else {
						System.out.println("***********: " + tempStr);
						System.out.println("Configure the wrong parameters in +" + inputParameter + ", please "
								+ "check \"IpPort\", \"User\", \"Password\", \"Downloaddir\", "
								+ "\"UploadDir\", \"Program\", \"Command\" and comments \"#\" so on,"
								+ " and add it according manual!");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception happen when read the " + inputParameter + ", please check the parameters!");
		} finally {
			if (rd != null) {
				rd.close();
			}
		}
		if (ip == null) {
			System.out.println("The FTP ip address should be setting, please check paramter file!");
		} else if (port == 0) {
			System.out.println("The FTP ip port should be setting, please check paramter file!");
		} else if (user == null) {
			System.out.println("The FTP user should be setting, please check paramter file!");
		} else if (passwd == null) {
			System.out.println("The FTP password should be setting, please check paramter file!");
		} else if (downdir == null) {
			System.out.println("The FTP download directory should be setting, please check paramter file!");
		} else if (uploaddir == null) {
			System.out.println("The FTP upload directory should be setting, please check paramter file!");
		} else if (program == null) {
			System.out.println(
					"The run program should be setting such as Autodock_vina etc., please check paramter file!");
		} else if (command == null) {
			System.out.println("The program command should be setting such as \"vina\", please check paramter file!");
		} else if (sign == null) {
			System.out.println("The token should be for delete no use files, please check paramter file!");
		} else {
			System.out.println("Every parameter is ok! Now preparing for work!");
		}

		try {
			dt.connect(ip, port, user, passwd);
			if (dt.isconnect() == true) {
				System.out.println("The ftp had connected!");
			} else {
				System.out.println("The ftp can not connect! Please check FTP!");
			}
		} catch (IOException e3) {
			System.out.println("FTP connect err in the server!");
		}

		try {
			// DataDistribute.ftpClient.changeWorkingDirectory(downdir);
			fileList = dt.ftpClient.listFiles(downdir);
		} catch (IOException e) {
			System.out.println("The change working directory is wrong!");
			e.printStackTrace();
		}
		// System.out.println("*****TEST!");
		// The defined contStart and countEnd can be recognized by buildJob function.
		for (int i = 0; i < fileList.length; i = i + maxNodes) {
			// System.out.println("***********$$$$ "+ fileList.length + maxNodes);
			countStart = i;
			countEnd = i + maxNodes;
			if (countEnd > fileList.length) {
				countEnd = fileList.length;
			}
			if (maxNodes > fileList.length) {
				tmpCount = fileList.length;
			} else {
				tmpCount = maxNodes;
			}

			// Create a job
			JPPFJob job = buildJob("Virtual Screening job");

			// set the job in blocking mode.
			job.setBlocking(true);

			// Submit the job and wait until the results are returned.
			// The results are returned as a list of Task<?> instances,
			// in the same order as the one in which the tasks where initially added
			// to the job.
			List<Task<?>> results = jppfClient.submitJob(job);
			// process the results
			proExeResults(job.getName(), results);

			System.gc();
		}
		dt.disconnect();
		System.gc();
	}

	// non-blocking mode
	public void executeNonBlockingTask(final JPPFClient jppfClient) throws Exception {

		// create jobs.
		JPPFJob job = buildJob("Virtual Screening job");

		// set the job in non-blocking mode.
		job.setBlocking(false);

		// Submit the job without waiting.

		jppfClient.submitJob(job);

		// The jobs are finished by cycle process function
		List<Task<?>> results = job.awaitResults();

		// process the results
		proExeResults(job.getName(), results);
	}

	// Build multi-jobs! This is a template from JPPF, need update for MolGridCal.
	public void executeMultipleConcurrentTasks(final JPPFClient jppfClient, final int numberOfJobs) throws Exception {
		// ensure that the client connection pool has as many connections
		// as the number of jobs to execute
		getNumberOfConnections(jppfClient, numberOfJobs);

		// this list will hold all the jobs submitted for execution,
		// so we can later collect and process their results
		final List<JPPFJob> jobList = new ArrayList<>(numberOfJobs);

		// create and submit all the jobs
		for (int i = 1; i <= numberOfJobs; i++) {
			// create a job with a distinct name
			JPPFJob job = buildJob("Virtual Screnning job " + i);

			// set the job in non-blocking (or asynchronous) mode.
			job.setBlocking(false);

			// submit the job for execution, without blocking the current thread
			jppfClient.submitJob(job);

			// add this job to the list
			jobList.add(job);
		}

		// the non-blocking jobs are submitted asynchronously, we can do anything else
		// in the meantime
		System.out.println("Doing something while the jobs are executing ...");

		// wait until the jobs are finished and process their results.
		for (JPPFJob job : jobList) {
			// wait if necessary for the job to complete and collect its results
			List<Task<?>> results = job.awaitResults();

			// process the job results
			proExeResults(job.getName(), results);
		}
	}

	// Ensure that the client has the desired number of connections.
	public void getNumberOfConnections(final JPPFClient jppfClient, final int numberOfConnections) throws Exception {
		// wait until the client has at least one connection pool with at least one
		// avaialable connection
		final JPPFConnectionPool pool = jppfClient.awaitActiveConnectionPool();

		// if the pool doesn't have the expected number of connections, change its size
		if (pool.getConnections().size() != numberOfConnections) {
			// set the pool size to the desired number of connections
			pool.setSize(numberOfConnections);
		}

		// wait until all desired connections are available (ACTIVE status)
		pool.awaitActiveConnections(Operator.AT_LEAST, numberOfConnections);
	}

	public synchronized void proExeResults(final String jobName, final List<Task<?>> results) {
		// print a results header
		System.out.printf("Results for job '%s' :\n", jobName);
		// process the results
		for (Task<?> task : results) {
			String taskName = task.getId();
			// if the task execution resulted in an exception
			if (task.getThrowable() != null) {
				System.out.println(taskName + ", an exception was raised: " + task.getThrowable().getMessage());
			}
		}
		System.gc();
	}

	// Save memory.
	protected void finalize() throws Throwable {
		super.finalize();
		// System.out.println("Memory start cleaning!");
	}
}
