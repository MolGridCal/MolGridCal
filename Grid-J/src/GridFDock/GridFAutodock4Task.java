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

	// private static FTPClient ftpClient = null;
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

	public GridFAutodock4Task(String ip, int port, String user, String passwd,
			String downdir, String uploaddir, String program, String command,
			String fltmp, String sign) {

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

		gridFDockTask(ip, port, user, passwd, downdir, uploaddir, program,
				command, fltmp, sign);
		System.out.println("*********** Working complete ************");
		System.gc();
	}

	/****************** This function can be modify *******************/

	public void gridFDockTask(String ipAdress, int port, String user,
			String passwd, String downdir, String uploaddir, String program,
			String command, String fltmp, String sign) {

		String userdir = System.getProperty("user.dir");
		String os = System.getProperty("os.name").toLowerCase();

/*		if (os.indexOf("windows") >= 0 || os.indexOf("linux") >= 0) {
			deleteFile1(userdir, sign);
		}*/

		System.out.println("*************Working started**************");
		System.out.println("###################################################################" + "\n"
				+ "# If you used MolGridCal in your work, please cite:               #" + "\n"
				+ "#                                                                 #" + "\n"
				+ "# Qifeng Bai, PLoS One. 2014 Sep 17;9(9):e107837.                 #" + "\n"
				+ "# Download site: https://github.com/MolGridCal/MolGridCal         #" + "\n"
				+ "# E-mail (molaical@yeah.net)                                    #" + "\n"
				+ "# Blog website (http://molgridcal.blog.163.com).                  #" + "\n"
				+ "###################################################################");

		/********************* Download file *********************/
		boolean tocken3 = true;
		boolean token1 = true;
		ExecuteThread nt = null;
		boolean tk = true;
		boolean tk1 = true;
		boolean tk2 = true;

		while (tocken3) {

			int remoteFile = 0;
			int localFile = 111111;

			int remoteFile1 = 0;
			int localFile1 = 111111;
			int i = 0;

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

					while (tk && i <= 20) {

						GridFDockRunner.dt.download(downdir + "/" + fltmp
								+ ".dpf", userdir + "/" + fltmp + ".dpf");
						GridFDockRunner.dt.download(downdir + "/" + fltmp
								+ ".pdbqt", userdir + "/" + fltmp + ".pdbqt");

						remoteFile = GridFDockRunner.dt.getFileSize(ipAdress,
								user, passwd, downdir, fltmp + ".dpf", port);
						localFile = getLocalFileSize(userdir, fltmp + ".dpf");

						remoteFile1 = GridFDockRunner.dt.getFileSize(ipAdress,
								user, passwd, downdir, fltmp + ".pdbqt", port);
						localFile1 = getLocalFileSize(userdir, fltmp + ".pdbqt");
						System.gc();

						if (localFile != remoteFile
								|| localFile1 != remoteFile1) {

							i++;

							System.out
									.println("FTP may something wrong, try it for a while!");
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
					e.printStackTrace();
				}
			}

		}

		/****************************** Start Thread *********************************/

		nt = new ExecuteThread(program, command, fltmp);
		nt.start();
		nt.setPriority(Thread.MIN_PRIORITY);
		System.gc();
		/********************************* END **************************************/

		int remoteFile = 0;
		int localFile = 111111;
		int j = 0;

		/****************************** Upload file ********************************/
		while (token1) {

			if (GridFDockRunner.cn.check(command) == 0) {

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					GridFDockRunner.dt.connect(ipAdress, port, user, passwd);
					System.out
							.println("******** Starting for transferring! ********");
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {

					e1.printStackTrace();
				}

				try {

					while (tk1 && j <= 20) {

						GridFDockRunner.dt.upload(userdir + "/" + fltmp
								+ ".dlg", uploaddir + "/" + fltmp + ".dlg");

						remoteFile = GridFDockRunner.dt.getFileSize(ipAdress,
								user, passwd, uploaddir, fltmp + ".dlg", port);

						localFile = getLocalFileSize(userdir, fltmp + ".dlg");
						System.gc();
						if (remoteFile != localFile) {

							j++;
							// System.out.println("###########: " + remoteFile);
							// System.out.println("///%%%%%%%%%%%: " +
							// localFile);
							System.out
									.println("FTP may something wrong, try it for a while!");
							
							System.gc();
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir
									+ "/" + fltmp + ".dlg");
						} else {
							tk1 = false;
						}
					}

				} catch (IOException e) {
					System.out.println("FTP upload err!");

				}

				try {
					GridFDockRunner.dt.logout();
					GridFDockRunner.dt.disconnect();
				} catch (IOException e) {
					System.out.println("FTP disconnect err!");
					e.printStackTrace();
				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					deleteFile(userdir, fltmp + ".dpf");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete ligand err!");
				}
				try {
					deleteFile(userdir, fltmp + ".pdbqt");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Delete output file err!");
				}
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

			System.out
					.println("The local file which downloads from FTP is not found.");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;

	}

}
