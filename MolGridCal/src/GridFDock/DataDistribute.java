package GridFDock;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

public class DataDistribute {

	String CODING_1 = "GBK";
	String CODING_2 = "iso-8859-1";

	public FTPClient ftpClient = new FTPClient();

	public DataDistribute() {
		this.ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
	}

	public void connect(String hostname, int port, String username, String password)
			throws IOException, NoSuchAlgorithmException {
		// ftpClient = new FTPSClient(false);
		// create with implicit TLS
		boolean tmp = true;
		int i = 0;
		ftpClient = new FTPClient();
		ftpClient.connect(hostname, port);
		do {
			i++;
			if (ftpClient.login(username, password) == true) {
				tmp = false;
			} else {
				System.out.println("Can not connect the FTP server! And will try it again!");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (tmp && i <= 100);
	}

	public boolean isconnect() {
		if (ftpClient.isConnected() == true) {
			return true;
		} else {
			return false;
		}
	}

	// Download the file.
	public Status download(String remote, String local) throws IOException {
		// The mode of transfer
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.enterLocalPassiveMode();
		// Set the binary for transferring
		// ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		Status result = null;
		boolean tmp1 = true;
		// Check the file
		FTPFile[] files = ftpClient.listFiles(new String(remote.getBytes(CODING_1), CODING_2));
		if (files.length != 1) {
			System.out.println("The remote file does not exist!");
			return Status.Remote_File_Noexist;
		}
		int i = 0;
		while (tmp1) {
			long lRemoteSize = files[0].getSize();

			File f = new File(local);
			// Judge whether the file exist.

			if (f.exists()) {
				long localSize = f.length();

				if (localSize >= lRemoteSize) {
					System.out.println("The size of local file is lager than the size of remote's file, and stop it.");
					return Status.Local_Bigger_Remote;
				}

				// record the downloading status.
				FileOutputStream out = new FileOutputStream(f, true);
				ftpClient.setRestartOffset(localSize);
				InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes(CODING_1), CODING_2));
				byte[] bytes = new byte[1024];
				long step = lRemoteSize / 100;
				long process = localSize / step;
				int c;
				while ((c = in.read(bytes)) != -1) {
					out.write(bytes, 0, c);
					localSize += c;
					long nowProcess = localSize / step;
					if (nowProcess > process) {
						process = nowProcess;
						/*
						 * if (process % 10 == 0) System.out.println(
						 * "Download Progressing" + process);
						 */

					}
				}
				in.close();
				out.close();
				boolean isDo = ftpClient.completePendingCommand();
				if (isDo && localSize == lRemoteSize) {
					result = Status.Download_From_Break_Success;
					tmp1 = false;
				} else {
					if (i > 10) {
						tmp1 = false;
					}
					i++;
					result = Status.Download_From_Break_Failed;
				}
			} else {
				OutputStream out = new FileOutputStream(f);
				InputStream in = ftpClient.retrieveFileStream(new String(remote.getBytes(CODING_1), CODING_2));
				byte[] bytes = new byte[1024];
				long step = lRemoteSize / 100;
				long process = 0;
				long localSize = 0L;
				int c;
				while ((c = in.read(bytes)) != -1) {
					out.write(bytes, 0, c);
					localSize += c;
					long nowProcess = localSize / step;
					if (nowProcess > process) {
						process = nowProcess;
						/*
						 * if (process % 10 == 0) System.out.println(
						 * "Download Progressing" + process);
						 */
					}
				}
				in.close();
				out.close();
				boolean upNewStatus = ftpClient.completePendingCommand();

				if (upNewStatus && localSize == lRemoteSize) {
					result = Status.Download_New_Success;
					tmp1 = false;
				} else {
					if (i > 10) {
						tmp1 = false;
					}
					i++;
					result = Status.Download_New_Failed;
				}
			}
		}
		return result;

	}

