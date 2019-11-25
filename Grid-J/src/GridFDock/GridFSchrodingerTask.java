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
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.jppf.node.protocol.AbstractTask;

public class GridFSchrodingerTask extends AbstractTask<String> {
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
	String commandDir;

	public GridFSchrodingerTask(String ip, int port, String user, String passwd, String downdir, String uploaddir,
			String program, String command, String fltmp, String sign, String commandDir) {
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
		this.commandDir = commandDir;
	}

	/************* assign the static parameters to run ****************/
	public void run() {
		gridFDockTask(ip, port, user, passwd, downdir, uploaddir, program, command, fltmp, sign, commandDir);
		System.out.println("*********** Working complete ************");
		System.gc();
	}

	/****************** This function can be modify *******************/

	public void gridFDockTask(String ipAdress, int port, String user, String passwd, String downdir, String uploaddir,
			String program, String command, String fltmp, String sign, String commandDir) {
		// Get the node directory in the local computer.
		String userdir = System.getProperty("user.dir");
		String os = System.getProperty("os.name").toLowerCase();
		System.out.println("*************Working started**************");
		System.out.println("###################################################################" + "\n"
				+ "# This work deploys Schrodinger Glide for virtual screening.      #" + "\n"
				+ "# If you used MolGridCal in your work, please cite:               #" + "\n"
				+ "#                                                                 #" + "\n"
				+ "# Qifeng Bai, PLoS One. 2014 Sep 17;9(9):e107837.                 #" + "\n"
				+ "# Download site: https://github.com/MolGridCal/MolGridCal         #" + "\n"
				+ "# E-mail (molgridcal@yeah.net)                                    #" + "\n"
				+ "# Blog website (http://molgridcal.blog.163.com).                  #" + "\n"
				+ "###################################################################");

		/********************* Download file *********************/
		boolean tocken3 = true;
		boolean token1 = true;
		ExecuteThread nt = null;
		boolean tk = true;
		boolean tk1 = true;
		String[] tmpStr = null;

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
		File tmpFile = new File("glide-dock.in");
		if (!tmpFile.exists()) {
			System.out.println("The input file does not exist, please place it in the node.");
		}

		// Copy input file to new file.
		tmpStr = fltmp.split("\\.");
		String tmpinputName = tmpStr[0] + ".in";
		try {
			copyFile(new File("glide-dock.in"), new File(tmpinputName));
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}

		// Modify the schrodinger input file.
		// Read file.
		BufferedReader data = null;
		FileInputStream fip = null;
		InputStreamReader isr = null;
		ArrayList<String> tmpArr = new ArrayList<String>();

		try {
			fip = new FileInputStream(userdir + "/" + tmpinputName);
			isr = new InputStreamReader(fip);
			data = new BufferedReader(isr);
		} catch (FileNotFoundException e3) {
			System.out.println("Schrodinger Glide input file cannot be found!");
			e3.printStackTrace();
		}

		String rl;
		// Story modified data.
		try {
			for (;;) {
				rl = data.readLine();
				// System.out.println(rl);
				if (rl == null) {
					break;
				}
				tmpStr = rl.trim().split("\\s+");
				if (tmpStr[0].equalsIgnoreCase("#")) {
					continue;
				} else if (tmpStr[0].equalsIgnoreCase("")) {
					continue;
				}
				tmpArr.add(rl);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				fip.close();
				isr.close();
				data.close();
			} catch (IOException e) {
				System.out.println("closing file err!");
				e.printStackTrace();
			}
		}

		rl = null;
		tmpStr = null;

		// Write file.
		OutputStreamWriter writer = null;
		BufferedWriter bufWriter = null;
		PrintWriter pWriter = null;

		try {
			writer = new OutputStreamWriter(new FileOutputStream(userdir + "/" + tmpinputName));
			bufWriter = new BufferedWriter(writer);
			pWriter = new PrintWriter(bufWriter);
		} catch (FileNotFoundException e) {
			System.out.println("The err of created file!");
			e.printStackTrace();
			System.exit(1);
		}
		for (int i = 0; i < tmpArr.size(); i++) {
			if (tmpArr.get(i).contains("LIGANDFILE")) {
				pWriter.printf("LIGANDFILE               " + fltmp + '\n');
				continue;
			}
			pWriter.printf(tmpArr.get(i) + '\n');
		}
		try {
			pWriter.flush();
			bufWriter.flush();
			writer.flush();

			pWriter.close();
			bufWriter.close();
			writer.close();
		} catch (IOException e) {
			System.out.println("closing file err!");
			e.printStackTrace();
		}

		/******************** Start Thread ******************************/
		// Merge path and command.
		if (commandDir.endsWith("/")) {
			command = commandDir + command + " " + tmpinputName + " -WAIT";
		} else {
			command = commandDir + "/" + command + " " + tmpinputName + " -WAIT";
		}

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
		/********************** Upload file *****************************/
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
					// Convert .mol2 to .dok
					tmpStr = tmpinputName.split("\\.");
					// Try to upload 10 times.
					while (tk1 && j <= 10) {
						// Upload result.
						GridFDockRunner.dt.upload(userdir + "/" + tmpStr[0] + "_pv.maegz",
								uploaddir + "/" + tmpStr[0] + "_pv.maegz");
						// Calculate the result's files size in Ftp server and local.
						remoteFile = GridFDockRunner.dt.getFileSize(ipAdress, user, passwd, uploaddir,
								tmpStr[0] + "_pv.maegz", port);

						localFile = getLocalFileSize(userdir, tmpStr[0] + "_pv.maegz");
						System.gc();
						if (remoteFile > localFile) {
							System.out.println("Remote File is bigger, delete it!");
							j++;
							// sleep 10 fs
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								System.out.println("Sleep interrupted when upload file.");
								e.printStackTrace();
							}
							System.gc();
							// Delete the file in ftp server.
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir + "/" + tmpStr[0] + "_pv.maegz");
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
							GridFDockRunner.dt.deleteFtpServerFile(uploaddir + "/" + tmpStr[0] + "_pv.maegz");
						} else {
							tk1 = false;
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
				try {
					// Start delete docking results to save node disk space.
					deleteFile(userdir, tmpStr[0] + "_pv.maegz");
					// Start delete log file to save node disk space.
					deleteFile(userdir, tmpStr[0] + ".log");
					// Start input file to save node disk space.
					deleteFile(userdir, tmpStr[0] + ".in");
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
			e.printStackTrace();
			System.out.println("The local file which downloads from FTP is not found.");
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

	// This fuction is used for copy a file.
	private void copyFile(File origin, File dest) throws IOException {
		FileChannel input = null;
		FileChannel output = null;
		try {
			input = new FileInputStream(origin).getChannel();
			output = new FileOutputStream(dest).getChannel();
			output.transferFrom(input, 0, input.size());
		} finally {
			input.close();
			output.close();
		}
	}

	// clear memory.
	protected void finalize() throws Throwable {
		super.finalize();
		// System.out.println("Memory start cleaning!");
	}
}
