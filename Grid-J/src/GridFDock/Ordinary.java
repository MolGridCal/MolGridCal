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

public class Ordinary {

	/* This method can be used to judge whether the program is running. */
	public int check(String keyWord) {
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		int a = 0;
		String str = "";
		BufferedReader in = null;
		keyWord.toLowerCase();
		
		try {

			String os = System.getProperty("os.name").toLowerCase();

			if (os.indexOf("windows") >= 0) {
				process = runtime.exec("Tasklist");
				try {
				in = new BufferedReader(new InputStreamReader(
						process.getInputStream()));

				while ((str = in.readLine()) != null) {

					String[] name = str.split("\\s+");
					for (int i = 0; i < name.length; i++) {
						if (name[i].toLowerCase().startsWith(keyWord)) {
							a = a+1;
						} 
					}
				}
				in.close();
				process.waitFor();
				process.destroy();
			}catch(Exception e){
				e.printStackTrace();
			}finally {
				if (in!= null) {
					try {
						in.close();
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
				
				if (process != null) {
					try {
						process.getOutputStream().close();
						process.getInputStream().close();
						process.getErrorStream().close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					process.destroy();
				}
			}
			
			} else if (os.indexOf("linux") >= 0) {

				process = runtime.exec("ps -a");
				try {
				in = new BufferedReader(new InputStreamReader(
						process.getInputStream()));

				while ((str = in.readLine()) != null) {
	
					String[] name = str.split("\\s+");
					for (int i = 0; i < name.length; i++) {
						if (name[i].toLowerCase().startsWith(keyWord)) {
							a = a + 1;
						} 
					}
				}
				
				in.close();
				process.waitFor();
				process.destroy();
				}catch (Exception e) {
					e.printStackTrace();
				}finally {
					if (in!= null) {
						try {
							in.close();
						} catch (IOException e) {

							e.printStackTrace();
						}
					}
					
					if (process != null) {
						try {
							process.getOutputStream().close();
							process.getInputStream().close();
							process.getErrorStream().close();
						} catch (IOException e) {
							e.printStackTrace();
						}

						process.destroy();
					}
				}

			} else {
				System.out
						.println("The other system is not support now, we need more fund support.");
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Some thing seemly wrong when check the process.");
		}
		return a;
	}
}