	// Upload File
	public Status upload(String local, String remote) throws IOException {

		ftpClient.enterLocalPassiveMode();

		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlEncoding("GBK");
		Status result = null;
		boolean tmp3 = true;

		String remoteFileName = remote;
		if (remote.contains("/")) {
			remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);

			if (CreateDirectory(remote, ftpClient) == Status.Create_Directory_Fail) {
				return Status.Create_Directory_Fail;
			}
		}

		long remoteSize = 0;
		long localSize = 111111;
		int i = 0;

		while (tmp3) {

			if (remoteSize == localSize) {
				tmp3 = false;
				return Status.File_Exits;
			} else if (remoteSize != localSize) {

				FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
				files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
				if (files.length == 1) {
					remoteSize = files[0].getSize();
					File f = new File(local);
					localSize = f.length();

					result = uploadFile(remoteFileName, f, ftpClient, remoteSize);

					// System.out.println("**********************************
					// localSize: "+
					// localSize);
					// System.out.println("**********************************
					// remoteSize: "+
					// remoteSize);
					if (result == Status.Upload_From_Break_Failed) {
						if (!ftpClient.deleteFile(remoteFileName)) {
							return Status.Delete_Remote_Faild;
						}
						result = uploadFile(remoteFileName, f, ftpClient, 0);
					}
				} else {
					result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
				}

				if (i > 3) {
					tmp3 = false;
				}
				i++;
			}

		}
		return result;
	}

	// disconnect with ftp server.
	public void disconnect() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}

	public void logout() {
		if (ftpClient.isConnected()) {
			try {
				ftpClient.logout();
			} catch (IOException e) {
				System.out.println("User logout err~~~!");
				e.printStackTrace();
			}
		}
	}

	// Create the directory
	public Status CreateDirectory(String remote, FTPClient ftpClient) throws IOException {
		Status status = Status.Create_Directory_Success;
		String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if (!directory.equalsIgnoreCase("/")
				&& !ftpClient.changeWorkingDirectory(new String(directory.getBytes(CODING_1), CODING_2))) {
			int start = 0;
			int end = 0;
			if (directory.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = directory.indexOf("/", start);
			while (true) {
				String subDirectory = new String(remote.substring(start, end).getBytes(CODING_1), CODING_2);
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					if (ftpClient.makeDirectory(subDirectory)) {
						ftpClient.changeWorkingDirectory(subDirectory);
					} else {
						System.out.println("the failure for creating directory.");
						return Status.Create_Directory_Fail;
					}
				}

				start = end + 1;
				end = directory.indexOf("/", start);// "/" is a token for making
				// fold.

				if (end <= start) {
					break;
				}
			}
		}
		return status;
	}

	// Transfer data from a breakpoint.
	public Status uploadFile(String remoteFile, File localFile, FTPClient ftpClient, long remoteSize)
			throws IOException {

		long step = localFile.length() / 100;
		long process = 0;
		long localreadbytes = 0L;
		boolean tmp2 = true;
		Status result = null;
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

		while (tmp2) {
			RandomAccessFile raf = new RandomAccessFile(localFile, "r");
			OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes(CODING_1), CODING_2));

			if (remoteSize > 0) {
				ftpClient.setRestartOffset(remoteSize);
				process = remoteSize / step;
				raf.seek(remoteSize);
				localreadbytes = remoteSize;
			}
			byte[] bytes = new byte[1024];
			int c;
			while ((c = raf.read(bytes)) != -1) {
				out.write(bytes, 0, c);
				localreadbytes += c;
				if (localreadbytes / step != process) {
					process = localreadbytes / step;
					// System.out.println("Upload Progress" + process);

				}
			}
			out.flush();
			raf.close();
			out.close();
			boolean judge = ftpClient.completePendingCommand();

			if (judge) {
				result = Status.Upload_From_Break_Success;
				tmp2 = false;
			} else {
				result = Status.Upload_New_File_Failed;
			}
		}
		return result;
	}

	public void searchAllDirectory(String pathname) throws IOException {
		ftpClient.changeWorkingDirectory(pathname);
		FTPFile[] fileList = ftpClient.listFiles(pathname);
		for (int i = 0; i < fileList.length; i++) {
			System.out.println(fileList[i].getName());
		}
		traverse(fileList);
	}

	// Get list name of directory.
	private void traverse(FTPFile[] fileList) throws IOException {
		String tempDir = null;
		for (FTPFile file : fileList) {
			if (file.getName().equals(".") || file.getName().equals("..")) {
				continue;
			}
			if (file.isDirectory()) {
				System.out.println("***************** Directory: " + file.getName() + "  Start **************");
				tempDir = ftpClient.printWorkingDirectory();
				if (tempDir.matches("^((/\\w+))+$"))
					tempDir += "/" + file.getName();
				else
					tempDir += file.getName();
				ftpClient.changeWorkingDirectory(new String(tempDir.getBytes(CODING_1), CODING_2));
				traverse(ftpClient.listFiles(tempDir));

				// If is not a directory.
				System.out.println("***************** Directory:" + file.getName() + "   End **************\n");
			} else {
				System.out.println("FileName:" + file.getName() + " FileSize:" + file.getSize() / (1024) + "KB"
						+ " CreateTime:" + file.getTimestamp().getTime());
			}
		}

		ftpClient.changeToParentDirectory();
	}

	// Get the file size from FTP site.
	public int getFileSize1(String server, String user, String pswd, String path, String filename, int port) {

		URL url = null;
		URLConnection con = null;
		int c;
		int t = 0;

		BufferedInputStream bis;

		try {

			url = new URL("ftp://" + user + ":" + pswd + "@" + server + ":" + port + path + "/" + filename);

			url.getFile().length();
			con = url.openConnection();
			con.connect();
			InputStream urlfs = con.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while ((c = urlfs.read()) != -1)
				out.write((byte) c);
			urlfs.close();
			t = out.size();
			// return loadmap (new ByteArrayInputStream(out.toByteArray());

		} catch (MalformedURLException e) {
			System.out.println("When get the size of file from site, the url is wrong.");
		} catch (IOException e) {
			System.out.println("When get the size of file from site, it can not open site or the file does not exist.");
		}

		return t;
	}

	// get file size in ftp sever.
	public int getFileSize(String server, String user, String pswd, String path, String filename, int port) {
		int size = 0;
		FTPFile[] ftpFile;
		try {
			if (ftpClient.isConnected() == false) {
				ftpClient.connect(server, port);
				ftpClient.login(user, pswd);
			}
			ftpClient.enterLocalPassiveMode();
			ftpFile = ftpClient.listFiles(path + "/" + filename);
			// FTPFile ftpFile = ftpClient.mlistFile(path+ "/" + filename);
			if (ftpFile != null) {
				// String name = ftpFile[0].getName();
				// System.out.println("test: " + ftpFile.getSize());
				size = (int) ftpFile[0].getSize();
				// String timestamp =
				// ftpFile.getTimestamp().getTime().toString();
				// String type = ftpFile.isDirectory() ? "Directory" : "File";
				// System.out.println("Name: " + name);
				// System.out.println("Size: " + size);
				// System.out.println("Type: " + type);
				// System.out.println("Timestamp: " + timestamp);
				// close ftpclient
				// ftpClient.logout();
				// ftpClient.disconnect();
			} else {
				System.out.println("The pointed file or directory may not exist!");
			}
		} catch (SocketException e) {
			System.out.println("Socket is err, can not get file size!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can not get file size!");
			e.printStackTrace();
		}
		// System.out.println("remote: " + size +" ");
		ftpFile = null;
		return size;

	}

	// delete file
	public Boolean deleteFtpServerFile(String remoteFilePath) {

		try {
			return ftpClient.deleteFile(remoteFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
