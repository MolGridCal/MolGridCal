/*
 * MolGridCal
 * Copyright MolGridCal Team
 * http://molgridcal.codeplex.com
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
import java.security.NoSuchAlgorithmException;

import org.jppf.node.protocol.AbstractTask;
import org.jppf.node.protocol.AbstractTask;

public class GridFAutodock4Task extends AbstractTask<String> {
	String CODING_1 = "GBK";
	String CODING_2 = "iso-8859-1";
	String fltmp;
	String ip;
	int port;
	String user;
	String passwd;
	String downdir;
	String uploaddir;
	String command;
	String program;
	String sign;

	public GridFAutodock4Task(String ip, int port, String user, String passwd, String downdir, String uploaddir,
			String program, String command, String fltmp, String sign) {
		this.ip = ip;
		this.port = port;
		this.user = user;
		this.passwd = passwd;
		this.downdir = downdir;
		this.uploaddir = uploaddir;
		this.command = command;
		this.fltmp = fltmp;
		this.program = program;
		this.sign = sign;
	}

	/************* assign the static parameters to run ****************/
	public void run() {
		gridFDockTask(ip, port, user, passwd, downdir, uploaddir, program, command, fltmp, sign);
		System.out.println("*********** Working complete ************");
		System.gc();
	}

	/****************** This function can be modify *******************/

	public void gridFDockTask(String ipAdress, int port, String user, String passwd, String downdir, String uploaddir,
			String program, String command, String fltmp, String sign) {
		// Get the node directory in the local computer.
		String userdir = System.getProperty("user.dir");
		String os = System.getProperty("os.name").toLowerCase();
		System.out.println("*************Working started**************");
		System.out.println("###################################################################" + "\n"
				+ "# This work is performed by Autodock4 docking for virtual screening.   #" + "\n"
				+ "# If you used MolGridCal in your work, please cite:               	  #" + "\n"
				+ "#                                                                      #" + "\n"
				+ "# Qifeng Bai, PLoS One. 2014 Sep 17;9(9):e107837.                      #" + "\n"
				+ "# Download site: https://github.com/MolGridCal/MolGridCal              #" + "\n"
				+ "# E-mail (molgridcal@yeah.net)                                         #" + "\n"
				+ "# Blog website (http://molgridcal.blog.163.com).                       #" + "\n"
				+ "###################################################################");

		/********************* Download file *********************/
		boolean tocken3 = true;
		boolean token1 = true;
		ExecuteThread nt = null;
		boolean tk = true;
		boolean tk1 = true;

		// Make sure download right.
		while (tocken3) {
			int remoteFile = 0;
			int localFile = 111111;

			int remoteFile1 = 0;
			int localFile1 = 111111;
			int i = 0;

			// Make sure no running jobs, then download file.
			if (GridFDockRunner.cn.check(command) <= 0) {
				try {
					GridFDockRunner.dt.connect(ipAdress, port, user, passwd);
					System.out.println("Connect to the server for working!");
				} catch (NoSuchAlgorithmException e1) {
					System.out.println("Nodes connect Algorithm err!");
					e1.printStackTrace();
				} catch (IOException e1) {
					System.out.println("Nodes connect the server err!");
					e1.printStackTrace();
				}

				try {
					// Try to download file for 20 times correctly.
					while (tk && i <= 20) {
						// Download file
						GridFDockRunner.dt.download(downdir + "/" + fltmp + ".dpf", userdir + "/" + fltmp + ".dpf");
						GridFDockRunner.dt.download(downdir + "/" + fltmp + ".pdbqt", userdir + "/" + fltmp + ".pdbqt");
						// Calculate the files size in Ftp server and local.
						remoteFile = GridFDockRunner.dt.getFileSize(ipAdress, user, passwd, downdir, fltmp + ".dpf",
								port);
						localFile = getLocalFileSize(userdir, fltmp + ".dpf");
						remoteFile1 = GridFDockRunner.dt.getFileSize(ipAdress, user, passwd, downdir, fltmp + ".pdbqt",
								port);
						localFile1 = getLocalFileSize(userdir, fltmp + ".pdbqt");
						System.gc();

						if (localFile != remoteFile || localFile1 != remoteFile1) {
							i++;
							System.out.println("FTP may something wrong, try it for a while!");
							System.gc();
						} else {
							tk = false;
						}
					}

				} catch (Exception e4) {
					System.out.println("FTP download err!");
					e4.printStackTrace();
				}
				try {
					// Close connection.
					GridFDockRunner.dt.logout();
					GridFDockRunner.dt.disconnect();
				} catch (IOException e2) {
					System.out.println("FTP disconnect err!");
					e2.printStackTrace();
				}

				if (remoteFile != localFile || remoteFile1 != localFile1) {
					System.out.println("File download err from FTP!");
				}
				tocken3 = false;

			} else {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					System.out.println("Some interrupted exception happened, please check it.");
					System.exit(0);
					e.printStackTrace();
				}
			}

		}

		/****************************** Start Thread *********************************/
		// Start running molecular docking task.
		nt = new ExecuteThread(program, command, fltmp);
		nt.start();
		nt.setPriority(Thread.MIN_PRIORITY);
		System.gc();
		/********************************* END **************************************/
		int remoteFile = 0;
		int localFile = 111111;
		int j = 0;
		/****************************** Upload file ********************************/
		// Make sure put the results to FTP server correctly.
		while (token1) {
			// Start the upload until no pointed program running.
			if (GridFDockRunner.cn.check(command) == 0) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					GridFDockRunner.dt.connect(ipAdress, port, user, passwd);
					System.out.println("******** Starting for transferring! ********");
				} catch (NoSuchAlgorithmException e1) {
					System.out.println("Connection err in upload part.");
					e1.printStackTrace();
				} catch (IOException e1) {
					System.out.println("Connection I/O err in upload part.");
					e1.printStackTrace();
				}

				try {
					// Try to upload 10 times.
					while (tk1 && j <= 10) {
						// Upload result.
						GridFDockRunner.dt.upload(userdir + "/" + fltmp + ".dlg", uploaddir + "/" + fltmp + ".dlg");
						// Calculate the result's files size in Ftp server and local.
						remoteFile = GridFDockRunner.dt.getFileSize(ipAdress, user, passwd, uploaddir, fltmp + ".dlg",
								port);
						localFile = getLocalFileSize(userdir, fltmp + ".dlg");
						System.gc();
						if (remoteFile != localFile) {
							j++;
							System.out.println("FTP may something wrong, try it for a while!");
							// sleep 10 fs
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								System.out.println("Sleep interrupted when upload file.");
								e.printStackTrace();
							}
							System.gc();
							// Delete the file in ftp server.
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir + "/" + fltmp + ".dlg");
						} else {
							tk1 = false;
						}
					}
				} catch (IOException e) {
					System.out.println("FTP upload err!");

				} finally {
					try {
						GridFDockRunner.dt.logout();
						GridFDockRunner.dt.disconnect();
					} catch (IOException e) {
						System.out.println("FTP disconnect err!");
						e.printStackTrace();
					}
				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					System.out.println("Sleep interrupted exception when prepared to delete ligand.");
					e.printStackTrace();
				}

				// Start to delete download ligand to save node disk space.
				try {
					deleteFile(userdir, fltmp + ".dpf");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete ligand err!");
				}
				// Start delete docking results to save node disk space.
				try {
					deleteFile(userdir, fltmp + ".pdbqt");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete output file err!");
				}
				// Start delete docking results to save node disk space.
				try {
					deleteFile(userdir, fltmp + ".dlg");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete output file err!");
				}
				token1 = false;
			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
		}
		nt = null;
		System.gc();
	}

	/********************* Delete the file ***************************/
	private void deleteFile(String path, String filename) {
		File file = new File(path);
		File[] files = file.listFiles();

		for (int i = 0; i < files.length; i++) {
			for (int j = 0; j < 5; j++) {
				if ((files[i].getName().equalsIgnoreCase(filename))) {
					files[i].delete();
					files[i].deleteOnExit();
				}
			}
		}
	}

	private void deleteFile1(String path, String filename) {
		File file = new File(path);
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			for (int j = 0; j < 10; j++) {
				if ((files[i].getName().contains(filename))) {
					System.gc();
					files[i].delete();
					files[i].deleteOnExit();
				}
			}
		}
	}

	/********************** Get local file size **********************/
	private int getLocalFileSize(String path, String filename) {
		int size = 0;
		File file = new File(path + "/" + filename);
		try {
			FileInputStream tmp = new FileInputStream(file);
			BufferedInputStream fis = new BufferedInputStream(tmp);
			size = fis.available();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("The local file which downloads from FTP is not found.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	// Save memory.
	protected void finalize() throws Throwable {
		super.finalize();
		// System.out.println("Memory start cleaning!");
	}
}
