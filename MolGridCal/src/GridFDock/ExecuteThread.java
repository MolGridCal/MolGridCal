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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/*
 * This Class realizes to run the program on nodes with one Thread.
 */

public class ExecuteThread extends Thread {

	Runtime rn = Runtime.getRuntime();
	Process pr = null;
	InputStreamReader ir = null;
	Thread t;
	private String name;
	private String execute;
	String command;
	String program;
	String line;

	public ExecuteThread(String program, String execute, String name) {
		this.name = name;
		this.execute = execute;
		this.program = program;
	}

	public void run() {
		try {
			// Find the current path.
			String userdir = System.getProperty("user.dir");
			String os = System.getProperty("os.name").toLowerCase();

			/************************* This part can be modified ********************************/
			if (program.equalsIgnoreCase("Autodock_Vina")) {
				if (os.indexOf("windows") >= 0) {
					command = userdir + "\\" + execute + ".exe" + " --config"
							+ " conf.txt" + " --ligand " + name + " --out "
							+ "out_" + name + " --log " + "log_" + name;

				} else if (os.indexOf("linux") >= 0) {
					command = userdir + "/" + execute + " --config"
							+ " conf.txt" + " --ligand " + name + " --out "
							+ "out_" + name + " --log " + "log_" + name;
				}

			} else if (program.equalsIgnoreCase("Autodock4")) {

				if (os.indexOf("windows") >= 0) {
					command = userdir + "\\" + execute + ".exe" + " -p " + name
							+ ".dpf" + " -l " + name + ".dlg";
				} else if (os.indexOf("linux") >= 0) {
					command = userdir + "/" + execute + " -p " + name + ".dpf"
							+ " -l " + name + ".dlg";
				}

			} else {
				System.out.println("Please input the correct program's name!");
			}
			/********************************* END **********************************************/

			// System.out.println(command);

			System.out.println("Now the " + program + " running********");

			pr = rn.exec(command);


			InputStreamReader ir = new InputStreamReader(pr.getInputStream());
			LineNumberReader Inr = new LineNumberReader(ir);

			while ((line = Inr.readLine()) != null) {
				Thread.sleep(10);
			}

			pr.waitFor();
			pr.destroy();
			Inr.close();
			ir.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ir != null) {
				try {
					ir.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			
			if (pr != null) {
				try {
					pr.getOutputStream().close();
					pr.getInputStream().close();
					pr.getErrorStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				pr.destroy();
			}
		}

	}
}
