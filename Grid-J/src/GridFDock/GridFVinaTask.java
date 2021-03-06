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

public class GridFVinaTask extends AbstractTask<String> {
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

	public GridFVinaTask(String ip, int port, String user, String passwd, String downdir, String uploaddir,
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
				+ "# This work is performed by Autodock vina for virtual screening.    #" + "\n"
				+ "# If you used MolGridCal in your work, please cite:                 #" + "\n"
				+ "#                                                                   #" + "\n"
				+ "# Qifeng Bai, PLoS One. 2014 Sep 17;9(9):e107837.                   #" + "\n"
				+ "# Download site: https://github.com/MolGridCal/MolGridCal           #" + "\n"
				+ "# E-mail (molgridcal@yeah.net)                                      #" + "\n"
				+ "# Blog website (http://molgridcal.blog.163.com).                    #" + "\n"
				+ "###################################################################");

		/********************* Download file *********************/
		boolean tocken3 = true;
		boolean token1 = true;
		ExecuteThread nt = null;
		boolean tk = true;
		boolean tk1 = true;
		boolean tk2 = true;

		// Make sure download right.
		while (tocken3) {
			int remoteFile = 0;
			int localFile = 111111;
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
						GridFDockRunner.dt.download(downdir + "/" + fltmp, userdir + "/" + fltmp);
						// Calculate the files size in Ftp server and local.
						remoteFile = GridFDockRunner.dt.getFileSize(ipAdress, user, passwd, downdir, fltmp, port);
						localFile = getLocalFileSize(userdir, fltmp);
						System.gc();

						if (localFile != remoteFile) {
							System.out.println("File is downloading, try it for a while!");
							i++;
							// sleep 10 fs
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							System.gc();
						} else {
							tk = false;
						}
					}

				} catch (IOException e4) {
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
				if (remoteFile != localFile) {
					System.out.println("File download err from FTP!");

				}
				tocken3 = false;

			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("Some interrupted exception happened, please check it.");
					System.exit(0);
					e.printStackTrace();
				}
			}

		}

		// Judge whether input file exists.
		File tmpFile = new File("conf.txt");
		if (!tmpFile.exists()) {
			System.out.println("The input file does not exist, please place it in the node.");
		}

		/********************** Start Thread *************************/
		// Start running molecular docking task.
		nt = new ExecuteThread(program, command, fltmp);
		nt.start();
		nt.setPriority(Thread.MIN_PRIORITY);
		try {
			nt.join();
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			System.out.println("The task was terminated in the client!");
		}
		System.gc();
		/********************************* END **************************************/
		int remoteFile = 0;
		int localFile = 111111;

		int remoteFile1 = 0;
		int localFile1 = 111111;

		int j = 0;
		int k = 0;
		/********************** * Upload file *******************************/
		// Make sure put the results to FTP server correctly.
		while (token1) {
			// Start the upload until no pointed program running.
			if (GridFDockRunner.cn.check(command) == 0) {
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
						GridFDockRunner.dt.upload(userdir + "/out_" + fltmp, uploaddir + "/" + "out_" + fltmp);
						// Calculate the result's files size in Ftp server and local.
						remoteFile = GridFDockRunner.dt.getFileSize(ipAdress, user, passwd, uploaddir, "out_" + fltmp,
								port);

						localFile = getLocalFileSize(userdir, "out_" + fltmp);
						System.gc();
						if (remoteFile > localFile) {
							System.out.println("Remote File is bigger, delete it!");
							j++;
							// sleep 10 fs
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								System.out.println("Sleep interrupted when upload result file.");
								e.printStackTrace();
							}
							System.gc();
							// Delete the file in ftp server.
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir + "/" + "out_" + fltmp);
						} else if (remoteFile < localFile) {
							System.out.print("Remote File is smaller, it should continue upload!");
							// sleep 10 fs
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								System.out.println("Sleep interrupted when upload file.");
								e.printStackTrace();
							}
							System.gc();
							// Delete the file in ftp server.
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir + "/" + "out_" + fltmp);
						} else {
							tk1 = false;
						}
					}

				} catch (IOException e) {
					System.out.println("FTP upload err!");
					e.printStackTrace();

				}

				try {
					// Try to upload 10 times.
					while (tk2 && k <= 10) {
						// Upload log result.
						GridFDockRunner.dt.upload(userdir + "/log_" + fltmp, uploaddir + "/" + "log_" + fltmp);
						// Calculate the result's files size in Ftp server and local.
						remoteFile1 = GridFDockRunner.dt.getFileSize(ipAdress, user, passwd, uploaddir, "log_" + fltmp,
								port);
						localFile1 = getLocalFileSize(userdir, "log_" + fltmp);
						System.gc();
						if (remoteFile1 > localFile1) {
							System.out.println("Remote File is bigger, delete it!");
							k++;
							// sleep 10 fs
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								System.out.println("Sleep interrupted when upload log file.");
								e.printStackTrace();
							}
							System.gc();
							// Delete the file in ftp server.
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir + "/" + "log_" + fltmp);
						} else if (remoteFile1 < localFile1) {
							System.out.print("Remote File is smaller, it should continue upload!");
							// sleep 10 fs
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								System.out.println("Sleep interrupted when upload log file.");
								e.printStackTrace();
							}
							System.gc();
							// Delete the file in ftp server.
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir + "/" + "log_" + fltmp);
						} else {
							tk2 = false;
						}
					}

				} catch (IOException e) {
					System.out.println("FTP upload err!");
					e.printStackTrace();
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
					Thread.sleep(20);
				} catch (InterruptedException e) {
					System.out.println("Sleep interrupted exception when prepared to delete ligand.");
					e.printStackTrace();
				}

				// Start to delete download ligand to save node disk space.
				try {
					deleteFile(userdir, fltmp);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete ligand err!");
				}
				// Start delete docking results to save node disk space.
				try {
					deleteFile(userdir, "out_" + fltmp);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete output file err!");
				}
				// Start delete log file to save node disk space.
				try {
					deleteFile(userdir, "log_" + fltmp);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete output file err!");
				}
				token1 = false;
			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("Sleep interrupted exception when delete final results.");
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
			for (int j = 0; j < 5; j++) {
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
		FileInputStream fis = null;
		try {

			fis = new FileInputStream(file);
			size = fis.available();

		} catch (FileNotFoundException e) {
			System.out.println("The local file which downloads from FTP is not found.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return size;
	}

	// Save memory.
	protected void finalize() throws Throwable {
		super.finalize();
		// System.out.println("Memory start cleaning!");
	}
}
